(ns grokkery.color2d
  (:use
    [grokkery.core]
    [grokkery.colors]
    [grokkery.util])
  (:import
    [javax.media.opengl GL]
    [java.nio FloatBuffer]
    [com.sun.opengl.util BufferUtil]))


(def default-coordfns {:x (fn [gx gy] gx), :y (fn [gx gy] gy)})

(def default-cscale-colors hot+)
(def default-cscale-min 0)
(def default-cscale-max 1)


(def words-per-vert 2)


(defn get-num-verts [nc nr]
  (* 2 (+ nr 1) nc))


(defn #^FloatBuffer make-color-buffer [#^doubles values nc nr #^objects cscale-colors cscale-min cscale-max]
  (let [num-verts (get-num-verts nc nr)
        float0 (float 0)
        int0 (int 0)
        vmin (double cscale-min)
        vmax (double cscale-max)
        num-inrange-colors (- (alength cscale-colors) 2)
        offset-to-index (double (/ num-inrange-colors (- cscale-max cscale-min)))
        idxmax (dec (alength cscale-colors))
        buf (BufferUtil/newFloatBuffer (* words-per-color num-verts))]

    (let [ni (int (dec nc)), nj (int (dec nr))]
      (loop [i (int 0)]
        (doto buf
          (.put float0) (.put float0) (.put float0) (.put float0)
          (.put float0) (.put float0) (.put float0) (.put float0))

        (let [j0 (* i nj), j1 (+ j0 nj)]
          (loop [j j0]
            (let [v (aget values j)
                  inrange-idx (* offset-to-index (- v vmin))
                  idx (Math/max int0 (Math/min idxmax (int (inc inrange-idx))))
                  #^floats c (aget cscale-colors idx)
                  r (aget c 0), g (aget c 1), b (aget c 2), a (aget c 3)]
              (doto buf
                (.put r) (.put g) (.put b) (.put a)
                (.put r) (.put g) (.put b) (.put a)))
            (when (< j j1) (recur (inc j)))))
        (when (< i ni) (recur (inc i)))))

    (.flip buf)
    buf))


(defn #^FloatBuffer make-vertex-buffer [x-coordfn y-coordfn nc nr]
  (let [grid-x (make-array Float/TYPE (inc nc) (inc nr))
        grid-y (make-array Float/TYPE (inc nc) (inc nr))]

    (let [ni (int nc)
          nj (int nr)
          gx-step (float (/ 1 nc))
          gy-step (float (/ 1 nr))]
      (loop [i (int 0)]
        (let [gx (* i gx-step)
              column-x #^floats (aget grid-x i)
              column-y #^floats (aget grid-y i)]
          (loop [j (int 0)]
            (let [gy (* j gy-step)
                  x (float (x-coordfn gx gy))
                  y (float (y-coordfn gx gy))]
              (aset column-x j x)
              (aset column-y j y))
            (when (< j nj) (recur (inc j)))))
        (when (< i ni) (recur (inc i)))))

    (let [ni (int (dec nc))
          nj (int nr)
          num-verts (get-num-verts nc nr)
          buf (BufferUtil/newFloatBuffer (* words-per-vert num-verts))]
      (loop [i (int 0)]
        (let [column-xa #^floats (aget grid-x i)
              column-ya #^floats (aget grid-y i)
              column-xb #^floats (aget grid-x (inc i))
              column-yb #^floats (aget grid-y (inc i))]
          (loop [j (int 0)]
            (let [xa (aget column-xa j)
                  ya (aget column-ya j)
                  xb (aget column-xb j)
                  yb (aget column-yb j)]
              (doto buf
                (.put xa) (.put ya)
                (.put xb) (.put yb)))
            (when (< j nj) (recur (inc j)))))
        (when (< i ni) (recur (inc i))))

      (.flip buf)
      buf)))


(defn draw-color2d [#^GL gl data x-coordfn y-coordfn attrs]
  (let [nc (:num-cols data)
        nr (:num-rows data)
        num-verts (get-num-verts nc nr)

        verts (make-vertex-buffer x-coordfn y-coordfn nc nr)

        values (:values data)
        cscale-colors (or (:colorscale-colors attrs) default-cscale-colors)
        cscale-min (or (:colorscale-min attrs) default-cscale-min)
        cscale-max (or (:colorscale-max attrs) default-cscale-max)
        colors (make-color-buffer values nc nr cscale-colors cscale-min cscale-max)]

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


(defn color2d [fignum values num-cols num-rows]
  (add-plot fignum {:values (double-array values), :num-cols num-cols, :num-rows num-rows} default-coordfns draw-color2d {}))