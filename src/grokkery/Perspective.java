package grokkery;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

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
        layout.addView(CommandView.id(), IPageLayout.BOTTOM, 0.7f, GraphView.id());
    }
    
}
