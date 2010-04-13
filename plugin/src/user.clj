(ns user
  (:use
    [grokkery.core]
    [grokkery.util]
    [grokkery]
    [grokkery.plot :only [plot]]
    [grokkery.color2d :only [color2d]]
    [grokkery.colors]
    [clojure.contrib.pprint]
    [clojure.contrib.seq-utils]
    [clojure.contrib.generic.math-functions]))


(defn e []
  (.printStackTrace *e *err*))