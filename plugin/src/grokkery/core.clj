(ns grokkery.core
  (:use
    grokkery.util)
  (:import
    [java.util NoSuchElementException]
    [javax.media.opengl GL]))


(def fallback-coordlims [0 10])


(defn get-valid-limits [limits]
  (if
    (and
      (not-empty limits)
      (< (min-of limits) (max-of limits)))
    limits
    fallback-coordlims))


(let [figs (ref {})]

  (defn get-figref [fignum]
    (@figs fignum))
  
  
  (defn get-fig [fignum]
    @(get-figref fignum))
  
  
  (defn find-unused-fignum []
    (first
      (filter
        (complement (set (keys @figs)))
        (iterate inc 0))))
  
  
  (defn add-fig []
    (dosync
      (let [fignum (find-unused-fignum)]
        (alter figs
          assoc fignum (ref {}))
        fignum)))
  
  
  (defn remove-fig [fignum]
    (dosync
      (alter figs
        dissoc fignum)))
  
  
  (defn find-unused-plotnum [fig]
    (first
      (filter
        (complement (set (keys (:plots fig))))
        (iterate inc 0))))
  
  
  (defn add-plot [fignum data coords drawfn attrs]
    (dosync
      (let [figref (get-figref fignum)
            plotnum (find-unused-plotnum @figref)]
        (alter figref
          assoc-in [:plots plotnum] {:fig fignum, :plot plotnum, :data data, :coords coords, :drawfn drawfn, :attrs attrs})
        plotnum)))
  
  
  (defn remove-plot [fignum plotnum]
    (dosync
      (alter (get-figref fignum)
        update-in [:plots] dissoc plotnum)))
  
  
  (defn update-plot-field [fignum plotnum key f args]
    (dosync
      (alter (get-figref fignum)
        update-in [:plots plotnum key] #(apply f % args))))
  
  
  (defn set-plot-field [fignum plotnum key value]
    (dosync
      (alter (get-figref fignum)
        assoc-in [:plots plotnum key] value)))
  
  
  ; Awkward to use directly without varags
  (defn update-attr [fignum plotnum attrkey f args]
    (dosync
      (alter (get-figref fignum)
        update-in [:plots plotnum :attrs attrkey] #(apply f % args))))
  
  
  (defn set-attr [fignum plotnum attrkey attr]
    (dosync
      (alter (get-figref fignum)
        assoc-in [:plots plotnum :attrs attrkey] attr)))
  
  
  (defn update-axes [fignum f args]
    (dosync
      (alter (get-figref fignum)
        update-in [:axes] #(apply f % args))))
  
  
  (defn replace-axes [fignum axes]
    (dosync
      (alter (get-figref fignum)
        assoc :axes axes)))
  
  
  (defn set-limits [fignum limits]
    (dosync
      (alter (get-figref fignum)
        assoc :limits limits)))
  
  
  (defn update-coordlims [fignum coordkey f args]
    (dosync
      (alter (get-figref fignum)
        update-in [:limits coordkey] #(apply f (get-valid-limits %) args))))
  
  
  (defn set-coordlims [fignum coordkey coordlims]
    (dosync
      (alter (get-figref fignum)
        assoc-in [:limits coordkey] coordlims)))
  
  
  (defn derive-coord [fignum derived-coordkey derived-coordfn]
    (dosync
      (alter (get-figref fignum)
        assoc-in [:derived-coords derived-coordkey] derived-coordfn))))




(defn get-plot [fig plotnum]
  (get-in fig [:plots plotnum]))


(defn get-coordlims [fig coordkey]
  (get-valid-limits (get-in fig [:limits coordkey])))


(defn get-coordkey [fig axiskey]
  (get-in fig [:axes axiskey]))


(defn get-axislims [fig axiskey]
  (get-coordlims fig (get-coordkey fig axiskey)))


(defn get-coord [fig coordkey fraction]
  (let [lims (get-coordlims fig coordkey)
        min (min-of lims)
        max (max-of lims)]
    (+ min (* fraction (- max min)))))


(defn- coord-entry [fig axiskey fraction]
  (let [coordkey (get-coordkey fig axiskey)]
    {coordkey (get-coord fig coordkey fraction)}))


(defn get-coords
  ([fig x-fraction y-fraction]
    (dissoc
      (merge
        (coord-entry fig :north x-fraction)
        (coord-entry fig :south x-fraction)
        (coord-entry fig :east  y-fraction)
        (coord-entry fig :west  y-fraction))
      nil))
  ([fig x-fraction y-fraction axiskeys]
    (select-keys
      (get-coords fig x-fraction y-fraction)
      (map #(get-coordkey fig %) axiskeys))))




(defn set-data [fignum plotnum data]
  (set-plot-field fignum plotnum :data data))


(defn update-data [fignum plotnum f & args]
  (update-plot-field fignum plotnum :data f args))


(defn update-coords [fignum plotnum f & args]
  (update-plot-field fignum plotnum :coords f args))


(defn def-coord [fignum plotnum coordkey coordfn]
  (update-coords fignum plotnum assoc coordkey coordfn))


(defn rekey-coord [fignum plotnum old-coordkey new-coordkey]
  (update-coords fignum plotnum
    (fn [coords]
      (when-not (contains? coords old-coordkey)
        (throw (NoSuchElementException. (str "No coord by this key: fig = " fignum ", plot = " plotnum ", key = " old-coordkey))))
      (-> coords
        (assoc new-coordkey (get coords old-coordkey))
        (dissoc old-coordkey)))))


(defn get-coordfn [fig plot coordkey]
  (or
    (get (:coords plot) coordkey)
    (if-let [dcoordfn (get (:derived-coords fig) coordkey)] (partial dcoordfn plot))))


(defn set-drawfn [fignum plotnum drawfn]
  (set-plot-field fignum plotnum :drawfn drawfn))


(defn set-attrs [fignum plotnum attrs]
  (set-plot-field fignum plotnum :attrs attrs))


(defn update-attrs [fignum plotnum f & args]
  (update-plot-field fignum plotnum :attrs f args))


(defn set-axes [fignum south-coordkey west-coordkey]
  (update-axes fignum merge {:south south-coordkey, :west west-coordkey}))


(defn pan
  ([fignum coordkey-amounts]
    (dosync
      (dorun
        (map
          (fn [[k v]] (update-coordlims fignum k #(map (partial + v) %) []))
          coordkey-amounts))))
  ([fignum coordkey amount]
    (pan fignum {coordkey amount}))
  ([fignum coordkey amount & more]
    (pan fignum (apply hash-map coordkey amount more))))


(defn zoom-coord [anchor factor coord]
  (+ anchor (* factor (- coord anchor))))


(defn zoom [fignum steps anchor-coords]
  (let [factor (Math/pow 1.111 steps)]
    (dosync
      (dorun
        (map
          (fn [[k v]] (update-coordlims fignum k #(map (partial zoom-coord v factor) %) []))
          anchor-coords)))))




(defmacro derived-coordfn [bindings & body]
  (let [plot-sym (gensym 'plot)
        args-sym (gensym 'args)]
    `(fn [~plot-sym & ~args-sym]
       (let
         ~(vec
            (mapcat
              (fn [%] [(first %) `(if-let [coordfn# (get (:coords ~plot-sym) ~(second %))] (apply coordfn# ~args-sym))])
              (partition 2 bindings)))
         ~@body))))




(defn prep-plot [#^GL gl xlim ylim]
  (doto gl
    (.glMatrixMode GL/GL_PROJECTION)
    (.glLoadIdentity)
    (.glOrtho (min-of xlim) (max-of xlim) (min-of ylim) (max-of ylim) -1 1)
    (.glMatrixMode GL/GL_MODELVIEW)
    (.glLoadIdentity)
    
    (.glEnable GL/GL_BLEND)
    (.glBlendFunc GL/GL_SRC_ALPHA GL/GL_ONE_MINUS_SRC_ALPHA)
    
    (.glEnable GL/GL_POINT_SMOOTH)
    (.glHint GL/GL_POINT_SMOOTH_HINT GL/GL_NICEST)
    
    (.glEnable GL/GL_LINE_SMOOTH)
    (.glHint GL/GL_LINE_SMOOTH_HINT GL/GL_NICEST)))


(defn draw-plot [#^GL gl fig plotnum axis-coordkeys]
  (let [plot (get-plot fig plotnum)
        
        x-coordkey (:south axis-coordkeys)
        x-coordfn (get-coordfn fig plot x-coordkey)
        x-limits (get-coordlims fig x-coordkey)
        
        y-coordkey (:west axis-coordkeys)
        y-coordfn (get-coordfn fig plot y-coordkey)
        y-limits (get-coordlims fig y-coordkey)
        
        drawfn (:drawfn plot)]
    
    (when (and x-coordfn y-coordfn drawfn)
      (gl-push-all gl
        (prep-plot gl x-limits y-limits)
        (drawfn gl (:data plot) x-coordfn y-coordfn (:attrs plot))))))


(defn draw-plots [gl fignum]
  (when-let [fig (get-fig fignum)]
    (dorun
      (map
        #(draw-plot gl fig % (:axes fig))
        (keys (:plots fig))))))
