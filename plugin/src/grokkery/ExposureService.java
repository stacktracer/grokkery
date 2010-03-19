package grokkery;

import clojure.lang.Namespace;
import clojure.lang.Symbol;
import clojure.lang.Var;

public class ExposureService
{
    
    public static final Symbol namespaceName = Symbol.create("user");
    
    
    public void expose(Object object, String name)
    {
        System.err.println("Exposing \"" + name + "\": " + object);
        
        Namespace namespace = Namespace.findOrCreate(namespaceName);
        Var.intern(namespace, Symbol.create(name), object);
    }
    
}
