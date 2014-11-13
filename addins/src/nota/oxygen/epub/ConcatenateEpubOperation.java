package nota.oxygen.epub;

import java.io.StringWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorWorkspaceAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.editor.WSEditor;
import nota.oxygen.common.BaseAuthorOperation;
import nota.oxygen.common.Utils;

public class ConcatenateEpubOperation extends BaseAuthorOperation {
	private String epubFilePath = "";
	private String newXhtmlFileName = "concatenated.xhtml";
	private Document xhtmlDocument = null;
	private Element htmlElementAdded;
	private Element headElementAdded;
	private Element bodyElementAdded;
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[]{};
	}

	@Override
	public String getDescription() {
		return "Concatenates epub files";
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		// get epub folder path
		epubFilePath = EpubUtils.getEpubFolder(getAuthorAccess());
		if (epubFilePath.equals("")) {
			showMessage("Could not access epub folder");
			return;
		}
		
		// create new document for concatenating
		createDocument();
		if (xhtmlDocument == null) {
			showMessage("Could not create new document for concatenating");
			return;
		}
		
		try {
			
			URL[] xhtmlUrls = EpubUtils.getSpineUrls(getAuthorAccess(), false);
			if (xhtmlUrls.length < 2) {
				showMessage("Epub cannot be concatenated - only one xhtml file");
				return;
			}
			
			// traverse each xhtml document in epub
			for (URL xhtmlUrl : xhtmlUrls) {
				// get a authoraccess object to the xhtml document
				AuthorAccess xhtmlAccess = EpubUtils.getAuthorDocument(getAuthorAccess(), xhtmlUrl);
				
				// close xhtml document
				xhtmlAccess.getEditorAccess().close(true);
				
				AuthorElement rootElement = xhtmlAccess.getDocumentController().getAuthorDocumentNode().getRootElement();
				if (rootElement == null) {
					showMessage("Found no root in xhtml file");
					return;
				}
				
				// get html node from author document
				AuthorNode authorNode = getFirstElement(xhtmlAccess.getDocumentController().findNodesByXPath("/html", rootElement, true, true, true, true));
				if (authorNode == null) {
					throw new AuthorOperationException("Found no html in xhtml file");
				}

				// serialize and deserialize html
				String nodeContent = Utils.serialize(xhtmlAccess, authorNode);
				Node html = Utils.deserializeElement(nodeContent);
					
				// add html attributes
				NamedNodeMap htmlAttributes = html.getAttributes();
				for (int j = 0; j < htmlAttributes.getLength(); j++) {
					Attr attribute = (Attr) htmlAttributes.item(j);
					htmlElementAdded.setAttributeNS(attribute.getNamespaceURI(), attribute.getName(), attribute.getValue());
				}

				// traverse html nodes
				NodeList htmlNodes = html.getChildNodes();
				for (int i = 0; i < htmlNodes.getLength(); i++) {
					Node htmlNode = htmlNodes.item(i);
					if (htmlNode.getNodeName().equals("head")) {
						// get head nodes
						NodeList headNodes = htmlNode.getChildNodes();
						NodeList headNodesAdded = headElementAdded.getChildNodes();

						// append head elements
						for (int j = 0; j < headNodes.getLength(); j++) {
							Node headNode = xhtmlDocument.importNode(headNodes.item(j), true);
							String metaValue = getMetaNodeValue(headNode);

							// check if head element not already exists
							boolean exists = false;
							for (int k = 0; k < headNodesAdded.getLength(); k++) {
								Node headNodeAdded = headNodesAdded.item(k);
								String metaValueAdded = getMetaNodeValue(headNodeAdded);

								if (headNodeAdded.isEqualNode(headNode))	exists = true;
								else if (!metaValue.equals("")	&& !metaValueAdded.equals("") && metaValue.equals(metaValueAdded)) exists = true;
							}

							// append head element
							if (!exists) headElementAdded.appendChild(headNode);
						}
					}

					if (htmlNode.getNodeName().equals("body")) {
						// create new section
						Element sectionElement = (Element) xhtmlDocument.createElement("section");

						// append body attributes to new section
						NamedNodeMap bodyAttributes = htmlNode.getAttributes();
						for (int j = 0; j < bodyAttributes.getLength(); j++) {
							Attr attribute = (Attr) bodyAttributes.item(j);
							sectionElement.setAttributeNS(attribute.getNamespaceURI(), attribute.getName(), attribute.getValue());
						}

						// append body elements
						NodeList bodyNodes = htmlNode.getChildNodes();
						for (int j = 0; j < bodyNodes.getLength(); j++) {
							sectionElement.appendChild(xhtmlDocument.importNode(bodyNodes.item(j),	true));
						}

						// get all references
						NodeList refNodes = sectionElement.getElementsByTagName("a");
						for (int j = 0; j < refNodes.getLength(); j++) {
							Node refNode = refNodes.item(j);
							NamedNodeMap attributes = refNode.getAttributes();
							for (int k=0; k<attributes.getLength(); k++) {
								Attr attribute = (Attr) attributes.item(k);
								if (attribute.getNodeName().equalsIgnoreCase("href")) {
									// remove file reference
									attribute.setNodeValue(attribute.getNodeValue().substring(attribute.getNodeValue().indexOf("#")));
								}
							}
						}

						// append section to body element
						bodyElementAdded.appendChild(sectionElement);
					}
				}

				// remove xhtml document from opf document
				String fileName = xhtmlAccess.getUtilAccess().getFileName(xhtmlAccess.getEditorAccess().getEditorLocation().toString());
				EpubUtils.removeOpfItem(getAuthorAccess(), fileName);

				// delete xhtml document
				xhtmlAccess.getWorkspaceAccess().delete(xhtmlAccess.getEditorAccess().getEditorLocation());
			}
			
			saveDocument();
			
			// add xhtml document to opf document
			EpubUtils.addOpfItem(getAuthorAccess(), newXhtmlFileName);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void createDocument() {
		DocumentBuilderFactory factory;
		DocumentBuilder builder;
		
		try {
			// create dom document
			factory = DocumentBuilderFactory.newInstance();
			factory.setExpandEntityReferences(false);
			builder = factory.newDocumentBuilder();
			xhtmlDocument = builder.newDocument();

			// add html, head and body to document
			htmlElementAdded = (Element) xhtmlDocument.createElement("html");
			headElementAdded = (Element) xhtmlDocument.createElement("head");
			bodyElementAdded = (Element) xhtmlDocument.createElement("body");
			htmlElementAdded.appendChild(headElementAdded);
			htmlElementAdded.appendChild(bodyElementAdded);
			xhtmlDocument.appendChild(htmlElementAdded);
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void saveDocument() {
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer;
		
		try {
			// new transformer
			transformer = transFactory.newTransformer();
			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			
			// DOMImplementation domImpl = xhtmlDocument.getImplementation();
			// DocumentType doctype = domImpl.createDocumentType("html",
			// "-//W3C//DTD XHTML 1.0 Transitional//EN",
			// "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
			// transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,
			// doctype.getPublicId());
			// transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
			// doctype.getSystemId());
			
			// transform concatenated document to xml content string
			DOMSource source = new DOMSource(xhtmlDocument);
			StreamResult result = new StreamResult(buffer);
			transformer.transform(source, result);
			String xmlContent = buffer.toString();

			// open new editor with concatenated xml content
			AuthorWorkspaceAccess wa = getAuthorAccess().getWorkspaceAccess();
			URL newEditorUrl = wa.createNewEditor("xhtml", "text/xml", xmlContent);

			// save content in editor into new concatenated xhtml file
			WSEditor editor = wa.getEditorAccess(newEditorUrl);
			editor.saveAs(new URL(epubFilePath + "/" + newXhtmlFileName));

			// close xhtml file
			wa.close(new URL(epubFilePath + "/" + newXhtmlFileName));
			
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getMetaNodeValue(Node meta) {
		String value = "";
		
		if (meta.getNodeName().equalsIgnoreCase("meta")) {
			Node metaName = meta.getAttributes().getNamedItem("name");
			if (metaName != null) {
				value = String.valueOf(meta.getAttributes().getNamedItem("name").getNodeValue());
			}
		}
		
		return value;
	}
	
	@Override
	protected void parseArguments(ArgumentsMap args) throws IllegalArgumentException {
		// Nothing to parse!!!
	}
}
