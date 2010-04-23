(binding [*compile-path* "/home/mike/projects/grokkery/code/plugin/build/classes/"
          *warn-on-reflection* true]
  (dorun
    (map compile
      ['grokkery.util
       'grokkery.core
       'grokkery.rcp.saxis-canvas
       'grokkery.rcp.waxis-canvas
       'grokkery.rcp.content-canvas
       'grokkery.rcp.GrokkeryService
       'grokkery.rcp.GrokkeryPlugin
       'grokkery.rcp.ReplConsole
       'grokkery.rcp.FigureView
       'grokkery.rcp.Perspective
       'grokkery.rcp.GrokkeryApp
       'grokkery.colors
       'grokkery.plot
       'grokkery.color2d
       'grokkery
       'user])))
