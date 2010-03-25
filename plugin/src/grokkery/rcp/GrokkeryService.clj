(ns grokkery.rcp.GrokkeryService
  (:gen-class
    :methods [[expose [Object String] void]]))


(defn -expose [this obj obj-name]
  (create-ns 'user)
  (intern 'user (symbol obj-name) obj))
