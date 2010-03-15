(ns grokkery.util
  (:import
    [org.eclipse.swt SWT]
    [org.eclipse.swt.widgets Listener]
    [org.eclipse.swt.events VerifyListener VerifyEvent]))


(defn or-flags [& flags]
  (reduce bit-or 0 flags))


(defn add-listener [widget event-type f & args]
  (let [listener (proxy [Listener] []
                   (handleEvent [event] (apply f event args)))]
    (.addListener widget event-type listener)
    listener))


(defn add-verify-listener [widget f & args]
  (let [listener (proxy [VerifyListener] []
                   (verifyText [event]
                     (when (apply f event args)
                       (set! (. event doit) false))))]
    (.addVerifyListener widget listener)
    listener))
    