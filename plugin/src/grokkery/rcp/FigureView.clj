(ns grokkery.rcp.FigureView
  (:use
    grokkery.util
    grokkery.core
    grokkery.rcp.saxis-canvas
    grokkery.rcp.waxis-canvas
    grokkery.rcp.content-canvas)
  (:import
    [java.util Timer TimerTask]
    [org.eclipse.ui IWorkbenchPage IViewPart]
    [org.eclipse.swt SWT SWTException]
    [org.eclipse.swt.graphics GC]
    [org.eclipse.swt.widgets Composite Control])
  (:gen-class
    :extends org.eclipse.ui.part.ViewPart
    :state state
    :init init-instance
    :exposes-methods {init superInit}))


(def id "grokkery.rcp.FigureView")


(def max-fps 30)



(defn new-fig []
  (ui-sync-exec
    #(let [fignum (add-fig)]
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


(defn- redraw-all [#^Control control]
  (let [size (.getSize control)
        width (.x size)
        height (.y size)]
  (.redraw control 0 0 width height true)))


(defn- #^TimerTask make-redraw-task [#^Control control redraw?]
  (proxy [TimerTask] []
    (run []
      (try
        (..
          control
          (getDisplay)
          (syncExec
            #(when (and
                     (not (.isDisposed control))
                     @redraw?)
               (redraw-all control)
               (dosync (ref-set redraw? false)))))
        
        (catch SWTException e
          (when-not (#{SWT/ERROR_DEVICE_DISPOSED SWT/ERROR_WIDGET_DISPOSED} (.code e))
            (throw e)))))))


(defn- start-animator [#^Control control fps fignum]
  (let [thread-name (format "Fig %d Animator" fignum)
        timer (Timer. thread-name true)
        redraw? (ref true)]
    (.schedule timer
      (make-redraw-task control redraw?)
      (long 0)
      (long (max 1 (/ 1000 fps))))
    #(dosync (ref-set redraw? true))))


(defn -createPartControl [figview #^Composite parent]
  (let [fignum (get-fignum figview)
        saxis-canvas (make-saxis-canvas parent fignum)
        waxis-canvas (make-waxis-canvas parent fignum)
        content-canvas (make-content-canvas parent fignum)
        trigger-redraw (start-animator parent max-fps fignum)]
    
    (add-watch (get-figref fignum) fignum
      (fn [& _] (trigger-redraw)))
    
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