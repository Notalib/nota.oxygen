package dk.nota.epub.content;


import dk.nota.xml.XmlAccessProvider;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;

public class Splitter extends AbstractContentTransformation {
	
	public Splitter(XdmNode concatDocument, XdmNode opfDocument)
			throws SaxonApiException {
		super(XmlAccessProvider.getXmlAccess().getXsltTransformer(
				"/dk/nota/xml/xslt/epub-split.xsl"), opfDocument);
		transformer.setGlobalContextItem(concatDocument);
		addParameter("UPDATE_OPF", new XdmAtomicValue(true));
	}

}
