package dk.nota.oxygen.actions.epub;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;

import javax.swing.AbstractAction;

import dk.nota.epub.EpubAccess;
import dk.nota.epub.EpubAccessProvider;
import dk.nota.epub.EpubException;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.EditorAccessProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;

public abstract class EpubAction extends AbstractAction {
	
	private boolean backupArchive = false;
	private boolean closeEditors;
	protected EpubAccess epubAccess;
	
	public EpubAction(String name, boolean closeEditors) {
		super(name);
		this.closeEditors = closeEditors;
	}
	
	public EpubAction(String name, boolean closeEditors,
			boolean backupArchive) {
		super(name);
		this.backupArchive = backupArchive;
		this.closeEditors = closeEditors;
	}
	
	public abstract void actionPerformed(EditorAccess editorAccess,
			LinkedList<URL> affectedEditorUrls);
	
	@Override
	public void actionPerformed(ActionEvent event) {
		EditorAccess editorAccess = EditorAccessProvider.getEditorAccess();
		URL editorUrl = editorAccess.getCurrentEditorUrl();
		LinkedList<URL> affectedEditorUrls = new LinkedList<URL>();
		for (URL url : editorAccess.getArchiveEditorUrls(EditorAccess
				.getArchiveUrlComponent(editorUrl))) {
			WSEditor editor = editorAccess.getEditor(url);
			if (editor.isModified()) {
				editorAccess.showErrorMessage("Unsaved changes in archive: "
						+ "please review and save before trying again");
				return;
			}
			affectedEditorUrls.add(url);
		}
		if (closeEditors)
			affectedEditorUrls.forEach(url -> editorAccess.close(url));
		try {
			epubAccess = EpubAccessProvider.getEpubAccess(URI.create(
					EditorAccess.getArchiveUrlComponent(editorUrl)));
			if (backupArchive) epubAccess.backupArchive();
		} catch (EpubException e) {
			editorAccess.showErrorMessage("Unable to get EPUB access", e);
			return;
		} catch (IOException e) {
			editorAccess.showErrorMessage("Unable to back up EPUB archive", e);
			return;
		}
		actionPerformed(editorAccess, affectedEditorUrls);
	}

}
