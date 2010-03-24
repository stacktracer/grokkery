(ns user
  (:use
    [grokkery]
    [grokkery.plot]
    [grokkery.FigureView :only [new-fig]]
    [clojure.contrib.generic.math-functions]))


(defn e []
  (.printStackTrace *e *err*))