package dk.nota.dtb.conversion;

import java.net.URI;

import dk.nota.xml.AbstractXsltTransformation;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.Xslt30Transformer;

public class InspirationConverter extends AbstractXsltTransformation {

	public InspirationConverter(Xslt30Transformer transformer,
			XdmNode dtbDocument, URI outputUri) throws SaxonApiException {
		super(transformer);
		addParameter("OUTPUT_URI", new XdmAtomicValue(outputUri));
		transformer.setGlobalContextItem(dtbDocument);
	}
	
}
