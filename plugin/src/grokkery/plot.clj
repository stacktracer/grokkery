(ns grokkery.plot)


(let [plots (ref {})]

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
        (alter plots
          assoc-in [fignum :plots plotnum] plot)
        plot)))
  
  
  (defn remove-plot [fignum plotnum]
    (dosync
      (alter plots
        update-in [fignum :plots] dissoc plotnum)))
  
  
  (defn alter-plot-field [fignum plotnum key f args]
    (dosync
      (alter plots
        update-in [fignum :plots plotnum key] #(apply f % args))))
  
  
  (defn set-plot-field [fignum plotnum key value]
    (dosync
      (alter plots
        assoc-in [fignum :plots plotnum key] value)))
  
  
  (defn set-axes [fignum xaxis-coordkey yaxis-coordkey]
    (let [updates {:bottom xaxis-coordkey :left yaxis-coordkey}]
      (dosync
        (alter plots
          update-in [fignum :axes] merge updates))))
  
  
  (defn get-axis [fignum axiskey]
    (get-in @plots [fignum :axes axiskey]))
  
  
  (defn get-plots [fignum]
    (get-in @plots [fignum :plots]))
  
  
  (defn get-plot [fignum plotnum]
    (get-in @plots [fignum :plots plotnum])))




(defn draw-plot [gl plot xaxis-coordkey yaxis-coordkey]
  (when-let [drawfn (:drawfn plot)]
    (when-let [x-coordfn (get (:coords plot) xaxis-coordkey)]
      (when-let [y-coordfn (get (:coords plot) yaxis-coordkey)]
        (drawfn gl (:data plot) x-coordfn y-coordfn (:attrs plot))))))


(defn draw-plots [gl fignum]
  (let [xaxis-coordkey (or (get-axis fignum :bottom) :x)
        yaxis-coordkey (or (get-axis fignum :left) :y)]
    (dorun
      (map
        (fn [[_ plot]] (draw-plot gl plot xaxis-coordkey yaxis-coordkey))
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
