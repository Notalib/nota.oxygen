package dk.nota.epub.conversion;

import java.net.URI;

import dk.nota.xml.AbstractXsltTransformation;
import dk.nota.xml.XmlAccessProvider;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;

public class DaisyComicConverter extends AbstractXsltTransformation {
	
	public DaisyComicConverter(XdmNode concatDocument, URI outputUri)
			throws SaxonApiException {
		super(XmlAccessProvider.getXmlAccess().getXsltTransformer(
				"/dk/nota/xml/xslt/epub-daisy-comic.xsl"));
		addParameter("OUTPUT_URI", new XdmAtomicValue(outputUri));
		transformer.setGlobalContextItem(concatDocument);
	}

}
