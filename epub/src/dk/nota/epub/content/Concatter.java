package dk.nota.epub.content;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import javax.xml.transform.SourceLocator;

import dk.nota.xml.XmlAccessProvider;
import dk.nota.xml.AbstractTransformationListener;
import dk.nota.xml.XmlAccess;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;

public class Concatter extends AbstractContentTransformation {
	
	private LinkedList<URI> images = new LinkedList<URI>();
	private LinkedList<URI> originalDocuments = new LinkedList<URI>();
	
	public Concatter(XdmNode opfDocument, boolean updateOpf)
			throws SaxonApiException {
		super(XmlAccessProvider.getXmlAccess().getXsltTransformer(
				"/dk/nota/xml/xslt/epub-concat.xsl"), opfDocument);
		addListener(new ConcatListener());
		addParameter("UPDATE_OPF", new XdmAtomicValue(updateOpf));
	}
	
	public Concatter(LinkedHashMap<URI,XdmNode> documentMap,
			XdmNode opfDocument, boolean updateOpf) throws SaxonApiException {
		this(opfDocument, updateOpf);
		addParameter("CONTENT_DOCUMENTS", new XdmValue(documentMap.values()));
	}
	
	public LinkedList<URI> getImages() {
		return images;
	}
	
	public LinkedList<URI> getOriginalDocuments() {
		return originalDocuments;
	}
	
	private class ConcatListener extends AbstractTransformationListener {
		
		private ConcatListener() {
		}
		
		@Override
		public void message(XdmNode message, boolean terminate,
				SourceLocator sourceLocator) {
			// Get values of <nota:document> elements passed by xsl:message
			XdmSequenceIterator messageIterator = message.axisIterator(Axis
					.DESCENDANT_OR_SELF, new QName(XmlAccess.NAMESPACE_NOTA,
							"document"));
			messageIterator.forEachRemaining(
					i -> originalDocuments.add(URI.create(i.getStringValue())));
			messageIterator = message.axisIterator(Axis.DESCENDANT_OR_SELF,
					new QName(XmlAccess.NAMESPACE_NOTA, "image"));
			messageIterator.forEachRemaining(
					i -> images.add(URI.create(i.getStringValue())));
		}
	}
	
}
