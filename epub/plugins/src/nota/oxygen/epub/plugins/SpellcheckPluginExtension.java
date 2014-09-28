package nota.oxygen.epub.plugins;

import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

public class SpellcheckPluginExtension implements WorkspaceAccessPluginExtension {

	public SpellcheckPluginExtension() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean applicationClosing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void applicationStarted(StandalonePluginWorkspace workspace) {
		workspace.addMenuBarCustomizer(new SpellcheckMenuBarCustomizer(workspace));
	}

}
