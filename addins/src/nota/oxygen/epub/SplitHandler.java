package nota.oxygen.epub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

public class SplitHandler extends DefaultHandler {	
	private Map<String, String> htmlAttributes;
	private String title;
	private Map<String, String> metaNodes;
	private List<String> cssLinks;
	private String characterData;
	
	public SplitHandler() {
		htmlAttributes = new HashMap<String, String>();
		metaNodes = new TreeMap<String, String>();
		cssLinks = new ArrayList<String>();
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

	public void startDocument() {
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
	}

	public void endElement(String uri, String localName, String qualifiedName) {
		if (qualifiedName.equals("title") && title == null) {
			title = characterData;
		}
	}

	public void characters(char characters[], int start, int length) {
		characterData = new String(characters, start, length);
		characterData = characterData.replaceAll("&(?!amp;)", "&amp;");
		characterData = characterData.replaceAll("<", "&lt;");
		characterData = characterData.replaceAll(">", "&gt;");
	}
}
