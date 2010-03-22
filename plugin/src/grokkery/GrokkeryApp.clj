(ns grokkery.GrokkeryApp
  (:require
    [grokkery.Perspective :as Perspective])
  (:import
    [grokkery ReplConsole]
    [org.eclipse.equinox.app IApplication IApplicationContext]
    [org.eclipse.swt.widgets Display]
    [org.eclipse.swt.graphics Point]
    [org.eclipse.ui IWorkbench PlatformUI]
    [org.eclipse.ui.application WorkbenchAdvisor WorkbenchWindowAdvisor ActionBarAdvisor]
    [org.eclipse.ui.console ConsolePlugin])
  (:gen-class
    :implements [org.eclipse.equinox.app.IApplication]))


(def window-title-root "Grokkery")
(def window-size [1024 768])
(def save-and-restore? false)


(defn workbench-window-advisor [configurer]
  (proxy [WorkbenchWindowAdvisor] [configurer]
    
    (createActionBarAdvisor [configurer]
      (ActionBarAdvisor. configurer))
    
    (preWindowOpen []
      (doto (.getWindowConfigurer this)
        (.setInitialSize (Point. (first window-size) (second window-size)))
        (.setShowCoolBar false)
        (.setShowStatusLine false)
        (.setTitle
          (if-let [client (System/getProperty "grokkery.client")]
            (str window-title-root " - " client)
            window-title-root))))))


(defn workbench-advisor []
  (proxy [WorkbenchAdvisor] []
    
    (initialize [configurer]
      (.setSaveAndRestore configurer save-and-restore?))
    
    (createWorkbenchWindowAdvisor [configurer]
      (workbench-window-advisor configurer))
    
    (getInitialWindowPerspectiveId []
      Perspective/id)
    
    (postStartup []
      (let [console (ReplConsole.)]
        (doto (.. ConsolePlugin getDefault getConsoleManager)
          (.addConsoles (into-array [console]))
          (.showConsoleView console))))))


(defn app-retval [workbench-retval]
  (if
    (= workbench-retval PlatformUI/RETURN_RESTART)
    IApplication/EXIT_RESTART
    IApplication/EXIT_OK))


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