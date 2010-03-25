(ns user
  (:use
    [grokkery]
    [grokkery.plot]
    [grokkery.rcp.FigureView :only [new-fig]]
    [clojure.contrib.generic.math-functions]))


(defn e []
  (.printStackTrace *e *err*))