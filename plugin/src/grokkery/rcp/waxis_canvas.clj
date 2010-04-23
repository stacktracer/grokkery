(ns grokkery.rcp.waxis-canvas
  (:use
    clojure.contrib.import-static
    grokkery.util
    grokkery.core)
  (:import
    [org.eclipse.swt SWT]
    [org.eclipse.swt.graphics GC Cursor]
    [org.eclipse.swt.widgets Canvas Listener Event Composite]))

(import-static java.lang.Math floor ceil log10 round pow)




(defn get-tick-step [axis-min axis-max approx-num-ticks]
  (let [approx-step (/ (- axis-max axis-min) approx-num-ticks)
        prelim-step (pow 10 (round (log10 approx-step)))
        prelim-num-ticks (int (floor (/ (- axis-max axis-min) prelim-step)))]
    (cond
      (>= prelim-num-ticks (* 5 approx-num-ticks)) (* prelim-step 5)
      (>= prelim-num-ticks (* 2 approx-num-ticks)) (* prelim-step 2)
      (>= approx-num-ticks (* 5 prelim-num-ticks)) (/ prelim-step 5)
      (>= approx-num-ticks (* 2 prelim-num-ticks)) (/ prelim-step 2)
      :else prelim-step)))


(defn get-tick-locations [axis-min axis-max step]
  (let [n0 (int (floor (/ axis-min step)))
        n1 (inc (int (ceil (/ axis-max step))))]
    (for [n (range n0 n1)] (* n step))))


(defn get-tick-string [step number]
  ; The tick step will always be 1*10^k, 2*10^k, or 5*10^k, where k is an integer.
  ; We want to back out k, but we don't want the combination of rounding error with
  ; floor() to give us k-1 when the tick step is (1-eps)*10^k.
  (let [fudge-factor 1.1
        num-decimal-places (int (max 0 (int (- (floor (log10 (* step fudge-factor)))))))]
    (format (str "%." num-decimal-places "f") number)))




(defn- get-system-color [#^GC gc color-id]
  (.. gc (getDevice) (getSystemColor color-id)))


(def left-padding 2)
(def right-padding 2)
(def tick-line-width 1)
(def tick-length 5)

(def pixels-between-ticks 50)

(defn- bg-color [#^GC gc] (get-system-color gc SWT/COLOR_WIDGET_BACKGROUND))
(defn- tick-color [#^GC gc] (get-system-color gc SWT/COLOR_WIDGET_NORMAL_SHADOW))
(defn- ticktext-color [#^GC gc] (get-system-color gc SWT/COLOR_WIDGET_FOREGROUND))
(defn- axis-label-color [#^GC gc] (get-system-color gc SWT/COLOR_WIDGET_FOREGROUND))


(defn- set-fg-color [#^GC gc get-color]
  (.setForeground gc (get-color gc)))


(defn- set-bg-color [#^GC gc get-color]
  (.setBackground gc (get-color gc)))


(defn- set-line-width [#^GC gc w]
  (.setLineWidth gc w))



(defn- get-waxis-lims [fig]
  (let [lims (get-axislims fig :left)]
    {:min (min-of lims), :max (max-of lims)}))


(defn- get-waxis-ticks [fig height]
  (let [{:keys [min max]} (get-waxis-lims fig)
        approx-num-ticks (/ height pixels-between-ticks)
        step (get-tick-step min max approx-num-ticks)
        locs (if (pos? height) (get-tick-locations min max step) [])]
    {:step step, :locs locs}))


(defn get-string-width [#^GC gc s]
  (.. gc (stringExtent s) x))


(defn get-waxis-width [fig #^GC gc height]
  (let [{:keys [step locs]} (get-waxis-ticks fig height)
        number-width (->> locs
                       (map (partial get-tick-string step))
                       (map (partial get-string-width gc))
                       (max-of)
                       (ceil))]
    (+ left-padding number-width right-padding tick-length)))


(defn get-j [height ymin ymax y]
  (- height 0.5 (/ (* height (- y ymin)) (- ymax ymin))))


(defn get-j-fn [fig height]
  (let [{:keys [min max]} (get-waxis-lims fig)]
    (partial get-j height min max)))


(defn draw-waxis [fig #^GC gc width height]
  (let [{:keys [step locs]} (get-waxis-ticks fig height)
        j-of (get-j-fn fig height)]
    
    ; Tick marks
    (set-fg-color gc tick-color)
    (set-line-width gc tick-line-width)
    (let [i0 width, i1 (- i0 tick-length)]
      (doseq [j (map #(round (double (j-of %))) locs)]
        (.drawLine gc i0 j i1 j)))
    
    ; Tick labels
    (set-fg-color gc ticktext-color)
    (doseq [y locs]
      (let [text (get-tick-string step y)
            text-extent (.stringExtent gc text)
            i (- width tick-length right-padding (.x text-extent))
            j (round (double (- (j-of y) (* 0.5 (.y text-extent)))))]
        (.drawString gc text i j)))))


(defn #^Canvas make-yaxis-canvas [parent fignum draw]
  (let [canvas (Canvas. parent SWT/DOUBLE_BUFFERED)]
    (add-listener canvas SWT/Paint
      (fn [#^Event event]
        (draw (get-fig fignum) (.gc event) (get-width canvas) (get-height canvas))))
    canvas))


(defn #^Canvas make-waxis-canvas [parent fignum]
  (make-yaxis-canvas parent fignum draw-waxis))