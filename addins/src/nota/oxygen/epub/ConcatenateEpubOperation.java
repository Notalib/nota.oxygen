package nota.oxygen.epub;

import java.net.URL;

import javax.swing.JTextArea;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import nota.oxygen.common.BaseAuthorOperation;
import nota.oxygen.common.Utils;

public class ConcatenateEpubOperation extends BaseAuthorOperation {
	private String epubFilePath = "";
	private String newXhtmlFileName = "concatenated.xhtml";

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[]{};
	}

	@Override
	public String getDescription() {
		return "Concatenates epub files";
	}

	@Override
	protected void parseArguments(ArgumentsMap args) throws IllegalArgumentException {
		// Nothing to parse!!!
	}
	
	@Override
	protected void doOperation() throws AuthorOperationException {		
		try {
			// get epub folder path
			epubFilePath = EpubUtils.getEpubFolder(getAuthorAccess());
			if (epubFilePath.equals("")) {
				showMessage("Could not access epub folder");
				return;
			}
			
			// construct a new document
			Document doc = EpubUtils.createDocument();
			if (doc == null) {
				showMessage("Could not construct new document");
				return;
			}
			
			// get all xhtml files in epub (besides nav.html)
			URL[] xhtmlUrls = EpubUtils.getSpineUrls(getAuthorAccess(), false);
			if (xhtmlUrls.length < 2) {
				showMessage("This epub cannot be concatenated (only one xhtml file)");
				return;
			}
			
			Element htmlElementAdded = (Element) doc.createElement("html");
			Element headElementAdded = (Element) doc.createElement("head");
			Element bodyElementAdded = (Element) doc.createElement("body");
			
			// traverse each xhtml document in epub
			for (URL xhtmlUrl : xhtmlUrls) {
				// get xml from each xhtml document
				WSTextEditorPage editorPage = EpubUtils.getTextDocument(getAuthorAccess(), xhtmlUrl);
				JTextArea textArea = (JTextArea) ((WSTextEditorPage) editorPage).getTextComponent();
				String docText = textArea.getText();
				if (docText == null) {
					showMessage("Could not get xml from xhtml document");
					return;
				}
				
				// deserialize xml
				Node html = Utils.deserializeElement(docText);
				
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
							Node headNode = doc.importNode(headNodes.item(j), true);
							String metaValue = EpubUtils.getMetaNodeValue(headNode);

							// check if head element not already exists
							boolean exists = false;
							for (int k = 0; k < headNodesAdded.getLength(); k++) {
								Node headNodeAdded = headNodesAdded.item(k);
								String metaValueAdded = EpubUtils.getMetaNodeValue(headNodeAdded);

								if (headNodeAdded.isEqualNode(headNode))	exists = true;
								else if (!metaValue.equals("")	&& !metaValueAdded.equals("") && metaValue.equals(metaValueAdded)) exists = true;
							}

							// append head element
							if (!exists) headElementAdded.appendChild(headNode);
						}
					}

					if (htmlNode.getNodeName().equals("body")) {
						// create new section
						Element sectionElement = (Element) doc.createElement("section");

						// append body attributes to new section
						NamedNodeMap bodyAttributes = htmlNode.getAttributes();
						for (int j = 0; j < bodyAttributes.getLength(); j++) {
							Attr attribute = (Attr) bodyAttributes.item(j);
							sectionElement.setAttributeNS(attribute.getNamespaceURI(), attribute.getName(), attribute.getValue());
						}

						// append body elements
						NodeList bodyNodes = htmlNode.getChildNodes();
						for (int j = 0; j < bodyNodes.getLength(); j++) {
							sectionElement.appendChild(doc.importNode(bodyNodes.item(j),	true));
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
				String fileName = getAuthorAccess().getUtilAccess().getFileName(xhtmlUrl.toString());
				if (!EpubUtils.removeOpfItem(getAuthorAccess(), fileName)) {
					showMessage(EpubUtils.ERROR_MESSAGE);
					return;
				}

				// delete xhtml document
				getAuthorAccess().getWorkspaceAccess().delete(xhtmlUrl);
			}
			
			htmlElementAdded.appendChild(headElementAdded);
			htmlElementAdded.appendChild(bodyElementAdded);
			doc.appendChild(htmlElementAdded);
			
			// save new concatenated xhtml document
			if (!EpubUtils.saveDocument(getAuthorAccess(), doc, new URL(epubFilePath + "/" + newXhtmlFileName))) {
				showMessage(EpubUtils.ERROR_MESSAGE);
				return;
			}
			
			// add xhtml document to opf document
			if (!EpubUtils.addOpfItem(getAuthorAccess(), newXhtmlFileName)) {
				showMessage(EpubUtils.ERROR_MESSAGE);
				return;
			}

			// save opf
			getAuthorAccess().getEditorAccess().save();
			
			// add unique ids to missing elements
			if (!EpubUtils.addUniqueIds(getAuthorAccess(), new URL(epubFilePath + "/" + newXhtmlFileName))) {
				showMessage(EpubUtils.ERROR_MESSAGE);
				return;
			}
			
			// update navigation documents
			if (!EpubUtils.updateNavigationDocuments(getAuthorAccess())) {
				showMessage(EpubUtils.ERROR_MESSAGE);
				return;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			showMessage("Could not finalize operation - an error occurred: " + e.getMessage());
			return;
		}
	}
}
