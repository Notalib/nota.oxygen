package nota.oxygen.epub;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import nota.oxygen.common.Utils;
import nota.oxygen.epub.headings.UpdateNavigationDocumentsOperation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorWorkspaceAccess;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;

public class EpubUtils {	
	public static URL getEpubUrl(URL baseEpubUrl, String url)
	{
		URL epubUrl = Utils.getZipRootUrl(baseEpubUrl);
		if (epubUrl == null) return null;
		try {
			return new URL(epubUrl, url);
		} catch (MalformedURLException e) {
			return null;
		}
		
	}
	
	public static AuthorAccess getAuthorDocument(AuthorAccess authorAccess, URL docUrl)
	{
		AuthorWorkspaceAccess wa = authorAccess.getWorkspaceAccess();
		WSEditor editor = wa.getEditorAccess(docUrl);
		if (editor == null)
		{
			if (!wa.open(docUrl, WSEditor.PAGE_AUTHOR)) return null;
			editor = wa.getEditorAccess(docUrl);
			if (editor == null) return null;
		}
		if (editor.getCurrentPageID() != WSEditor.PAGE_AUTHOR) editor.changePage(WSEditor.PAGE_AUTHOR);
		WSEditorPage wep = editor.getCurrentPage();
		WSAuthorEditorPage aea = (wep instanceof WSAuthorEditorPage ? (WSAuthorEditorPage)wep : null);
		if (aea == null) return null;
		return aea.getAuthorAccess();
	}
	
	public static WSTextEditorPage getTextDocument(AuthorAccess authorAccess, URL docUrl)
	{
		AuthorWorkspaceAccess wa = authorAccess.getWorkspaceAccess();
		WSEditor editor = wa.getEditorAccess(docUrl);
		if (editor == null)
		{
			if (!wa.open(docUrl, WSEditor.PAGE_TEXT)) return null;
			editor = wa.getEditorAccess(docUrl);
			if (editor == null) return null;
		}
		editor.close(true);
		if (editor.getCurrentPageID() != WSEditor.PAGE_TEXT) editor.changePage(WSEditor.PAGE_TEXT);
		WSEditorPage wep = editor.getCurrentPage();
		WSTextEditorPage editorPage = (wep instanceof WSTextEditorPage ? (WSTextEditorPage)wep : null);
		if (editorPage == null) return null;
		return editorPage;
	}
	
