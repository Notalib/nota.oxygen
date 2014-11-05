package nota.oxygen.epub;

import java.io.StringWriter;
import java.net.MalformedURLException;
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
	private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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
		}

		
		
		
		try {
			
			
			// create dom document
			builder = factory.newDocumentBuilder();
			xhtmlDocument = builder.newDocument();
			htmlElement = (Element)xhtmlDocument.createElement("html");
			headElement = (Element)xhtmlDocument.createElement("head");
			bodyElement = (Element)xhtmlDocument.createElement("body");
			htmlElement.appendChild(headElement);
			htmlElement.appendChild(bodyElement);
			xhtmlDocument.appendChild(htmlElement);
			
			
			URL[] xhtmlUrls = EpubUtils.getSpineUrls(opfAccess, false);
			
			
			// traverse each xhtml document in epub
			for (AuthorAccess xhtmlAccess : EpubUtils.getSpine(opfAccess, false)) {

				AuthorElement htmlElem = xhtmlAccess.getDocumentController().getAuthorDocumentNode().getRootElement();
				if (htmlElem != null) {
					// add attributes to the html element of new xhtml file
					AuthorNode htmlNode = getFirstElement(xhtmlAccess.getDocumentController().findNodesByXPath("/html", htmlElem, true, true, true, true));
					if (htmlNode == null) {
						throw new AuthorOperationException("Found no html in xhtml file");
					}
					
					String htmlContent = Utils.serialize(xhtmlAccess, htmlNode);
					Element htmlElementSource = Utils.deserializeElement(htmlContent);
					
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
							// append elements to the head of new xhtml file
							AuthorElement head = getFirstElement(xhtmlAccess.getDocumentController().findNodesByXPath("/html/head", true, true, true));
							if (head == null) {
								throw new AuthorOperationException("Found no head in xhtml file");
							}
							
							String headContent = Utils.serialize(xhtmlAccess, head);
							Element headElementSource = Utils.deserializeElement(headContent);
							
							NodeList listOfSrcHeadElements = headElementSource.getChildNodes();
							NodeList listOfDesHeadElements = headElement.getChildNodes();
							
							for (int i=0; i<listOfSrcHeadElements.getLength(); i++) {
								Node node = xhtmlDocument.importNode(listOfSrcHeadElements.item(i), true);
								
								boolean exists = false;
								for (int j=0; j<listOfDesHeadElements.getLength(); j++) {
									if (listOfDesHeadElements.item(j).isEqualNode(node)) {
										exists = true;
									}
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
							// append elements to the body of new xhtml file
							AuthorNode bodyNode = getFirstElement(xhtmlAccess.getDocumentController().findNodesByXPath("/html/body", bodyElem, true, true, true, true));
							if (bodyNode == null) {
								throw new AuthorOperationException("Found no body in xhtml file");
							}
							
							String bodyContent = Utils.serialize(xhtmlAccess, bodyNode);
							Element bodyElementSource = Utils.deserializeElement(bodyContent);
							Element sectionElement = (Element)xhtmlDocument.createElement("section");
							
							NamedNodeMap bodyAttributes = bodyElementSource.getAttributes();
							for (int j=0; j<bodyAttributes.getLength(); j++) {
								Attr node = (Attr) bodyAttributes.item(j);
								sectionElement.setAttributeNS(node.getNamespaceURI(), node.getName(), node.getValue());
							}
							
							NodeList listOfBodyElements = bodyElementSource.getChildNodes();
							for (int i=0; i<listOfBodyElements.getLength(); i++) {
								sectionElement.appendChild(xhtmlDocument.importNode(listOfBodyElements.item(i), true));
							}
							
							bodyElement.appendChild(sectionElement);

						} catch (Exception e) {
							throw e;
						}
					}
				}
			}
			
			
			
			// transform dom document to string
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer;
			transformer = transFactory.newTransformer();
			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(new DOMSource(xhtmlDocument), new StreamResult(buffer));
			String str = buffer.toString();
			
			
			
			
			// save string into new xhtml concatenated document
			AuthorWorkspaceAccess wa = getAuthorAccess().getWorkspaceAccess();
			URL newEditorUrl = wa.createNewEditor("xhtml", "text/xml", str);
			WSEditor editor = wa.getEditorAccess(newEditorUrl);
			editor.saveAs(new URL("zip:file:/C:/Users/ybk/nota.oxygen/samples/heading_sample_single_textfile.epub!/EPUB/concatenated.xhtml"));
			
		} catch (ParserConfigurationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
						e.printStackTrace();
		}
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		// Nothing to parse!!!
	}
}
