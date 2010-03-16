package grokkery;

import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor
{
    
    @Override
    public void initialize(IWorkbenchConfigurer configurer)
    {
//        configurer.setSaveAndRestore(true);
    }
    
    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer)
    {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }
    
    @Override
    public String getInitialWindowPerspectiveId()
    {
        return Perspective.id();
    }
    
    @Override
    public void postStartup()
    {
        IConsole console = new ReplConsole();
        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        consoleManager.addConsoles(new IConsole[] { console });
        consoleManager.showConsoleView(console);
    }
    
}
