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



(defmacro time2 [msg expr]
  `(let [start# (. System (nanoTime))
         ret# ~expr]
     (prn (str ~msg " -- elapsed time: " (/ (double (- (. System (nanoTime)) start#)) 1000000.0) " ms"))
     ret#))




(def words-per-color 4)
(def words-per-vert 2)


(defn get-num-verts [nu nv]
  (* 2 (+ nv 1) nu))


(defn interpose-every-n [sep coll n]
  (mapcat cons (repeat sep) (partition-all n coll)))


(defn make-color-buffer [values value-to-color nu nv]
  (let [values2 (double-array values)
        num-verts (get-num-verts nu nv)
        color-temp (float-array words-per-color)
        zero (float 0)
        buf (BufferUtil/newFloatBuffer (* words-per-color num-verts))]

    (let [ni (int (dec nu)), nj (int (dec nv))]
      (loop [i (int 0)]
        (doto buf
          (.put zero) (.put zero) (.put zero) (.put zero)
          (.put zero) (.put zero) (.put zero) (.put zero))

        (let [offset (* i nj)]
          (loop [j (int 0)]
            (value-to-color (aget values2 (+ offset j)) color-temp)
            (let [r (aget color-temp 0)
                  g (aget color-temp 1)
                  b (aget color-temp 2)
                  a (aget color-temp 3)]
              (doto buf
                (.put r) (.put g) (.put b) (.put a)
                (.put r) (.put g) (.put b) (.put a)))
            (when (< j nj) (recur (inc j)))))
        (when (< i ni) (recur (inc i)))))

    (.flip buf)
    buf))


(defn make-vertex-buffer [origin u v nu nv]
  (let [num-verts (get-num-verts nu nv)
        buf (BufferUtil/newFloatBuffer (* words-per-vert num-verts))]

    (let [ni (int (dec nu)), nj (int nv)
          ox (float (origin 0)), oy (float (origin 1))
          ux (float (u 0)), uy (float (u 1))
          vx (float (v 0)), vy (float (v 1))]

      (loop [i (int 0)]
        (let [xa0 (+ ox (* i ux)), ya0 (+ oy (* i uy))
              xb0 (+ ox (* (inc i) ux)), yb0 (+ oy (* (inc i) uy))]
          (loop [j (int 0)]
            (let [xa (+ xa0 (* j vx)), ya (+ ya0 (* j vy))
                  xb (+ xb0 (* j vx)), yb (+ yb0 (* j vy))]
              (doto buf
                (.put xa) (.put ya)
                (.put xb) (.put yb)))
            (when (< j nj) (recur (inc j)))))
        (when (< i ni) (recur (inc i)))))

    (.flip buf)
    buf))



(def nu 350)
(def nv 150)
(def values (take (* nu nv) (repeatedly rand)))
(def origin [0 0])
(def u [0.9 -0.1])
(def v [0.4 2.3])

(defn value-to-color [cell-value #^floats rgba-array]
  (let [one (float 1)
        zero (float 0)
        v (float cell-value)]
    (doto rgba-array
      (aset 0 v)
      (aset 1 zero)
      (aset 2 (- one v))
      (aset 3 one))))


(defn draw-surf [#^GL gl data x-coordfn y-coordfn attrs]
  (let [num-verts (get-num-verts nu nv)
        verts (time2 "make-vertex-buffer" (make-vertex-buffer origin u v nu nv))
        colors (time2 "make-color-buffer " (make-color-buffer values value-to-color nu nv))]

    (doto gl
      (.glShadeModel GL/GL_FLAT)

      (.glEnableClientState GL/GL_VERTEX_ARRAY)
      (.glEnableClientState GL/GL_COLOR_ARRAY)
      (.glBindBuffer GL/GL_ARRAY_BUFFER 0)

      (.glVertexPointer words-per-vert GL/GL_FLOAT 0 verts)
      (.glColorPointer words-per-color GL/GL_FLOAT 0 colors)
      (.glDrawArrays GL/GL_QUAD_STRIP 0 num-verts)

      (.glDisableClientState GL/GL_VERTEX_ARRAY)
      (.glDisableClientState GL/GL_COLOR_ARRAY))))




(defn surf [fignum]
  (add-plot fignum [] default-coordfns draw-surf {}))