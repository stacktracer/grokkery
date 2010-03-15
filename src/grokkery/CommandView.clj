(ns grokkery.CommandView
  (:use
    grokkery.util)
  (:import
    [org.eclipse.swt SWT]
    [org.eclipse.swt.widgets Text])
  (:gen-class
    :extends org.eclipse.ui.part.ViewPart
    :state state
    :init init-instance
    :methods [#^{:static true} [id [] String]]))


(defn -id []
  "grokkery.CommandView")


(defn -init-instance []
  [[] (ref {:focusable nil})])


(defn -createPartControl [this parent]
  (let [control (Text. parent (or-flags SWT/MULTI SWT/WRAP SWT/LEFT))]
    (add-verify-listener control
      (fn [event]
        (let [c (.character event)]
          (if
            (or
              (< (int c) (int \a))
              (> (int c) (int \z)))
            :veto))))
    (dosync (alter (.state this) assoc :focusable control))))


(defn -setFocus [this]
  (when-let [focusable (:focusable @(.state this))] (.setFocus focusable)))
