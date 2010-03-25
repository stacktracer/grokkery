(ns grokkery.rcp.GrokkeryPlugin
  (:import
    [java.util Properties]
    [org.eclipse.ui.plugin AbstractUIPlugin]
    [org.eclipse.jface.resource ImageDescriptor]
    [grokkery.rcp GrokkeryService])
  (:gen-class
    :extends org.eclipse.ui.plugin.AbstractUIPlugin
    :exposes-methods {start superStart
                      stop superStop}))


(def id "grokkery")


(def plugin (ref nil))


(defn -start [this context]
  (.superStart this context)
  (dosync (ref-set plugin this))
  (.registerService context (.getName GrokkeryService) (GrokkeryService.) (Properties.)))


(defn -stop [this context]
  (dosync (ref-set plugin nil))
  (.superStop this context))