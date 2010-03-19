(binding [*compile-path* "/home/mike/projects/grokkery/code/plugin/build/classes/"]
  (dorun
  	(map compile
  		['grokkery.GraphView
  		 'grokkery.ReplConsole])))
