package nota.oxygen.epub;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class FindIdHandler extends DefaultHandler {
	private String id;

	public void startDocument() {
	}

	public void endDocument() {
	}

	public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) {
		if (qualifiedName.equals("body")) {
			if (attributes != null) {
				int numberAttributes = attributes.getLength();
				for (int loopIndex = 0; loopIndex < numberAttributes; loopIndex++) {
					if (attributes.getQName(loopIndex).equals("id")) {
						id = attributes.getValue(loopIndex);
					}
				}
			}
		}
	}

	public void endElement(String uri, String localName, String qualifiedName) {
	}

	/**
	 * This will be called everytime parser encounter a value node
	 * */
	public void characters(char characters[], int start, int length) {
	}

	public String getId() {
		return id;
	}
}