package lists;


import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import common.BaseAuthorOperation;

/**
 * Indents the selected list items 
 * @author Ole Holst Andersen (oha@nota.nu)
 */
public class IndentListOperation extends BaseAuthorOperation {

	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			AuthorEditorAccess edtAccess = getAuthorAccess().getEditorAccess();
			AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
			int selStart = edtAccess.getSelectionStart();
			int selEnd = edtAccess.getSelectionEnd();
			showMessage("Selection: "+selStart+"-"+selEnd);
			AuthorElement list = getNamedCommonParentElementOfSelection(listElement, null);
			if (list==null) {
				throw new AuthorOperationException("Current selection has no common parent "+listElement+" element");
			}
			List<AuthorNode> items = list.getContentNodes();
			int firstItemIndex = 0;
			int lastItemIndex = items.size()-1;
			for (int i=0; i<items.size(); i++) {
				AuthorNode item = items.get(i);
				if (item.getType()!=AuthorNode.NODE_TYPE_ELEMENT) {
					throw new AuthorOperationException("List contains non-element child");
				}
				boolean found = false;
				if (item.getStartOffset()<=selStart && selStart<=item.getEndOffset()) {
					showMessage("First Item: "+item.getStartOffset()+"-"+item.getEndOffset());
					firstItemIndex = i;
					found = true;
				}
				if (item.getStartOffset()<=selEnd && selEnd<=item.getEndOffset()) {
					showMessage("Last Item: "+item.getStartOffset()+"-"+item.getEndOffset());
					lastItemIndex = i;
					found = true;
				}
				if (!found) {
					showMessage("Other Item: "+item.getStartOffset()+"-"+item.getEndOffset());
				}
			}

			String xml = serialize(list);
			Element listElem = deserialize(xml);
			Element li = null;
			NodeList children = listElem.getChildNodes();
			List<Node> childrenToMove = new ArrayList<Node>();
			for (int i=0; i<children.getLength(); i++) {
				if (children.item(i).getLocalName().equals(itemElement)) {
					li = (Element)children.item(i);
				}
				if (firstItemIndex<=i && i<=lastItemIndex) childrenToMove.add(children.item(i));
			}
			if (li==null) throw new AuthorOperationException("No list item was selected to indent");
			Element nestedListElem = (Element)listElem.cloneNode(false);
			nestedListElem.removeAttribute("id");
			li = (Element)li.cloneNode(false);
			li.removeAttribute("id");
			li.appendChild(nestedListElem);
			listElem.insertBefore(li, listElem.getChildNodes().item(firstItemIndex));
			for (Node c : childrenToMove) {
				nestedListElem.appendChild(listElem.removeChild(c));
			}
			docCtrl.deleteNode(list);
			docCtrl.insertXMLFragment(serialize(listElem), edtAccess.getCaretOffset());
		}
		catch (Exception e) {
			throw new AuthorOperationException(
					"Unexpected "+e.getClass().getName()+" occured: "+e.getMessage(),
					e);
		}
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		listElement = (String)args.getArgumentValue(ARG_LIST_ELEMENT);
		itemElement = (String)args.getArgumentValue(ARG_ITEM_ELEMENT);
	}
	
	private static String ARG_LIST_ELEMENT = "list element";
	private static String ARG_ITEM_ELEMENT = "item element";
	private String listElement;
	private String itemElement;

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor(ARG_LIST_ELEMENT, ArgumentDescriptor.TYPE_STRING, "list element name"),
				new ArgumentDescriptor(ARG_ITEM_ELEMENT, ArgumentDescriptor.TYPE_STRING, "item element name")
		};
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Indent list items";
	}

}
