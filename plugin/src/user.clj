(ns user
  (:use
    [grokkery.core]
    [grokkery.util]
    [grokkery]
    [clojure.contrib.seq-utils]
    [clojure.contrib.generic.math-functions])
  (:import
    [javax.media.opengl GL]
    [java.nio FloatBuffer]
    [com.sun.opengl.util BufferUtil]))


(defn e []
  (.printStackTrace *e *err*))



(defmacro time2 [msg expr]
  `(let [start# (. System (nanoTime))
         ret# ~expr]
     (if true
       (prn (str ~msg " -- elapsed time: " (/ (double (- (. System (nanoTime)) start#)) 1000000.0) " ms")))
     ret#))




(def words-per-color 4)
(def words-per-vert 2)


(defn get-num-verts [nu nv]
  (* 2 (+ nv 1) nu))


(defn #^FloatBuffer make-color-buffer [#^doubles values nu nv #^objects colors min-value max-value]
  (let [num-verts (get-num-verts nu nv)
        float0 (float 0)
        int0 (int 0)
        valmin (double min-value)
        valmax (double max-value)
        num-inrange-colors (- (alength colors) 2)
        offset-to-index (double (/ num-inrange-colors (- max-value min-value)))
        idxmax (dec (alength colors))
        buf (BufferUtil/newFloatBuffer (* words-per-color num-verts))]

    (let [ni (int (dec nu)), nj (int (dec nv))]
      (loop [i (int 0)]
        (doto buf
          (.put float0) (.put float0) (.put float0) (.put float0)
          (.put float0) (.put float0) (.put float0) (.put float0))

        (let [j0 (* i nj), j1 (+ j0 nj)]
          (loop [j j0]
            (let [value (aget values j)
                  inrange-idx (* offset-to-index (- value valmin))
                  idx (Math/max int0 (Math/min idxmax (int (inc inrange-idx))))
                  #^floats c (aget colors idx)
                  r (aget c 0), g (aget c 1), b (aget c 2), a (aget c 3)]
              (doto buf
                (.put r) (.put g) (.put b) (.put a)
                (.put r) (.put g) (.put b) (.put a)))
            (when (< j j1) (recur (inc j)))))
        (when (< i ni) (recur (inc i)))))

    (.flip buf)
    buf))


(defn #^FloatBuffer make-vertex-buffer [x-coordfn y-coordfn nu nv]
  (let [num-verts (get-num-verts nu nv)
        buf (BufferUtil/newFloatBuffer (* words-per-vert num-verts))]

    (let [gx-step (float (/ 1 nu)), gy-step (float (/ 1 nv))
          ni (int (dec nu)), nj (int nv)]
      (loop [i (int 0)]
        (loop [j (int 0)]
          (let [gxa (* i gx-step), gxb (+ gxa gx-step), gy (* j gy-step),
                xa (float (x-coordfn gxa gy)), ya (float (y-coordfn gxa gy)),
                xb (float (x-coordfn gxb gy)), yb (float (y-coordfn gxb gy))]
            (doto buf
              (.put xa) (.put ya)
              (.put xb) (.put yb)))
        (when (< j nj) (recur (inc j))))
      (when (< i ni) (recur (inc i)))))


#_
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




(defn float-array2d [values]
  (into-array
    (map
      #(into-array Float/TYPE (map float %))
      values)))


(defn make-colors
  ([fraction-to-rgba len color-below color-above]
    (float-array2d
      (concat
        [(or color-below (fraction-to-rgba 0))]
        (map #(fraction-to-rgba (/ % len)) (range len))
        [(or color-above (fraction-to-rgba 1))])))
  ([fraction-to-rgba len]
    (make-colors fraction-to-rgba len nil nil)))


(defn gray [fraction]
  [fraction fraction fraction 1])


(defn hot [fraction]
  [(* 3 fraction)
   (* 3 (- fraction (/ 1 3)))
   (* 3 (- fraction (/ 2 3)))
   1])


(defn hot2 [fraction]
  (hot (* fraction fraction)))


(defn hot3 [fraction]
  (hot (* fraction (* fraction fraction))))


(defn hot4 [fraction]
  (hot (* (* fraction fraction) (* fraction fraction))))


(def nu 350)
(def nv 150)
(def values (double-array (take (* nu nv) (repeatedly rand))))
(def origin [0 0])
(def u [0.9 -0.1])
(def v [0.4 2.3])

(def gray-64 (make-colors gray 64))

(def gray-64+red+green (make-colors gray 64 [1 0 0 1] [0 1 0 1]))

(def hot4-64+blue+green (make-colors hot4 64 [0 0.4 1 1] [0 1 0.4 1]))


(defn draw-surf [#^GL gl data x-coordfn y-coordfn attrs]
  (let [num-verts (get-num-verts nu nv)
        verts (time2 "make-vertex-buffer" (make-vertex-buffer x-coordfn y-coordfn nu nv))
        colors (time2 "make-color-buffer " (make-color-buffer values nu nv hot4-64+blue+green 0.01 0.99))]

    (doto gl
      (.glShadeModel GL/GL_FLAT)

      (.glEnableClientState GL/GL_VERTEX_ARRAY)
      (.glEnableClientState GL/GL_COLOR_ARRAY)
      (.glBindBuffer GL/GL_ARRAY_BUFFER 0)

      (.glVertexPointer (int words-per-vert) (int GL/GL_FLOAT) (int 0) verts)
      (.glColorPointer (int words-per-color) (int GL/GL_FLOAT) (int 0) colors)
      (.glDrawArrays GL/GL_QUAD_STRIP 0 num-verts)

      (.glDisableClientState GL/GL_VERTEX_ARRAY)
      (.glDisableClientState GL/GL_COLOR_ARRAY))))




(defn surf [fignum]
  (add-plot fignum [] {:x (fn [gx gy] gx), :y (fn [gx gy] gy)} draw-surf {}))