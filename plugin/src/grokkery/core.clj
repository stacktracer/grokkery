(ns grokkery.core)


(let [figs (ref {})]

  (defn get-fig [fignum]
    (@figs fignum))
  
  
  (defn find-unused-plotnum [fig]
    (first
      (filter
        (complement (set (keys (:plots (get-fig 0)))))
        (iterate inc 0))))
  
  
  (defn add-plot [fignum data coords drawfn attrs]
    (dosync
      (let [plotnum (find-unused-plotnum (get-fig fignum))
            plot {:fig fignum
                  :plot plotnum
                  :data data
                  :coords coords
                  :drawfn drawfn
                  :attrs attrs}]
        (alter figs
          assoc-in [fignum :plots plotnum] plot)
        plot)))
  
  
  (defn remove-plot [fignum plotnum]
    (dosync
      (alter figs
        update-in [fignum :plots] dissoc plotnum)))
  
  
  (defn update-plot-field [fignum plotnum key f args]
    (dosync
      (alter figs
        update-in [fignum :plots plotnum key] #(apply f % args))))
  
  
  (defn set-plot-field [fignum plotnum key value]
    (dosync
      (alter figs
        assoc-in [fignum :plots plotnum key] value)))
  
  
  ; Awkward to use directly without varags
  (defn update-attr [fignum plotnum attrkey f args]
    (dosync
      (alter figs
        update-in [fignum :plots plotnum :attrs attrkey] #(apply f % args))))
  
  
  (defn set-attr [fignum plotnum attrkey attr]
    (dosync
      (alter figs
        assoc-in [fignum :plots plotnum :attrs attrkey] attr)))
  
  
  (defn set-axes [fignum xaxis-coordkey yaxis-coordkey]
    (let [updates {:bottom xaxis-coordkey :left yaxis-coordkey}]
      (dosync
        (alter figs
          update-in [fignum :axes] merge updates))))
  
  
  (defn update-coordlims [fignum coordkey f args]
    (dosync
      (alter figs
        update-in [fignum :limits coordkey] #(apply f % args))))
  
  
  (defn update-coordmin [fignum coordkey f args]
    (dosync
      (alter figs
        update-in [fignum :limits coordkey :min] #(apply f % args))))
  
  
  (defn update-coordmax [fignum coordkey f args]
    (dosync
      (alter figs
        update-in [fignum :limits coordkey :max] #(apply f % args))))
  
  
  (defn set-coordlims [fignum coordkey coordmin coordmax]
    (let [coordlims {:min coordmin :max coordmax}]
      (dosync
        (alter figs
          assoc-in [fignum :limits coordkey] coordlims))))
  
  
  (defn set-coordmin [fignum coordkey coordmin]
    (dosync
      (alter figs
        assoc-in [fignum :limits coordkey :min] coordmin)))
  
  
  (defn set-coordmax [fignum coordkey coordmax]
    (dosync
      (alter figs
        assoc-in [fignum :limits coordkey :max] coordmax))))




(defn get-plot [fig plotnum]
  (get-in fig [:plots plotnum]))


(defn get-axis [fig axiskey]
  (get-in fig [:axes axiskey]))


(defn get-coordlims [fig coordkey]
  (get-in fig [:limits coordkey]))


(defn get-coordmin [fig coordkey]
  (:min (get-coordlims fig coordkey)))


(defn get-coordmax [fig coordkey]
  (:max (get-coordlims fig coordkey)))




(defn set-data [fignum plotnum data]
  (set-plot-field fignum plotnum :data data))


(defn update-data [fignum plotnum f & args]
  (update-plot-field fignum plotnum :data f args))


(defn update-coords [fignum plotnum f & args]
  (update-plot-field fignum plotnum :coords f args))


(defn def-coord [fignum plotnum coordkey coordfn]
  (update-coords fignum plotnum assoc coordkey coordfn))


(defn set-drawfn [fignum plotnum drawfn]
  (set-plot-field fignum plotnum :drawfn drawfn))


(defn set-attrs [fignum plotnum attrs]
  (set-plot-field fignum plotnum :attrs attrs))


(defn update-attrs [fignum plotnum f & args]
  (update-plot-field fignum plotnum :attrs f args))




(defn draw-plot [gl plot bottom-coordkey left-coordkey]
  (when-let [drawfn (:drawfn plot)]
    (when-let [x-coordfn (get (:coords plot) bottom-coordkey)]
      (when-let [y-coordfn (get (:coords plot) left-coordkey)]
        (drawfn gl (:data plot) x-coordfn y-coordfn (:attrs plot))))))


(defn draw-plots [gl fignum]
  (when-let [fig (get-fig fignum)]
    (let [bottom-coordkey (or (get-axis fig :bottom) :x)
          left-coordkey (or (get-axis fig :left) :y)]
      (dorun
        (map
          (fn [[_ plot]] (draw-plot gl plot bottom-coordkey left-coordkey))
          (:plots fig))))))
