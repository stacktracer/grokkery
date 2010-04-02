(ns user
  (:use
    [grokkery.core]
    [grokkery.util]
    [grokkery]
    [clojure.contrib.seq-utils]
    [clojure.contrib.generic.math-functions])
  (:import
    [javax.media.opengl GL]
    [com.sun.opengl.util BufferUtil]))


(defn e []
  (.printStackTrace *e *err*))












(defn cell-colorfn [cell-value #^floats rgba-array]
  (let [one (float 1)
        zero (float 0)
        v (float cell-value)]
    (doto rgba-array
      (aset 0 v)
      (aset 1 zero)
      (aset 2 (- one v))
      (aset 3 one))))


(defn interpose-every-n [sep coll n]
  (mapcat cons (repeat sep) (partition-all n coll)))


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
        color-temp (float-array words-per-color)
        num-verts (* 2 (+ n2 1) n1)
        verts (BufferUtil/newFloatBuffer (* words-per-vert num-verts))
        colors (BufferUtil/newFloatBuffer (* words-per-color num-verts))]

    (doseq [value (interpose-every-n 0 values n2)]
      (cell-colorfn value color-temp)
      (let [r (aget color-temp 0)
            g (aget color-temp 1)
            b (aget color-temp 2)
            a (aget color-temp 3)]
      (doto colors
        (.put r) (.put g) (.put b) (.put a)
        (.put r) (.put g) (.put b) (.put a))))

    (.flip colors)


    (let [corners (for [i1 (range (inc n1))]
                    (let [col-origin (+vv origin (*sv i1 v1))]
                      (for [i2 (range (inc n2))]
                        (+vv col-origin (*sv i2 v2)))))

          vert-points (mapcat
                        #(apply interleave %)
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