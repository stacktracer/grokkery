(ns grokkery.util
  (:import
    [org.eclipse.swt SWT]
    [org.eclipse.swt.widgets Listener Display]
    [org.eclipse.swt.events VerifyListener VerifyEvent]
    [org.eclipse.ui PlatformUI]
    [javax.media.opengl GL]))


(defn or-flags [& flags]
  (reduce bit-or 0 flags))


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


(defn mouse-button-down? [event]
	(not (zero? (bit-and (.stateMask event) SWT/BUTTON_MASK))))


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