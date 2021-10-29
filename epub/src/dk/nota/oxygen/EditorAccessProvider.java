package dk.nota.oxygen;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

public class EditorAccessProvider {
	
	public static EditorAccess getEditorAccess() {
		return new EditorAccess(PluginWorkspaceProvider.getPluginWorkspace());
	}

}
