(set! *warn-on-reflection* true)
(binding [*compile-path* "/home/mike/projects/grokkery/code/plugin/build/classes/"]
  (dorun
    (map compile
      ['grokkery.util
       'grokkery.rcp.GrokkeryService
       'grokkery.rcp.GrokkeryPlugin
       'grokkery.rcp.ReplConsole
       'grokkery.rcp.FigureView
       'grokkery.rcp.Perspective
       'grokkery.rcp.GrokkeryApp
       'grokkery.core
       'grokkery
       'user])))
