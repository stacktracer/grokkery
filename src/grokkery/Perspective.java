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
        
        layout.addView(GraphView.id(), IPageLayout.LEFT, 1.0f, editorArea);
        
        layout.addView(IConsoleConstants.ID_CONSOLE_VIEW, IPageLayout.BOTTOM, 0.7f, GraphView.id());
        layout.getViewLayout(IConsoleConstants.ID_CONSOLE_VIEW).setCloseable(false);
    }
    
}
