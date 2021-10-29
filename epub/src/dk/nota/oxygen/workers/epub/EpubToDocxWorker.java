package dk.nota.oxygen.workers.epub;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dk.nota.dtb.conversion.DtbToDocxConverter;
import dk.nota.epub.EpubAccess;
import dk.nota.epub.content.Concatter;
import dk.nota.epub.conversion.EpubToDtbConverter;
import dk.nota.oxygen.ResultsListener;
import dk.nota.xml.DocumentResult;
import dk.nota.xml.XmlAccessProvider;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import ro.sync.document.DocumentPositionedInfo;

public class EpubToDocxWorker extends AbstractEpubWorkerWithResults {
	
	private URI outputUri;
	private URI tempOutputUri;

	public EpubToDocxWorker(EpubAccess epubAccess, XdmNode opfDocument,
			ResultsListener listener, File outputFile) {
		super("EPUB-TO-DOCX CONVERSION", listener, epubAccess, opfDocument);
		outputUri = outputFile.toURI();
		tempOutputUri = outputUri.resolve(outputFile.getName() + ".tmp/");
	}

	@Override
	protected DocumentResult doInBackground() throws Exception {
		fireResultsUpdate("EPUB-TO-DTBOOK CONVERSION STARTING");
		Concatter concatter = new Concatter(opfDocument, false);
		concatter.addListener(getResultsListener());
		DocumentResult documentResult = new DocumentResult(concatter.call());
		XdmNode concatDocument = documentResult.getDocuments().iterator()
				.next();
		EpubToDtbConverter dtbConverter = new EpubToDtbConverter(concatDocument,
				opfDocument, outputUri);
		dtbConverter.addListener(getResultsListener());
		documentResult = new DocumentResult(dtbConverter.call());
		copyImages(tempOutputUri.resolve("word/media/"), concatter.getImages());
		XdmNode dtbDocument = documentResult.getDocuments().iterator().next();
		// The Docx-related stylesheets require the input to be a document, so
		// we have to jump through some hoops here
		DocumentBuilder documentBuilder = XmlAccessProvider.getXmlAccess()
				.getDocumentBuilder();
		DtbToDocxConverter docxConverter = new DtbToDocxConverter(
				documentBuilder.build(dtbDocument.asSource()));
		docxConverter.addListener(getResultsListener());
		docxConverter.addParameter("OUTPUT_URI", new XdmAtomicValue(
				tempOutputUri));
		documentResult = new DocumentResult(docxConverter.call());
		documentResult.writeDocuments(XmlAccessProvider.getXmlAccess()
				.getSerializer());
		Path tempDocxFolderPath = Paths.get(tempOutputUri);
		Path docxFilePath = Paths.get(outputUri);
		fireResultsUpdate("ZIPPING FILES...");
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(
				Files.newOutputStream(docxFilePath))) {
					Files.walk(tempDocxFolderPath)
						.filter(path -> !Files.isDirectory(path))
						.forEach(path -> {
							ZipEntry zipEntry = new ZipEntry(tempDocxFolderPath
									.relativize(path).toString());
							try {
								zipOutputStream.putNextEntry(zipEntry);
								zipOutputStream.write(Files.readAllBytes(path));
								zipOutputStream.closeEntry();
								Files.delete(path);
							} catch (Exception e) {
								getResultsListener().writeException(e,
										DocumentPositionedInfo.SEVERITY_FATAL);
							}
				});
		}
		fireResultsUpdate("DELETING TEMPORARY FILES...");
		Files.walkFileTree(tempDocxFolderPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult postVisitDirectory(Path folder,
					IOException exception) throws IOException {
				Files.delete(folder);
				return FileVisitResult.CONTINUE;
			}
		});
		return documentResult;
	}
	
	

}
