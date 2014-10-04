package nota.oxygen.epub.plugins;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

public class SpellcheckAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4108894724934340545L;
	
	private StandalonePluginWorkspace workspace;

	
	public SpellcheckAction(StandalonePluginWorkspace workspace)
	{
		super("ePub Spellcheck...");
		putValue(SHORT_DESCRIPTION, "Spellcheck xhtml files in ePub 3.0 document");
		this.workspace = workspace;
	}
	
	@Override
	public boolean isEnabled() 
	{
		WSEditor editor = workspace.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
		if (editor != null)
		{
			if (editor.getCurrentPageID() == EditorPageConstants.PAGE_AUTHOR) 
			{
				URL loc = editor.getEditorLocation();
				if (loc.getProtocol() == "zip") return true;
			}
		}
		return false;
	};

	@Override
	public void actionPerformed(ActionEvent e) {
		WSEditor editor = workspace.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
		if (editor != null)
		{
			String message = String.format("Current document location: %s", editor.getEditorLocation());
			JOptionPane.showMessageDialog(null, message);
		}		
	}
}
