package dk.nota.epub.content;

import dk.nota.xml.XmlAccessProvider;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

public class NavigationUpdater extends AbstractContentTransformation {

	public NavigationUpdater(XdmNode opfDocument) throws SaxonApiException {
		super(XmlAccessProvider.getXmlAccess().getXsltTransformer(
				"/dk/nota/xml/xslt/epub-navigation-update.xsl"), opfDocument);
	}

}
