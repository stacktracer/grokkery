(ns grokkery.rcp.graph
  (:use
    clojure.contrib.import-static
    grokkery.util
    grokkery.core)
  (:import
    [org.eclipse.swt SWT]
    [org.eclipse.swt.graphics GC]
    [org.eclipse.swt.widgets Control Listener Event Composite]))

(import-static java.lang.Math floor ceil log10 round pow)




(def tick-line-width 1)
(def tick-length 5)

(def pixels-between-ticks 50)




(defn attach-graph-mouse-listeners [#^Control control fignum & axiskeys]
  (let [grab-coords (ref {})
        
        get-mouse-coords (fn [#^Event event]
                           (get-coords (get-fig fignum)
                             (/ (float (.x event)) (float (get-width control)))
                             (- 1 (/ (float (.y event)) (float (get-height control))))
                             axiskeys))
        
        ; Up/down events are sometimes interleaved out of order wrt move events.
        ; To deal with this, we track the button state manually: @grab-coords is
        ; nil iff no buttons are down. As a result, we treat press and drag events
        ; the same way, and release and move events the same way.
        
        on-press-or-drag (fn [event]
                           (dosync
                             (if (nil? @grab-coords)
                               (ref-set grab-coords (get-mouse-coords event))
                               (pan fignum (merge-with - @grab-coords (get-mouse-coords event))))))
        
        on-release-or-move (fn [event]
                             (dosync
                               (ref-set grab-coords nil)))]
    
    (add-listener control SWT/MouseUp on-release-or-move)
    (add-listener control SWT/MouseDown on-press-or-drag)
    (add-listener control SWT/MouseMove #(if (mouse-button-down? %) (on-press-or-drag %) (on-release-or-move %)))
    
    (add-listener control SWT/MouseWheel
      (fn [#^Event event]
        (dosync
          (zoom fignum (- (.count event)) (get-mouse-coords event)))))))




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




(defn get-axis-lims [fig axiskey]
  (let [lims (get-axislims fig axiskey)]
    {:min (min-of lims), :max (max-of lims)}))


(defn get-axis-ticks [fig axiskey extent-pixels]
  (let [{:keys [min max]} (get-axis-lims fig axiskey)
        approx-num-ticks (/ extent-pixels pixels-between-ticks)
        step (get-tick-step min max approx-num-ticks)
        locs (get-tick-locations min max step)]
    {:step step, :locs locs}))




(defn get-system-color [#^GC gc color-id]
  (.. gc (getDevice) (getSystemColor color-id)))


(defn bg-color [#^GC gc] (get-system-color gc SWT/COLOR_WIDGET_BACKGROUND))
(defn tick-color [#^GC gc] (get-system-color gc SWT/COLOR_WIDGET_NORMAL_SHADOW))
(defn ticktext-color [#^GC gc] (get-system-color gc SWT/COLOR_WIDGET_FOREGROUND))
(defn axis-label-color [#^GC gc] (get-system-color gc SWT/COLOR_WIDGET_FOREGROUND))


(defn set-fg-color [#^GC gc get-color]
  (.setForeground gc (get-color gc)))


(defn set-bg-color [#^GC gc get-color]
  (.setBackground gc (get-color gc)))