package dk.nota.epub.content;

import dk.nota.xml.AbstractXsltTransformation;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.Xslt30Transformer;

public abstract class AbstractContentTransformation
		extends AbstractXsltTransformation {
	
	protected XdmNode opfDocument;

	public AbstractContentTransformation(Xslt30Transformer transformer,
			XdmNode opfDocument) {
		super(transformer);
		this.opfDocument = opfDocument;
		addParameter("OPF_DOCUMENT", opfDocument);
	}

}
