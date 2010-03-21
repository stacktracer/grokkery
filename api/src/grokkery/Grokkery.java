package grokkery;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Grokkery
{
    
    /**
     * Exposes {@code object}, as {@code name}, to the Grokkery UI.
     * 
     * @throws RuntimeException if anything goes wrong
     */
    public static void expose(Object object, String name)
    {
        InstanceHolder.grokkery.internalExpose(object, name);
    }
    
    
    // IODH idiom. See http://blogs.metsci.com/hogye/?p=70
    private static class InstanceHolder
    {
        public static final Grokkery grokkery;
        
        static
        {
            try
            {
                grokkery = new Grokkery();
                grokkery.start();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    
    
    private static final String[] pluginProps = { "osgi.bundles", "org.eclipse.equinox.common@2:start,org.eclipse.update.configurator@3:start,reference:file:/home/mike/projects/grokkery/code/plugin@start",
                                                  "osgi.dev", "file:/home/mike/projects/grokkery/eclipse/.metadata/.plugins/org.eclipse.pde.core/grokkery.application/dev.properties",
                                                  "osgi.install.area", "file:/opt/eclipse",
                                                  "osgi.framework", "file:/opt/eclipse/plugins/org.eclipse.osgi_3.5.2.R35x_v20100126.jar",
                                                  "osgi.configuration.cascaded", "false",
                                                  "osgi.bundles.defaultStartLevel", "4",
                                                  "osgi.clean", "true",
                                                  "osgi.debug", "",
                                                  "osgi.noShutdown", "true",
                                                  "eclipse.consoleLog", "true",
                                                  "eclipse.application", "grokkery.application" };
    
    private static final String serviceName = "grokkery.GrokkeryService";
    
    
    private static class Exposure
    {
        public final Object object;
        public final String name;
        
        public Exposure(Object object, String name)
        {
            this.object = object;
            this.name = name;
        }
    }
    
    
    private Object service;
    private final Object serviceLock;
    private final List<Exposure> exposureBacklog;
    
    
    private Grokkery()
    {
        service = null;
        serviceLock = new Object();
        exposureBacklog = new LinkedList<Exposure>();
    }
    
    private void start() throws Exception
    {
        Thread pluginThread = new Thread("Grokkery Plugin")
        {
            public void run()
            {
                ServiceTracker serviceTracker = null;
                try
                {
                    EclipseStarter.setInitialProperties(props(pluginProps));
                    final BundleContext context = EclipseStarter.startup(new String[0], null);
                    
                    serviceTracker = new ServiceTracker(context, serviceName, new ServiceTrackerCustomizer()
                    {
                        public Object addingService(ServiceReference ref)
                        {
                            synchronized (serviceLock)
                            {
                                if (service != null) throw new RuntimeException();
                                
                                service = context.getService(ref);
                                if (service == null) throw new RuntimeException();
                                if (!serviceName.equals(service.getClass().getName())) throw new RuntimeException();
                                
                                for (Exposure x : exposureBacklog) doExpose(service, x.object, x.name);
                                return service;
                            }
                        }
                        
                        public void modifiedService(ServiceReference ref, Object service)
                        {
                            synchronized (serviceLock)
                            {
                                Grokkery.this.service = service;
                            }
                        }
                        
                        public void removedService(ServiceReference ref, Object service)
                        {
                            synchronized (serviceLock)
                            {
                                service = null;
                                context.ungetService(ref);
                            }
                        }
                    });
                    serviceTracker.open();
                    
                    EclipseStarter.run(null);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
                finally
                {
                    if (serviceTracker != null) serviceTracker.close();
                }
            }
        };
        pluginThread.start();
    }
    
    private void internalExpose(Object object, String name)
    {
        synchronized (serviceLock)
        {
            if (service == null)
            {
                System.err.println("Adding exposure of \"" + name + "\" to backlog");
                exposureBacklog.add(new Exposure(object, name));
            }
            else
            {
                doExpose(service, object, name);
            }
        }
    }
    
    private static void doExpose(Object service, Object object, String name)
    {
        try
        {
            System.err.println("Attempting to expose \"" + name + "\"");
            Method exposeMethod = service.getClass().getMethod("expose", Object.class, String.class);
            exposeMethod.invoke(service, object, name);
            System.err.println("Exposed \"" + name + "\"");
        }
        catch (Exception e)
        {
            System.err.println("Failed to expose \"" + name + "\": " + e);
            e.printStackTrace(System.err);
        }
    }
    
    private static Properties props(String... keysAndValues)
    {
        if (keysAndValues.length % 2 != 0) throw new IllegalArgumentException("Length of property keys-and-values array is odd: " + keysAndValues.length);
        
        Properties props = new Properties();
        for (int i = 0; i < keysAndValues.length; i += 2) props.put(keysAndValues[i], keysAndValues[i + 1]);
        return props;
    }
    
}
