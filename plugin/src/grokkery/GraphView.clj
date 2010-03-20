(ns grokkery.GraphView
  (:use
    grokkery.util)
  (:import
    [javax.media.opengl GLContext]
    [javax.media.opengl GL]
    [org.eclipse.swt SWT]
    [org.eclipse.swt.graphics GC]
    [org.eclipse.swt.widgets Canvas Listener]
    [glsimple GLSimpleListener]
    [glsimple.swt GLSimpleSwtCanvas GLSimpleSwtAnimator])
  (:gen-class
    :extends org.eclipse.ui.part.ViewPart
    :state state
    :init init-instance
    :exposes-methods {init superInit}
    :methods [#^{:static true} [id [] String]]))


(defn -id []
  "grokkery.GraphView")


(defn -init-instance []
  [[] (ref {:focusable nil})])


(defn -init
  
  ([this site]
    (.superInit this site)
    (.setPartName this (str "Fig " (.getSecondaryId site))))
  
  ([this site memento]
    (.init this site)))


(defn draw-xaxis

  ([data]
    (fn [gc bounds] (draw-xaxis data gc bounds)))
  
  ([data gc bounds]
    ; IMPLEMENT ME
    ))


(defn get-xaxis-height [gc]
  30)


(defn draw-yaxis
  
  ([data]
    (fn [gc bounds] (draw-yaxis data gc bounds)))
  
  ([data gc bounds]
    ; IMPLEMENT ME
    ))


(defn get-yaxis-width [gc]
  30)


(defn draw-content
  
  ([data]
    (fn [gl bounds] (draw-content data gl bounds)))
  
  ([data gl bounds]
    (doto gl
      (.glClearColor 1 1 1 1)
      (.glClear GL/GL_COLOR_BUFFER_BIT)
      
      (.glEnable GL/GL_BLEND)
      (.glBlendFunc GL/GL_SRC_ALPHA GL/GL_ONE_MINUS_SRC_ALPHA)
      (.glEnable GL/GL_POINT_SMOOTH)
      (.glHint GL/GL_POINT_SMOOTH_HINT GL/GL_NICEST)
      (.glPointSize 10)
      (.glColor4f 0.84 0.14 0.03 1)
      
      (.glBegin GL/GL_POINTS)
      (.glVertex2f 0 0)
      (.glEnd))))


(defn make-canvas [parent draw]
  (let [canvas (Canvas. parent SWT/DOUBLE_BUFFERED)]
    (add-listener canvas SWT/Paint (fn [event] (draw (.gc event) (.getBounds canvas))))
    canvas))


(defn make-gl-canvas [parent draw]
  (let [bounds (ref {:x 0, :y 0, :width 0, :height 0})
        canvas (GLSimpleSwtCanvas.
                 parent
                 (into-array GLSimpleListener
                   [(proxy [GLSimpleListener] []
                      
                      (init [context])
                      
                      (display [context]
                        (draw (.getGL context) @bounds))
                      
                      (reshape [context x y width height]
                        (dosync (ref-set bounds {:x x, :y y, :width width, :height height})))
                      
                      (displayChanged [context modeChanged deviceChanged]))]))]
    
    (.start (GLSimpleSwtAnimator. 60 canvas))
    canvas))


(defn -createPartControl [this parent]
  (let [data :IMPLEMENT-ME
        x-axis (make-canvas parent (draw-xaxis data))
        y-axis (make-canvas parent (draw-yaxis data))
        content-area (make-gl-canvas parent (draw-content data))]
    
    (.setLayout parent nil)
    (add-listener parent SWT/Resize
      (fn [event]
        (let [gc (GC. parent)
              parent-width (.. parent (getSize) x)
              parent-height (.. parent (getSize) y)]
          (try
            (let [yaxis-width (get-yaxis-width gc)
                  xaxis-height (get-xaxis-height gc)
                  content-width (- parent-width yaxis-width)
                  content-height (- parent-height xaxis-height)]
              (.setBounds y-axis 0 0 yaxis-width content-height)
              (.setBounds x-axis yaxis-width content-height content-width xaxis-height)
              (.setBounds content-area yaxis-width 0 content-width content-height))
            (finally (.dispose gc))))))
    
    (dosync (alter (.state this) assoc :focusable content-area))))


(defn -setFocus [this]
  (when-let [focusable (:focusable @(.state this))] (.setFocus focusable)))
