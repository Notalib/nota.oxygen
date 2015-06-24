package nota.oxygen.epub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

public class ConcatHandler extends DefaultHandler {
	private Map<String, String> htmlAttributes;
	private String title;
	private Map<String, String> metaNodes;
	private List<String> cssLinks;
	
	private boolean isBody;
	private List<String> bodyLines;
	
	private String characterData;
	
	private String fileEpubType;

	public ConcatHandler() {
		htmlAttributes = new HashMap<String, String>();
		metaNodes = new LinkedHashMap <String, String>();
		cssLinks = new ArrayList<String>();
		bodyLines = new ArrayList<String>();
	}

	public Map<String, String> getHtmlAttributes() {
		return htmlAttributes;
	}
	
	public String getTitle() {
		return title;
	}
	
	public Map<String, String> getMetaNodes() {
		return metaNodes;
	}
	
	public List<String> getCssLinks() {
		return cssLinks;
	}

	public List<String> getBodyLines() {
		return bodyLines;
	}

	public void setFileEpubType(String fileEpubType) {
		this.fileEpubType = fileEpubType;
	}

	public void startDocument() {
		isBody = false;
	}

	public void endDocument() {
	}

	public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) {
		if (qualifiedName.equals("html")) {
			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					if (!htmlAttributes.containsKey(attributes.getQName(i))) {
						String key = attributes.getQName(i);
						String value = attributes.getValue(i);
						if (key.equals("lang") && value.equals("")) value = "da";
						if (key.equals("xml:lang") && value.equals("")) value = "da";
						htmlAttributes.put(key, value);
					}
				}
			}
		}
		
		if (qualifiedName.equals("meta")) {
			if (attributes != null) {
				String name = null;
				String content = null;
				for (int i = 0; i < attributes.getLength(); i++) {
					if (attributes.getQName(i).equals("name")) name = attributes.getValue(i);
					else if (attributes.getQName(i).equals("content")) content = attributes.getValue(i);
				}
				
				if (name != null && !metaNodes.containsKey(name)) {
					metaNodes.put(name, content);
				}
			}
		}

		if (qualifiedName.equals("link")) {
			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					if (attributes.getQName(i).equals("href")) {
						if (!cssLinks.contains(attributes.getValue(i))) {
							cssLinks.add(attributes.getValue(i));
						}
					}
				}
			}
		}
		
		if (qualifiedName.equals("body")) {
			isBody = true;
			bodyLines.add(getLine("section", attributes, true));
		} else if (isBody) {
			if (isBody) {
				bodyLines.add(getLine(qualifiedName, attributes, false));
			}
		}
	}

	public void endElement(String uri, String localName, String qualifiedName) {
		if (qualifiedName.equals("title") && title == null) {
			title = characterData;
		}
		
		if (qualifiedName.equals("body")) {
			isBody = false;
			bodyLines.add("</section>");
		} else if (isBody) {
			bodyLines.add("</" + qualifiedName + ">");
		}
	}

	public void characters(char characters[], int start, int length) {
		characterData = new String(characters, start, length);
		
		if (isBody) {
			if (characterData.indexOf("\n") < 0 && characterData.length() > 0) {
				characterData = characterData.replaceAll("&(?!amp;)", "&amp;");
				characterData = characterData.replaceAll("<", "&lt;");
				characterData = characterData.replaceAll(">", "&gt;");
				bodyLines.add(characterData);
			}
		}
	}

	private String getLine(String qualifiedName, Attributes attributes, boolean topSection) {
		String line = "<" + qualifiedName;

		if (attributes != null) {
			int numberAttributes = attributes.getLength();

			for (int i=0; i<numberAttributes; i++) {
				line += ' ';
				line += attributes.getQName(i);
				line += "=\"";
				
				String attrValue = attributes.getValue(i);
				attrValue = attrValue.replaceAll("&(?!amp;)", "&amp;");
				
				if (topSection && attributes.getQName(i).equals("epub:type")) {
					String[] epubTypes = attributes.getValue(i).split("\\ ");
					boolean epubTypeOk = false;
					for (String epubType : epubTypes) {
						if (!epubType.equals("frontmatter") && !epubType.equals("bodymatter") && !epubType.equals("backmatter") && !epubType.equals("rearmatter")) {
							epubTypeOk = true;
						}
					}

					if (!epubTypeOk && !fileEpubType.equals("") && !fileEpubType.equals("frontmatter") && !fileEpubType.equals("bodymatter") && !fileEpubType.equals("backmatter") && !fileEpubType.equals("rearmatter")) {
						attrValue = attrValue + " " + fileEpubType;
					}
				}
				
				line += attrValue;
				line += '"';
			}
		}

		line += '>';

		return line;
	}
}
