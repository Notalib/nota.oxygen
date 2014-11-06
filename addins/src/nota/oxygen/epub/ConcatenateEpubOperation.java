package nota.oxygen.epub;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
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

	private AuthorAccess opfAccess;
	private String xhtmlFileName = "concatenated.xhtml";
	private String epubFilePath = "";
	private DocumentBuilderFactory factory;
	private DocumentBuilder builder = null;
	private Document xhtmlDocument;
	private Element htmlElement;
	private Element headElement;
	private Element bodyElement;
	
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[]{};
	}

	@Override
	public String getDescription() {
		return "Concatenates epub files";
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void doOperation() throws AuthorOperationException {
		URL opfUrl = EpubUtils.getPackageUrl(getAuthorAccess());
		if (opfUrl == null) {
			showMessage("Could not find pagkage file for document");
			return;
		}
		
		opfAccess = EpubUtils.getAuthorDocument(getAuthorAccess(), opfUrl);
		if (opfAccess == null) {
			showMessage("Could not access pagkage file for document");
			return;
		}
		
		epubFilePath = EpubUtils.getEpubFolder(getAuthorAccess());
		if (epubFilePath.equals("")) {
			showMessage("Could not access epub folder");
			return;
		}
		
		try {
			// create dom document
			factory = DocumentBuilderFactory.newInstance();
			factory.setExpandEntityReferences(false);
			builder = factory.newDocumentBuilder();
			xhtmlDocument = builder.newDocument();
			
			// add html, head and body do document
			htmlElement = (Element)xhtmlDocument.createElement("html");
			headElement = (Element)xhtmlDocument.createElement("head");
			bodyElement = (Element)xhtmlDocument.createElement("body");
			htmlElement.appendChild(headElement);
			htmlElement.appendChild(bodyElement);
			xhtmlDocument.appendChild(htmlElement);
			
			//URL[] xhtmlUrls = EpubUtils.getSpineUrls(opfAccess, false);
			
			// traverse each xhtml document in epub
			for (AuthorAccess xhtmlAccess : EpubUtils.getSpine(opfAccess, false)) {

				AuthorElement htmlElem = xhtmlAccess.getDocumentController().getAuthorDocumentNode().getRootElement();
				if (htmlElem != null) {
					// get html node from author document
					AuthorNode htmlNode = getFirstElement(xhtmlAccess.getDocumentController().findNodesByXPath("/html", htmlElem, true, true, true, true));
					if (htmlNode == null) {
						throw new AuthorOperationException("Found no html in xhtml file");
					}
					
					// serialize and deserialize html
					String htmlContent = Utils.serialize(xhtmlAccess, htmlNode);
					Element htmlElementSource = Utils.deserializeElement(htmlContent);
					
					// add attributes in html to the html of xhtml file
					NamedNodeMap htmlAttributes = htmlElementSource.getAttributes();
					for (int j=0; j<htmlAttributes.getLength(); j++) {
						Attr node = (Attr) htmlAttributes.item(j);
						htmlElement.setAttributeNS(node.getNamespaceURI(), node.getName(), node.getValue());
					}
					
					AuthorElement headElem = null;
					for (AuthorNode node : htmlElem.getElementsByLocalName("head")) {
						headElem = (AuthorElement)node;
						break;
					}
					
					if (headElem != null) {
						try {
							// get head node from author document
							AuthorElement head = getFirstElement(xhtmlAccess.getDocumentController().findNodesByXPath("/html/head", true, true, true));
							if (head == null) {
								throw new AuthorOperationException("Found no head in xhtml file");
							}
							
							// serialize and deserialize head
							String headContent = Utils.serialize(xhtmlAccess, head);
							Element headElementSource = Utils.deserializeElement(headContent);
							
							// get head elements
							NodeList listOfSrcHeadElements = headElementSource.getChildNodes();
							NodeList listOfDesHeadElements = headElement.getChildNodes();
							
							// append head elements to the head of new xhtml file
							for (int i=0; i<listOfSrcHeadElements.getLength(); i++) {
								Node node = xhtmlDocument.importNode(listOfSrcHeadElements.item(i), true);
								String metaSourceValue = "";
								if (node.getNodeName().equalsIgnoreCase("meta")) {
									Node metaName = node.getAttributes().getNamedItem("name");
									if (metaName != null) {
										metaSourceValue = String.valueOf(node.getAttributes().getNamedItem("name").getNodeValue());
									}
								}
								
								boolean exists = false;
								for (int j=0; j<listOfDesHeadElements.getLength(); j++) {
									String metaNewValue = "";
									if (listOfDesHeadElements.item(j).getNodeName().equalsIgnoreCase("meta")) {
										Node metaName = listOfDesHeadElements.item(j).getAttributes().getNamedItem("name");
										if (metaName != null) {
											metaNewValue = String.valueOf(listOfDesHeadElements.item(j).getAttributes().getNamedItem("name").getNodeValue());
										}
									}
									
									if (listOfDesHeadElements.item(j).isEqualNode(node)) exists = true;
									else if (!metaSourceValue.equals("") && !metaNewValue.equals("") && metaSourceValue.equals(metaNewValue)) exists = true;
								}
								
								if (!exists) {
									headElement.appendChild(node);
								}
							}
						} catch (Exception e) {
							throw e;
						}
					}
					
					AuthorElement bodyElem = null;
					for (AuthorNode node : htmlElem.getElementsByLocalName("body")) {
						bodyElem = (AuthorElement)node;
						break;
					}
					if (bodyElem != null) {
						try {
							// get body node from author document
							AuthorNode bodyNode = getFirstElement(xhtmlAccess.getDocumentController().findNodesByXPath("/html/body", bodyElem, true, true, true, true));
							if (bodyNode == null) {
								throw new AuthorOperationException("Found no body in xhtml file");
							}
							
							// serialize and deserialize body
							String bodyContent = Utils.serialize(xhtmlAccess, bodyNode);
							Element bodyElementSource = Utils.deserializeElement(bodyContent);
							Element sectionElement = (Element)xhtmlDocument.createElement("section");
							
							// append body attributes to section of new xhtml file
							NamedNodeMap bodyAttributes = bodyElementSource.getAttributes();
							for (int j=0; j<bodyAttributes.getLength(); j++) {
								Attr node = (Attr) bodyAttributes.item(j);
								sectionElement.setAttributeNS(node.getNamespaceURI(), node.getName(), node.getValue());
							}
							
							// append body elements to the section of new xhtml file
							NodeList listOfBodyElements = bodyElementSource.getChildNodes();
							for (int i=0; i<listOfBodyElements.getLength(); i++) {
								sectionElement.appendChild(xhtmlDocument.importNode(listOfBodyElements.item(i), true));
							}
							
							/*NodeList nList = sectionElement.getElementsByTagName("a");
							for (int i=0; i<nList.getLength(); i++) {
								NamedNodeMap attr = nList.item(i).getAttributes();
								String fileName = "";
								String noteRef = "";
								for (int j=0; j<attr.getLength(); j++) {
									Attr node = (Attr) attr.item(j);
									if (node.getNodeName().equalsIgnoreCase("href")) {
										String[] href = node.getValue().split("#");
										if (href.length == 2) {
											fileName = href[0];
											noteRef = href[1];
										}

										if (!fileName.equals("") && !noteRef.equals("")) {
											node.setNodeValue(xhtmlFileName + "#" + fileName + "_n_o_t_a_" + noteRef);
										}
									}
								}
							}*/
							
							bodyElement.appendChild(sectionElement);

						} catch (Exception e) {
							throw e;
						}
					}
				}
				
				// close xhtml document
				xhtmlAccess.getEditorAccess().close(true);
				
				// delete xhtml document
				xhtmlAccess.getWorkspaceAccess().delete(xhtmlAccess.getEditorLocation());
			}
			
			// close opf docuement
			opfAccess.getEditorAccess().close(true);
			
			// transform concatenated document to xml content string
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");	
			//DOMImplementation domImpl = xhtmlDocument.getImplementation();
			//DocumentType doctype = domImpl.createDocumentType("html", "-//W3C//DTD XHTML 1.0 Transitional//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
			//transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
			//transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
			DOMSource source = new DOMSource(xhtmlDocument);
			StreamResult result = new StreamResult(buffer);
			transformer.transform(source, result);
			String xmlContent = buffer.toString();
			
			// open new editor with concatenated xml content
			AuthorWorkspaceAccess wa = getAuthorAccess().getWorkspaceAccess();
			URL newEditorUrl = wa.createNewEditor("xhtml", "text/xml", xmlContent);
			
			// save content in editor into new concatenated xhtml file
			WSEditor editor = wa.getEditorAccess(newEditorUrl);
			editor.saveAs(new URL(epubFilePath + "/" + xhtmlFileName));
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void parseArguments(ArgumentsMap args) throws IllegalArgumentException {
		// Nothing to parse!!!
	}
}
