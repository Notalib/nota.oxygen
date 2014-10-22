package nota.oxygen.epub.headings;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import nota.oxygen.common.BaseAuthorOperation;
import nota.oxygen.common.Utils;
import nota.oxygen.epub.EpubUtils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class InsertHeadingOperation extends BaseAuthorOperation {

	private static String ARG_HEADER_FRAGMENT = "header fragment";
	private String headerFragment;

	private static String ARG_PARENT_SECTION_XPATH = "parent section xpath";
	private String parentSectionXPath;
	
	private static String ARG_HEADING_OPERATION_TYPE = "heading operation type";
	private String headingOperationType;
	
	private static String SUBLEVEL_OPERATION_TYPE = "Sublevel";
	private static String SAME_LEVEL_OPERATION_TYPE = "Same level";
	
	private boolean isSubLevel() {
		return SUBLEVEL_OPERATION_TYPE.equals(headingOperationType);
	}
	
	private boolean isSameLevel() {
		return SAME_LEVEL_OPERATION_TYPE.equals(headingOperationType);
	}

	private static String ARG_PREVIOUS_HEADING_XPATH = "previous heading xpath";
	private String previousHeadingXPath;
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[]{
				new ArgumentDescriptor(ARG_HEADER_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Header fragment"),
				new ArgumentDescriptor(ARG_PARENT_SECTION_XPATH, ArgumentDescriptor.TYPE_XPATH_EXPRESSION, "Parent section XPath"),
				new ArgumentDescriptor(ARG_HEADING_OPERATION_TYPE, ArgumentDescriptor.TYPE_CONSTANT_LIST, "Heading operation type", new String[] {SAME_LEVEL_OPERATION_TYPE, SUBLEVEL_OPERATION_TYPE}, "Same level"),
				new ArgumentDescriptor(ARG_PREVIOUS_HEADING_XPATH, ArgumentDescriptor.TYPE_XPATH_EXPRESSION, "Previous heading XPath")
				};
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		headerFragment = (String)args.getArgumentValue(ARG_HEADER_FRAGMENT);
		parentSectionXPath = (String)args.getArgumentValue(ARG_PARENT_SECTION_XPATH);
		headingOperationType = (String)args.getArgumentValue(ARG_HEADING_OPERATION_TYPE);
		previousHeadingXPath = (String)args.getArgumentValue(ARG_PREVIOUS_HEADING_XPATH);

	}

	@Override
	public String getDescription() {
		return "Inserts a heading, with the side-effect of updating the ePub navigation documents";
	}
	
	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			AuthorElement headingCandidate;
			try {
				 headingCandidate = getCurrentElement();
			} catch (BadLocationException e) {
				showMessage("No element is selected");
				return;
			}
			AuthorElement parentSection = findElementByXPath(parentSectionXPath, headingCandidate);
			if (parentSection != headingCandidate.getParent()) {
				showMessage("The selected element is not a direct child of a section");
				return;
			}
			try {
				if (findElementByXPath(previousHeadingXPath) == null) {
					showMessage("The selected element is before the heading of it's parent section");
					return;
				}
				AuthorDocumentController ctrl = getAuthorAccess().getDocumentController();
				String headingText  = headingCandidate.getTextContent();
				ctrl.surroundInFragment(
						headerFragment, 
						headingCandidate.getStartOffset()+1, 
						headingCandidate.getEndOffset()-1);
				AuthorDocumentFragment headingFragment = ctrl.createDocumentFragment(headingCandidate.getContentNodes().get(0), true);
				int headingStartOffset = headingCandidate.getStartOffset();
				ctrl.deleteNode(headingCandidate);
				ctrl.insertFragment(headingStartOffset, headingFragment);
				List<AuthorNode> nodes = parentSection.getContentNodes();
				AuthorNode lastNode = nodes.get(nodes.size()-1);
				ctrl.surroundInFragment(
						String.format("<%s xmlns='%s'/>", parentSection.getLocalName(), parentSection.getNamespace()),
						headingStartOffset,
						lastNode.getEndOffset());
				AuthorElement newSection = (AuthorElement)lastNode.getParent();
				int offset = newSection.getStartOffset();
				if (!isSubLevel()) {
					AuthorDocumentFragment newSectionFragment = ctrl.createDocumentFragment(newSection, true);
					ctrl.deleteNode(newSection);
					offset = parentSection.getEndOffset()+1;
					ctrl.insertFragment(offset, newSectionFragment);
				}
				AddNavigationEntries(
						headingText, 
						newSection.getAttribute("id").getValue(), 
						parentSection.getAttribute("id").getValue());
				getAuthorAccess().getEditorAccess().setCaretPosition(offset+2);
				Utils.bringFocusToDocumentTab(getAuthorAccess());
			} 
			catch (BadLocationException e) {
				throw new AuthorOperationException(
						String.format("Unexpected BadLocationException while moving new level section: %s", e.getMessage()),
						e);
			}
		}
		catch (AuthorOperationException e) {
			throw e;
		}
		catch (Exception e) {
			throw new AuthorOperationException(
					String.format("An unexpected %s occured: %s", 
							e.getClass().getName(),
							e.getMessage()),
					e);
		}
		
	}
	
	private void InsertNcxPlayOrder(Element navPoint, XPath ncxXPath) throws XPathExpressionException {
		Element prevNavPoint = (Element)ncxXPath.compile("preceding-sibling::ncx:navPoint[1]").evaluate(navPoint, XPathConstants.NODE);
		if (prevNavPoint == null) {
			prevNavPoint = (Element)ncxXPath.compile("parent::ncx:navPoint[1]").evaluate(navPoint, XPathConstants.NODE);
		}
		int playOrder = 1;
		if (prevNavPoint != null) {
			String val = prevNavPoint.getAttribute("playOrder");
			playOrder = Integer.parseInt(val) + 1;
		}
		NodeList navs = (NodeList)ncxXPath.compile(String.format("//*[number(@playOrder)>=%d]", playOrder)).evaluate(
				prevNavPoint.getOwnerDocument(), 
				XPathConstants.NODESET);
		for (int i = 0; i < navs.getLength(); i++) {
			Element nav = (Element)navs.item(i);
			nav.setAttribute(
					"playOrder", 
					String.format("%d", Integer.parseInt(nav.getAttribute("playOrder"))+1));
		}
		navPoint.setAttribute("playOrder", String.format("%d", playOrder));
	}
	
	private boolean AddNcxNavigationEntries(Document ncx, String headingText, String sectionId, String previousSectionId)
			throws AuthorOperationException {
		try {
			URL ncxUrl = new URL(ncx.getDocumentURI());
			URL docUrl = getAuthorAccess().getEditorAccess().getEditorLocation();
			Element prevSecNavPoint = null;
			Element prevSecContent = null;
			XPath ncxXPath = Utils.getXPath("ncx", EpubUtils.NCX_NS);
			NodeList navPointSrcs = (NodeList)ncxXPath.compile("//ncx:navPoint/ncx:content/@src").evaluate(ncx, XPathConstants.NODESET);
			for (int i = 0; i < navPointSrcs.getLength(); i++) {
				Attr srcAttribute = (Attr)navPointSrcs.item(i);
				try {
					URL src = new URL(ncxUrl, srcAttribute.getValue());
					if (src.sameFile(docUrl)) {
						if (previousSectionId.equals(src.getRef())) {
							prevSecContent = srcAttribute.getOwnerElement();
							prevSecNavPoint = (Element)prevSecContent.getParentNode();
							break;
						}
					}
				}
				catch (MalformedURLException e) {
					continue;//Ignore src attributes with malformed urls
				}
				
			}
			if (prevSecNavPoint == null) {
				showMessage(String.format("Could not find navPoint in ncx pointing to section with id '%s'", previousSectionId));
				return false;
			}
			String srcValue = prevSecContent.getAttribute("src");
			srcValue = String.format("%s#%s", srcValue.substring(0, srcValue.indexOf('#')-1), sectionId);
			Element navPoint = ncx.createElementNS(EpubUtils.NCX_NS, "navPoint");
			Element text = ncx.createElementNS(EpubUtils.NCX_NS, "text");
			text.setTextContent(headingText);
			Element navLabel = ncx.createElementNS(EpubUtils.NCX_NS, "navLabel");
			navLabel.appendChild(text);
			navPoint.appendChild(navLabel);
			Element content = ncx.createElementNS(EpubUtils.NCX_NS, "content");
			content.setAttribute("src", srcValue);
			navPoint.appendChild(content);
			if (isSubLevel()) {
				NodeList childNavPoints = prevSecNavPoint.getElementsByTagNameNS(EpubUtils.NCX_NS, "navPoint");
				if (childNavPoints.getLength() == 0) {
					prevSecNavPoint.appendChild(navPoint);
				}
				else {
					prevSecNavPoint.insertBefore(navPoint, childNavPoints.item(0));
				}
			}
			else if (isSameLevel()) {
				prevSecNavPoint.getParentNode().insertBefore(navPoint, prevSecNavPoint.getNextSibling());
			}
			InsertNcxPlayOrder(navPoint, ncxXPath);
			return true;
		}
		catch (Exception e) {
			throw new AuthorOperationException(
					String.format(
							"Could not add ncx navigation entries due to an unexpected %s: %s", 
							e.getClass().getName(), 
							e.getMessage()),
					e);
		}
	}
	
	private boolean AddXHTMLNavNavigationEntries(Document nav, String headingText, String sectionId, String previousSectionId)
			throws AuthorOperationException {
		try {
			URL navUrl = new URL(nav.getDocumentURI());
			URL docUrl = getAuthorAccess().getEditorAccess().getEditorLocation();
			XPath xhtmlXPath = Utils.getXPath("x", EpubUtils.XHTML_NS);
			Element prevSecA = null;
			NodeList hrefs = (NodeList)xhtmlXPath.compile("//x:li/x:a/@href").evaluate(nav, XPathConstants.NODESET);
			for (int i = 0; i < hrefs.getLength(); i++) {
				Attr hrefAttr = (Attr)hrefs.item(i);
				URL href = new URL(navUrl, hrefAttr.getValue());
				if (href.sameFile(docUrl)) {
					if (previousSectionId.equals(href.getRef())) {
						prevSecA = hrefAttr.getOwnerElement();
						break;
					}
				}
			}
			if (prevSecA == null) {
				showMessage(String.format("Could not find toc entry in xhtml navigation document pointing to section with id '%s'", previousSectionId));
				return false;
			}
			Element prevSecLi = (Element)prevSecA.getParentNode();
			String hrefValue = prevSecA.getAttribute("href");
			hrefValue = String.format(
					"%s#%s",
					hrefValue.substring(0,  hrefValue.indexOf('#')),
					sectionId);
			Element a = nav.createElementNS(EpubUtils.XHTML_NS, "a");
			a.setTextContent(headingText);
			a.setAttribute("href", hrefValue);
			Element li = nav.createElementNS(EpubUtils.XHTML_NS, "li");
			li.appendChild(a);
			if (headingOperationType.equals(SUBLEVEL_OPERATION_TYPE)) {
				Element ol;
				NodeList ols = prevSecLi.getElementsByTagNameNS(EpubUtils.XHTML_NS, "ol");
				if (ols.getLength() > 0) {
					 ol = (Element)ols.item(0);
				}
				else {
					ol = nav.createElementNS(EpubUtils.XHTML_NS, "ol");
					prevSecLi.appendChild(ol);
				}
				NodeList lis = ol.getElementsByTagNameNS(EpubUtils.XHTML_NS, "li");
				if (lis.getLength() > 0) {
					ol.insertBefore(li, lis.item(0));
				}
				else {
					ol.appendChild(li);
				}
			}
			else if (headingOperationType.equals(SAME_LEVEL_OPERATION_TYPE)) {
				prevSecLi.getParentNode().insertBefore(li, prevSecLi.getNextSibling());
			}
			else {
				throw new AuthorOperationException(String.format("Unknown operation type %s", headingOperationType));
			}
			return true;
		}
		catch (Exception e) {
			throw new AuthorOperationException(
					String.format(
							"Could not add ncx navigation entries due to an unexpected %s: %s", 
							e.getClass().getName(), 
							e.getMessage()),
					e
					);
		}
	}
	
	private void AddNavigationEntries(String headingText, String sectionId, String previousSectionId) 
			throws AuthorOperationException {
		AuthorAccess opfAccess = EpubUtils.getAuthorDocument(
				getAuthorAccess(), 
				EpubUtils.getPackageUrl(getAuthorAccess()));
		if (opfAccess == null) {
			showMessage("Could not find package file");
			return;
		}
		AuthorAccess ncxAccess = EpubUtils.getNCXDocument(opfAccess);
		boolean foundOne = false;
		if (ncxAccess != null) {
			foundOne = true;
			Document ncx = Utils.deserializeDocument(
					Utils.serialize(ncxAccess, ncxAccess.getDocumentController().getAuthorDocumentNode()),
					ncxAccess.getEditorAccess().getEditorLocation().toString());
			if (AddNcxNavigationEntries(ncx, headingText, sectionId, previousSectionId)) {
				Utils.replaceRoot(ncx, ncxAccess);
			}
		}
		AuthorAccess navAccess = EpubUtils.getXHTMLNavDocument(opfAccess);
		if (navAccess != null) {
			foundOne = true;
			Document nav = Utils.deserializeDocument(
					Utils.serialize(navAccess, navAccess.getDocumentController().getAuthorDocumentNode()),
					navAccess.getEditorAccess().getEditorLocation().toString());
			if (AddXHTMLNavNavigationEntries(nav, headingText, sectionId, previousSectionId)) {
				Utils.replaceRoot(nav,  navAccess);
			}
		}
		if (!foundOne) {
			showMessage("Found no navigation documents in epub container");
		}
	}

}