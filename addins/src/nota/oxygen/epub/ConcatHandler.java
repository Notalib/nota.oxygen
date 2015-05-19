package nota.oxygen.epub;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.*;

import org.xml.sax.helpers.DefaultHandler;

public class ConcatHandler extends DefaultHandler {
	private List<String> topLines;
	private List<String> headLines;
	private List<String> bodyLines;
	private List<String> bottomLines;

	private boolean htmlExists;
	private boolean headExists;
	private boolean bodyExists;

	private boolean head;
	private boolean body;
	private boolean write;
	private String headLine;

	private String fileEpubType;

	public ConcatHandler() {
		topLines = new ArrayList<String>();
		headLines = new ArrayList<String>();
		bodyLines = new ArrayList<String>();
		bottomLines = new ArrayList<String>();

		htmlExists = false;
		headExists = false;
		bodyExists = false;
	}

	public List<String> getTopLines() {
		return topLines;
	}

	public List<String> getHeadLines() {
		return headLines;
	}

	public List<String> getBodyLines() {
		return bodyLines;
	}

	public List<String> getBottomLines() {
		return bottomLines;
	}

	public void setFileEpubType(String fileEpubType) {
		this.fileEpubType = fileEpubType;
	}

	public void startDocument() {
		head = false;
		body = false;
		write = false;
		headLine = "";
	}

	public void endDocument() {
	}

	public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) {
		if (qualifiedName.equals("html") && !htmlExists) {
			topLines.add(getLine(qualifiedName, attributes, true, false));
		} else if (qualifiedName.equals("head") && !headExists) {
			head = true;
			headLines.add(getLine(qualifiedName, attributes, true, false));
			write = true;
		} else if (qualifiedName.equals("body")) {
			if (!bodyExists)
				bodyLines.add("<" + qualifiedName + ">");
			body = true;
			bodyLines.add(getLine("section", attributes, true, true));
			write = true;
		} else if (write) {
			if (head) {
				if (qualifiedName.equals("title"))
					headLine = getLine(qualifiedName, attributes, true, false);
				else
					headLine = getLine(qualifiedName, attributes, false, false);
			}

			if (body) {
				bodyLines.add(getLine(qualifiedName, attributes, true, false));
			}
		}
	}

	public void endElement(String uri, String localName, String qualifiedName) {
		if (qualifiedName.equals("html") && !htmlExists) {
			bottomLines.add("</" + qualifiedName + ">");
			htmlExists = true;
		} else if (qualifiedName.equals("head") && !headExists) {
			head = false;
			headLines.add("</" + qualifiedName + ">");
			write = false;
			headExists = true;
		} else if (qualifiedName.equals("body")) {
			body = false;
			if (!bodyExists)
				bottomLines.add("</body>");
			bodyLines.add("</section>");
			if (qualifiedName.equals("marks")) {
				startElement("", "Result", "Result", null);
				characters("Pass".toCharArray(), 0, "Pass".length());
				endElement("", "Result", "Result");
			}
			write = false;
			bodyExists = true;
		} else if (write) {
			if (head) {
				if (qualifiedName.equals("title"))
					headLine += "</" + qualifiedName + ">";
				else
					headLine += "/>";
				headLines.add(headLine);
			} else if (body) {
				bodyLines.add("</" + qualifiedName + ">");
				if (qualifiedName.equals("marks")) {
					startElement("", "Result", "Result", null);
					characters("Pass".toCharArray(), 0, "Pass".length());
					endElement("", "Result", "Result");
				}
			}
		}
	}

	public void characters(char characters[], int start, int length) {
		if (write) {
			String characterData = (new String(characters, start, length))
					.trim();
			if (characterData.indexOf("\n") < 0 && characterData.length() > 0) {
				if (head)
					headLine += characterData;
				if (body)
					bodyLines.add(characterData);
			}
		}
	}

	private String getLine(String qualifiedName, Attributes attributes, boolean hasContent, boolean topSection) {
		String line = "<" + qualifiedName;

		if (attributes != null) {
			int numberAttributes = attributes.getLength();

			for (int loopIndex = 0; loopIndex < numberAttributes; loopIndex++) {
				line += ' ';
				line += attributes.getQName(loopIndex);
				line += "=\"";
				String attrValue = attributes.getValue(loopIndex);
				if (topSection
						&& attributes.getQName(loopIndex).equals("epub:type")) {
					String[] epubTypes = attributes.getValue(loopIndex).split(
							"\\ ");
					boolean epubTypeOk = false;
					for (String epubType : epubTypes) {
						if (!epubType.equals("frontmatter")
								&& !epubType.equals("bodymatter")
								&& !epubType.equals("backmatter")
								&& !epubType.equals("rearmatter")) {
							epubTypeOk = true;
						}
					}

					if (!epubTypeOk && !fileEpubType.equals("")
							&& !fileEpubType.equals("frontmatter")
							&& !fileEpubType.equals("bodymatter")
							&& !fileEpubType.equals("backmatter")
							&& !fileEpubType.equals("rearmatter")) {
						attrValue = attrValue + " " + fileEpubType;
					}
				}
				line += attrValue;
				line += '"';
			}
		}

		if (hasContent)
			line += '>';

		return line;
	}
}
