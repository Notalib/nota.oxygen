package nota.oxygen.epub;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class PackageHandler extends DefaultHandler {
	private String identifier;
	private String title;
	private String creator;
	private String language;
	private String source;
	private String format;
	private String publisher;
	private String date;
	
	private String characterData;
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getCreator() {
		return creator;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public String getSource() {
		return source;
	}
	public String getFormat() {
		return format;
	}
	
	public String getPublisher() {
		return publisher;
	}
	
	public String getDate() {
		return date;
	}
	
	public void startDocument() {
	}

	public void endDocument() {
	}
	
	public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) {
		characterData = "";
	}

	public void endElement(String uri, String localName, String qualifiedName) {
		if (qualifiedName.equals("dc:identifier")) {
			identifier = characterData;
		}
		
		if (qualifiedName.equals("dc:title")) {
			title = characterData;
		}
		
		if (qualifiedName.equals("dc:creator")) {
			creator = characterData;
		}
		
		if (qualifiedName.equals("dc:language")) {
			language = characterData;
		}
		
		if (qualifiedName.equals("dc:source")) {
			source = characterData;
		}
		
		if (qualifiedName.equals("dc:format")) {
			format = characterData;
		}
		
		if (qualifiedName.equals("dc:publisher")) {
			publisher = characterData;
		}
		
		if (qualifiedName.equals("dc:date")) {
			date = characterData;
		}
	}

	public void characters(char characters[], int start, int length) {
		String tempCharacterData = new String(characters, start, length);
		tempCharacterData = tempCharacterData.replaceAll("&(?!amp;)", "&amp;");
		tempCharacterData = tempCharacterData.replaceAll("<", "&lt;");
		tempCharacterData = tempCharacterData.replaceAll(">", "&gt;");
		tempCharacterData = tempCharacterData.replaceAll("\"", "&quot;");
		tempCharacterData = tempCharacterData.replaceAll("'", "&apos;");
		characterData += tempCharacterData;
	}
}
