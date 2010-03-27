(ns user
  (:use
    [grokkery.core]
    [grokkery.rcp.FigureView :only [new-fig]]
    [grokkery]
    [clojure.contrib.generic.math-functions]))


(defn e []
  (.printStackTrace *e *err*))