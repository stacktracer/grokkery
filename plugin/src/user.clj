(ns user
  (:use
    [grokkery.core]
    [grokkery.util]
    [grokkery]
    [clojure.contrib.generic.math-functions])
  (:import
    [javax.media.opengl GL]
    [com.sun.opengl.util BufferUtil]))


(defn e []
  (.printStackTrace *e *err*))












(defn cell-colorfn [cell-value]
  [cell-value 0 (- 1 cell-value) 1])


(defn interpose-every-n [sep coll n]
  (reduce #(concat %1 [sep] %2) [] (partition n coll)))


(defn duplicate [coll]
  (interleave coll coll))



(defn +sv [sa vb]
  [(+ sa (first vb)) (+ sa (second vb))])


(defn *sv [sa vb]
  [(* sa (first vb)) (* sa (second vb))])


(defn +vs [va sb]
  [(+ (first va) sb) (+ (second va) sb)])


(defn *vs [va sb]
  [(* (first va) sb) (* (second va) sb)])


(defn +vv [va vb]
  [(+ (first va) (first vb)) (+ (second va) (second vb))])


(defn *vv [va vb]
  [(* (first va) (first vb)) (* (second va) (second vb))])



(def n1 113)
(def n2 47)
(def values (take (* n1 n2) (repeatedly rand)))
(def origin [0 0])
(def v1 [0.9 -0.1])
(def v2 [0.4 2.3])

(defn draw-surf [#^GL gl data x-coordfn y-coordfn attrs]

  (.glShadeModel gl GL/GL_FLAT)
  (.glEnable gl GL/GL_CULL_FACE)


  (let [words-per-vert 2
        words-per-color 4
        num-verts (* 2 (+ n2 1) n1)
        verts (BufferUtil/newFloatBuffer (* words-per-vert num-verts))
        colors (BufferUtil/newFloatBuffer (* words-per-color num-verts))]


    (let [vert-colors (duplicate
                        (map cell-colorfn
                          (interpose-every-n 0 values n2)))]

      (doseq [x (apply concat vert-colors)] (.put colors (float x))))

    (.flip colors)


    (let [corners (for [i1 (range (inc n1))]
                    (let [col-origin (+vv origin (*sv i1 v1))]
                      (for [i2 (range (inc n2))]
                        (+vv col-origin (*sv i2 v2)))))

          vert-points (reduce
                        #(concat %1 (reduce interleave %2))
                        []
                        (partition 2 (butlast (rest (duplicate corners)))))]

      (doseq [x (apply concat vert-points)] (.put verts (float x))))

    (.flip verts)


    (.glEnableClientState gl GL/GL_VERTEX_ARRAY)
    (.glEnableClientState gl GL/GL_COLOR_ARRAY)
    (.glBindBuffer gl GL/GL_ARRAY_BUFFER 0)

    (.glVertexPointer gl words-per-vert GL/GL_FLOAT 0 verts)
    (.glColorPointer gl words-per-color GL/GL_FLOAT 0 colors)
    (.glDrawArrays gl GL/GL_QUAD_STRIP 0 num-verts)

    (.glDisableClientState gl GL/GL_VERTEX_ARRAY)
    (.glDisableClientState gl GL/GL_COLOR_ARRAY)))




(defn surf [fignum]
  (add-plot fignum [] default-coordfns draw-surf {}))