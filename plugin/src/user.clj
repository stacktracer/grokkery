(ns user
  (:use
    [grokkery.core]
    [grokkery.util]
    [grokkery]
    [grokkery.color2d :only [color2d]]
    [grokkery.colors]
    [clojure.contrib.pprint]
    [clojure.contrib.seq-utils]
    [clojure.contrib.generic.math-functions]))


(defn e []
  (.printStackTrace *e *err*))


(defn x-from-polar [r-coordkey th-coordkey]
  (derived-coordfn [r r-coordkey, th th-coordkey]
    (* r (cos th))))


(defn y-from-polar [r-coordkey th-coordkey]
  (derived-coordfn [r r-coordkey, th th-coordkey]
    (* r (sin th))))