package grokkery;

import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

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
    
}
