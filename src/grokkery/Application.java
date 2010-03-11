package grokkery;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class Application implements IApplication
{
    
    @Override
    public Object start(IApplicationContext context)
    {
        Display display = PlatformUI.createDisplay();
        try
        {
            int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
            return (returnCode == PlatformUI.RETURN_RESTART ? IApplication.EXIT_RESTART : IApplication.EXIT_OK);
        }
        finally
        {
            display.dispose();
        }
    }
    
    @Override
    public void stop()
    {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) return;
        
        final Display display = workbench.getDisplay();
        display.syncExec(new Runnable()
        {
            public void run()
            {
                if (!display.isDisposed()) workbench.close();
            }
        });
    }
    
}
