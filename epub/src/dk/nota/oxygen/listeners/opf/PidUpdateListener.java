package dk.nota.oxygen.listeners.opf;

import java.net.URI;
import java.util.Map;

import dk.nota.epub.EpubAccess;
import dk.nota.epub.EpubAccessProvider;
import dk.nota.epub.EpubException;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.EditorAccessProvider;
import ro.sync.exml.workspace.api.listeners.WSEditorListener;

public class PidUpdateListener extends WSEditorListener {
	
	@Override
	public void editorSaved(int saveType) {
		EditorAccess editorAccess = EditorAccessProvider.getEditorAccess();
		URI archiveUri = editorAccess.getArchiveUri();
		EpubAccess epubAccess;
		try {
			epubAccess = EpubAccessProvider.getEpubAccess(archiveUri);
		} catch (EpubException e) {
			editorAccess.showErrorMessage("Unable to get EPUB access", e);
			return;
		}
		Map<String,String> metadataMap;
		try {
			metadataMap = epubAccess.getContentAccess()
					.getDublinCoreMetadata();
		} catch (EpubException e) {
			editorAccess.showErrorMessage("Unable to get EPUB metadata", e);
			return;
		}
		epubAccess.setPid(metadataMap.get("identifier"));
	}

}
