package dk.nota.oxygen.workers.epub;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;

import dk.nota.archive.ArchiveAccess;
import dk.nota.epub.EpubAccess;
import dk.nota.oxygen.ResultsListener;
import dk.nota.oxygen.workers.AbstractWorkerWithResults;
import dk.nota.xml.DocumentResult;
import net.sf.saxon.s9api.XdmNode;

public abstract class AbstractEpubWorkerWithResults
		extends AbstractWorkerWithResults<DocumentResult,Object> {
	
	protected EpubAccess epubAccess;
	protected XdmNode opfDocument;
	
	protected AbstractEpubWorkerWithResults(String title,
			ResultsListener listener, EpubAccess epubAccess,
			XdmNode opfDocument) {
		super(title, listener);
		this.epubAccess = epubAccess;
		this.opfDocument = opfDocument;
	}
	
	protected void copyImages(URI outputFolderUri, LinkedList<URI> imageUris)
			throws IOException {
		Path outputFolderPath = Paths.get(outputFolderUri);
		Files.createDirectories(outputFolderPath);
		ArchiveAccess archiveAccess = epubAccess.getArchiveAccess();
		try (FileSystem epubFileSystem = archiveAccess
				.getArchiveAsFileSystem()) {
			for (URI imageUri : imageUris) {
				Path imagePath = epubFileSystem.getPath(archiveAccess
						.relativizeUriToArchive(imageUri));
				Path newImagePath = outputFolderPath.resolve(imagePath
						.getFileName().toString());
				Files.copy(imagePath, newImagePath, StandardCopyOption
						.REPLACE_EXISTING);
			}
		}
	}

}
