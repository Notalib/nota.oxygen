package dk.nota.epub.conversion;

import java.net.URI;

import dk.nota.epub.content.AbstractContentTransformation;
import dk.nota.xml.XmlAccessProvider;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;

public class EpubToDtbConverter extends AbstractContentTransformation {
	
	public EpubToDtbConverter(XdmNode xhtmlDocument, XdmNode opfDocument,
			URI outputUri) throws SaxonApiException {
		super(XmlAccessProvider.getXmlAccess().getXsltTransformer(
				"/dk/nota/xml/xslt/epub-xhtml-to-dtb.xsl"), opfDocument);
		addParameter("CONCAT_DOCUMENT", xhtmlDocument);
		addParameter("OUTPUT_URI", new XdmAtomicValue(outputUri));
	}
	
	public EpubToDtbConverter(XdmNode xhtmlDocument, XdmNode opfDocument,
			URI outputUri, XdmNode navDocument) throws SaxonApiException {
		this(xhtmlDocument, opfDocument, outputUri);
		addParameter("NAV_DOCUMENT", navDocument);
	}
	

}
