package nota.oxygen.common.table;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;

import nota.oxygen.common.BaseAuthorOperation;
import nota.oxygen.common.dtbook.v2005.Dtbook2005UniqueAttributesRecognizer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.commons.id.DefaultUniqueAttributesRecognizer;


/**
 * Fixes the headers attribute of the cell in the selected table
 * @author Ole Holst Andersen (oha@nota.nu)
 */
public class FixTableHeadersOperation extends BaseAuthorOperation {
	
	private XPath xpath;
	private String prefix;
	
	private void initXPath(Element tableElement) {
		String ns = tableElement.getNamespaceURI();
		if (ns != null) {
			xpath = getXPath("d", ns);
			prefix = "d:";
		}
		else {
			xpath = getXPath();
			prefix = "";
		}
	}
	
	private int getAttributeAsInt(Element elem, String attrName, int defVal) {
		if (elem.hasAttribute(attrName)) {
			try {
				return Integer.parseInt(elem.getAttribute(attrName));
			}
			catch (NumberFormatException e) {}
		}
		
		return defVal;
	}
	
	private int getColumnIndex(Node tableCell) {
		if (tableCell instanceof Element) {
			Element tcElement = (Element)tableCell;
			int span = 0;
			if (tcElement.getLocalName()=="td" || tcElement.getLocalName()=="th") {
				span = getAttributeAsInt(tcElement, "cols", 1);
			}
			return getColumnIndex(tcElement.getPreviousSibling())+span;
		}
		return 0;
	}
	
	private String getHeadersAttributeValue(Element tdElement, Element tableElement) {
		String headers = "";
		String expr = prefix+"thead/"+prefix+"tr|"+prefix+"tbody/"+prefix+"tr|"+prefix+"tr";
		try
		{
			Node rowNode = (Node)xpath.evaluate(expr, tableElement, XPathConstants.NODE);
			int columnIndex = getColumnIndex(tdElement);
			if (rowNode instanceof Element) {
				NodeList firstRow = rowNode.getChildNodes();
				for (int i=0; i<columnIndex; i++) {
					if (i>=firstRow.getLength()) break;
					if (firstRow.item(i) instanceof Element) {
						Element e = (Element)firstRow.item(i);
						if (e.getLocalName()=="td" || e.getLocalName()=="th") {
							if (getColumnIndex(firstRow.item(i))==columnIndex) {
								headers = e.getAttribute("id");
								break;
							}
						}
					}
				}
			}
		}
		catch (XPathException e) {}
		if (tdElement.getParentNode().hasChildNodes()) {
			Element firstRowCellElement = (Element)tdElement.getParentNode().getFirstChild();
			if (firstRowCellElement.getLocalName()=="th") headers += " "+firstRowCellElement.getAttribute("id");
		}
		return headers.trim();
	}

	@Override
	public void doOperation()
			throws AuthorOperationException {
		try {
			AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
			AuthorElement tableAElem = getNamedCommonParentElementOfSelection("table", null);
			if (tableAElem == null) {
				throw new AuthorOperationException(
						"The current selection is not inside a table");
			}
			
			DefaultUniqueAttributesRecognizer uaReq = new Dtbook2005UniqueAttributesRecognizer();
			uaReq.activated(getAuthorAccess());
			
			uaReq.assignUniqueIDs(tableAElem.getStartOffset(), tableAElem.getEndOffset(), false);

			String tableXml = docCtrl.serializeFragmentToXML(docCtrl.createDocumentFragment(tableAElem, true));
			Element tableXmlElem = deserializeElement(tableXml);
			if (tableXmlElem==null) throw new AuthorOperationException("Could not deserialize table Element to DOM");
			
			initXPath(tableXmlElem);

			String expr = "//"+prefix+"tr/"+prefix+"td";
			NodeList tableCells = (NodeList) xpath.evaluate(expr, tableXmlElem, XPathConstants.NODESET);
			if (tableCells==null) throw new AuthorOperationException("Found no td table cells");
			for (int i = 0; i < tableCells.getLength(); i++) {
				showMessage("Before item retrieval i="+i);
				Element td = (Element) tableCells.item(i);
				String headers = getHeadersAttributeValue(td, tableXmlElem);
				if (headers == null) {
					if (td.hasAttribute("headers")) td.removeAttribute("headers");
				}
				else {
					td.setAttribute("headers", headers);
				}
			}

			tableXml = serialize(tableXmlElem);

			docCtrl.deleteNode(tableAElem);
			docCtrl.insertXMLFragment(tableXml, getAuthorAccess().getEditorAccess().getCaretOffset());
		}
		catch (Exception e) {
			if (e instanceof AuthorOperationException) {
				throw (AuthorOperationException)e;
			}
			else {
				String msg = "Unexpected "+e.getClass().getSimpleName()+" occured";
				if (e.getMessage()!=null) msg += ": "+e.getMessage();
//						+ "\nStack Trace:\n" + e.getStackTrace().toString();
				throw new AuthorOperationException(msg, e);
			}
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {};
	}

	@Override
	public String getDescription() {
		return "Fix table header attributes";
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		// No arguments to parse
		
	}

}
