(ns grokkery
  (:require
    [grokkery.Figure :as Figure])
  (:use
    grokkery.util)
  (:import
    [javax.media.opengl GL]
    [org.eclipse.ui PlatformUI IWorkbenchPage]))


(defn get-page []
  (..
    PlatformUI
    (getWorkbench)
    (getActiveWorkbenchWindow)
    (getActivePage)))


(defn show-fig [fignum]
  (ui-sync-exec
    #(.showView (get-page)
       Figure/id
       (str fignum)
       IWorkbenchPage/VIEW_VISIBLE)))


(defn get-fig [fignum]
  (ui-sync-exec
    #(..
       (get-page)
       (findViewReference Figure/id (str fignum))
       (getView true))))


(defn get-fignum [fig]
  (ui-sync-exec
    #(Integer/parseInt
       (.. fig (getViewSite) (getSecondaryId)))))


(defn add-plot [fig data-ref drawfn-ref]
  (ui-sync-exec
    #(Figure/add-plot fig data-ref drawfn-ref)))


(defn draw-scatter
  ([x-axfn y-axfn]
    (fn [gl data]
      (draw-scatter x-axfn y-axfn gl data)))
  ([x-axfn y-axfn #^GL gl data]
    (doto gl
      (.glEnable GL/GL_BLEND)
      (.glBlendFunc GL/GL_SRC_ALPHA GL/GL_ONE_MINUS_SRC_ALPHA)
      (.glEnable GL/GL_POINT_SMOOTH)
      (.glHint GL/GL_POINT_SMOOTH_HINT GL/GL_NICEST)
      (.glPointSize 10)
      (.glColor4f 0.84 0.14 0.03 1))
    
    (.glBegin gl GL/GL_POINTS)
    (dorun
      (map
        #(.glVertex2f gl (x-axfn %) (y-axfn %))
        data))
    (.glEnd gl)))
