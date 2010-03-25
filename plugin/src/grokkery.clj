(ns grokkery
  (:use
    grokkery.util
    grokkery.plot)
  (:import
    [javax.media.opengl GL]))


(defn draw-scatter [#^GL gl data x-axfn y-axfn attrs]
  (doto gl
    (.glEnable GL/GL_BLEND)
    (.glBlendFunc GL/GL_SRC_ALPHA GL/GL_ONE_MINUS_SRC_ALPHA)
    (.glEnable GL/GL_POINT_SMOOTH)
    (.glHint GL/GL_POINT_SMOOTH_HINT GL/GL_NICEST)
    (.glPointSize 10)
    (.glColor4f 0.84 0.14 0.03 1))
  
  (gl-draw gl GL/GL_POINTS
    (try
      (dorun
        (map
          #(.glVertex2f gl (x-axfn %) (y-axfn %))
          data)))))


(defn scatter-plot [fignum data]
  (add-plot fignum data {:x first :y second} draw-scatter {}))