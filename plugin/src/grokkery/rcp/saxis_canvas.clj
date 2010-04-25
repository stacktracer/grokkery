(ns grokkery.rcp.saxis-canvas
  (:use
    clojure.contrib.import-static
    grokkery.util
    grokkery.core
    grokkery.rcp.axis-canvas)
  (:import
    [org.eclipse.swt SWT]
    [org.eclipse.swt.graphics GC Cursor]
    [org.eclipse.swt.widgets Canvas Listener Event Composite]))

(import-static java.lang.Math ceil round)




(def top-padding 2)
(def bottom-padding 2)


(defn get-saxis-lims [fig]
  (get-axis-lims fig :south))


(defn get-saxis-ticks [fig width]
  (get-axis-ticks fig :south width))


(defn get-string-height [#^GC gc s]
  (.. gc (stringExtent s) y))


(defn get-saxis-height [fig #^GC gc]
  (let [number-height (get-string-height gc "+0.12345678E-9")]
    (+ bottom-padding number-height top-padding tick-length)))


(defn get-i [width xmin xmax x]
  (+ 0.5 (/ (* width (- x xmin)) (- xmax xmin))))


(defn get-i-fn [fig width]
  (let [{:keys [min max]} (get-saxis-lims fig)]
    (partial get-i width min max)))


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
            text-extent (.stringExtent gc text)
            i (round (double (- (i-of x) (* 0.5 (.x text-extent)))))
            j (+ tick-length top-padding)]
        (.drawString gc text i j)))))


(defn #^Canvas make-xaxis-canvas [parent fignum draw]
  (let [canvas (Canvas. parent SWT/DOUBLE_BUFFERED)]
    (add-listener canvas SWT/Paint
      (fn [#^Event event]
        (draw (get-fig fignum) (.gc event) (get-width canvas) (get-height canvas))))
    canvas))


(defn #^Canvas make-saxis-canvas [parent fignum]
  (make-xaxis-canvas parent fignum draw-saxis))