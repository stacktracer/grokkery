(ns grokkery.ReplConsole
  (:require
    clojure.main)
  (:import
    [java.io InputStreamReader PrintWriter]
    [clojure.lang LineNumberingPushbackReader]
    [org.eclipse.swt SWT]
    [org.eclipse.swt.graphics Color]
    [org.eclipse.ui.console IOConsoleInputStream IOConsoleOutputStream])
  (:gen-class
    :extends org.eclipse.ui.console.IOConsole
    :init init-instance
    :constructors {[] [String org.eclipse.jface.resource.ImageDescriptor]}
    :post-init post-init-instance
    :exposes-methods {createPage superCreatePage}))


(defn -init-instance []
  [["Clojure Repl" nil] nil])


(defn -createPage [this view]
  (intern 'user 'page (.. view (getSite) (getPage)))
  (.. System err (println "-createPage"))
  (. this superCreatePage view))


; Create a GraphView
;(use 'grokkery.util)
;(ui-run-async #(.showView page "grokkery.GraphView"))


(defn -post-init-instance [this]
  (let [in (.getInputStream this)
        out (.newOutputStream this)
        err (.newOutputStream this)]
    (.setColor in (Color. nil 0 0 0))
    (.setColor out (Color. nil 0 0 192))
    (.setColor err (Color. nil 255 0 0))
    
    (doto
      (Thread.
        #(binding [*in* (LineNumberingPushbackReader. (InputStreamReader. in))
                   *out* (PrintWriter. out true)
                   *err* (PrintWriter. err true)]
           (clojure.main/repl
             :init (fn [] (in-ns 'user))))
        "Clojure Repl")
      (.setDaemon true)
      (.start))))