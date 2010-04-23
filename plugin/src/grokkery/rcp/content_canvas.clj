(ns grokkery.rcp.content-canvas
  (:use
    grokkery.util
    grokkery.core)
  (:import
    [java.util Timer TimerTask]
    [javax.media.opengl GL GLContext GLDrawableFactory]
    [org.eclipse.swt SWT SWTException]
    [org.eclipse.swt.opengl GLCanvas GLData]
    [org.eclipse.swt.graphics GC Cursor]
    [org.eclipse.swt.widgets Canvas Listener Event]))


(def target-fps 30)


(defn- attach-mouse-listeners [#^Canvas canvas fignum]
  (let [grab-coords (ref {})
        get-mouse-coords (fn [#^Event event]
                           (get-coords (get-fig fignum)
                             (/ (float (.x event)) (float (.. canvas (getSize) x)))
                             (- 1 (/ (float (.y event)) (float (.. canvas (getSize) y))))))
        
        ; Up/down events are sometimes interleaved out of order wrt move events.
        ; To deal with this, we track the button state manually: @grab-coords is
        ; nil iff no buttons are down. As a result, we treat press and drag events
        ; the same way, and release and move events the same way.
        
        on-press-or-drag (fn [event]
                           (dosync
                             (if (nil? @grab-coords)
                               (ref-set grab-coords (get-mouse-coords event))
                               (pan fignum (merge-with - @grab-coords (get-mouse-coords event))))))
        
        on-release-or-move (fn [event]
                             (dosync
                               (ref-set grab-coords nil)))]
    
    (.setCursor canvas (Cursor. (.getDisplay canvas) SWT/CURSOR_SIZEALL))
    
    (add-listener canvas SWT/MouseUp on-release-or-move)
    (add-listener canvas SWT/MouseDown on-press-or-drag)
    (add-listener canvas SWT/MouseMove #(if (mouse-button-down? %) (on-press-or-drag %) (on-release-or-move %)))
    
    (add-listener canvas SWT/MouseWheel
      (fn [#^Event event]
        (dosync
          (zoom fignum (- (.count event)) (get-mouse-coords event)))))))


(defn- #^GLCanvas make-gl-canvas [parent]
  (let [gl-data (GLData.)]
    (set! (.doubleBuffer gl-data) true)
    (GLCanvas. parent SWT/NONE gl-data)))


(defn- #^GLContext create-gl-context [#^GLCanvas canvas]
  (.setCurrent canvas)
  (.. GLDrawableFactory (getFactory) (createExternalGLContext)))


(defn- init-gl [#^GL gl]
  (.setSwapInterval gl 0)
  (.glClearColor gl 1 1 1 1))


(defn- #^TimerTask make-redraw-task [#^Canvas canvas f]
  (proxy [TimerTask] []
    (run []
      (try
        (..
          canvas
          (getDisplay)
          (syncExec (fn [] (when (not (.isDisposed canvas)) (f)))))
        (catch SWTException e
          (when-not (#{SWT/ERROR_DEVICE_DISPOSED SWT/ERROR_WIDGET_DISPOSED} (.code e)) (throw e)))))))


(defn- start-animator [fignum canvas fps f]
  (doto (Timer. (format "Fig %d Animator" fignum) true)
    (.schedule (make-redraw-task canvas f) (long 0) (long (max 1 (/ 1000 fps))))))


(defn #^GLCanvas make-content-canvas [parent fignum]
  (let [canvas (make-gl-canvas parent)
        context (create-gl-context canvas)
        gl (.getGL context)
        reshaped (ref true)]
      
      (attach-mouse-listeners canvas fignum)
      
      (add-listener canvas SWT/Resize (fn [ev] (dosync (ref-set reshaped true))))
      
      (.setCurrent canvas)
      (.makeCurrent context)
      (init-gl gl)
      
      (start-animator fignum canvas target-fps
        #(do
           (.setCurrent canvas)
           (.makeCurrent context)
           (let [gl (.getGL context)]
           
             (when @reshaped
               (let [bounds (.getBounds canvas)
                     w (.width bounds)
                     h (.height bounds)]
                 (.glViewport gl 0 0 w h))
               (dosync (ref-set reshaped false)))
             
             (.glClear gl GL/GL_COLOR_BUFFER_BIT)
             (draw-plots gl fignum))
           
           (.swapBuffers canvas)
           (.release context)))
    
    canvas))