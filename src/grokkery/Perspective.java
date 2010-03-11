package grokkery;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);
		
        System.err.println("Perpsective.createInitialLayout(): thread = " + Thread.currentThread().getContextClassLoader() + ", class = " + Activator.class.getClassLoader());

		
		layout.addStandaloneView("grokkery.view",  false, IPageLayout.LEFT, 1.0f, editorArea);
	}

}
