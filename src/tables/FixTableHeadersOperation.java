package tables;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.undo.CannotUndoException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.commons.id.DefaultUniqueAttributesRecognizer;

import common.BaseAuthorOperation;
import common.Dtbook110UniqueAttributesRecognizer;

public class FixTableHeadersOperation extends BaseAuthorOperation {

	
	private int getIntAttr(Element elem, String attrName, int defVal)
			throws AuthorOperationException {
		int res = defVal;
		if (elem.hasAttribute(attrName)) {
			String val = elem.getAttribute(attrName);
			try {
				res = Integer.decode(val);
			} catch (NumberFormatException e) {
				throw new AuthorOperationException("Invalid " + attrName
						+ " attribute value " + val);
			}
		}
		return res;
	}
	
	private void setHeadersAttrInColumn(int colIndex, String id, Iterator<Element> trElements)
		throws AuthorOperationException
	{
		while (trElements.hasNext()) {
			Element tr = trElements.next();
			int thisColIndex = 0;
			Element td = (Element)tr.getFirstChild();
			while (td!= null) {
				if (colIndex==thisColIndex)
				{
					if (td.getLocalName()=="td")
					{
						td.setAttribute("headers", id);
					}
				}
				thisColIndex += getIntAttr(td, "colspan", 1);
			}
		}
	}

	@Override
	public void doOperation(AuthorAccess access, ArgumentsMap map)
			throws IllegalArgumentException, AuthorOperationException {
		try {
			AuthorDocumentController docCtrl = access.getDocumentController();
			docCtrl.beginCompoundEdit();
			try {
				AuthorElement tableAElem = getNamedCommonParentElementOfSelection(
						access, "table", null);
				if (tableAElem == null) {
					throw new AuthorOperationException(
							"The current selection is not inside a table");
				}
				Dtbook110UniqueAttributesRecognizer uaReq = new Dtbook110UniqueAttributesRecognizer();
				uaReq.assignUniqueIDs(tableAElem.getStartOffset(), tableAElem
						.getEndOffset(), false);

				String tableXml = docCtrl.serializeFragmentToXML(docCtrl
						.createDocumentFragment(tableAElem, true));
				Element tableXmlElem = deserialize(tableXml);

				String expr = (tableXmlElem.getNamespaceURI() == null) ? "//tr/td"
						: "//d:tr/d:td";
				XPath xpath = getXPath("d", tableXmlElem.getNamespaceURI());
				NodeList tableCells = (NodeList) xpath.evaluate(expr,
						tableXmlElem, XPathConstants.NODESET);
				for (int i = 0; i < tableCells.getLength(); i++) {
					Element td = (Element) tableCells.item(i);
					td.removeAttribute("headers");
				}
				Element firstTR = null;
				expr = (tableXmlElem.getNamespaceURI() == null) ? "//tr[not(parent::tfoot)]"
						: "//d:tr[not(parent::d:tfoot)]";
				NodeList nl = (NodeList) xpath.evaluate(expr, tableXmlElem,
						XPathConstants.NODESET);
				if (nl.getLength() == 1) {
					firstTR = (Element) nl.item(0);
				}
				
				Set<Element> trElements = new HashSet<Element>();
				nl = tableXmlElem.getElementsByTagName("tr");
				for (int i=0; i<nl.getLength(); i++) trElements.add((Element)nl.item(i));
				
				if (firstTR != null) {
					int colIndex = 0;
					Element th = (Element)firstTR.getFirstChild();
					while (th!=null) {
						if (th.getLocalName()=="th")
						{
							setHeadersAttrInColumn(colIndex, th.getAttribute("id"), trElements.iterator());							
						}
						colIndex += getIntAttr(th, "colspan", 1);
						th = (Element)th.getNextSibling();
					}
						
				}

				tableXml = serialize(tableXmlElem);

				docCtrl.deleteNode(tableAElem);
				docCtrl.insertXMLFragment(tableXml, access.getCaretOffset());
				docCtrl.endCompoundEdit();
			} catch (Exception e) {
				docCtrl.endCompoundEdit();
				try {
					docCtrl.getUndoManager().undo();
				} catch (CannotUndoException es) {
					// Do nothing
				}
				throw e;
			}
		} catch (AuthorOperationException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Unexpected exception occured:\n" + e.getMessage()
					+ "\nStack Trace:\n" + e.getStackTrace().toString();
			throw new AuthorOperationException(msg, e);
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

}
