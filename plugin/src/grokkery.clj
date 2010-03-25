(ns grokkery
  (:use
    grokkery.util
    grokkery.plot)
  (:import
    [javax.media.opengl GL]))


(def default-pointsize 7)


(def default-color [0.84 0.14 0.03 1])


(defn set-color [#^GL gl c]
  (.glColor4f gl (c 0) (c 1) (c 2) (c 3)))


(defn scatter-setup [#^GL gl x-axfn y-axfn attrs]
  (doto gl
    (.glEnable GL/GL_BLEND)
    (.glBlendFunc GL/GL_SRC_ALPHA GL/GL_ONE_MINUS_SRC_ALPHA)
    (.glEnable GL/GL_POINT_SMOOTH)
    (.glHint GL/GL_POINT_SMOOTH_HINT GL/GL_NICEST)
    (.glPointSize (or (:pointsize attrs) default-pointsize)))
  (when-not (:color-fn attrs)
    (set-color gl (or (:color attrs) default-color))))


(defn scatter-point-drawfn [#^GL gl x-axfn y-axfn attrs]
  (if-let [color-fn (:color-fn attrs)]
    #(do
       (set-color gl (color-fn %))
       (.glVertex2f gl (x-axfn %) (y-axfn %)))
    #(.glVertex2f gl (x-axfn %) (y-axfn %))))


(defn draw-scatter [#^GL gl data x-axfn y-axfn attrs]
  (scatter-setup gl x-axfn y-axfn attrs)
  (gl-draw gl GL/GL_POINTS
    (dorun
      (map (scatter-point-drawfn gl x-axfn y-axfn attrs) data))))


(defn scatter-plot [fignum data]
  (add-plot fignum data {:x first :y second} draw-scatter {}))