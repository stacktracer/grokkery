(ns grokkery.View
	(:import
		[org.eclipse.swt SWT]
		[javax.media.opengl GLContext]
		[glsimple GLSimpleListener]
		[glsimple.swt GLSimpleSwtCanvas GLSimpleSwtAnimator])
  (:gen-class
		:extends org.eclipse.ui.part.ViewPart
	  :state state
	  :init init-instance
	  :methods [#^{:static true} [id [] String]]))


(defn	-id []
	"grokkery.view")


(defn -init-instance []
 	[[] (ref {:content-area nil, :x-axis nil, :y-axis nil})])


(defn get-state [this k]
	(k @(.state this)))


(defn draw-x-axis [gl bounds]
	; IMPLEMENT ME
	)


(defn draw-y-axis [gl bounds]
	; IMPLEMENT ME
	)


(defn draw-content-area [gl bounds]
	; IMPLEMENT ME
	)


(defn create-canvas [parent draw]
	(let [bounds (ref {:x 0, :y 0, :width 0, :height 0})
				canvas (GLSimpleSwtCanvas. parent
	  															 (into-array GLSimpleListener
		  														   [(proxy [GLSimpleListener] []
		  														   		
		  								 							 		(init [context])
		  								 							 		
	  									 									(display [context]
	  									 											(draw (.getGL context) @bounds))
	  									 									
	  									 									(reshape [context x y width height]
	  									 										(dosync (ref-set bounds {:x x, :y y, :width width, :height height})))
	  									 									
	  									 									(displayChanged [context modeChanged deviceChanged]))]))]
	  									 									
		(.start (GLSimpleSwtAnimator. 60 canvas))
		canvas))


(defn -createPartControl [this parent]
	; Use multiple assocs in same dosync
	(dosync
		(ref-set (.state this) {:x-axis (create-canvas parent draw-x-axis),
	 													:y-axis (create-canvas parent draw-y-axis),
	 													:content-area (create-canvas parent draw-content-area)})))


(defn -setFocus [this]
	(.setFocus (get-state this :content-area)))



