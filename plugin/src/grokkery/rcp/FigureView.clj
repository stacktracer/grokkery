(ns grokkery.rcp.FigureView
  (:use
    grokkery.util
    grokkery.core
    grokkery.rcp.content-area)
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