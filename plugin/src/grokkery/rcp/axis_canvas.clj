(ns grokkery.rcp.axis-canvas
  (:use
    grokkery.util
    grokkery.core)
  (:import
    [org.eclipse.swt SWT]
    [org.eclipse.swt.graphics GC Cursor]
    [org.eclipse.swt.widgets Canvas Listener Event Composite]))


(defn get-xaxis-height [gc]
  30)


(defn get-yaxis-width [gc]
  30)


(defn #^Canvas make-axis-canvas [parent fignum]
  (let [canvas (Canvas. parent SWT/DOUBLE_BUFFERED)]
    (add-listener canvas SWT/Paint
      (fn [#^Event event]
        (when-let [draw nil]
          (draw (.gc event) (.getBounds canvas)))))
    canvas))