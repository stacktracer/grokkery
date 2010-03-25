(ns grokkery.plot)


(let [plots (ref {})]

  (let [used-plotnums (ref {})]
    (defn- take-plotnum! [fignum]
      (dosync
        (alter used-plotnums
          update-in [fignum] #(if % (inc %) 0))
        (@used-plotnums fignum))))
  
  
  (defn add-plot [fignum data axfns drawfn attrs]
    (dosync
      (let [plotnum (take-plotnum! fignum)
            plot {:fig fignum
                  :plot plotnum
                  :data data
                  :axfns axfns
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
  
  
  (defn set-axkeys [fignum x-axkey y-axkey]
    (dosync
      (alter plots
        assoc-in [fignum :axkeys :horizontal] x-axkey)
      (alter plots
        assoc-in [fignum :axkeys :vertical] y-axkey)))
  
  
  (defn get-axkey [fignum axid]
    (get-in @plots [fignum :axkeys axid]))
  
  
  (defn get-plots [fignum]
    (get-in @plots [fignum :plots]))
  
  
  (defn get-plot [fignum plotnum]
    (get-in @plots [fignum :plots plotnum])))




(defn draw-plot [gl plot x-axkey y-axkey]
  (when-let [drawfn (:drawfn plot)]
    (when-let [x-axfn (get (:axfns plot) x-axkey)]
      (when-let [y-axfn (get (:axfns plot) y-axkey)]
        (drawfn gl (:data plot) x-axfn y-axfn (:attrs plot))))))


(defn draw-plots [gl fignum]
  (let [x-axkey (or (get-axkey fignum :horizontal) :x)
        y-axkey (or (get-axkey fignum :vertical) :y)]
    (dorun
      (map
        (fn [[_ plot]] (draw-plot gl plot x-axkey y-axkey))
        (get-plots fignum)))))




(defn set-data [fignum plotnum data]
  (set-plot-field fignum plotnum :data data))


(defn alter-data [fignum plotnum f & args]
  (alter-plot-field fignum plotnum :data f args))


(defn alter-axfns [fignum plotnum f & args]
  (alter-plot-field fignum plotnum :axfns f args))


(defn put-axfn [fignum plotnum axkey axfn]
  (alter-axfns fignum plotnum assoc axkey axfn))


(defn set-drawfn [fignum plotnum drawfn]
  (set-plot-field fignum plotnum :drawfn drawfn))


(defn set-attrs [fignum plotnum attrs]
  (set-plot-field fignum plotnum :attrs attrs))


(defn alter-attrs [fignum plotnum f & args]
  (alter-plot-field fignum plotnum :attrs f args))


(defn set-attr [fignum plotnum attrkey attr]
  (alter-attrs fignum plotnum assoc attrkey attr))
