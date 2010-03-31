(ns grokkery
  (:require
    [grokkery.rcp.FigureView :as FigureView])
  (:use
    grokkery.util
    grokkery.core)
  (:import
    [javax.media.opengl GL]))


(def default-axes {:bottom :x, :left :y})

(def default-limits {:x fallback-coordlims, :y fallback-coordlims})

(def default-coordfns {:x first, :y second})

(def default-point-size 7)

(def default-point-color (cycle [[0.84 0.14 0.03 1]
                                 [0.13 0.35 0.84 1]
                                 [0.09 0.64 0.13 1]
                                 [0.84 0.68 0.00 1]]))


(defn default-attrs [plotnum]
  {:point-color (nth default-point-color plotnum)})


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
  (let [plotnum (add-plot fignum data default-coordfns draw-basic {})]
    (set-attrs fignum plotnum (default-attrs plotnum))
    plotnum))