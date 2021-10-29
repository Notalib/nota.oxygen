package dk.nota.epub.conversion;

import java.io.File;
import java.util.LinkedList;

import dk.nota.epub.content.AbstractContentTransformation;
import dk.nota.xml.XmlAccessProvider;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

public class DocxToEpubConverter extends AbstractContentTransformation {

	public DocxToEpubConverter(XdmNode opfDocument, File[] sourceFiles)
			throws SaxonApiException {
		super(XmlAccessProvider.getXmlAccess().getXsltTransformer(
				"/dk/nota/xml/xslt/epub-import-docx.xsl"), opfDocument);
		LinkedList<XdmAtomicValue> sourceUris =
				new LinkedList<XdmAtomicValue>();
		for (File docxFile : sourceFiles) {
			String url = docxFile.getName().endsWith(".docx") ?
					"zip:" + docxFile.toURI().toString() + "!/word/" :
					// ASCII encoding is for some reason required for
					// resolving DTD references in .kat files with
					// non-ASCII URIs
					docxFile.toURI().toASCIIString();
			sourceUris.add(new XdmAtomicValue(url));
		}
		addParameter("SOURCE_URIS", new XdmValue(sourceUris));
	}

	
	
}
