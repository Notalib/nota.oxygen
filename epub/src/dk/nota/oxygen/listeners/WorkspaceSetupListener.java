package dk.nota.oxygen.listeners;

import java.net.URL;

import dk.nota.epub.EpubAccessProvider;
import dk.nota.epub.EpubException;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.listeners.opf.PidUpdateListener;
import dk.nota.oxygen.plugin.NotaPluginExtension;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

public class WorkspaceSetupListener extends WSEditorChangeListener {
	
	private StandalonePluginWorkspace pluginWorkspace;
	
	public WorkspaceSetupListener(StandalonePluginWorkspace pluginWorkspace) {
		this.pluginWorkspace = pluginWorkspace;
	}
	
	@Override
	public void editorActivated(URL editorUrl) {
		establishWorkspace(editorUrl, false);
	}
	
	@Override
	public void editorClosed(URL editorUrl) {
		hideAllPluginToolbars();
	}
	
	@Override
	public void editorOpened(URL editorUrl) {
		establishWorkspace(editorUrl, true);
	}
	
	@Override
	public void editorRelocated(URL previousEditorUrl, URL newEditorUrl) {
		establishWorkspace(newEditorUrl, false);
	}
	
	@Override
	public void editorSelected(URL editorUrl) {
		establishWorkspace(editorUrl, false);
	}
	
	private void establishWorkspace(URL editorUrl, boolean newEditor) {
		WSEditor editor = pluginWorkspace.getEditorAccess(editorUrl,
				StandalonePluginWorkspace.MAIN_EDITING_AREA);
		hideAllPluginToolbars();
		if (editor == null || editor.getDocumentTypeInformation() == null)
			return;
		switch (editor.getDocumentTypeInformation().getName()) {
		case "dtbook110":
			pluginWorkspace.showToolbar(NotaPluginExtension.DTB_TOOLBAR);
			return;
		case "XHTML [EPUB 3]":
			if (!setupEpubAccess(editorUrl)) return;
			if (editorUrl.toString().endsWith("/nav\\.xhtml"))
				pluginWorkspace.showToolbar(NotaPluginExtension
						.NAV_TOOLBAR);
			else pluginWorkspace.showToolbar(NotaPluginExtension
					.XHTML_TOOLBAR);
			break;
		case "OPF":
			if (!setupEpubAccess(editorUrl)) return;
			if (newEditor) editor.addEditorListener(
					new PidUpdateListener());
			pluginWorkspace.showToolbar(NotaPluginExtension.OPF_TOOLBAR);
			break;
		case "NCX":
			if (!setupEpubAccess(editorUrl)) return;
			pluginWorkspace.showToolbar(NotaPluginExtension.NAV_TOOLBAR);
			break;
		default:
			return;
		}
	}
	
	private void hideAllPluginToolbars() {
		pluginWorkspace.hideToolbar(NotaPluginExtension.DTB_TOOLBAR);
		pluginWorkspace.hideToolbar(NotaPluginExtension.NAV_TOOLBAR);
		pluginWorkspace.hideToolbar(NotaPluginExtension.OPF_TOOLBAR);
		pluginWorkspace.hideToolbar(NotaPluginExtension.XHTML_TOOLBAR);
	}
	
	private boolean setupEpubAccess(URL editorUrl) {
		try {
			EpubAccessProvider.getEpubAccess(EditorAccess.getArchiveUri(
					editorUrl));
		} catch (EpubException e) {
			return false;
		}
		return true;
	}
		
}