	public static URL getPackageUrl(AuthorAccess authorAccess) {
		try {
			if (authorAccess == null) return null;
			URL docUrl = authorAccess.getDocumentController().getAuthorDocumentNode().getXMLBaseURL(); 
			AuthorAccess containerDocAccess = getAuthorDocument(authorAccess, getEpubUrl(docUrl, "META-INF/container.xml"));
			if (containerDocAccess == null) return null;
			Element rootElem = Utils.deserializeElement(Utils.serialize(
					containerDocAccess, 
					containerDocAccess.getDocumentController().getAuthorDocumentNode()));
			containerDocAccess.getEditorAccess().close(true);
			XPath xp = Utils.getXPath("ns", "urn:oasis:names:tc:opendocument:xmlns:container");
			try {
				String relUrl = xp.evaluate("//ns:rootfile[@media-type='application/oebps-package+xml']/@full-path", rootElem); 
				return getEpubUrl(docUrl, relUrl);
			} catch (XPathExpressionException e) {
				return null;
			}
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public static String getEpubFolder(AuthorAccess authorAccess) {
		try {
			if (authorAccess == null) return null;
			URL docUrl = authorAccess.getDocumentController().getAuthorDocumentNode().getXMLBaseURL(); 
			AuthorAccess containerDocAccess = getAuthorDocument(authorAccess, getEpubUrl(docUrl, "META-INF/container.xml"));
			if (containerDocAccess == null) return null;
			Element rootElem = Utils.deserializeElement(Utils.serialize(
					containerDocAccess, 
					containerDocAccess.getDocumentController().getAuthorDocumentNode()));
			containerDocAccess.getEditorAccess().close(true);
			XPath xp = Utils.getXPath("ns", "urn:oasis:names:tc:opendocument:xmlns:container");
			try {
				URL rootUrl = Utils.getZipRootUrl(docUrl);
				String epubUrl = xp.evaluate("//ns:rootfile[@media-type='application/oebps-package+xml']/@full-path", rootElem); 
				return rootUrl.toString() + epubUrl.substring(0, epubUrl.indexOf("/"));
			} catch (XPathExpressionException e) {
				return "";
			}
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public static URL getPackageItemURLByXPath(AuthorAccess opfAccess, String xpath) {
		try {
			AuthorNode[] res = opfAccess.getDocumentController().findNodesByXPath(xpath, true, true, true);
			if (res.length > 0)
			{
				AttrValue itemHref = ((AuthorElement)res[0]).getAttribute("href");
				if (itemHref != null)
				{
					return new URL(opfAccess.getEditorAccess().getEditorLocation(), itemHref.getValue());
				}
			}
		}
		catch (Exception e) {
			return null;
		}
		return null;
	}

	public static AuthorAccess getPackageItemDocumentByXPath(AuthorAccess opfAccess, String xpath) {
		URL itemUrl = getPackageItemURLByXPath(opfAccess, xpath);
		if (itemUrl != null) {
			return getAuthorDocument(opfAccess, itemUrl);
		}
		return null;
	}

	public static AuthorAccess getXHTMLNavDocument(AuthorAccess opfAccess) {
		return getPackageItemDocumentByXPath(opfAccess, "//item[@media-type='application/xhtml+xml' and @properties='nav']");
	}

	public static AuthorAccess getNCXDocument(AuthorAccess opfAccess) {
		return getPackageItemDocumentByXPath(opfAccess, "//item[@media-type='application/x-dtbncx+xml']");
	}
	
	public static URL[] getSpineUrls(AuthorAccess opfAccess, boolean includeNonLinear) throws AuthorOperationException {
		String xpath = "/package/spine/itemref" + (includeNonLinear ? "" : "[not(@linear='no')]");
		AuthorNode[] itemrefs = opfAccess.getDocumentController().findNodesByXPath(xpath, true, true, true);
		List<URL> res = new ArrayList<URL>();
		for (int i = 0; i < itemrefs.length; i++) {
			AuthorElement itemref = (AuthorElement)itemrefs[i];
			AttrValue idref = itemref.getAttribute("idref");
			if (idref != null) {
				URL spineUrl = getPackageItemURLByXPath(
						opfAccess, 
						String.format("/package/manifest/item[@id='%s']", idref.getValue()));
				if (spineUrl != null) res.add(spineUrl);
			}
		}
		return res.toArray(new URL[res.size()]);
	}
	
	public static AuthorAccess[] getSpine(AuthorAccess opfAccess, boolean includeNonLinear) throws AuthorOperationException {
		List<AuthorAccess> res = new ArrayList<AuthorAccess>();
		for (URL spineUrl : getSpineUrls(opfAccess, includeNonLinear)) {
			AuthorAccess spineAccess = getAuthorDocument(opfAccess, spineUrl);
			if (spineAccess != null) res.add(spineAccess);
		}
		return res.toArray(new AuthorAccess[0]);
	}

	private static AuthorElement getFirstElement(AuthorNode[] nodes) {
		for (int i=0; i<nodes.length; i++) {
			if (nodes[i] instanceof AuthorElement) return (AuthorElement)nodes[i];
		}
		return null;
	}
	
	public static boolean removeOpfItem(AuthorAccess opfAccess, String fileName) {
		ERROR_MESSAGE = "";
		
		if (opfAccess == null) {
			ERROR_MESSAGE = "Could not access opf document";
			return false;
		}
		
		AuthorDocumentController opfCtrl = opfAccess.getDocumentController();
		opfCtrl.beginCompoundEdit();
		
		try {
			AuthorElement manifest = getFirstElement(opfCtrl.findNodesByXPath("/package/manifest", true, true, true));
			if (manifest == null) {
				ERROR_MESSAGE = "Found no manifest in package file";
				return false;
			}
			
			AuthorElement item = getFirstElement(opfCtrl.findNodesByXPath(String.format("/package/manifest/item[@href='%s']", fileName), true, true, true));
			if (item != null) {
				String idValue = item.getAttribute("id").getValue();
				opfCtrl.deleteNode(item);
				
				AuthorElement itemRef = getFirstElement(opfCtrl.findNodesByXPath(String.format("/package/spine/itemref[@idref='%s']", idValue), true, true, true));
				if (itemRef != null) {
					opfCtrl.deleteNode(itemRef);
				}
			}
		}
		catch (Exception e) {
			opfCtrl.cancelCompoundEdit();
			ERROR_MESSAGE = "Could not add item to opf document - an error occurred: " + e.getMessage();
			return false;
		}
		
		opfCtrl.endCompoundEdit();
		
		return true;
	}
	
	public static boolean addOpfItem(AuthorAccess opfAccess, String fileName) {
		ERROR_MESSAGE = "";
		
		if (opfAccess == null) {
			ERROR_MESSAGE = "Could not access opf document";
			return false;
		}
		
		AuthorDocumentController opfCtrl = opfAccess.getDocumentController();
		opfCtrl.beginCompoundEdit();
		
		try {
			AuthorElement manifest = getFirstElement(opfCtrl.findNodesByXPath("/package/manifest", true, true, true));
			AuthorElement spine = getFirstElement(opfCtrl.findNodesByXPath("/package/spine", true, true, true));
			if (manifest == null) {
				ERROR_MESSAGE = "Found no manifest in package file";
				return false;
			}
		
			AuthorElement item = getFirstElement(opfCtrl.findNodesByXPath(String.format("/package/manifest/item[@href='%s']", fileName), true, true, true));
			if (item == null) {
				String itemXml = "<item xmlns='" + EpubUtils.OPF_NS + "' media-type='application/xhtml+xml' href='" + fileName + "'/>";
				opfCtrl.insertXMLFragment(itemXml, manifest.getEndOffset());
				opfCtrl.getUniqueAttributesProcessor().assignUniqueIDs(manifest.getStartOffset(), manifest.getEndOffset(), true);
				
				item = getFirstElement(opfCtrl.findNodesByXPath(String.format("/package/manifest/item[@href='%s']", fileName), true, true, true));
				if (item != null) {
					String idValue = item.getAttribute("id").getValue();

					AuthorElement itemRef = getFirstElement(opfCtrl.findNodesByXPath(String.format("/package/spine/itemref[@idref='%s']", idValue), true, true, true));
					if (itemRef == null) {
						String itemRefXml = "<itemref xmlns='" + EpubUtils.OPF_NS + "' idref='" + idValue + "'/>";
						opfCtrl.insertXMLFragment(itemRefXml, spine.getEndOffset());
					}
				}
			}
		} catch (Exception e) {
			opfCtrl.cancelCompoundEdit();
			ERROR_MESSAGE = "Could not add item to opf document - an error occurred: " + e.getMessage();
			return false;
		}
		
		opfCtrl.endCompoundEdit();
		
		return true;
	}
	
	public static Document createDocument() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			return doc;
		} catch (ParserConfigurationException e) {
		}
		
		return null;
	}
	
	public static AuthorAccess saveDocument(AuthorAccess authorAccess, Document doc, URL file) {
		ERROR_MESSAGE = "";
		
		try {
			// remove all empty lines in document
			removeEmptyLines(doc);
			
			// create transformer
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			
			StringWriter writer = new StringWriter();
			writer.write("<?xml version='1.0' encoding='UTF-8'?>\n");
			writer.write("<!DOCTYPE html>\n");
			StreamResult result = new StreamResult(writer);
			
			// transform concatenated document to xml content string
			transformer.transform(new DOMSource(doc), result);
			
			// open new editor with concatenated xml content
			AuthorWorkspaceAccess awa = authorAccess.getWorkspaceAccess();
			URL newEditorUrl = awa.createNewEditor("xhtml", "text/xml", writer.toString());

			// save content in editor into new concatenated xhtml file
			WSEditor editor = awa.getEditorAccess(newEditorUrl);
			editor.saveAs(file);
			if (editor.getCurrentPageID() != WSEditor.PAGE_AUTHOR) editor.changePage(WSEditor.PAGE_AUTHOR);
			WSEditorPage wep = editor.getCurrentPage();
			WSAuthorEditorPage aea = (wep instanceof WSAuthorEditorPage ? (WSAuthorEditorPage)wep : null);
			if (aea == null) {
				ERROR_MESSAGE = "Could not get document";
				return null;
			}
			return aea.getAuthorAccess();
		} catch (TransformerException e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not save document - an error occurred: " + e.getMessage();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not save document - an error occurred: " + e.getMessage();
			return null;
		}
	}
	
	private static void removeEmptyLines(Document doc) {
		try {
			XPath xp = XPathFactory.newInstance().newXPath();
			NodeList nl = (NodeList) xp.evaluate("//text()[normalize-space(.)='']", doc, XPathConstants.NODESET);
			for (int i=0; i < nl.getLength(); ++i) {
			    Node node = nl.item(i);
			    node.getParentNode().removeChild(node);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean addUniqueIds(AuthorAccess xhtmlAccess) {
		ERROR_MESSAGE = "";
		
		try {
			AuthorElement rootElement = xhtmlAccess.getDocumentController().getAuthorDocumentNode().getRootElement();
			if (rootElement == null) {
				ERROR_MESSAGE = "Found no root in xhtml file";
				return false;
			}

			// insert unique ids for elements
			XHTMLUniqueAttributesRecognizer uniqueAttributesRecognizer = new XHTMLUniqueAttributesRecognizer();
			uniqueAttributesRecognizer.activated(xhtmlAccess);
			uniqueAttributesRecognizer.assignUniqueIDs(rootElement.getStartOffset(), rootElement.getEndOffset(), false);
			uniqueAttributesRecognizer.deactivated(xhtmlAccess);

			// save and close
			xhtmlAccess.getEditorAccess().save();
			xhtmlAccess.getEditorAccess().close(true);
		} catch (Exception e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not add ids to document - an error occurred: " + e.getMessage();
			return false;
		}
		
		return true;
	}
	
	public static boolean updateNavigationDocuments(AuthorAccess authorAccess) {
		ERROR_MESSAGE = "";
		
		try {
			UpdateNavigationDocumentsOperation navigationOperation = new UpdateNavigationDocumentsOperation();
			navigationOperation.setAuthorAccess(authorAccess);
			navigationOperation.doOperation();
		} catch (AuthorOperationException e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not update navigation documents - an error occurred: " + e.getMessage();
			return false;
		}
		
		return true;
	}
	
	public static String getMetaNodeValue(Node meta) {
		String value = "";
		if (meta.getNodeName().equalsIgnoreCase("meta")) {
			Node metaName = meta.getAttributes().getNamedItem("name");
			if (metaName != null) {
				value = String.valueOf(meta.getAttributes().getNamedItem("name").getNodeValue());
			}
		}
		return value;
	}
	
	public static String XHTML_NS = "http://www.w3.org/1999/xhtml";
	public static String NCX_NS = "http://www.daisy.org/z3986/2005/ncx/";
	public static String EPUB_NS = "http://www.idpf.org/2007/ops";
	public static String OPF_NS = "http://www.idpf.org/2007/opf";
	
	public static String ERROR_MESSAGE;
	
	public static String CONCAT_FILENAME = "concat.xhtml";
}
