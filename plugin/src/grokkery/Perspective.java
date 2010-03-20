package grokkery;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;
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
        
        IPlaceholderFolderLayout north = layout.createPlaceholderFolder("north", IPageLayout.TOP, 0.62f, editorArea);
        north.addPlaceholder("grokkery.GraphView:*");
        
        layout.addView(IConsoleConstants.ID_CONSOLE_VIEW, IPageLayout.BOTTOM, 0.62f, editorArea);
        layout.getViewLayout(IConsoleConstants.ID_CONSOLE_VIEW).setCloseable(false);
    }
    
}
