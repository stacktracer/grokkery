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
  (let [control (Text. parent (or-flags SWT/MULTI SWT/WRAP SWT/LEFT))
        editable-index (ref 0)]
    (add-verify-listener control
      (fn [event]
        (dosync
          (if
            (< (.start event) @editable-index)
              (do
                (println (str "veto: " (.start event) " < " @editable-index))
                :veto)
            (when
              (#{\newline \return} (.character event))
              (do
                (set! (. event text) (str (.text event) ">> "))
                (ref-set editable-index (+ 4 (.end event)))))))))
    
    (dosync (alter (.state this) assoc :focusable control))))


(defn -setFocus [this]
  (when-let [focusable (:focusable @(.state this))] (.setFocus focusable)))
