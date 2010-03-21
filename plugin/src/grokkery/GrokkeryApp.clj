(ns grokkery.GrokkeryApp
  (:import
    [grokkery ReplConsole Perspective ApplicationWorkbenchWindowAdvisor]
    [org.eclipse.equinox.app IApplication IApplicationContext]
    [org.eclipse.swt.widgets Display]
    [org.eclipse.ui IWorkbench PlatformUI]
    [org.eclipse.ui.application WorkbenchAdvisor]
    [org.eclipse.ui.console ConsolePlugin])
  (:gen-class
    :implements [org.eclipse.equinox.app.IApplication]))


(defn app-retval [workbench-retval]
  (if
    (= workbench-retval PlatformUI/RETURN_RESTART)
    IApplication/EXIT_RESTART
    IApplication/EXIT_OK))


(defn workbench-advisor []
  (proxy [WorkbenchAdvisor] []
    
    (initialize [configurer]
      #_(.setSaveAndRestore configurer true))
    
    (createWorkbenchWindowAdvisor [window-configurer]
      (ApplicationWorkbenchWindowAdvisor. window-configurer))
    
    (getInitialWindowPerspectiveId []
      (Perspective/id))
    
    (postStartup []
      (let [console (ReplConsole.)]
        (doto (.. ConsolePlugin getDefault getConsoleManager)
          (.addConsoles (into-array [console]))
          (.showConsoleView console))))))


(defn -start [app context]
  (let [display (PlatformUI/createDisplay)]
    (try
      (app-retval
        (PlatformUI/createAndRunWorkbench display (workbench-advisor)))
      (finally
        (.dispose display)))))


(defn -stop [app]
  (when-let [workbench (PlatformUI/getWorkbench)]
    (when-let [display (.getDisplay workbench)]
      (.syncExec display
        #(when-not (.isDisposed display) (.close workbench))))))