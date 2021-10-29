package dk.nota.dtb.conversion;

import dk.nota.xml.XmlAccess;
import dk.nota.xml.XmlAccessProvider;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Xslt30Transformer;

public enum InspirationOutput {
	
	INSP_AUDIO, INSP_BRAILLE, INSP_ETEXT, INSP_PRINT, INSP_PROOF;
	
	public String getName() {
		switch (this) {
		case INSP_AUDIO: return "Inspiration: Lyd";
		case INSP_BRAILLE: return "Inspiration: Punkt";
		case INSP_ETEXT: return "Inspiration: E-tekst";
		case INSP_PRINT: return "Inspiration: Tryk";
		case INSP_PROOF: return "Inspiration: Korrektur";
		default: return null;
		}
	}
	
	public String getPrefix() {
		switch (this) {
		case INSP_AUDIO: return "INSL";
		case INSP_BRAILLE: return "INSP";
		case INSP_ETEXT: return "INSE";
		case INSP_PRINT: return "INST";
		case INSP_PROOF: return "INSK";
		default: return null;
		}
	}
	
	public Xslt30Transformer getTransformer() throws SaxonApiException {
		XmlAccess xmlAccess = XmlAccessProvider.getXmlAccess();
		switch (this) {
		case INSP_AUDIO: return xmlAccess.getXsltTransformer(
				"/dk/nota/xml/xslt/dtb-inspiration-audio.xsl");
		case INSP_BRAILLE: return xmlAccess.getXsltTransformer(
				"/dk/nota/xml/xslt/dtb-inspiration-braille.xsl");
		case INSP_ETEXT: return xmlAccess.getXsltTransformer(
				"/dk/nota/xml/xslt/dtb-inspiration-etext.xsl");
		case INSP_PRINT: return xmlAccess.getXsltTransformer(
				"/dk/nota/xml/xslt/dtb-inspiration-print.xsl");
		case INSP_PROOF: return xmlAccess.getXsltTransformer(
				"/dk/nota/xml/xslt/dtb-inspiration-proof.xsl");
		default: return null;
		}
	}
	
}
