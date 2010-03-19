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
    
    private static final String exposureServiceName = "grokkery.ExposureService";
    
    
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
    
    
    private final Object exposureServiceLock;
    private final List<Exposure> exposureBacklog;
    private Object exposureService;
    
    
    private Grokkery()
    {
        exposureServiceLock = new Object();
        exposureBacklog = new LinkedList<Exposure>();
        exposureService = null;
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
                    
                    serviceTracker = new ServiceTracker(context, exposureServiceName, new ServiceTrackerCustomizer()
                    {
                        public Object addingService(ServiceReference ref)
                        {
                            synchronized (exposureServiceLock)
                            {
                                if (exposureService != null) throw new RuntimeException();
                                
                                exposureService = context.getService(ref);
                                if (exposureService == null) throw new RuntimeException();
                                if (!exposureServiceName.equals(exposureService.getClass().getName())) throw new RuntimeException();
                                
                                for (Exposure x : exposureBacklog) doExpose(exposureService, x.object, x.name);
                                return exposureService;
                            }
                        }
                        
                        public void modifiedService(ServiceReference ref, Object service)
                        {
                            synchronized (exposureServiceLock)
                            {
                                exposureService = service;
                            }
                        }
                        
                        public void removedService(ServiceReference ref, Object service)
                        {
                            synchronized (exposureServiceLock)
                            {
                                exposureService = null;
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
        synchronized (exposureServiceLock)
        {
            if (exposureService == null)
            {
                System.err.println("Adding exposure of \"" + name + "\" to backlog");
                exposureBacklog.add(new Exposure(object, name));
            }
            else
            {
                doExpose(exposureService, object, name);
            }
        }
    }
    
    private static void doExpose(Object exposureService, Object object, String name)
    {
        try
        {
            System.err.println("Attempting to expose \"" + name + "\"");
            Method exposeMethod = exposureService.getClass().getMethod("expose", Object.class, String.class);
            exposeMethod.invoke(exposureService, object, name);
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
