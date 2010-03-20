(ns grokkery
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
         "grokkery.GraphView"
         (str number)
         IWorkbenchPage/VIEW_VISIBLE))))