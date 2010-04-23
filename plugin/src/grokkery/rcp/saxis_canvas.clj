(ns grokkery.rcp.saxis-canvas
  (:use
    grokkery.util
    grokkery.core)
  (:import
    [org.eclipse.swt SWT]
    [org.eclipse.swt.graphics GC Cursor]
    [org.eclipse.swt.widgets Canvas Listener Event Composite]))


(defn get-saxis-height [fig gc]
  30)


(defn draw-saxis [fignum gc width height]
  )


(defn #^Canvas make-axis-canvas [parent fignum draw]
  (let [canvas (Canvas. parent SWT/DOUBLE_BUFFERED)]
    (add-listener canvas SWT/Paint
      (fn [#^Event event]
        (draw fignum (.gc event) (get-width canvas) (get-height canvas))))
    canvas))


(defn #^Canvas make-saxis-canvas [parent fignum]
  (make-axis-canvas parent fignum draw-saxis))