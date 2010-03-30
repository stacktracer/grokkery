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
  
  
  ; Accept lims for multiple coords (set-coordlims 0 :x [0 1] :y [-2 2])
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


(defn get-coordlims [fig coordkey]
  (get-in fig [:limits coordkey]))


(defn get-coordmin [fig coordkey]
  (:min (get-coordlims fig coordkey)))


(defn get-coordmax [fig coordkey]
  (:max (get-coordlims fig coordkey)))


(defn get-axis [fig axiskey default-coordkey]
  (let [coordkey (or (get-in fig [:axes axiskey]) default-coordkey)]
    (assoc
      (get-coordlims fig coordkey)
      :coord coordkey)))


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









; Try destructuring args
(defn get-limits [x-axis y-axis]
  (let [xmin (:min x-axis)
        xmax (:max x-axis)
        ymin (:min y-axis)
        ymax (:max y-axis)]
    (merge
      (if (some nil? [xmin xmax]) {:xmin 0 :xmax 1} {:xmin xmin :xmax xmax})
      (if (some nil? [ymin ymax]) {:ymin 0 :ymax 1} {:ymin ymin :ymax ymax}))))


(defn prep-plot [#^GL gl attrs]
  (.glPointSize gl (or (:pointsize attrs) default-pointsize)
  (gl-set-color gl (or (:color attrs) default-color)))
  (let [xmin
        xmax
        ymin
        ymax]
  
    (doto gl
      (.glMatrixMode GL/GL_PROJECTION)
      (.glLoadIdentity)
      (.glOrtho xmin xmax ymin ymax -1 1)
      (.glMatrixMode GL/GL_MODELVIEW)
      (.glLoadIdentity)
      
      (.glEnable GL/GL_BLEND)
      (.glBlendFunc GL/GL_SRC_ALPHA GL/GL_ONE_MINUS_SRC_ALPHA)
      
      (.glEnable GL/GL_POINT_SMOOTH)
      (.glHint GL/GL_POINT_SMOOTH_HINT GL/GL_NICEST)
      
      (.glEnable GL/GL_LINE_SMOOTH)
      (.glHint GL/GL_LINE_SMOOTH_HINT GL/GL_NICEST))))


(defn draw-plot [gl plot bottom-axis left-axis]
  (when-let [drawfn (:drawfn plot)]
    (when-let [bottom-coordfn (get (:coords plot) (:coord bottom-axis))]
      (when-let [left-coordfn (get (:coords plot) (:coord left-axis))]
        (gl-push-all gl
          (let [attrs (:attrs plot)]
            (prep-plot gl attrs)
            (drawfn gl (:data plot) bottom-coordfn left-coordfn attrs)))))))


(defn draw-plots [gl fignum]
  (when-let [fig (get-fig fignum)]
    (let [bottom-axis (get-axis fig :bottom :x)
          left-axis (get-axis fig :left :y)]
      (dorun
        (map
          (fn [[_ plot]] (draw-plot gl plot bottom-axis left-axis))
          (:plots fig))))))
