(binding [*compile-path* "/home/mike/projects/grokkery/code/plugin/build/classes/"]
  (dorun
    (map compile
      ['grokkery.GrokkeryService
       'grokkery.GrokkeryPlugin
       'grokkery.ReplConsole
       'grokkery.Figure
       'grokkery.Perspective
       'grokkery.GrokkeryApp])))
