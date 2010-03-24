(set! *warn-on-reflection* true)
(binding [*compile-path* "/home/mike/projects/grokkery/code/plugin/build/classes/"]
  (dorun
    (map compile
      ['grokkery.GrokkeryService
       'grokkery.GrokkeryPlugin
       'grokkery.ReplConsole
       'grokkery.FigureView
       'grokkery.Perspective
       'grokkery.GrokkeryApp
       'grokkery])))
