package dk.nota.oxygen.actions.epub;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import dk.nota.archive.ArchiveAccess;
import dk.nota.epub.EpubException;
import dk.nota.oxygen.EditorAccess;

public class ReloadDocumentsAction extends EpubAction {

	public ReloadDocumentsAction() {
		super("Reload", true);
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess,
			LinkedList<URL> affectedEditorUrls) {
		ArchiveAccess archiveAccess = epubAccess.getArchiveAccess();
		LinkedList<Path> documentPaths;
		try {
			documentPaths = archiveAccess.getDirectoryContents("EPUB");
		} catch (IOException e) {
			editorAccess.showErrorMessage(
					"Unable to read contents of EPUB folder", e);
			return;
		}
		LinkedHashMap<String,String> updateMap =
				new LinkedHashMap<String,String>();
		documentPaths.forEach(
				path -> {
					String fileName = path.getFileName().toString();
					if (fileName.endsWith(".xhtml") &&
							!fileName.equals("nav.xhtml"))
						updateMap.put(path.getFileName().toString(),
								"application/xhtml+xml");
				});
		try {
			epubAccess.getContentAccess().updateOpf(updateMap,
					new LinkedList<String>(), "reloaded", true);
		} catch (EpubException e) {
			editorAccess.showErrorMessage("Unable to update OPF", e);
		}
		affectedEditorUrls.forEach(url -> editorAccess.open(url));
	}

}
