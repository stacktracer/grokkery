(ns grokkery
  (:require
    [grokkery.Figure :as Figure])
  (:use
    grokkery.util)
  (:import
    [org.eclipse.ui PlatformUI IWorkbenchPage]))


(defn figure [number]
  (ui-run-async
    #(..
       PlatformUI
       (getWorkbench)
       (getActiveWorkbenchWindow)
       (getActivePage)
       (showView
         Figure/id
         (str number)
         IWorkbenchPage/VIEW_VISIBLE))))