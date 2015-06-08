package nota.oxygen.epub.notes;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import nota.oxygen.common.BaseAuthorOperation;
import nota.oxygen.common.Utils;
import nota.oxygen.epub.EpubUtils;
import nota.oxygen.epub.Splitter;

public class InsertNoteRefOperation extends BaseAuthorOperation {
	private String epub;
	private String epubFolder;
	private String fileName;
	
	private AuthorAccess opfAccess;
	private URL[] xhtmlUrls;
	private List<String> xhtmlFileNames;
	private List<Document> xhtmlDocs;
	
	private static String ARG_HEADER_FRAGMENT = "header fragment";
	private String headerFragment;
	
	private static String ARG_SINGLE_NOTE = "single note";
	private static String[] YES_NO = new String[] {"yes", "no"};
	private boolean singleNote;
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] { 
				new ArgumentDescriptor(ARG_HEADER_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Header fragment"),
				new ArgumentDescriptor(ARG_SINGLE_NOTE, ArgumentDescriptor.TYPE_CONSTANT_LIST, "Single note", YES_NO, YES_NO[1])
		};
	}
	
	@Override
	protected void parseArguments(ArgumentsMap args) throws IllegalArgumentException {
		headerFragment = (String)args.getArgumentValue(ARG_HEADER_FRAGMENT);
		String temp = (String)args.getArgumentValue(ARG_SINGLE_NOTE);
		singleNote = YES_NO[0].equals(temp);
	}
	
	@Override
	public String getDescription() {
		return "Insert noteref(s)";
	}
	
	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			// get epub zip path
			/*epub = Utils.getZipPath(getAuthorAccess().getEditorAccess().getEditorLocation().toString());
			if (epub == null || epub.equals("")) {
				showMessage("Could not find epub zip path");
				return;
			}

			// get epub folder
			epubFolder = EpubUtils.getEpubFolder(getAuthorAccess());
			if (epubFolder == null || epubFolder.equals("")) {
				showMessage("Could not find epub folder");
				return;
			}
			
			// find document filename
			fileName = getAuthorAccess().getUtilAccess().getFileName(getAuthorAccess().getEditorAccess().getEditorLocation().getFile().toString());
			if (fileName == null || fileName.equals("")) {
				showMessage("Could not find document filename");
				return;
			}
						
			NoteRefInserter.main(new String[] { epub, epubFolder, fileName });*/
			
			
			
			
			
			
			// find document filename
			fileName = getAuthorAccess().getUtilAccess().getFileName(getAuthorAccess().getEditorAccess().getEditorLocation().getFile().toString());
			if (fileName == null || fileName.equals("")) {
				showMessage("Could not find document filename");
				return;
			}
			
			// find package file for document
			URL opfUrl = EpubUtils.getPackageUrl(getAuthorAccess());
			if (opfUrl == null) {
				showMessage("Could not find pagkage file for document");
				return;
			}

			// access package file for document
			opfAccess = EpubUtils.getAuthorDocument(getAuthorAccess(), opfUrl);
			if (opfAccess == null) {
				showMessage("Could not access pagkage file for document");
				return;
			}
			
			// get all xhtml files in epub (besides nav.html)
			xhtmlUrls = EpubUtils.getSpineUrls(opfAccess, true);
			if (xhtmlUrls.length < 1) {
				showMessage("Cannot insert noteref(s) (no xhtml files)");
				return;
			}
			
			// read xhtml files
			createDocs();
			
			// close package file for document
			opfAccess.getEditorAccess().close(true);
			
			// add noteref(s)
			if (singleNote) addSingleNoteRef();
			else addMultiNoteRefs();
		} catch (AuthorOperationException e) {
			throw e;
		} catch (Exception e) {
			throw new AuthorOperationException(String.format("An unexpected %s occured: %s", e.getClass().getName(), e.getMessage()), e);
		}
	}
	
	public void createDocs() throws AuthorOperationException, ParserConfigurationException {
		xhtmlDocs = new ArrayList<Document>();
		xhtmlFileNames = new ArrayList<String>();
		
		// traverse each xhtml document in epub
		for (URL xhtmlUrl : xhtmlUrls) {
			// get filename
			String xhtmlFileName = getAuthorAccess().getUtilAccess().getFileName(xhtmlUrl.toString());
			if (xhtmlFileName.equals("nav.xhtml") || 
				!xhtmlFileName.substring(xhtmlFileName.lastIndexOf(".")).equals(".xhtml") || 
				xhtmlFileName.substring(xhtmlFileName.lastIndexOf("-")).equals("-footnotes.xhtml") ||
				xhtmlFileName.substring(xhtmlFileName.lastIndexOf("-")).equals("-rearnotes.xhtml")) {
				continue;
			}

			// get access to document
			AuthorAccess xhtmlAccess = EpubUtils.getAuthorDocument(opfAccess, xhtmlUrl);
			if (xhtmlAccess == null) {
				showMessage("Could not access xhtml document " + xhtmlFileName);
				return;
			}
			
			// add missing id's to document (ex. noterefs)
			if (!EpubUtils.addUniqueIds(xhtmlAccess)) {
				showMessage(EpubUtils.ERROR_MESSAGE);
				return;
			}

			// extract html from each document
			WSTextEditorPage editorPage = EpubUtils.getTextDocument(getAuthorAccess(), xhtmlUrl);
			JTextArea textArea = (JTextArea) ((WSTextEditorPage) editorPage).getTextComponent();
			String html = textArea.getText();
			if (html == null) {
				showMessage("Could not extract html from xhtml document " + xhtmlFileName);
				return;
			}

			// deserialize html
			Node htmlNode = Utils.deserializeElement(html);

			// create new document and import html
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(htmlNode, true);
			doc.appendChild(importedNode);

			xhtmlDocs.add(doc);
			xhtmlFileNames.add(xhtmlFileName);
		}
	}
	
	private void addSingleNoteRef() throws AuthorOperationException {
		AuthorElement currentElement, parentElement, listElement;
		try {
			// get current and parent element
			currentElement = getCurrentElement();
			parentElement = (AuthorElement) getCurrentElement().getParent();
		} catch (BadLocationException e) {
			showMessage("No element is selected");
			return;
		}

		// find li element
		if (currentElement.getName().equals("li")) {
			listElement = currentElement;
		} else if (parentElement.getName().equals("li")) {
			listElement = parentElement;
		} else {
			showMessage("Noteref can not be inserted here");
			return;
		}

		// check if noteref is not already inserted
		if (!currentElement.getName().equals("a") && currentElement.getElementsByLocalName("a").length == 0) {
			addNoteRef(listElement, currentElement);
		}
	}
	
	private void addMultiNoteRefs() throws AuthorOperationException {
		AuthorNode[] notes = getAuthorAccess().getDocumentController().findNodesByXPath("/html/body/ol/li", true, true, true);
		if (notes == null) {
			throw new AuthorOperationException("Found no notes in document");
		}
		
		for (AuthorNode note : notes) {
			AuthorElement listElement = ((AuthorElement) note);
			
			// find current element
			AuthorElement currentElement;
			if (listElement.getElementsByLocalName("p").length == 0) {
				currentElement = listElement;
			} else if (listElement.getElementsByLocalName("p").length == 1) {
				currentElement = listElement.getElementsByLocalName("p")[0];
			} else {
				currentElement = listElement.getElementsByLocalName("p")[listElement.getElementsByLocalName("p").length-1];
			}

			// check if noteref is not already inserted
			if (currentElement.getElementsByLocalName("a").length == 0) {
				addNoteRef(listElement, currentElement);
			}
		}
	}
	
	private void addNoteRef(AuthorElement listElement, AuthorElement currentElement) throws AuthorOperationException {
		// find id attribute
		AttrValue id = listElement.getAttribute("id");
		if (id == null) {
			showMessage("Could not find id attribute");
			return;
		}
		
		// find epub;type attribute
		AttrValue epubType = listElement.getAttribute("epub:type");
		if (epubType == null) {
			showMessage("Could not find epub:type attribute");
			return;
		}
		
		// check if epub:type attribute is footnote or rearnote
		if (!epubType.getRawValue().equals("footnote") && !epubType.getRawValue().equals("rearnote")) {
			showMessage("epub:type should be either footnote or rearnote, noteref can not be inserted here");
			return;
		}
		
		// traverse each xhtml document in epub
		for (int docIdx = 0; docIdx < xhtmlDocs.size(); docIdx++) {
			// iterate through links
			NodeList links = xhtmlDocs.get(docIdx).getElementsByTagName("a");
			for (int i = 0; i < links.getLength(); i++) {
				Node linkNode = links.item(i);
				NamedNodeMap attrs = linkNode.getAttributes();

				String epubTypeAttr = "", hrefAttr = "", idAttr = "";
				for (int j = 0; j < attrs.getLength(); j++) {
					Attr attr = (Attr) attrs.item(j);
					if (attr.getName().equals("epub:type")) epubTypeAttr = attr.getValue();
					if (attr.getName().equals("href")) hrefAttr = attr.getValue();
					if (attr.getName().equals("id")) idAttr = attr.getValue();
				}
				
				if (epubTypeAttr.equals("noteref")) {
					if (hrefAttr.lastIndexOf("#") + 1 > 0) {
						String tes = id.getRawValue();
						if (hrefAttr.substring(hrefAttr.lastIndexOf("#") + 1).equals(id.getRawValue())) {
							String headerFragmentCopy = headerFragment;
							headerFragmentCopy = headerFragmentCopy.replace("$href", xhtmlFileNames.get(docIdx) + "#" + idAttr);
							AuthorDocumentController ctrl = getAuthorAccess().getDocumentController();
							try {
								ctrl.beginCompoundEdit();
								//ctrl.surroundInFragment(headerFragmentCopy, currentElement.getStartOffset()+1, currentElement.getEndOffset()-1);
								ctrl.insertXMLFragment(headerFragmentCopy, currentElement.getEndOffset());
								ctrl.endCompoundEdit();
							} catch (Exception e) {
								ctrl.cancelCompoundEdit();
								throw new AuthorOperationException(String.format("An unexpected %s occured: %s", e.getClass().getName(), e.getMessage()), e);
							}

							if (!singleNote) return;
						}
					} else {
						showMessage(String.format("Noteref in document %s with id %s does not have a correct reference %s", xhtmlFileNames.get(docIdx), idAttr, hrefAttr));
					}
				}
			}
		}
	}
}
