(ns user
  (:use
    [grokkery.core]
    [grokkery.util]
    [grokkery]
    [grokkery.color2d :only [color2d]]
    [clojure.contrib.seq-utils]
    [clojure.contrib.generic.math-functions]))


(defn e []
  (.printStackTrace *e *err*))


;(def nc 350)
;(def nr 150)
;(def values (double-array (take (* nc nr) (repeatedly rand))))

