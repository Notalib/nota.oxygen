package nota.oxygen.epub.headings;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;

import nota.oxygen.common.BaseAuthorOperation;
import nota.oxygen.common.Utils;
import nota.oxygen.epub.EpubUtils;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class UpdateNavigationDocumentsOperation extends BaseAuthorOperation {
	private AuthorAccess authorAccess;
	private AuthorAccess opfAccess;
	private Document ncx;
	private Document xhtmlNav;
	private List<NavItem> pageItems;
	private List<TocItem> topLevelTocItems;
	private int playOrder;

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[]{};
	}

	@Override
	public String getDescription() {
		return "Updates the navigation documents of a ePub 3 document (ncx and xhtml nav documents)";
	}

	@Override
	public void doOperation() throws AuthorOperationException {
		if (getAuthorAccess() == null) {
			opfAccess = authorAccess;
		}
		else {
			authorAccess = getAuthorAccess();
			
			URL opfUrl = EpubUtils.getPackageUrl(authorAccess);
			if (opfUrl == null) {
				showMessage("Could not find pagkage file for document");
				return;
			}
			opfAccess = EpubUtils.getAuthorDocument(getAuthorAccess(), opfUrl);
			if (opfAccess == null) {
				showMessage("Could not access pagkage file for document");
			}
		}
		
		AuthorAccess ncxAccess = EpubUtils.getNCXDocument(opfAccess);
		ncx = Utils.getDOMDocument(ncxAccess);
		AuthorAccess xhtmlNavAccess = EpubUtils.getXHTMLNavDocument(opfAccess);
		xhtmlNav = Utils.getDOMDocument(xhtmlNavAccess);
		pageItems = new ArrayList<NavItem>();
		topLevelTocItems = new ArrayList<TocItem>();
		playOrder = 0;
		for (AuthorAccess textDocAccess : EpubUtils.getSpine(opfAccess, false)) {
			topLevelTocItems.addAll(getTocItems(textDocAccess, opfAccess));
		}
		foldTocItemsByPartAndChapter();
		updateNcx();
		updateXhtmlNav();
		ncxAccess.getDocumentController().beginCompoundEdit();
		try {
			Utils.replaceRoot(ncx, ncxAccess);
			xhtmlNavAccess.getDocumentController().beginCompoundEdit();
			try {
				Utils.replaceRoot(xhtmlNav, xhtmlNavAccess);
			}
			catch (AuthorOperationException e) {
				xhtmlNavAccess.getDocumentController().cancelCompoundEdit();
				throw e;
			}
		}
		catch (AuthorOperationException e) {
			ncxAccess.getDocumentController().cancelCompoundEdit();
			throw e;
		}
		ncxAccess.getDocumentController().endCompoundEdit();
		xhtmlNavAccess.getDocumentController().endCompoundEdit();
		
		Utils.bringFocusToDocumentTab(authorAccess);
	}
	
	private Element createSkeletonNcxRootElement() throws AuthorOperationException {
		Element ncxElement = ncx.createElementNS(EpubUtils.NCX_NS, "ncx");
		ncxElement.setAttribute("version", "2005-1");
		ncxElement.appendChild(ncx.createElementNS(EpubUtils.NCX_NS, "head"));
		Element docTitleElement = ncx.createElementNS(EpubUtils.NCX_NS, "docTitle");
		for (AuthorNode node : opfAccess.getDocumentController().findNodesByXPath("/package/metadata/dc:title", true, true, true)) {
			try {
				docTitleElement.setTextContent(node.getTextContent());
				break;
			} catch (DOMException e) {
				continue;
			} catch (BadLocationException e) {
				continue;
			}
		}
		ncxElement.appendChild(docTitleElement);
		ncxElement.appendChild(ncx.createElementNS(EpubUtils.NCX_NS, "head"));
		return ncxElement;
	}
	
	private void updateNcx() throws AuthorOperationException {
		if (ncx == null) return;
		Element ncxElement = ncx.getDocumentElement();
		if (ncxElement == null) {
			ncxElement = createSkeletonNcxRootElement();
			ncx.appendChild(ncxElement);
		}
		Element pageListElement = Utils.getChildElementByNameNS(ncxElement, EpubUtils.NCX_NS, "pageList");
		if (pageListElement == null) {
			pageListElement = ncx.createElementNS(EpubUtils.NCX_NS, "pageList");
			pageListElement.appendChild(createNavLabel("List of Pages"));
			Element navListElement = Utils.getChildElementByNameNS(ncxElement, EpubUtils.NCX_NS, "navList");
			if (navListElement == null) {
				ncxElement.appendChild(pageListElement);
			}
			else {
				ncxElement.insertBefore(pageListElement, navListElement);
			}
		}
		Element navMapElement = Utils.getChildElementByNameNS(ncxElement, EpubUtils.NCX_NS, "navMap");
		if (navMapElement == null) {
			navMapElement = ncx.createElementNS(EpubUtils.NCX_NS, "navMap");
			navMapElement.appendChild(createNavLabel("Table of Content"));
			ncxElement.insertBefore(navMapElement, pageListElement);
		}
		//Assertion: ncx is at minimum a skeleton ncx with navMap and pageList
		for (Element navPoint : Utils.getChildElementsByNameNS(navMapElement, EpubUtils.NCX_NS, "navPoint")) {
			navMapElement.removeChild(navPoint);
		}
		int depth = 0;
		for (int i = 0; i < topLevelTocItems.size(); i++) {
			TocItem tocItem = topLevelTocItems.get(i);
			navMapElement.appendChild(tocItem.getAsNcxNavPoint());
			if (tocItem.getDepth() > depth) {
				depth = tocItem.getDepth();
			}
		}
		int maxPageNormal = 0;
		if (pageItems.size() > 0) {
			for (Element pageTarget : Utils.getChildElementsByNameNS(pageListElement, EpubUtils.NCX_NS, "pageTarget")) {
				pageListElement.removeChild(pageTarget);
			}
			for (int i = 0; i < pageItems.size(); i++) {
				NavItem pageItem = pageItems.get(i);
				pageListElement.appendChild(pageItem.getAsNcxPageTarget());
				if (pageItem.isOfClass("page-normal") && maxPageNormal < pageItem.getTextAsInteger()) {
					maxPageNormal = pageItem.getTextAsInteger();
				}
			}
		}
		else {
			ncxElement.removeChild(pageListElement);
		}
		Element headElement = Utils.getChildElementByNameNS(ncxElement, EpubUtils.NCX_NS, "head");
		if (headElement == null) {
			headElement = ncx.createElementNS(EpubUtils.NCX_NS, "head");
			ncxElement.insertBefore(headElement, ncxElement.getFirstChild());
		}
		Element depthMetaElement = null;
		Element totalPageCountMetaElement = null;
		Element maxPageNumberMetaElement = null;
		for (Element meta : Utils.getChildElementsByNameNS(headElement, EpubUtils.NCX_NS, "meta")) {
			switch (meta.getAttribute("name")) {
			case "dtb:depth":
				depthMetaElement = meta;
				break;
			case "dtb:totalPageCount":
				totalPageCountMetaElement = meta;
				break;
			case "dtb:maxPageNumber":
				maxPageNumberMetaElement = meta;
				break;
			}
		}
		if (depthMetaElement == null) {
			depthMetaElement = ncx.createElementNS(EpubUtils.NCX_NS, "meta");
			depthMetaElement.setAttribute("name", "dtb:depth");
			headElement.appendChild(depthMetaElement);
		}
		depthMetaElement.setAttribute("content", String.format("%d", depth));
		if (totalPageCountMetaElement == null) {
			totalPageCountMetaElement = ncx.createElementNS(EpubUtils.NCX_NS, "meta");
			totalPageCountMetaElement.setAttribute("name", "dtb:totalPageCount");
			headElement.appendChild(totalPageCountMetaElement);
		}
		totalPageCountMetaElement.setAttribute("content", String.format("%d", pageItems.size()));
		if (maxPageNumberMetaElement == null) {
			maxPageNumberMetaElement = ncx.createElementNS(EpubUtils.NCX_NS, "meta");
			maxPageNumberMetaElement.setAttribute("name", "dtb:maxPageNumber");
			headElement.appendChild(maxPageNumberMetaElement);
		}
		maxPageNumberMetaElement.setAttribute("content", String.format("%d", maxPageNormal));		
	}
	
	private void updateXhtmlNav() {
		if (xhtmlNav == null) return;
		Element htmlElement = xhtmlNav.getDocumentElement();
		if (htmlElement == null) {
			htmlElement = xhtmlNav.createElementNS(EpubUtils.XHTML_NS, "html");
			htmlElement.appendChild(xhtmlNav.createElementNS(EpubUtils.XHTML_NS, "head"));
			xhtmlNav.appendChild(htmlElement);
		}
		if (htmlElement.getAttribute("xmlns:epub")==null) {
			htmlElement.setAttribute("xmlns:epub", EpubUtils.EPUB_NS);
		}
		Element bodyElement = Utils.getChildElementByNameNS(htmlElement, EpubUtils.XHTML_NS, "body");
		if (bodyElement==null) {
			bodyElement = xhtmlNav.createElementNS(EpubUtils.XHTML_NS, "body");
			bodyElement.setAttributeNS(EpubUtils.EPUB_NS, "epub:type", "frontmatter");
			htmlElement.appendChild(bodyElement);
		}
		Element tocNav = null;
		Element pageListNav = null;
		for (Element nav : Utils.getChildElementsByNameNS(bodyElement, EpubUtils.XHTML_NS, "nav")) {
			for (String type : nav.getAttributeNS(EpubUtils.EPUB_NS, "type").split("\\s+")) {
				switch (type) {
				case "toc":
					tocNav = nav;
					break;
				case "page-list":
					pageListNav = nav;
					break;
				}
			}
		}
		if (tocNav == null) {
			tocNav = xhtmlNav.createElementNS(EpubUtils.XHTML_NS, "nav");
			tocNav.setAttributeNS(EpubUtils.EPUB_NS, "epub:type", "toc");
			Element h1 = xhtmlNav.createElementNS(EpubUtils.XHTML_NS, "h1");
			h1.setTextContent("Table of Contents");
			tocNav.appendChild(h1);
			bodyElement.appendChild(tocNav);
		}
		for (Element tocOl : Utils.getChildElementsByNameNS(tocNav, EpubUtils.XHTML_NS, "ol")) {
			tocNav.removeChild(tocOl);
		}
		Element tocOl = xhtmlNav.createElementNS(EpubUtils.XHTML_NS, "ol");
		for (int i = 0; i < topLevelTocItems.size(); i++) {
			tocOl.appendChild(topLevelTocItems.get(i).getAsXhtmlListItem());
		}
		tocNav.appendChild(tocOl);
		if (pageListNav == null) {
			pageListNav = xhtmlNav.createElementNS(EpubUtils.XHTML_NS, "nav");
			pageListNav.setAttributeNS(EpubUtils.EPUB_NS, "epub:type", "page-list");
			Element h1 = xhtmlNav.createElementNS(EpubUtils.XHTML_NS, "h1");
			h1.setTextContent("List of Pages");
			pageListNav.appendChild(h1);
			bodyElement.appendChild(pageListNav);
		}		
		for (Element pageListOl : Utils.getChildElementsByNameNS(pageListNav, EpubUtils.XHTML_NS, "ol")) {
			pageListNav.removeChild(pageListOl);
		}
		Element pageListOl = xhtmlNav.createElementNS(EpubUtils.XHTML_NS, "ol");
		for (int i = 0; i < pageItems.size(); i++) {
			pageListOl.appendChild(pageItems.get(i).getAsXhtmlListItem());
		}
		pageListNav.appendChild(pageListOl);
	}
	
	private void foldTocItemsByPartAndChapter() {
		int i = 1;
		while (i < topLevelTocItems.size()) {
			if (topLevelTocItems.get(i-1).isOfType("part") && topLevelTocItems.get(i).isOfType("chapter")) {
				topLevelTocItems.get(i-1).childItems.add(topLevelTocItems.remove(i));
				continue;
			}
			i++;
		}
	}
	
	private List<TocItem> getTocItems(AuthorAccess textDocAccess, AuthorAccess opfAccess) 
			throws AuthorOperationException {
		try {
			URI textDocUri = URI.create(
					authorAccess.getUtilAccess().makeRelative(
							opfAccess.getEditorAccess().getEditorLocation(), 
							textDocAccess.getEditorAccess().getEditorLocation()));
//			URI textDocUri = URI.create(Utils.relativizeURI(
//					opfAccess.getEditorAccess().getEditorLocation().toString(), 
//					textDocAccess.getEditorAccess().getEditorLocation().toString()));
			List<TocItem> res = new ArrayList<TocItem>();
			AuthorElement htmlElem = textDocAccess.getDocumentController().getAuthorDocumentNode().getRootElement();
			if (htmlElem != null) {
				AuthorElement bodyElem = null;
				for (AuthorNode node : htmlElem.getElementsByLocalName("body")) {
					bodyElem = (AuthorElement)node;
					break;
				}
				if (bodyElem != null) {
					//REMARK: if body element has a epub:type attribute it will act as a section element,
					//        otherwise the body element is assumed to be a container for section elements
					if (bodyElem.getAttribute("epub:type") != null) {
						res.add(getTocItem(bodyElem, textDocAccess.getDocumentController(), textDocUri));
					}
					else {
						AuthorElement[] sectionElements = bodyElem.getElementsByLocalName("section");
						for (int i = 0; i < sectionElements.length; i++) {
							res.add(getTocItem(sectionElements[i], textDocAccess.getDocumentController(), textDocUri));
						}
					}
				}
			}
			return res;
		}
		catch (AuthorOperationException e) {
			throw e;
		}
		catch (Exception e) {
			throw new AuthorOperationException(
					String.format(
							"An unexpected %s occured while getting toc items: %s", 
							e.getClass().getName(), e.getMessage()),
					e);
		}
		  
	}
	
	private TocItem getTocItem(AuthorElement sectionElement, AuthorDocumentController textDocCtrl, URI textDocUri) 
			throws AuthorOperationException {
		playOrder++;
		TocItem item = new TocItem();
		item.epubType = (sectionElement.getAttribute("epub:type")!=null) ? sectionElement.getAttribute("epub:type").getValue() : "";
		item.order = playOrder;
		item.text = "***";
		if (sectionElement.getAttribute("id") == null && !"body".equals(sectionElement.getLocalName())) {
			textDocCtrl.getUniqueAttributesProcessor().assignUniqueIDs(sectionElement.getStartOffset()-1, sectionElement.getEndOffset()+1, true);
		}
		item.targetUri = getTargetUri(sectionElement, textDocUri);
		for (AuthorNode node : textDocCtrl.findNodesByXPath("h1|h2|h3|h4|h5|h6", sectionElement, true, true, true, true)) {
			AuthorElement hx = (AuthorElement)node;
			try {
				item.text = hx.getTextContent();
			} catch (BadLocationException e) {
				continue;
			}
			break;
		}
		for (AuthorNode child : sectionElement.getContentNodes()) {
			if (child instanceof AuthorElement) {
				AuthorElement elem = (AuthorElement)child;
				if (elem.getLocalName().equals("section")) {
					item.childItems.add(getTocItem(elem, textDocCtrl, textDocUri));
				}
				else
				{
					addPageItems(elem, textDocUri);
				}
			}
		}
		return item;
	}
	
	private URI getTargetUri(AuthorElement elem, URI textDocUri) {
		URI res = textDocUri;
		AttrValue idVal = elem.getAttribute("id");
		if (idVal != null) {
			res = res.resolve(String.format("#%s", idVal.getValue()));
		}
		return res;
	}
	
	private void addPageItems(AuthorElement elem, URI textDocUri) {
		NavItem pageItem = new NavItem();
		pageItem.epubType = (elem.getAttribute("epub:type")!=null) ? elem.getAttribute("epub:type").getValue() : "";
		if (pageItem.isOfType("pagebreak")) {
			playOrder++;
			pageItem.order = playOrder;
			pageItem.text = (elem.getAttribute("title")!=null) ? elem.getAttribute("title").getValue() : "";
			pageItem.classValue = (elem.getAttribute("class")!=null) ? elem.getAttribute("class").getValue() : "";
			pageItem.targetUri = getTargetUri(elem, textDocUri);
			pageItems.add(pageItem);
		}
		else {
			for (AuthorNode node : elem.getContentNodes()) {
				if (node instanceof AuthorElement) addPageItems((AuthorElement)node, textDocUri);
			}
		}
	}
	

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		// Nothing to parse!!!
	}
	
	private Element createNavLabel(String text) {
		Element navLabelElement = ncx.createElementNS(EpubUtils.NCX_NS, "navLabel");
		Element textElement = ncx.createElementNS(EpubUtils.NCX_NS, "text");
		textElement.setTextContent(text);
		navLabelElement.appendChild(textElement);
		return navLabelElement;
	}

	public void setAuthorAccess(AuthorAccess authorAccess) {
		this.authorAccess = authorAccess;
	}
	
	public class NavItem {
		public URI targetUri;
		public String text;
		public int order;
		public String epubType;
		public String classValue;
		
		public boolean isOfType(String type) {
			if (epubType == null) return false;
			for (String t : epubType.split("\\s+")) {
				if (t.equals(type)) return true;
			}
			return false;
		}
		
		public boolean isOfClass(String cls) {
			if (classValue == null) return false;
			for (String t : classValue.split("\\s+")) {
				if (t.equals(cls)) return true;
			}
			return false;
		}
		
		public int getTextAsInteger() {
			try {
				int res = Integer.parseInt(text);
				if (res > 0) {
					return res;
				}
			}
			catch (NumberFormatException e) {
				//Do nothing, just return 0 at the end
			}
			return 0;
		}
		
		protected Element getContent() {
			Element content = ncx.createElementNS(EpubUtils.NCX_NS, "content");
			content.setAttribute("src", targetUri.toString());
			return content;
		}
		
		public Element getAsNcxPageTarget() {
			Element pageTarget = ncx.createElementNS(EpubUtils.NCX_NS, "pageTarget");
			pageTarget.setAttribute("id", String.format("pageTarget-%d", order));
			pageTarget.setAttribute("playOrder", String.format("%d", order));
			pageTarget.setAttribute("type", isOfClass("page-normal") ? "normal" : "special"); 
			pageTarget.appendChild(createNavLabel(text));
			pageTarget.appendChild(getContent());
			return pageTarget;
		}
		
		public Element getAsXhtmlListItem() {
			Element li = xhtmlNav.createElementNS(EpubUtils.XHTML_NS, "li");
			Element a = xhtmlNav.createElementNS(EpubUtils.XHTML_NS, "a");
			a.setAttribute("href", targetUri.toString());
			a.setTextContent(text);
			li.appendChild(a);
			return li;
		}
	}
	
	public class TocItem extends NavItem {
		public List<TocItem> childItems = new ArrayList<TocItem>();
		
		public int getDepth() {
			int depth = 1;
			for (int i = 0; i < childItems.size(); i++) {
				if (depth < childItems.get(i).getDepth()+1) {
					depth = childItems.get(i).getDepth()+1;
				}
			}
			return depth;
		}
		
		public Element getAsNcxNavPoint() {
			Element navPoint = ncx.createElementNS(EpubUtils.NCX_NS, "navPoint");
			navPoint.setAttribute("id", String.format("navPoint-%d", order));
			navPoint.setAttribute("playOrder", String.format("%d", order));
			navPoint.appendChild(createNavLabel(text));
			navPoint.appendChild(getContent());
			for (TocItem child : childItems) {
				navPoint.appendChild(child.getAsNcxNavPoint());
			}
			return navPoint;
		}

		@Override
		public Element getAsXhtmlListItem() {
			Element li = super.getAsXhtmlListItem();
			if (childItems.size() > 0) {
				Element ol = xhtmlNav.createElementNS(EpubUtils.XHTML_NS, "ol");
				for (int i = 0; i < childItems.size(); i++) {
					ol.appendChild(childItems.get(i).getAsXhtmlListItem());
				}
				li.appendChild(ol);
			}
			return li;
		}
		
		
	}
}
