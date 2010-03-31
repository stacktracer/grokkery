(ns grokkery
  (:require
    [grokkery.rcp.FigureView :as FigureView])
  (:use
    grokkery.util
    grokkery.core)
  (:import
    [javax.media.opengl GL]))


(def default-point-size 7)

(def default-point-color [0.84 0.14 0.03 1])

(def default-axes {:bottom :x, :left :y})

(def default-limits {:x fallback-coordlims, :y fallback-coordlims})

(def default-coordfns {:x first, :y second})


(defn default-attrs [fignum]
  {:point-color [1 0 0 1]})


(defn new-fig []
  (doto (FigureView/new-fig)
    (replace-axes default-axes)
    (set-limits default-limits)))


(defn basic-point-drawfn [#^GL gl x-coordfn y-coordfn attrs]
  (if-let [colorfn (:point-colorfn attrs)]
    #(do
       (gl-set-color gl (colorfn %))
       (.glVertex2f gl (x-coordfn %) (y-coordfn %)))
    #(.glVertex2f gl (x-coordfn %) (y-coordfn %))))


(defn draw-basic [#^GL gl data x-coordfn y-coordfn attrs]
  (.glPointSize gl (or (:point-size attrs) default-point-size))
  (gl-set-color gl (or (:point-color attrs) default-point-color))
  (gl-draw gl GL/GL_POINTS
    (dorun
      (map (basic-point-drawfn gl x-coordfn y-coordfn attrs) data))))


(defn plot [fignum data]
  (add-plot fignum data default-coordfns draw-basic (default-attrs fignum)))