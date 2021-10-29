package dk.nota.oxygen.workers.epub;

import java.net.MalformedURLException;
import java.net.URI;

import dk.nota.epub.EpubAccess;
import dk.nota.epub.content.Concatter;
import dk.nota.epub.conversion.EpubToDtbConverter;
import dk.nota.xml.XmlAccessProvider;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.EditorAccessProvider;
import dk.nota.oxygen.ResultsListener;
import dk.nota.xml.DocumentResult;
import net.sf.saxon.s9api.XdmNode;

public class EpubToDtbWorker extends AbstractEpubWorkerWithResults {
	
	private URI outputUri;
	
	public EpubToDtbWorker(EpubAccess epubAccess, XdmNode opfDocument,
			ResultsListener listener, URI outputUri) {
		super("EPUB-TO-DTBOOK CONVERSION", listener, epubAccess, opfDocument);
		this.outputUri = outputUri;
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
		copyImages(outputUri.resolve("./"), concatter.getImages());
		documentResult.writeDocuments(XmlAccessProvider.getXmlAccess()
				.getDtbSerializer());
		return documentResult;
	}
	
	@Override
	protected void done() {
		super.done();
		EditorAccess editorAccess = EditorAccessProvider.getEditorAccess();
		try {
			editorAccess.open(outputUri.toURL());
		} catch (MalformedURLException e) {
			editorAccess.showErrorMessage("Unable to convert URI to URL", e);
		}
	}

}
