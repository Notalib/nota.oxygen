package dk.nota.oxygen.workers.epub;

import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import dk.nota.epub.EpubAccess;
import dk.nota.epub.content.Concatter;
import dk.nota.epub.conversion.DaisyComicConverter;
import dk.nota.oxygen.ResultsListener;
import dk.nota.xml.DocumentResult;
import dk.nota.xml.XmlAccessProvider;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

public class DaisyComicWorker extends AbstractEpubWorkerWithResults {
	
	private URI outputUri;

	public DaisyComicWorker(EpubAccess epubAccess, XdmNode opfDocument,
			ResultsListener resultsListener, URI outputUri) {
		super("DAISY COMIC CONVERSION", resultsListener, epubAccess,
				opfDocument);
		this.outputUri = outputUri;
	}

	@Override
	protected DocumentResult doInBackground() throws Exception {
		fireResultsUpdate("DAISY COMIC CONVERSION STARTING");
		Concatter concatter = new Concatter(opfDocument, true);
		concatter.addListener(getResultsListener());
		DocumentResult documentResult = new DocumentResult(concatter.call());
		copyImages(outputUri, concatter.getImages());
		DaisyComicConverter daisyComicConverter = new DaisyComicConverter(
				documentResult.getDocuments().iterator().next(), outputUri);
		documentResult = new DocumentResult(daisyComicConverter.call());
		Serializer serializer = XmlAccessProvider.getXmlAccess()
				.getSerializer();
		for (URI uri : documentResult.getUris()) {
			Path path = Paths.get(uri);
			if (path.toString().endsWith(".mdf"))
				serializer.setOutputProperty(Serializer.Property.METHOD,
						"text");
			else serializer.setOutputProperty(Serializer.Property.METHOD,
					"xml");
			try (OutputStream outputStream = Files.newOutputStream(path)) {
				serializer.setOutputStream(outputStream);
				serializer.serializeNode(documentResult.getDocument(uri));
			}
		}
		return documentResult;
	}

}
