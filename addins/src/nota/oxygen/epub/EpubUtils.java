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

	public static String XHTML_NS = "http://www.w3.org/1999/xhtml";
	public static String NCX_NS = "http://www.daisy.org/z3986/2005/ncx/";
	public static String EPUB_NS = "http://www.idpf.org/2007/opf";
}
