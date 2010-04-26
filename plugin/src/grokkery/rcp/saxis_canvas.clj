(ns grokkery.rcp.saxis-canvas
  (:use
    clojure.contrib.import-static
    grokkery.util
    grokkery.core
    grokkery.rcp.graph)
  (:import
    [org.eclipse.swt SWT]
    [org.eclipse.swt.graphics GC]
    [org.eclipse.swt.widgets Canvas Listener Event Composite]))

(import-static java.lang.Math ceil round)




(def top-padding 2)
(def middle-padding 2)
(def bottom-padding 2)


(defn get-saxis-lims [fig]
  (get-axis-lims fig :south))


(defn get-saxis-ticks [fig width]
  (get-axis-ticks fig :south width))


(defn get-saxis-height [fig #^GC gc]
  (let [string-height (get-string-height gc)]
    (+
      tick-length
      top-padding
      string-height
      middle-padding
      string-height
      bottom-padding)))


(defn get-i [width xmin xmax x]
  (+ 0.5 (/ (* width (- x xmin)) (- xmax xmin))))


(defn get-i-fn [fig width]
  (let [{:keys [min max]} (get-saxis-lims fig)]
    (partial get-i width min max)))


(defn get-string-i [gc i-center text]
  (round
    (double
      (-
        i-center
        (* 0.5 (get-string-width gc text))))))  


(defn draw-saxis [fig #^GC gc width height]
  (let [{:keys [step locs]} (get-saxis-ticks fig width)
        i-of (get-i-fn fig width)]
    
    ; Tick marks
    (set-fg-color gc tick-color)
    (.setLineWidth gc tick-line-width)
    (let [j0 0, j1 tick-length]
      (doseq [i (map #(round (double (i-of %))) locs)]
        (.drawLine gc i j0 i j1)))
    
    ; Tick labels
    (set-fg-color gc ticktext-color)
    (doseq [x locs]
      (let [text (get-tick-string step x)
            i (get-string-i gc (i-of x) text)
            j (+ tick-length top-padding)]
        (.drawString gc text i j)))
    
    ; Axis label
    (set-fg-color gc axislabel-color)
    (let [text "__ AXIS __ LABEL __"
          i (get-string-i gc (* 0.5 width) text)
          j (+ tick-length top-padding (get-string-height gc) middle-padding)]
      (.drawString gc text i j))))


(defn #^Canvas make-xaxis-canvas [parent fignum draw]
  (let [canvas (Canvas. parent SWT/DOUBLE_BUFFERED)]
    (attach-graph-mouse-listeners canvas fignum :north :south)
    (set-mouse-cursor canvas SWT/CURSOR_SIZEWE)
    (add-listener canvas SWT/Paint
      (fn [#^Event event]
        (draw (get-fig fignum) (.gc event) (get-width canvas) (get-height canvas))))
    canvas))


(defn #^Canvas make-saxis-canvas [parent fignum]
  (make-xaxis-canvas parent fignum draw-saxis))