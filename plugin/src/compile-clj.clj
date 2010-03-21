(binding [*compile-path* "/home/mike/projects/grokkery/code/plugin/build/classes/"]
  (dorun
  	(map compile
  		['grokkery.GrokkeryApp
  		 'grokkery.GrokkeryService
  		 'grokkery.GrokkeryPlugin
  		 'grokkery.GraphView
       'grokkery.ReplConsole])))
