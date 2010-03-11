(ns grokkery.View

	(:import
		[org.eclipse.swt SWT]
		[org.eclipse.jface.viewers TableViewer])

  (:gen-class
		:extends org.eclipse.ui.part.ViewPart
	  :state state
	  :init init2
	  :methods [#^{:static true} [id [] String]]))

	(defn -init2 []
  	[[] (ref {:viewer nil})])

	(defn -getViewer [this]
		(:viewer @(.state this)))

	(defn -setViewer [this viewer]
		(dosync (alter (.state this) assoc :viewer viewer))
		viewer)


	(defn	-id []
		"grokkery.view")

	(defn -createPartControl [this parent]
		(-setViewer this
			(TableViewer. parent (reduce bit-or [SWT/MULTI SWT/H_SCROLL SWT/V_SCROLL]))))

	(defn -setFocus [this]
		(.. (-getViewer this) (getControl) (setFocus)))
