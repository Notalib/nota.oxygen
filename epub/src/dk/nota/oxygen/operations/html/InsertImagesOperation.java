package dk.nota.oxygen.operations.html;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import dk.nota.epub.EpubAccess;
import dk.nota.epub.EpubAccessProvider;
import dk.nota.epub.EpubException;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.EditorAccessProvider;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import uk.co.jaimon.test.SimpleImageInfo;

public class InsertImagesOperation extends XhtmlEpubAuthorOperation {
	
	private String createFragment(File[] imageFiles) throws IOException {
		String fragment = "";
		String imgBase = "<img xmlns='http://www.w3.org/1999/xhtml' "
				+ "lang='da' xml:lang='da' alt='Illustration' "
				+ "src='%s' height='%s' width='%s'/>";
		String figureBase = "<figure xmlns='http://www.w3.org/1999/xhtml' "
				+ "class='image'>%s</figure>";
		String seriesBase = "<figure xmlns='http://www.w3.org/1999/xhtml' "
				+ "class='image-series'>%s</figure>";
		for (File imageFile : imageFiles) {
			SimpleImageInfo info = new SimpleImageInfo(imageFile);
			String imgFragment = String.format(imgBase, "images/" + imageFile
					.getName(), info.getHeight(), info.getWidth());
			String figureFragment = String.format(figureBase, imgFragment);
			fragment += figureFragment;
		}
		if (imageFiles.length > 1) return String.format(seriesBase, fragment);
		return fragment;
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		EditorAccess editorAccess = EditorAccessProvider.getEditorAccess();
		File[] imageFiles = getWorkspace().chooseFiles(null,
				"Insert", new String[] { "gif", "jpg", "jpeg", "png" },
				"Image files");
		if (imageFiles == null) return;
		HashMap<String,String> fileTypes = new HashMap<String,String>();
		try {
			EpubAccess epubAccess = EpubAccessProvider.getEpubAccess(editorAccess
					.getArchiveUri());
			for (File file : imageFiles) {
				fileTypes.put("images/" + file.getName(), Files
						.probeContentType(file.toPath()));
				epubAccess.getArchiveAccess().copyFileToArchiveFolder(
						"EPUB/images", true, file);
			}
			epubAccess.getContentAccess().updateOpfWithImages(fileTypes);
			String fragment = createFragment(imageFiles);
			getDocumentController().insertXMLFragment(fragment,
					getSelectionStart());
		} catch (IOException | EpubException e) {
			throw new AuthorOperationException(e.toString());
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null; // No arguments to return
	}

	@Override
	public String getDescription() {
		return "Inserts images along with the required figure elements";
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException {
		// No arguments to parse
	}
	
	
	
}
