(ns grokkery.Perspective
  (:require
    [grokkery.FigureView :as FigureView])
  (:use
    clojure.contrib.import-static)
  (:gen-class
    :implements [org.eclipse.ui.IPerspectiveFactory]))

(import-static org.eclipse.ui.IPageLayout LEFT RIGHT TOP BOTTOM ID_EDITOR_AREA)
(import-static org.eclipse.ui.console.IConsoleConstants ID_CONSOLE_VIEW)


(def id "grokkery.Perspective")


(defn set-uncloseable [layout view-id]
  (.. layout (getViewLayout view-id) (setCloseable false)))


(defn -createInitialLayout [this layout]
  (.setEditorAreaVisible layout false)
  (.addPlaceholder
    (.createPlaceholderFolder layout "north" TOP (float 0.62) ID_EDITOR_AREA)
    (str FigureView/id ":*"))
  
  (.addView layout ID_CONSOLE_VIEW BOTTOM (float 0.62) ID_EDITOR_AREA)
  (set-uncloseable layout ID_CONSOLE_VIEW))    