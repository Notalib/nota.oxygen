package nota.oxygen.epub;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import nota.oxygen.common.Utils;

import org.w3c.dom.Element;

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

	public static  AuthorAccess getNCXDocument(AuthorAccess opfAccess) {
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
	
	public static void removeOpfItem(AuthorAccess authorAccess, String fileName) throws Exception {
		URL opfUrl = EpubUtils.getPackageUrl(authorAccess);
		if (opfUrl == null) {
			//showMessage("Could not find pagkage file for document");
			return;
		}
		
		AuthorAccess opfAccess = EpubUtils.getAuthorDocument(authorAccess, opfUrl);
		if (opfAccess == null) {
			//showMessage("Could not access pagkage file for document");
			return;
		}
		
		AuthorDocumentController opfCtrl = opfAccess.getDocumentController();
		opfCtrl.beginCompoundEdit();
		
		try {
			AuthorElement manifest = getFirstElement(opfCtrl.findNodesByXPath("/package/manifest", true, true, true));
			if (manifest == null) {
				throw new AuthorOperationException("Found no manifest in package file");
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
			throw e;
		}
		
		opfCtrl.endCompoundEdit();
	}
	
	public static void addOpfItem(AuthorAccess authorAccess, String fileName) throws Exception {
		URL opfUrl = EpubUtils.getPackageUrl(authorAccess);
		if (opfUrl == null) {
			//showMessage("Could not find pagkage file for document");
			return;
		}
		
		AuthorAccess opfAccess = EpubUtils.getAuthorDocument(authorAccess, opfUrl);
		if (opfAccess == null) {
			//showMessage("Could not access pagkage file for document");
			return;
		}
		
		AuthorDocumentController opfCtrl = opfAccess.getDocumentController();
		opfCtrl.beginCompoundEdit();
		
		try {
			AuthorElement manifest = getFirstElement(opfCtrl.findNodesByXPath("/package/manifest", true, true, true));
			AuthorElement spine = getFirstElement(opfCtrl.findNodesByXPath("/package/spine", true, true, true));
			if (manifest == null) {
				throw new AuthorOperationException("Found no manifest in package file");
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
		}
		catch (Exception e) {
			opfCtrl.cancelCompoundEdit();
			throw e;
		}
		
		opfCtrl.endCompoundEdit();
	}
	
	public static String XHTML_NS = "http://www.w3.org/1999/xhtml";
	public static String NCX_NS = "http://www.daisy.org/z3986/2005/ncx/";
	public static String EPUB_NS = "http://www.idpf.org/2007/ops";
	public static String OPF_NS = "http://www.idpf.org/2007/opf";
}
