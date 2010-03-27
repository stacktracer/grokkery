(ns grokkery.core)


(let [figs (ref {})]

  (let [used-plotnums (ref {})]
    (defn- take-plotnum! [fignum]
      (dosync
        (alter used-plotnums
          update-in [fignum] #(if % (inc %) 0))
        (@used-plotnums fignum))))
  
  
  (defn add-plot [fignum data coords drawfn attrs]
    (dosync
      (let [plotnum (take-plotnum! fignum)
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
  
  
  (defn alter-plot-field [fignum plotnum key f args]
    (dosync
      (alter figs
        update-in [fignum :plots plotnum key] #(apply f % args))))
  
  
  (defn set-plot-field [fignum plotnum key value]
    (dosync
      (alter figs
        assoc-in [fignum :plots plotnum key] value)))
  
  
  (defn get-plots [fignum]
    (get-in @figs [fignum :plots]))
  
  
  (defn get-plot [fignum plotnum]
    (get-in @figs [fignum :plots plotnum]))
  
  
  
  
  (defn set-axes [fignum xaxis-coordkey yaxis-coordkey]
    (let [updates {:bottom xaxis-coordkey :left yaxis-coordkey}]
      (dosync
        (alter figs
          update-in [fignum :axes] merge updates))))
  
  
  (defn get-axis [fignum axiskey]
    (get-in @figs [fignum :axes axiskey]))
  
  
  
  
  (defn alter-coordlims [fignum coordkey f args]
    (dosync
      (alter figs
        update-in [fignum :limits coordkey] #(apply f % args))))
  
  
  (defn alter-coordmin [fignum coordkey f args]
    (dosync
      (alter figs
        update-in [fignum :limits coordkey :min] #(apply f % args))))
  
  
  (defn alter-coordmax [fignum coordkey f args]
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
        assoc-in [fignum :limits coordkey :max] coordmax)))
  
  
  (defn get-coordlims [fignum coordkey]
    (or
      (get-in @figs [fignum :limits coordkey])
      {:min 0 :max 1}))
  
  
  (defn get-coordmin [fignum coordkey]
    (:min (get-coordlims fignum coordkey)))
  
  
  (defn get-coordmax [fignum coordkey]
    (:max (get-coordlims fignum coordkey))))




(defn draw-plot [gl plot bottom-coordkey left-coordkey]
  (when-let [drawfn (:drawfn plot)]
    (when-let [x-coordfn (get (:coords plot) bottom-coordkey)]
      (when-let [y-coordfn (get (:coords plot) left-coordkey)]
        (drawfn gl (:data plot) x-coordfn y-coordfn (:attrs plot))))))


(defn draw-plots [gl fignum]
  (let [bottom-coordkey (or (get-axis fignum :bottom) :x)
        left-coordkey (or (get-axis fignum :left) :y)]
    (dorun
      (map
        (fn [[_ plot]] (draw-plot gl plot bottom-coordkey left-coordkey))
        (get-plots fignum)))))




(defn set-data [fignum plotnum data]
  (set-plot-field fignum plotnum :data data))


(defn alter-data [fignum plotnum f & args]
  (alter-plot-field fignum plotnum :data f args))


(defn alter-coords [fignum plotnum f & args]
  (alter-plot-field fignum plotnum :coords f args))


(defn def-coord [fignum plotnum coordkey coordfn]
  (alter-coords fignum plotnum assoc coordkey coordfn))


(defn set-drawfn [fignum plotnum drawfn]
  (set-plot-field fignum plotnum :drawfn drawfn))


(defn set-attrs [fignum plotnum attrs]
  (set-plot-field fignum plotnum :attrs attrs))


(defn alter-attrs [fignum plotnum f & args]
  (alter-plot-field fignum plotnum :attrs f args))


(defn set-attr [fignum plotnum attrkey attr]
  (alter-attrs fignum plotnum assoc attrkey attr))
