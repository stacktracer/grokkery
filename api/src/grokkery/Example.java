package grokkery;

public class Example
{
    
    public static class ExampleObject
    {
        public final String name;
        
        public ExampleObject(String name)
        {
            this.name = name;
        }
    }
    
    
    public static void main(String[] args) throws Exception
    {
        ExampleObject exampleObject = new ExampleObject("Hello Grokkery, from Application Land!");
        
        Grokkery.setClientName(Example.class.getSimpleName());
        Grokkery.expose(exampleObject, "exampleObject");
    }
    
}
