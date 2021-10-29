package dk.nota.oxygen.operations.html;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.stream.StreamSource;

import dk.nota.archive.ArchiveAccess;
import dk.nota.epub.EpubAccess;
import dk.nota.epub.EpubAccessProvider;
import dk.nota.epub.EpubException;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.EditorAccessProvider;
import dk.nota.oxygen.operations.AbstractAuthorOperation;
import dk.nota.xml.XmlAccess;
import dk.nota.xml.XmlAccessProvider;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.MessageListener;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.Xslt30Transformer;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;

public class ImportImageMapsOperation extends XhtmlEpubAuthorOperation {
	
	private int depth;
	private EditorAccess editorAccess;
	private EpubAccess epubAccess;
	private File[] imageFiles;
	private URI originalUri;
	
	private String cleanInput(File[] imageMapFiles) throws IOException {
		// Use a writer to create the input
		StringWriter inputWriter = new StringWriter();
		// Add XML declarations and root element
		inputWriter.write(
				"<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		// Iterate over chosen files
		for (File imageMapFile : imageMapFiles) {
			try (BufferedReader reader = new BufferedReader(new FileReader(
					imageMapFile))) {
				// Skip XML declaration in individual files
				reader.skip(44);
				// Replace invalid nohref attributes, root element and doctype
				reader.lines().forEach(
						s -> inputWriter.write(s.replaceAll(
								"(nohref |</*html>|<!DOCTYPE .+?>)", "")));
			}
		}
		inputWriter.write("</html>"); // Close root element
		return inputWriter.toString();
	}
	
	private String createFragment(String input) throws IOException,
			SaxonApiException {
		XmlAccess xmlAccess = XmlAccessProvider.getXmlAccess();
		Xslt30Transformer imageMapImporter = xmlAccess.getXsltTransformer(
						"/dk/nota/xml/xslt/epub-import-image-maps.xsl");
		imageMapImporter.setMessageListener(new MessageListener() {
			private int count = 0;
			@Override
			public void message(XdmNode message, boolean terminate,
					SourceLocator sourceLocator) {
				message.axisIterator(Axis.DESCENDANT_OR_SELF, new QName(
						XmlAccess.NAMESPACE_NOTA, "image"))
						.forEachRemaining(
								item -> imageFiles[count++] = new File(URI
										.create(item.getStringValue())));
			}
		});
		HashMap<QName,XdmValue> parameters = new HashMap<QName,XdmValue>();
		parameters.put(new QName("INSERTION_DEPTH"), new XdmAtomicValue(depth));
		parameters.put(new QName("ORIGINAL_URI"), new XdmAtomicValue(
				originalUri));
		imageMapImporter.setStylesheetParameters(parameters);
		Serializer serializer = xmlAccess.getXhtmlSerializer();
		XdmDestination xdmDestination = new XdmDestination();
		imageMapImporter.applyTemplates(new StreamSource(new StringReader(
				input)), xdmDestination);
		return serializer.serializeNodeToString(xdmDestination.getXdmNode());
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			editorAccess = EditorAccessProvider.getEditorAccess();
			epubAccess = EpubAccessProvider.getEpubAccess(URI.create(
					editorAccess.getArchiveUrlComponent()));
			File[] imageMapFiles = getWorkspace().chooseFiles(null, "Insert",
					new String[] { "html" }, "Image maps");
			if (imageMapFiles == null) return;
			imageFiles = new File[imageMapFiles.length];
			originalUri = imageMapFiles[0].toURI();
			String fragment = createFragment(cleanInput(imageMapFiles));
			insertImages();
			getDocumentController().insertXMLFragment(fragment,
					getSelectionStart());
		} catch (IOException | SaxonApiException | EpubException e) {
			throw new AuthorOperationException(
					"An error occurred during author operation", e);
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor("depth", ArgumentDescriptor
						.TYPE_STRING, "Depth at the insertion point")
		};
	}

	@Override
	public String getDescription() {
		return "Merges image maps into document at current position";
	}
	
	private void insertImages() throws IOException, SaxonApiException, EpubException {
		HashMap<String,String> fileTypes = new HashMap<String,String>();
		ArchiveAccess archiveAccess = epubAccess.getArchiveAccess();
		for (URI uri : archiveAccess.copyFilesToArchiveFolder("EPUB/images",
				true, imageFiles))
			fileTypes.put(epubAccess.relativizeUriToOpf(uri), "image/jpeg");
		epubAccess.getContentAccess().updateOpfWithImages(fileTypes);
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException {
		depth = Integer.parseInt((String)arguments.getArgumentValue("depth"));
	}

}
