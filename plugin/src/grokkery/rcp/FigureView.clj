(ns grokkery.rcp.FigureView
  (:use
    grokkery.util
    grokkery.core
    grokkery.rcp.saxis-canvas
    grokkery.rcp.waxis-canvas
    grokkery.rcp.content-canvas)
  (:import
    [org.eclipse.ui IWorkbenchPage IViewPart]
    [org.eclipse.swt SWT]
    [org.eclipse.swt.graphics GC]
    [org.eclipse.swt.widgets Composite])
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


(defn -createPartControl [figview #^Composite parent]
  (let [fignum (get-fignum figview)
        saxis-canvas (make-saxis-canvas parent fignum)
        waxis-canvas (make-waxis-canvas parent fignum)
        content-canvas (make-content-canvas parent fignum)]
    
    (.setLayout parent nil)
    (add-listener parent SWT/Resize
      (fn [event]
        (let [gc (GC. parent)
              parent-width (.. parent (getSize) x)
              parent-height (.. parent (getSize) y)
              fig (get-fig fignum)]
          (try
            (let [saxis-height (get-saxis-height fig gc)
                  content-height (- parent-height saxis-height)
                  waxis-width (get-waxis-width fig gc content-height)
                  content-width (- parent-width waxis-width)]
              (.setBounds waxis-canvas 0 0 waxis-width content-height)
              (.setBounds saxis-canvas waxis-width content-height content-width saxis-height)
              (.setBounds content-canvas waxis-width 0 content-width content-height))
            (finally (.dispose gc))))))
    
    (dosync
      (alter (.state figview) assoc :focusable content-canvas))))


(defn -setFocus [figview]
  (when-let [focusable (:focusable @(.state figview))]
    (.setFocus focusable)))