(ns grokkery
  (:require
    [grokkery.rcp.FigureView :as FigureView])
  (:use
    grokkery.util
    grokkery.core))


(def default-axes {:bottom :x, :left :y})

(def default-limits {:x fallback-coordlims, :y fallback-coordlims})


(defn new-fig []
  (doto (FigureView/new-fig)
    (replace-axes default-axes)
    (set-limits default-limits)))


(defn x-from-polar [r-coordkey th-coordkey]
  (derived-coordfn [r r-coordkey, th th-coordkey]
    (* r (Math/cos th))))


(defn y-from-polar [r-coordkey th-coordkey]
  (derived-coordfn [r r-coordkey, th th-coordkey]
    (* r (Math/sin th))))