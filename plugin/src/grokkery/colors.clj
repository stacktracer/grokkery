(ns grokkery.colors)


(def words-per-color 4)


(defn float-array2d [values]
  (into-array
    (map
      #(into-array Float/TYPE (map float %))
      values)))


(defn make-cscale-colors
  ([fraction-to-rgba len color-below color-above]
    (float-array2d
      (concat
        [(or color-below (fraction-to-rgba 0))]
        (map #(fraction-to-rgba (/ % len)) (range len))
        [(or color-above (fraction-to-rgba 1))])))
  ([fraction-to-rgba len]
    (make-cscale-colors fraction-to-rgba len nil nil)))




(def default-num-colors 64)


(defn get-gray [fraction]
  [fraction fraction fraction 1])

(def gray (make-cscale-colors get-gray default-num-colors))
(def gray+ (make-cscale-colors get-gray default-num-colors [1 0 0 1] [0 1 0 1]))


(defn get-hot [fraction]
  [(* 3 fraction)
   (* 3 (- fraction (/ 1 3)))
   (* 3 (- fraction (/ 2 3)))
   1])

(def hot (make-cscale-colors get-hot default-num-colors))
(def hot+ (make-cscale-colors get-hot default-num-colors [0 0.4 1 1] [0 1 0.4 1]))


(defn get-hot2 [fraction]
  (get-hot (* fraction fraction)))

(def hot2 (make-cscale-colors get-hot2 default-num-colors))
(def hot2+ (make-cscale-colors get-hot2 default-num-colors [0 0.4 1 1] [0 1 0.4 1]))


(defn get-hot3 [fraction]
  (get-hot (* fraction (* fraction fraction))))

(def hot3 (make-cscale-colors get-hot3 default-num-colors))
(def hot3+ (make-cscale-colors get-hot3 default-num-colors [0 0.4 1 1] [0 1 0.4 1]))


(defn get-hot4 [fraction]
  (get-hot (* (* fraction fraction) (* fraction fraction))))

(def hot4 (make-cscale-colors get-hot4 default-num-colors))
(def hot4+ (make-cscale-colors get-hot4 default-num-colors [0 0.4 1 1] [0 1 0.4 1]))