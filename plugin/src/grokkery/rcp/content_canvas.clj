(ns grokkery.rcp.content-canvas
  (:use
    grokkery.util
    grokkery.core)
  (:import
    [javax.media.opengl GL GLContext]
    [org.eclipse.swt.opengl GLCanvas]
    [org.eclipse.swt SWT]
    [org.eclipse.swt.graphics GC Cursor]
    [org.eclipse.swt.widgets Listener Event]
    [glsimple GLSimpleListener]
    [glsimple.swt GLSimpleSwtCanvas GLSimpleSwtAnimator]))


(def target-fps 30)


(def fpsinfo-reset-interval 10000)


(defn- update-fpsinfo [fpsinfo]
  (let [now (System/currentTimeMillis)]
    (if (or
          (not-every? fpsinfo [:count :start])
          (> now (+ (:start fpsinfo) fpsinfo-reset-interval)))
      {:count 0, :start now}
      (update-in fpsinfo [:count] inc))))


(defn- handle-fpsinfo-change [fignum ref old-fpsinfo new-fpsinfo]
  (when (and
          (every? old-fpsinfo [:count :start])
          (not= (:start old-fpsinfo) (:start new-fpsinfo)))
    (println
      (format "Fig %d refresh rate: %.1f fps"
        fignum
        (float (/ (* 1000 (inc (:count old-fpsinfo))) (- (:start new-fpsinfo) (:start old-fpsinfo))))))))


(defn- attach-mouse-listeners [fignum canvas]
  (let [grab-coords (ref {})
        get-mouse-coords (fn [event]
                           (get-coords (get-fig fignum)
                             (/ (.x event) (.. canvas (getSize) x))
                             (- 1 (/ (.y event) (.. canvas (getSize) y)))))
        
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
      (fn [event]
        (dosync
          (zoom fignum (- (.count event)) (get-mouse-coords event)))))))


(defn- make-gl-listener [fignum]
  (let [fpsinfo (add-watch (ref {}) fignum handle-fpsinfo-change)]
  
    (proxy [GLSimpleListener] []
    
      (init [#^GLContext context]
        (doto (.getGL context)
          (.setSwapInterval 0)
          (.glClearColor 1 1 1 1)))
      
      (display [#^GLContext context]
        (doto (.getGL context)
          (.glClear GL/GL_COLOR_BUFFER_BIT)
          (draw-plots fignum))
        (dosync (alter fpsinfo update-fpsinfo)))
      
      (reshape [#^GLContext context x y width height])
      
      (displayChanged [#^GLContext context modeChanged deviceChanged]))))


(defn #^GLCanvas make-content-canvas [parent fignum]
  (let [canvas (GLSimpleSwtCanvas. parent (into-array GLSimpleListener [(make-gl-listener fignum)]))]
    (attach-mouse-listeners fignum canvas)
    (.start (GLSimpleSwtAnimator. target-fps canvas))
    canvas))
