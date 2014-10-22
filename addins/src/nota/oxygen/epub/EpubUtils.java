package nota.oxygen.epub;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import nota.oxygen.common.Utils;

import org.w3c.dom.Element;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.access.AuthorWorkspaceAccess;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;

public class EpubUtils {
	
	private static Pattern EPUB_URL_REGEX = Pattern.compile("^(zip:file:[^!]+!/).+$");
	
	public static URL getEpubUrl(URL baseEpubUrl, String url)
	{
		Matcher m = EPUB_URL_REGEX.matcher(baseEpubUrl.toString());
		if (!m.matches()) return null;
		URL result;
		try {
			result = new URL(m.group(1));
			result = new URL(result, url);
			return result;
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
			if (!wa.open(docUrl)) return null;
			editor = wa.getEditorAccess(docUrl);
			if (editor == null) return null;
		}
		if (editor.getCurrentPageID() != WSEditor.PAGE_AUTHOR) editor.changePage(WSEditor.PAGE_AUTHOR);
		WSEditorPage wep = editor.getCurrentPage();
		WSAuthorEditorPage aea = (wep instanceof WSAuthorEditorPage ? (WSAuthorEditorPage)wep : null);
		if (aea == null) return null;
		return aea.getAuthorAccess();
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

	public static AuthorAccess getPackageItemDocumentBySPath(AuthorAccess opfAccess, String xpath) {
		try {
			AuthorNode[] res = opfAccess.getDocumentController().findNodesByXPath(xpath, true, true, true);
			if (res.length > 0)
			{
				AttrValue itemHref = ((AuthorElement)res[0]).getAttribute("href");
				if (itemHref != null)
				{
					return getAuthorDocument(
							opfAccess, 
							new URL(opfAccess.getEditorAccess().getEditorLocation(), itemHref.getValue()));
				}
			}
		}
		catch (Exception e) {
			return null;
		}
		return null;
	}

	public static AuthorAccess getXHTMLNavDocument(AuthorAccess opfAccess) {
		return getPackageItemDocumentBySPath(opfAccess, "//item[@media-type='application/xhtml+xml' and @properties='nav']");
	}

	public static  AuthorAccess getNCXDocument(AuthorAccess opfAccess) {
		return getPackageItemDocumentBySPath(opfAccess, "//item[@media-type='application/x-dtbncx+xml']");
	}
	
//	public static String getTOCName(String lang) {
//		switch (lang.toLowerCase()) {
//		case "da":
//		case "da-dk":
//			return "Indhold";
//		default:
//			return "Table of Contents";
//		}
//	}
//	
//	public static String getLOPName(String lang) {
//		switch (lang.toLowerCase()) {
//		case "da":
//		case "da-dk":
//			return "Sider";
//		default:
//			return "List of Pages";
//		}
//	}
//	
//	private static Element createNavLabel(String text, Document ncx) {
//		Element navLabelElement = ncx.createElementNS(NCX_NS, "navLabel");
//		Element textElement = ncx.createElementNS(NCX_NS, "text");
//		textElement.setTextContent(text);
//		navLabelElement.appendChild(textElement);
//		return navLabelElement;
//	}
//	
//	private static int addNavsToNcx(AuthorNode textNode, Element navMapContainer, Element pageList, int playOrder) {
//		playOrder++;
//		
//		return playOrder;
//	}
//	
//	public static Document generateNcx(AuthorAccess opfAccess) throws AuthorOperationException {
//		try {
//			AuthorDocumentController opfCtrl = opfAccess.getDocumentController();
//			URI opfUri = opfAccess.getEditorAccess().getEditorLocation().toURI();
//			Document ncx = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
//			Element root = ncx.createElementNS(NCX_NS, "ncx");
//			AuthorElement opfRoot = opfCtrl.getAuthorDocumentNode().getRootElement();
//			if (opfRoot == null) {
//				throw new AuthorOperationException("Package file has no root element");
//			}
//			AttrValue val = opfRoot.getAttribute("xml:lang");
//			String lang = (val == null ? "" : val.getValue().trim());
//			root.setAttribute("version", "2005-1");
//			if (lang.length() > 0) root.setAttribute("xml:lang", lang);
//			Element navMap = ncx.createElementNS(NCX_NS, "navMap");
//			navMap.appendChild(createNavLabel(getTOCName(lang), ncx));
//			Element pageList = ncx.createElementNS(NCX_NS, "pageList");
//			pageList.appendChild(createNavLabel(getLOPName(lang), ncx));
//			int playOrder = 0;
//			int pageCount = 0;
//			int maxPageNumber = 0;
//			Element lastFileNavPoint = null;
//			for (AuthorNode node : opfCtrl.findNodesByXPath("/package/spine/itemref[not(@linear='no']", true, true, true)) {
//				val = ((AuthorElement)node).getAttribute("idref");
//				String idref = (val == null) ? "" : val.getValue();
//				String xpath = String.format(
//						"/package/manifest/item[@media-type='application/xhtml+xml' and @id='%s']", 
//						StringEscapeUtils.escapeXml10(idref));
//				AuthorAccess textAccess = getPackageItemDocumentBySPath(opfAccess, xpath);
//
//				URI relUri = textAccess.getEditorAccess().getEditorLocation().toURI().relativize(opfUri);
//				AuthorDocumentController textCtrl = textAccess.getDocumentController();
//				if (textAccess == null) continue;
//				String fileTitle = null;
//				for (AuthorNode h : textCtrl.findNodesByXPath("/html/body/(h1|h2|h3|h4|h5|h6)", true, true, true)) {
//					fileTitle = h.getTextContent();
//				}
//				if (fileTitle != null) {
//					
//				}
//				else {
//					for (AuthorNode targetNode : textAccess.getDocumentController().findNodesByXPath("//section)|//*[@epub:type='pagebreak']", true, true, true)) {
//						playOrder++;
//						AuthorElement target = (Element)targetNode;
//						
//						if ()
//					}
//				}
//			}
//			ncx.appendChild(root);
//			return ncx; 
//		} catch (Exception e) {
//			throw new AuthorOperationException(
//					String.format("Could not generate ncx Document due to an unexpected %s: %s", e.getClass().getName(), e.getMessage()), 
//					e);
//		}
//		
//	}

	public static String XHTML_NS = "http://www.w3.org/1999/xhtml";
	public static String NCX_NS = "http://www.daisy.org/z3986/2005/ncx/";
}
