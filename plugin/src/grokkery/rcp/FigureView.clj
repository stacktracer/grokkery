(ns grokkery.rcp.FigureView
  (:use
    grokkery.util
    grokkery.core)
  (:import
    [javax.media.opengl GL GLContext]
    [org.eclipse.ui IWorkbenchPage IViewPart]
    [org.eclipse.swt.opengl GLCanvas]
    [org.eclipse.swt SWT]
    [org.eclipse.swt.graphics GC Cursor]
    [org.eclipse.swt.widgets Canvas Listener Event Composite]
    [glsimple GLSimpleListener]
    [glsimple.swt GLSimpleSwtCanvas GLSimpleSwtAnimator])
  (:gen-class
    :extends org.eclipse.ui.part.ViewPart
    :state state
    :init init-instance
    :exposes-methods {init superInit}))


(def id "grokkery.rcp.FigureView")


(let [used-fignum (ref -1)]
  (defn- take-fignum! []
    (dosync
      (alter used-fignum inc))
    @used-fignum))


(defn new-fig []
  (ui-sync-exec
    #(let [fignum (take-fignum!)]
       (.showView (get-active-page) id (str fignum) IWorkbenchPage/VIEW_VISIBLE)
       fignum)))


(defn get-fignum [#^IViewPart figview]
  (Integer/parseInt
    (.. figview (getViewSite) (getSecondaryId))))




(defn -init-instance []
  [[] (ref {:focusable nil})])


(defn -init
  ([figview site]
    (.superInit figview site)
    (.setPartName figview (str "Fig " (.getSecondaryId site))))
  ([figview site memento]
    (.init figview site)))


(defn get-xaxis-height [gc]
  30)


(defn get-yaxis-width [gc]
  30)


(defn- #^Canvas make-canvas [parent fignum]
  (let [canvas (Canvas. parent SWT/DOUBLE_BUFFERED)]
    (add-listener canvas SWT/Paint
      (fn [#^Event event]
        (when-let [draw nil]
          (draw (.gc event) (.getBounds canvas)))))
    canvas))



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


(defn- #^GLCanvas make-gl-canvas [parent fignum]
  (let [bounds (ref {:x 0, :y 0, :width 0, :height 0})
        fpsinfo (ref {})
        canvas (GLSimpleSwtCanvas.
                 parent
                 (into-array GLSimpleListener
                   [(proxy [GLSimpleListener] []
                      
                      (init [#^GLContext context]
                        (doto (.getGL context)
                          (.setSwapInterval 0)
                          (.glClearColor 1 1 1 1)))
                      
                      (display [#^GLContext context]
                        (doto (.getGL context)
                          (.glClear GL/GL_COLOR_BUFFER_BIT)
                          (draw-plots fignum))
                        (dosync (alter fpsinfo update-fpsinfo)))
                      
                      (reshape [#^GLContext context x y width height]
                        (dosync
                          (ref-set bounds {:x x, :y y, :width width, :height height})))
                      
                      (displayChanged [context modeChanged deviceChanged]))]))]
    
    (add-watch fpsinfo fignum handle-fpsinfo-change)
    (.start (GLSimpleSwtAnimator. 30 canvas))
    canvas))


(defn- #^GLCanvas make-content-canvas [parent fignum]
  (let [grab-coords (ref {})
        canvas (make-gl-canvas parent fignum)
        get-mouse-coords (fn [event]
                           (get-coords (get-fig fignum)
                             (/ (.x event) (.. canvas (getSize) x))
                             (- 1 (/ (.y event) (.. canvas (getSize) y)))))]
    
    (.setCursor canvas (Cursor. (.getDisplay canvas) SWT/CURSOR_SIZEALL))
    
    (add-listener canvas SWT/MouseDown
      (fn [event]
        (dosync
          (ref-set grab-coords (get-mouse-coords event)))))
    
    (add-listener canvas SWT/MouseMove
      (fn [event]
        (when (mouse-button-down? event)
          (dosync
            (pan fignum (merge-with - @grab-coords (get-mouse-coords event)))))))
    
    (add-listener canvas SWT/MouseWheel
      (fn [event]
        (dosync
          (zoom fignum (- (.count event)) (get-mouse-coords event)))))
    
    canvas))


(defn -createPartControl [figview #^Composite parent]
  (let [fignum (get-fignum figview)
        x-axis (make-canvas parent fignum)
        y-axis (make-canvas parent fignum)
        content-area (make-content-canvas parent fignum)]
    
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
      (alter (.state figview) assoc :focusable content-area))))


(defn -setFocus [figview]
  (when-let [focusable (:focusable @(.state figview))]
    (.setFocus focusable)))