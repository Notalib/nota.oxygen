package tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;

import common.BaseAuthorOperation;
import common.dtbook.v110.Dtbook110UniqueAttributesRecognizer;

/**
 * Fixes the headers attribute of the cell in the selected table
 * @author Ole Holst Andersen (oha@nota.nu)
 */
public class FixTableHeadersOperation extends BaseAuthorOperation {

	private static int getIntAttr(Element elem, String attrName, int defVal)
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
	
	private static boolean idrefsContainsId(String id, String idrefs) {
		for (String idref : idrefs.split(" ")) {
			if (idref == id) return true;
		}
		return false;
	}
	
	private static void addIdToCellHeaders(Element cell, String id) {
		if (cell.hasAttribute("headers")) {
			String headers = cell.getAttribute("headers");
			if (!idrefsContainsId(id, headers)) cell.setAttribute("headers", headers+" "+id);
		}
		else {
			cell.setAttribute("headers", id);
		}
	}
	
	
	private static Map<TableIndex, Element> generateTableMap(Element table) throws AuthorOperationException
	{
		HashMap<TableIndex, Element> result = new HashMap<TableIndex, Element>();
		List<Element> tableRows = getTableRows(table);
		for (int rowIndex = 0; rowIndex<tableRows.size(); rowIndex++) {
			Element row = tableRows.get(rowIndex);
			if (!row.hasChildNodes()) continue;
			int colIndex = 0;
			Element cell = (Element)row.getFirstChild();
			while (cell != null) {
				while (result.containsKey(new TableIndex(rowIndex, colIndex))) colIndex++;
				int rowspan = getIntAttr(cell, "rowspan", 1);
				int colspan = getIntAttr(cell, "colspan", 1);
				for (int dr=0; dr<rowspan; dr++) {
					for (int dc=0; dc<colspan; dc++) {
						result.put(new TableIndex(rowIndex+dr, colIndex+dc), cell);
					}
				}
				colIndex += colspan;
				if (cell.getNextSibling()!=null){
					cell = (Element)cell.getNextSibling();
				}
				else {
					cell = null;
				}
			}
			
		}
		return result;
	}

	private static List<Element> getTableRows(Element table) throws AuthorOperationException {
		List<Element> result = new ArrayList<Element>();
		String expr;
		XPath xpath = getXPath("d", table.getNamespaceURI());
		NodeList tableRows;
		try {
			expr = (table.getNamespaceURI() == null) ? "thead/tr" : "d:thead/d:tr";
			tableRows = (NodeList) xpath.evaluate(expr, table, XPathConstants.NODESET);
			for (int i=0; i<tableRows.getLength(); i++) result.add((Element)tableRows.item(i));
			expr = (table.getNamespaceURI() == null) ? "tr" : "d:tr";
			tableRows = (NodeList) xpath.evaluate(expr, table, XPathConstants.NODESET);
			for (int i=0; i<tableRows.getLength(); i++) result.add((Element)tableRows.item(i));
			expr = (table.getNamespaceURI() == null) ? "tbody/tr" : "d:tbody/d:tr";
			tableRows = (NodeList) xpath.evaluate(expr, table, XPathConstants.NODESET);
			for (int i=0; i<tableRows.getLength(); i++) result.add((Element)tableRows.item(i));
			expr = (table.getNamespaceURI() == null) ? "tfoot/tr" : "d:tfoot/d:tr";
			tableRows = (NodeList) xpath.evaluate(expr, table, XPathConstants.NODESET);
			for (int i=0; i<tableRows.getLength(); i++) result.add((Element)tableRows.item(i));
		}
		catch (Exception e) {
			throw new AuthorOperationException("Unexpected exception occured in getTableRows method:\n"+e.getMessage(), e);
		}
		return result;
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
			
			Dtbook110UniqueAttributesRecognizer uaReq = new Dtbook110UniqueAttributesRecognizer();
			uaReq.activated(getAuthorAccess());
			
			uaReq.assignUniqueIDs(tableAElem.getStartOffset(), tableAElem.getEndOffset(), false);

			String tableXml = docCtrl.serializeFragmentToXML(docCtrl
					.createDocumentFragment(tableAElem, true));
			Element tableXmlElem = deserialize(tableXml);
			

			// Remove pre-existing headers attributes on td table cells
			String expr = (tableXmlElem.getNamespaceURI() == null) ? "//tr/td"
					: "//d:tr/d:td";
			XPath xpath = getXPath("d", tableXmlElem.getNamespaceURI());
			NodeList tableCells = (NodeList) xpath.evaluate(expr, tableXmlElem, XPathConstants.NODESET);
			for (int i = 0; i < tableCells.getLength(); i++) {
				Element td = (Element) tableCells.item(i);
				td.removeAttribute("headers");
			}
			
			String comment = "";
			
			Map<TableIndex, Element> tableMap = generateTableMap(tableXmlElem);
			for (TableIndex rcIndex : tableMap.keySet()) {
				comment += "("+rcIndex.RowIndex+","+rcIndex.ColIndex+"):"+tableMap.get(rcIndex).getAttribute("id")+";";
				Element cell = tableMap.get(rcIndex);
				if (rcIndex.RowIndex>0) {
					Element colHeader = tableMap.get(new TableIndex(0, rcIndex.ColIndex));
					if (colHeader!=null) {
						if (colHeader.getLocalName()=="th" && colHeader.hasAttribute("id")) {
							addIdToCellHeaders(cell, colHeader.getAttribute("id"));
						}
					}
				}
				if (rcIndex.ColIndex>0) {
					Element rowHeader = tableMap.get(new TableIndex(rcIndex.RowIndex, 0));
					if (rowHeader!=null) {
						if (rowHeader.getLocalName()=="th" && rowHeader.hasAttribute("id")) {
							addIdToCellHeaders(cell, rowHeader.getAttribute("id"));
						}
					}
				}
			}
			comment = "<!--"+comment+"-->";
			tableXml = serialize(tableXmlElem);

			docCtrl.deleteNode(tableAElem);
			docCtrl.insertXMLFragment(tableXml, getAuthorAccess().getCaretOffset());
			docCtrl.insertXMLFragment(comment, getAuthorAccess().getCaretOffset());
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

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		// No arguments to parse
		
	}

}
