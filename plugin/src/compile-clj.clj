(binding [*compile-path* "/home/mike/projects/grokkery/code/build/classes/"]
  (dorun
  	(map compile
  		['grokkery.GraphView
  		 'grokkery.ReplConsole])))
