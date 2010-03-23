(ns grokkery.util
  (:import
    [org.eclipse.swt SWT]
    [org.eclipse.swt.widgets Listener Display]
    [org.eclipse.swt.events VerifyListener VerifyEvent]
    [org.eclipse.ui PlatformUI]))


(defn or-flags [& flags]
  (reduce bit-or 0 flags))


(defn add-listener [widget event-type f & args]
  (let [listener (proxy [Listener] []
                   (handleEvent [event] (apply f event args)))]
    (.addListener widget event-type listener)
    listener))


(defn ui-async-exec [f]
  (.asyncExec (Display/getDefault) f))


(defn ui-sync-exec [f]
  (let [retref (ref nil)]
    (.syncExec (Display/getDefault)
      #(let [retval (f)]
         (dosync (ref-set retref retval)))) 
    @retref))


(defn get-active-page []
  (..
    PlatformUI
    (getWorkbench)
    (getActiveWorkbenchWindow)
    (getActivePage)))