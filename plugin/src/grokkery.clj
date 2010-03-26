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


(defn scatter-setup [#^GL gl attrs]
  (doto gl
    (.glEnable GL/GL_BLEND)
    (.glBlendFunc GL/GL_SRC_ALPHA GL/GL_ONE_MINUS_SRC_ALPHA)
    (.glEnable GL/GL_POINT_SMOOTH)
    (.glHint GL/GL_POINT_SMOOTH_HINT GL/GL_NICEST)
    (.glPointSize (or (:pointsize attrs) default-pointsize)))
  (when-not (:colorfn attrs)
    (set-color gl (or (:color attrs) default-color))))


(defn scatter-point-drawfn [#^GL gl x-coordfn y-coordfn attrs]
  (if-let [colorfn (:colorfn attrs)]
    #(do
       (set-color gl (colorfn %))
       (.glVertex2f gl (x-coordfn %) (y-coordfn %)))
    #(.glVertex2f gl (x-coordfn %) (y-coordfn %))))


(defn draw-scatter [#^GL gl data x-coordfn y-coordfn attrs]
  (scatter-setup gl attrs)
  (gl-draw gl GL/GL_POINTS
    (dorun
      (map (scatter-point-drawfn gl x-coordfn y-coordfn attrs) data))))


(defn scatter-plot [fignum data]
  (add-plot fignum data {:x first :y second} draw-scatter {}))