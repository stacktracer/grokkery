(ns grokkery
  (:use
    grokkery.util
    grokkery.core)
  (:import
    [javax.media.opengl GL]))


(def default-pointsize 7)


(def default-color [0.84 0.14 0.03 1])


(defn set-color [#^GL gl c]
  (.glColor4f gl (c 0) (c 1) (c 2) (c 3)))


; Try destructuring args
(defn get-limits [x-axis y-axis]
  (let [xmin (:min x-axis)
        xmax (:max x-axis)
        ymin (:min y-axis)
        ymax (:max y-axis)]
    (merge
      (if (some nil? [xmin xmax]) {:xmin 0 :xmax 1} {:xmin xmin :xmax xmax})
      (if (some nil? [ymin ymax]) {:ymin 0 :ymax 1} {:ymin ymin :ymax ymax}))))


(defn setup [#^GL gl lims & args]
  (doto gl
    (.glMatrixMode GL/GL_PROJECTION)
    (.glLoadIdentity)
    (.glOrtho (:xmin lims) (:xmax lims) (:ymin lims) (:ymax lims) -1 1)
    (.glMatrixMode GL/GL_MODELVIEW)
    (.glLoadIdentity))
  
  (let [argset (set args)]
    (when (some argset [:blend :nice-points :nice-lines])
      (doto gl
        (.glEnable GL/GL_BLEND)
        (.glBlendFunc GL/GL_SRC_ALPHA GL/GL_ONE_MINUS_SRC_ALPHA)))
    (when (argset :nice-points)
      (doto gl
        (.glEnable GL/GL_POINT_SMOOTH)
        (.glHint GL/GL_POINT_SMOOTH_HINT GL/GL_NICEST)))
    (when (argset :nice-lines)
      (doto gl
        (.glEnable GL/GL_LINE_SMOOTH)
        (.glHint GL/GL_LINE_SMOOTH_HINT GL/GL_NICEST)))))


(defn scatter-setup [#^GL gl attrs]
  (.glPointSize gl (or (:pointsize attrs) default-pointsize))
  (when-not (:colorfn attrs)
    (set-color gl (or (:color attrs) default-color))))


(defn scatter-point-drawfn [#^GL gl x-coordfn y-coordfn attrs]
  (if-let [colorfn (:colorfn attrs)]
    #(do
       (set-color gl (colorfn %))
       (.glVertex2f gl (x-coordfn %) (y-coordfn %)))
    #(.glVertex2f gl (x-coordfn %) (y-coordfn %))))


(defn draw-scatter [#^GL gl data x-axis x-coordfn y-axis y-coordfn attrs]
  (setup gl (get-limits x-axis y-axis) :nice-points)
  (scatter-setup gl attrs)
  (gl-draw gl GL/GL_POINTS
    (dorun
      (map (scatter-point-drawfn gl x-coordfn y-coordfn attrs) data))))


(defn scatter-plot [fignum data]
  (add-plot fignum data {:x first :y second} draw-scatter {}))