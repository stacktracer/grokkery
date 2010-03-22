(ns grokkery
  (:require
    [grokkery.FigureView :as FigureView])
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
         FigureView/id
         (str number)
         IWorkbenchPage/VIEW_VISIBLE))))