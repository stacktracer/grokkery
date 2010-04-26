(ns grokkery.rcp.content-canvas
  (:use
    grokkery.util
    grokkery.core
    grokkery.rcp.graph)
  (:import
    [javax.media.opengl GL GLContext GLDrawableFactory]
    [org.eclipse.swt SWT]
    [org.eclipse.swt.opengl GLCanvas GLData]
    [org.eclipse.swt.graphics GC Color]
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


(defn draw-border [#^GL gl #^GC gc width height]
  (doto gl
    (.glMatrixMode GL/GL_PROJECTION)
    (.glLoadIdentity)
    (.glOrtho 0 width height 0 -1 1)
    (.glMatrixMode GL/GL_MODELVIEW)
    (.glLoadIdentity)
    (.glTranslatef 0.375 0.375 0)
    
    (.glEnable GL/GL_BLEND)
    (.glBlendFunc GL/GL_SRC_ALPHA GL/GL_ONE_MINUS_SRC_ALPHA)
    
    (.glEnable GL/GL_POLYGON_MODE)
    (.glPolygonMode GL/GL_FRONT_AND_BACK GL/GL_LINE))
  
  (let [c #^Color (border-color gc)]
    (println (.getRed c) (.getGreen c) (.getBlue c))
    (.glColor3ub gl (.getRed c) (.getGreen c) (.getBlue c)))
  
  (gl-draw gl GL/GL_QUADS
    (.glVertex2f gl 0 0)
    (.glVertex2f gl (dec width) 0)
    (.glVertex2f gl (dec width) (dec height))
    (.glVertex2f gl 0 (dec height))))


(defn #^GLCanvas make-content-canvas [parent fignum]
  (let [canvas (make-gl-canvas parent)
        context (create-gl-context canvas)
        gl (.getGL context)
        reshaped (ref true)]
    
    (attach-graph-mouse-listeners canvas fignum :north :south :east :west)
    (set-mouse-cursor canvas SWT/CURSOR_SIZEALL)
    
    (.setCurrent canvas)
    (.makeCurrent context)
    (init-gl gl)
    (.release context)
    
    (add-listener canvas SWT/Resize
      (fn [event]
        (dosync
          (ref-set reshaped true))))
    
    (add-listener canvas SWT/Paint
      (fn [#^Event event]
        (.setCurrent canvas)
        (.makeCurrent context)
        
        (let [gl (.getGL context)]
          
          (when @reshaped
            (let [bounds (.getBounds canvas)
                  w (.width bounds)
                  h (.height bounds)]
              (.glViewport gl 0 0 w h))
            (dosync (ref-set reshaped false)))

          (gl-push-all gl
            (.glClear gl GL/GL_COLOR_BUFFER_BIT)
            (draw-border gl (.gc event) (get-width canvas) (get-height canvas))
            (draw-plots gl fignum)))
        
        (.swapBuffers canvas)
        (.release context)))
    
    canvas))