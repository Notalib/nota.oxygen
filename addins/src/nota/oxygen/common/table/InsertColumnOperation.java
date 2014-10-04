package nota.oxygen.common.table;

import java.util.List;

import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.commons.table.operations.InsertColumnOperationBase;
import ro.sync.ecss.extensions.commons.table.operations.xhtml.XHTMLDocumentTypeHelper;

/**
 * @author OHA
 *
 */
public class InsertColumnOperation extends InsertColumnOperationBase {

	/**
	 * Default constructor
	 */
	public InsertColumnOperation() {
		super( new XHTMLDocumentTypeHelper());
	}

	@Override
	protected String getCellElementName(AuthorElement rowElement, int newColumnIndex) {
		List<AuthorNode> rowNodes = rowElement.getContentNodes();
		if (0<=newColumnIndex && newColumnIndex<rowNodes.size()) {
			return getLocalName(rowNodes.get(newColumnIndex).getName());
		}
		else if (rowNodes.size()>0) {
			return rowNodes.get(0).getName();
		}
		else {
			return "td";
		}
	}
	
	private String getLocalName(String qname) {
		if (qname==null) return null;
		int lastIndex = qname.lastIndexOf(":");
		if (0<=lastIndex && lastIndex+1<qname.length()) {
			return qname.substring(lastIndex+1);
		}
		return qname;
	}
	
	

}
