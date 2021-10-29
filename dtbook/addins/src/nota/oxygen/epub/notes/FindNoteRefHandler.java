package nota.oxygen.epub.notes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class FindNoteRefHandler extends DefaultHandler {
	private String fileName;
	
	private String id= null;
	private String className= null;
	private String epubType= null;
	private String href= null;
	
	private Map<String, List<String>> idMap;
	private Map<String, List<String>> fnMap;
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public Map<String, List<String>> getIDMap() {
		return idMap;
	}
	
	public Map<String, List<String>> getFNMap() {
		return fnMap;
	}
	
	public FindNoteRefHandler() {
		idMap = new LinkedHashMap<String, List<String>>();
		fnMap = new LinkedHashMap<String, List<String>>();
	}
	
	public void startDocument() {
	}

	public void endDocument() {
	}

	public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) {
		if (qualifiedName.equals("a")) {
			if (attributes != null) {
				if (attributes != null) {
					for (int i = 0; i < attributes.getLength(); i++) {
						if (attributes.getQName(i).equals("id")) id = attributes.getValue(i);
						if (attributes.getQName(i).equals("class")) className = attributes.getValue(i);
						if (attributes.getQName(i).equals("epub:type")) epubType = attributes.getValue(i);
						if (attributes.getQName(i).equals("href")) href = attributes.getValue(i);
					}
					
					if (className != null && className.equals("noteref") && epubType != null && epubType.equals("noteref")) {
						href = href.substring(href.indexOf("#"));
						href = href.replace("#", "");
						
						List<String> idList = idMap.get(href);
						if (idList == null) {
							idList = new ArrayList<String>();
							idMap.put(href, idList);
						}
						idList.add(id);
						
						List<String> fnList = fnMap.get(href);
						if (fnList == null) {
							fnList = new ArrayList<String>();
							fnMap.put(href, fnList);
						}
						fnList.add(fileName);
					}
				}
			}
		}
	}

	public void endElement(String uri, String localName, String qualifiedName) {
	}

	public void characters(char characters[], int start, int length) {
	}
}
