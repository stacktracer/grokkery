(ns grokkery
  (:require
    [grokkery.Figure :as Figure])
  (:use
    grokkery.util)
  (:import
    [org.eclipse.ui PlatformUI IWorkbenchPage]))


(defn get-active-page []
  (..
    PlatformUI
    (getWorkbench)
    (getActiveWorkbenchWindow)
    (getActivePage)))


(defn figure [number]
  (ui-sync-exec
    #(.showView (get-active-page)
       Figure/id
       (str number)
       IWorkbenchPage/VIEW_VISIBLE)))