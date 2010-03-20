package grokkery;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

public class Perspective implements IPerspectiveFactory
{
    
    public static String id()
    {
        return "grokkery.perspective";
    }
    
    @Override
    public void createInitialLayout(IPageLayout layout)
    {
        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(false);
        
        layout.addView(IConsoleConstants.ID_CONSOLE_VIEW, IPageLayout.BOTTOM, 0, editorArea);
        layout.getViewLayout(IConsoleConstants.ID_CONSOLE_VIEW).setCloseable(false);
    }
    
}
