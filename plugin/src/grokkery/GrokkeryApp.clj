(ns grokkery.GrokkeryApp
  (:import
    [grokkery ApplicationWorkbenchAdvisor]
    [org.eclipse.equinox.app IApplication IApplicationContext]
    [org.eclipse.swt.widgets Display]
    [org.eclipse.ui IWorkbench PlatformUI])
  (:gen-class
    :implements [org.eclipse.equinox.app.IApplication]))


(defn app-retval [workbench-retval]
  (if
    (= workbench-retval PlatformUI/RETURN_RESTART)
    IApplication/EXIT_RESTART
    IApplication/EXIT_OK))
    

(defn -start [app context]
  (let [display (PlatformUI/createDisplay)]
    (try
      (app-retval
        (PlatformUI/createAndRunWorkbench display (ApplicationWorkbenchAdvisor.)))
      (finally
        (.dispose display)))))


(defn -stop [app]
  (when-let [workbench (PlatformUI/getWorkbench)]
    (when-let [display (.getDisplay workbench)]
      (.syncExec display
        #(when-not (.isDisposed display) (.close workbench))))))