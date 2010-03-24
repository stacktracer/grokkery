(ns grokkery.plot)


(let [plots (ref {})]

  (let [used-plotnums (ref {})]
    (defn- take-plotnum! [fignum]
      (dosync
        (alter used-plotnums
          update-in [fignum]
            #(if % (inc %) 0))
        (@used-plotnums fignum))))
  
  
  (defn add-plot [fignum data-ref axfns-ref drawfn-ref attrs-ref]
    (dosync
      (let [plotnum (take-plotnum! fignum)
            plot {:fig fignum
                  :plot plotnum
                  :data data-ref
                  :axfns axfns-ref
                  :drawfn drawfn-ref
                  :attrs attrs-ref}]
        (alter plots
          update-in [fignum]
            assoc plotnum plot)
        plot)))
  
  
  (defn remove-plot [fignum plotnum]
    (dosync
      (alter plots
        update-in [fignum]
          dissoc plotnum)))
  
  
  (defn get-plots [fignum]
    (@plots fignum))
  
  
  (defn get-plot [fignum plotnum]
    ((get-plots fignum) plotnum)))




(let [axkeys (ref {})]

  (defn get-axkey [fignum axid]
    (if-let [fig-axkeys (@axkeys fignum)]
      (fig-axkeys axid)))
  
  
  (defn set-axkey [fignum axid axkey]
    (dosync
      (alter axkeys
        update-in [fignum]
          assoc axid axkey))))




(defn draw-plot [gl plot x-axkey y-axkey]
  (when-let [drawfn @(:drawfn plot)]
    (when-let [x-axfn (@(:axfns plot) x-axkey)]
      (when-let [y-axfn (@(:axfns plot) y-axkey)]
        (drawfn gl @(:data plot) x-axfn y-axfn @(:attrs plot))))))


(defn draw-plots [gl fignum]
  (let [x-axkey (or (get-axkey fignum :x) :x)
        y-axkey (or (get-axkey fignum :y) :y)]
    (dorun
      (map
        (fn [[_ plot]] (draw-plot gl plot x-axkey y-axkey))
        (get-plots fignum)))))




(defn alter-plot-field [fignum plotnum key f args]
  (dosync
    (alter ((get-plot fignum plotnum) key) #(apply f % args))))


(defn set-plot-field [fignum plotnum key value]
  (dosync
    (ref-set ((get-plot fignum plotnum) key) value)))




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
