(ns grokkery.rcp.ReplConsole
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
    :post-init post-init-instance))


(defn -init-instance []
  [["Clojure Repl" nil] nil])


(defn init-repl []
  (in-ns 'user))


(defn make-prompt-fn [out]
  (let [writer (PrintWriter. out)]
    #(doto writer
      (.format "%s=> " (into-array [(ns-name *ns*)]))
      (.flush))))


(defn -post-init-instance [this]
  ; Use separate stream for prompts so we can give them a different color
  (let [in (.getInputStream this)
        out (.newOutputStream this)
        err (.newOutputStream this)
        prompt-out (.newOutputStream this)
        prompt (make-prompt-fn prompt-out)]
    (.setColor in (Color. nil 0 0 0))
    (.setColor out (Color. nil 0 0 192))
    (.setColor err (Color. nil 255 0 0))
    (.setColor prompt-out (Color. nil 0 160 32))
    
    (doto
      (Thread.
        #(binding [*in* (LineNumberingPushbackReader. (InputStreamReader. in))
                   *out* (PrintWriter. out true)
                   *err* (PrintWriter. err true)]
           (clojure.main/repl :init init-repl :prompt prompt))
        "Clojure Repl")
      (.setDaemon true)
      (.start))))