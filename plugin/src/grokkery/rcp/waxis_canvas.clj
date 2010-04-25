(ns grokkery.rcp.waxis-canvas
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




(def left-padding 2)
(def right-padding 2)


(defn get-waxis-lims [fig]
  (get-axis-lims fig :west))


(defn get-waxis-ticks [fig height]
  (get-axis-ticks fig :west height))


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
    (.setLineWidth gc tick-line-width)
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