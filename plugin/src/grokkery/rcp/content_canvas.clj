(ns grokkery.rcp.content-canvas
  (:use
    grokkery.util
    grokkery.core
    grokkery.rcp.graph)
  (:import
    [javax.media.opengl GL GLContext GLDrawableFactory]
    [org.eclipse.swt SWT]
    [org.eclipse.swt.opengl GLCanvas GLData]
    [org.eclipse.swt.graphics GC Cursor]
    [org.eclipse.swt.widgets Canvas Listener Event]))


(defn- #^GLCanvas make-gl-canvas [parent]
  (let [gl-data (GLData.)]
    (set! (.doubleBuffer gl-data) true)
    (GLCanvas. parent SWT/NO_BACKGROUND gl-data)))


(defn- #^GLContext create-gl-context [#^GLCanvas canvas]
  (.setCurrent canvas)
  (.. GLDrawableFactory (getFactory) (createExternalGLContext)))


(defn- init-gl [#^GL gl]
  (.setSwapInterval gl 0)
  (.glClearColor gl 1 1 1 1))


(defn #^GLCanvas make-content-canvas [parent fignum]
  (let [canvas (make-gl-canvas parent)
        context (create-gl-context canvas)
        gl (.getGL context)
        reshaped (ref true)]
    
    (attach-graph-mouse-listeners canvas fignum :north :south :east :west)
    
    (.setCurrent canvas)
    (.makeCurrent context)
    (init-gl gl)
    (.release context)
    
    (add-listener canvas SWT/Resize
      (fn [event]
        (dosync
          (ref-set reshaped true))))
    
    (add-listener canvas SWT/Paint
      (fn [event]
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