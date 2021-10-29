package dk.nota.oxygen;

import java.net.URI;
import java.net.URL;
import java.util.LinkedList;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

/**
 * A class that wraps various editor-related functions, mainly from the  
 * {@link ro.sync.exml.workspace.api.PluginWorkspace} class.
 */

public class EditorAccess {
	
	private PluginWorkspace pluginWorkspace = PluginWorkspaceProvider
			.getPluginWorkspace();
	
	public EditorAccess(PluginWorkspace pluginWorkspace) {
		this.pluginWorkspace = pluginWorkspace;
	}
	
	public void close(URL editorUrl) {
		pluginWorkspace.close(editorUrl);
	}
	
	public URL[] getAllEditorUrls() {
		return pluginWorkspace.getAllEditorLocations(StandalonePluginWorkspace
				.MAIN_EDITING_AREA);
	}
	
	public LinkedList<URL> getArchiveEditorUrls(String archiveComponent) {
		LinkedList<URL> urls = new LinkedList<URL>();
		for (URL editorUrl : getAllEditorUrls()) {
			if (editorUrl.toString().contains(archiveComponent))
				urls.add(editorUrl);
		}
		return urls;
	}
	
	public URI getArchiveUri() {
		return URI.create(getArchiveUrlComponent());
	}
	
	public static URI getArchiveUri(URL editorUrl) {
		return URI.create(getArchiveUrlComponent(editorUrl));
	}
	
	public String getArchiveUrlComponent() {
		return getArchiveUrlComponent(getCurrentEditorUrl());
	}
	
	public static String getArchiveUrlComponent(URL editorUrl) {
		return editorUrl.toString().replaceFirst("^zip:", "").split("!/")[0];
	}
	
	public URL getCurrentEditorUrl() {
		return pluginWorkspace.getCurrentEditorAccess(StandalonePluginWorkspace
				.MAIN_EDITING_AREA).getEditorLocation();
	}
	
	public WSEditor getEditor(URL url) {
		return getEditor(url, false);
	}
	
	public WSEditor getEditor(URL url, boolean create) {
		WSEditor editor = pluginWorkspace.getEditorAccess(url,
				StandalonePluginWorkspace.MAIN_EDITING_AREA);
		if (editor == null && create) {
			if (!open(url)) return null;
			editor = pluginWorkspace.getEditorAccess(url,
					StandalonePluginWorkspace.MAIN_EDITING_AREA);
		}
		return editor;
	}
	
	public PluginWorkspace getPluginWorkspace() {
		return pluginWorkspace;
	}
	
	public boolean open(URL url) {
		return pluginWorkspace.open(url);
	}
	
	public void refresh(URL editorUrl) {
		pluginWorkspace.refreshInProject(editorUrl);
	}
	
	public void showErrorMessage(String message) {
		pluginWorkspace.showErrorMessage(message);
	}
	
	public void showErrorMessage(String message, Throwable throwable) {
		pluginWorkspace.showErrorMessage(message, throwable);
	}
	
	public void showInformationMessage(String message) {
		pluginWorkspace.showInformationMessage(message);
	}
	
	public void showStatusMessage(String message) {
		pluginWorkspace.showStatusMessage(message);
	}

}
