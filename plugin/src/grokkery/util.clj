(ns grokkery.util
  (:import
    [org.eclipse.swt SWT]
    [org.eclipse.swt.widgets Listener Display Event Control]
    [org.eclipse.swt.graphics Cursor]
    [org.eclipse.swt.events VerifyListener VerifyEvent]
    [org.eclipse.ui PlatformUI]
    [javax.media.opengl GL]))


(defn or-flags [& flags]
  (reduce bit-or 0 flags))


(defn min-of [coll]
  (if (empty? coll) nil (apply min coll)))


(defn max-of [coll]
  (if (empty? coll) nil (apply max coll)))


(defn add-listener [widget event-type f & args]
  (let [listener (proxy [Listener] []
                   (handleEvent [event] (apply f event args)))]
    (.addListener widget event-type listener)
    listener))


(defn ui-async-exec [f]
  (.asyncExec (Display/getDefault) f))


(defn ui-sync-exec [f]
  (let [retref (ref nil)]
    (.syncExec (Display/getDefault)
      #(let [retval (f)]
         (dosync (ref-set retref retval)))) 
    @retref))


(defn get-active-page []
  (..
    PlatformUI
    (getWorkbench)
    (getActiveWorkbenchWindow)
    (getActivePage)))


(defn mouse-button-down? [#^Event event]
	(not (zero? (bit-and (.stateMask event) SWT/BUTTON_MASK))))


(defn get-width [#^Control control]
  (.. control (getSize) x))


(defn get-height [#^Control control]
  (.. control (getSize) y))


(defn set-mouse-cursor [#^Control control cursor-id]
  (.setCursor control
   (Cursor.
     (.getDisplay control)
     cursor-id)))


(defn gl-set-color [#^GL gl c]
  (.glColor4f gl (c 0) (c 1) (c 2) (c 3)))


(defmacro gl-draw [gl primitive & body]
  `(do
     (.glBegin ~gl ~primitive)
     (try
       ~@body
       (finally
         (.glEnd ~gl)))))


(defmacro gl-push-all [gl & body]
  `(do
     (doto ~gl
       (.glPushAttrib ~GL/GL_ALL_ATTRIB_BITS)
       (.glMatrixMode ~GL/GL_MODELVIEW)
       (.glPushMatrix)
       (.glMatrixMode ~GL/GL_PROJECTION)
       (.glPushMatrix))
     (try
       ~@body
       (finally
         (doto ~gl
           (.glMatrixMode ~GL/GL_MODELVIEW)
           (.glPopMatrix)
           (.glMatrixMode ~GL/GL_PROJECTION)
           (.glPopMatrix)
           (.glPopAttrib))))))


(defmacro stopwatch [msg expr]
  `(let [start# (System/nanoTime)
         ret# ~expr]
     (prn (str ~msg " -- elapsed time: " (* 1e-6 (double (- (System/nanoTime) start#))) " ms"))
     ret#))