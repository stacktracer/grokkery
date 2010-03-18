(ns grokkery.util
  (:import
    [org.eclipse.swt SWT]
    [org.eclipse.swt.widgets Listener Display]
    [org.eclipse.swt.events VerifyListener VerifyEvent]))


(defn or-flags [& flags]
  (reduce bit-or 0 flags))


(defn add-listener [widget event-type f & args]
  (let [listener (proxy [Listener] []
                   (handleEvent [event] (apply f event args)))]
    (.addListener widget event-type listener)
    listener))


(defn ui-run-async [f]
  (. (Display/getDefault) (asyncExec f)))


(defn ui-run-sync [f]
  (. (Display/getDefault) (syncExec f)))