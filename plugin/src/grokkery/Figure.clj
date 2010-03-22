(ns grokkery.Figure
  (:use
    grokkery.util)
  (:import
    [javax.media.opengl GL GLContext]
    [org.eclipse.swt.opengl GLCanvas]
    [org.eclipse.swt SWT]
    [org.eclipse.swt.graphics GC]
    [org.eclipse.swt.widgets Canvas Listener Event Composite]
    [glsimple GLSimpleListener]
    [glsimple.swt GLSimpleSwtCanvas GLSimpleSwtAnimator])
  (:gen-class
    :extends org.eclipse.ui.part.ViewPart
    :state state
    :init init-instance
    :exposes-methods {init superInit}))


(def id "grokkery.Figure")


(defn -init-instance []
  [[] (ref {:focusable nil})])


(defn -init
  ([fig site]
    (.superInit fig site)
    (.setPartName fig (str "Fig " (.getSecondaryId site))))
  ([fig site memento]
    (.init fig site)))


(defn get-xaxis-height [gc]
  30)


(defn get-yaxis-width [gc]
  30)


(let [plotnums (ref {})
      plots (ref {})]

  (defn- claim-plotnum [fig]
    (dosync
      (alter plotnums
        update-in [fig]
          #(if % (inc %) 0))
      (plotnums fig)))


  (defn add-plot [fig data-ref drawfn-ref]
    (let [plotnum (claim-plotnum fig)
          plot {:data data-ref :drawfn drawfn-ref}]
      (dosync
        (alter plots
          update-in [fig]
            assoc plotnum plot))
      plotnum))


  (defn remove-plot [fig plotnum]
    (dosync
      (alter plots
        update-in [fig]
          dissoc plotnum)))


  (defn- draw-plot [gl plot]
    (when-let [drawfn @(:drawfn plot)]
      (drawfn gl @(:data plot))))


  (defn- draw-plots [fig gl]
    (dorun
      (map
        (fn [[_ plot]] (draw-plot gl plot))
        (@plots fig)))))


(defn- #^Canvas make-canvas [fig parent]
  (let [canvas (Canvas. parent SWT/DOUBLE_BUFFERED)]
    (add-listener canvas SWT/Paint
      (fn [#^Event event]
        (when-let [draw nil]
          (draw (.gc event) (.getBounds canvas)))))
    canvas))


(defn- #^GLCanvas make-gl-canvas [fig parent]
  (let [bounds (ref {:x 0, :y 0, :width 0, :height 0})
        canvas (GLSimpleSwtCanvas.
                 parent
                 (into-array GLSimpleListener
                   [(proxy [GLSimpleListener] []
                      
                      (init [#^GLContext context]
                        (doto (.getGL context)
                          (.setSwapInterval 0)
                          (.glClearColor 1 1 1 1)))
                      
                      (display [#^GLContext context]
                        (let [gl (.getGL context)]
                          (.glClear gl GL/GL_COLOR_BUFFER_BIT)
                          (draw-plots fig gl)))
                      
                      (reshape [#^GLContext context x y width height]
                        (dosync
                          (ref-set bounds {:x x, :y y, :width width, :height height})))
                      
                      (displayChanged [context modeChanged deviceChanged]))]))]
    
    (.start (GLSimpleSwtAnimator. 30 canvas))
    canvas))


(defn -createPartControl [fig #^Composite parent]
  (let [x-axis (make-canvas fig parent)
        y-axis (make-canvas fig parent)
        content-area (make-gl-canvas fig parent)]
    
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
    
    (dosync
      (alter (.state fig) assoc :focusable content-area))))


(defn -setFocus [fig]
  (when-let [focusable (:focusable @(.state fig))]
    (.setFocus focusable)))
