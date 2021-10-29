package dk.nota.dtb.conversion;

import dk.nota.xml.AbstractXsltTransformation;
import dk.nota.xml.XmlAccessProvider;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

public class DtbToDocxConverter extends AbstractXsltTransformation {
	
	private XdmNode dtbDocument;
	
	public DtbToDocxConverter(XdmNode dtbDocument) throws SaxonApiException {
		super(XmlAccessProvider.getXmlAccess().getXsltTransformer(
				"/dk/nota/xml/xslt/dtbook2docx/dtbook2docx.xsl"));
		this.dtbDocument = dtbDocument;
	}
	
	@Override
	public XdmValue call() throws SaxonApiException {
		transformer.setStylesheetParameters(parameters);
		return transformer.applyTemplates(dtbDocument.asSource());
	}

}
