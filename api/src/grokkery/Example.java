package grokkery;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class Example
{
    
    public static void main(String[] args) throws Exception
    {
        String bundles = bundleList("com.ibm.icu",
                                    "javax.servlet",
                                    "org.eclipse.compare.core",
                                    "org.eclipse.core.commands",
                                    "org.eclipse.core.contenttype",
                                    "org.eclipse.core.databinding",
                                    "org.eclipse.core.databinding.observable",
                                    "org.eclipse.core.databinding.property",
                                    "org.eclipse.core.expressions",
                                    "org.eclipse.core.jobs",
                                    "org.eclipse.core.runtime@start",
                                    "org.eclipse.core.runtime.compatibility.auth",
                                    "org.eclipse.core.runtime.compatibility.registry",
                                    "org.eclipse.core.variables",
                                    "org.eclipse.equinox.app",
                                    "org.eclipse.equinox.common@2:start",
                                    "org.eclipse.equinox.preferences",
                                    "org.eclipse.equinox.registry",
                                    "org.eclipse.help",
                                    "org.eclipse.jface",
                                    "org.eclipse.jface.databinding",
                                    "org.eclipse.jface.text",
                                    "org.eclipse.osgi@-1:start",
                                    "org.eclipse.osgi.services",
                                    "org.eclipse.swt",
                                    "org.eclipse.text",
                                    "org.eclipse.ui",
                                    "org.eclipse.ui.console",
                                    "org.eclipse.ui.workbench",
                                    "org.eclipse.ui.workbench.texteditor",
                                    "org.eclipse.swt.gtk.linux.x86_64",
                                    "reference:file:/home/mike/projects/grokkery/code/plugin@start");
        
        Properties props = props("osgi.dev", "file:/home/mike/projects/grokkery/eclipse/.metadata/.plugins/org.eclipse.pde.core/grokkery.application/dev.properties",
                                 "osgi.install.area", "file:/opt/eclipse",
                                 "osgi.framework", "file:/opt/eclipse/plugins/org.eclipse.osgi_3.5.2.R35x_v20100126.jar",
                                 "osgi.configuration.cascaded", "false",
                                 "osgi.bundles", bundles,
                                 "osgi.bundles.defaultStartLevel", "4",
                                 "osgi.clean", "true",
                                 "osgi.debug", "",
                                 "osgi.noShutdown", "true",
                                 "eclipse.consoleLog", "true",
                                 "eclipse.application", "grokkery.application");
        
        EclipseStarter.setInitialProperties(props);
        BundleContext context = EclipseStarter.startup(new String[0], null);
        
        Bundle grokkeryBundle = findBundle(context, "grokkery");
        Class<?> activatorClass = grokkeryBundle.loadClass("grokkery.Activator");
        for (Method m : activatorClass.getMethods()) System.err.println(m.getName());
        
        EclipseStarter.run(null);
    }
    
    private static Bundle findBundle(BundleContext context, String symbolicName)
    {
        for (Bundle b : context.getBundles()) if (symbolicName.equals(b.getSymbolicName())) return b;
        throw new NoSuchElementException(symbolicName);
    }
    
    private static String bundleList(String... bundles)
    {
        if (bundles.length == 0) return "";
        
        StringBuilder list = new StringBuilder();
        for (String s : bundles) list.append(",").append(s);
        return list.substring(1);
    }
    
    private static Properties props(String... keysAndValues)
    {
        if (keysAndValues.length % 2 != 0) throw new IllegalArgumentException("Length of property keys-and-values array is odd: " + keysAndValues.length);
        
        Properties props = new Properties();
        for (int i = 0; i < keysAndValues.length; i += 2) props.put(keysAndValues[i], keysAndValues[i + 1]);
        return props;
    }
    
}
