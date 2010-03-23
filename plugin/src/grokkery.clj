(ns grokkery
  (:require
    [grokkery.figure :as figure])
  (:use
    grokkery.util)
  (:import
    [javax.media.opengl GL]))




(defn plot
  ([fignum]
    (plot fignum [] {} nil {}))
  ([fignum data axfns drawfn]
    (plot fignum data axfns drawfn {}))
  ([fignum data axfns drawfn attrs]
    (ui-sync-exec
      #(add-plot fignum (ref data) (ref axfns) (ref drawfn) (ref attrs)))))





(defn draw-scatter [#^GL gl data x-axfn y-axfn attrs]
  (doto gl
    (.glEnable GL/GL_BLEND)
    (.glBlendFunc GL/GL_SRC_ALPHA GL/GL_ONE_MINUS_SRC_ALPHA)
    (.glEnable GL/GL_POINT_SMOOTH)
    (.glHint GL/GL_POINT_SMOOTH_HINT GL/GL_NICEST)
    (.glPointSize 10)
    (.glColor4f 0.84 0.14 0.03 1))
  
  (.glBegin gl GL/GL_POINTS)
  (dorun
    (map
      #(.glVertex2f gl (x-axfn %) (y-axfn %))
      data))
  (.glEnd gl))


(defn scatter-plot [fignum & data]
  (plot fignum data {:x first :y second} draw-scatter))