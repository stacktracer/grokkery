(ns grokkery.GrokkeryPlugin
  (:import
    [java.util Properties]
    [org.eclipse.ui.plugin AbstractUIPlugin]
    [org.eclipse.jface.resource ImageDescriptor]
    [grokkery GrokkeryService])
  (:gen-class
    :extends org.eclipse.ui.plugin.AbstractUIPlugin
    :exposes-methods {start superStart
                      stop superStop}
    :methods [#^{:static true} [id [] String]
              #^{:static true} [getDefault [] org.eclipse.ui.plugin.AbstractUIPlugin]
              #^{:static true} [getImageDescriptor [String] org.eclipse.jface.resource.ImageDescriptor]]))


(defn -id []
  "grokkery")


(def plugin (ref nil))


(defn -start [this context]
  (.superStart this context)
  (dosync (ref-set plugin this))
  (.registerService context (.getName GrokkeryService) (GrokkeryService.) (Properties.)))


(defn -stop [this context]
  (dosync (ref-set plugin nil))
  (.superStop this context))


(defn -getDefault []
  @plugin)


(defn -getImageDescriptor [image-path]
  (AbstractUIPlugin/imageDescriptorFromPlugin (-id) image-path))