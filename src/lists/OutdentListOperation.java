package lists;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;

import common.BaseAuthorOperation;
import common.id.dtbook110.Dtbook110UniqueAttributesRecognizer;

/**
 * Outdents lists by replacing the list with it's items
 * 
 * @author Ole Holst Andersen (oha@nota.nu)
 */
public class OutdentListOperation extends BaseAuthorOperation {

	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			AuthorElement aList = getNamedCommonParentElementOfSelection(listElement, null);
			if (aList == null) {
				throw new AuthorOperationException(
						"The current selection is not inside a list");
			}
			String listId = "";
			AttrValue idAttr = aList.getAttribute("id");
			if (idAttr!=null) listId = idAttr.getValue();
			if (listId.equals("")) {
				Dtbook110UniqueAttributesRecognizer uaReq = new Dtbook110UniqueAttributesRecognizer();
				uaReq.activated(getAuthorAccess());
				uaReq.assignUniqueIDs(aList.getStartOffset(), aList.getEndOffset(), true);
				aList = getNamedCommonParentElementOfSelection(listElement, null);
				listId = aList.getAttribute("id").getValue();
			}
			AuthorElement aParentList = (AuthorElement)aList.getParent().getParent();
			if (!aParentList.getLocalName().equals(listElement)) {
				showMessage("List not nested");
				return;
			}
			Element parentList = deserialize(serialize(aParentList));
			Document doc = parentList.getOwnerDocument();
			Element list = getDescentantElementById(parentList, listId);
			if (list==null) {
				throw new AuthorOperationException("Unexpectedly could not re-find list in serialized xml");
			}
			Element parentItem = (Element)list.getParentNode();
			if (parentItem.getParentNode()!=parentList || (!itemElement.equals(parentItem.getLocalName()))) {
				throw new AuthorOperationException("Parent list item was unexpectedly not a child of parent list");
			}
			Element itemBefore = null;
			Element itemAfter = null;
			while (list.getPreviousSibling()!=null) {
				Node prevSib = list.getPreviousSibling();
				if (itemBefore==null ) itemBefore = doc.createElementNS(parentItem.getNamespaceURI(), parentItem.getLocalName());
				
				parentItem.removeChild(prevSib);
				itemBefore.insertBefore(prevSib, itemBefore.getFirstChild());
			}
			while (list.getNextSibling()!=null) {
				Node nextSib = list.getNextSibling();
				if (itemAfter==null ) itemAfter = doc.createElementNS(parentItem.getNamespaceURI(), parentItem.getLocalName());
				parentItem.removeChild(nextSib);
				itemAfter.appendChild(nextSib);
			}
			if (itemBefore!=null) parentList.insertBefore(itemBefore, parentItem);
			while (list.getFirstChild()!=null) {
				Node firstChild = list.getFirstChild();
				list.removeChild(firstChild);
				parentList.insertBefore(firstChild, parentItem);
			}
			if (itemAfter!=null) parentList.insertBefore(itemAfter, parentItem);
			parentList.removeChild(parentItem);
			
			AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
			docCtrl.deleteNode(aParentList);
			docCtrl.insertXMLFragment(serialize(parentList), getAuthorAccess().getEditorAccess().getCaretOffset());	
		}
		catch (AuthorOperationException e) {
			throw e;
		}
		catch (Exception e) {
			String msg = "Unexpected "+e.getClass().getName()
			+" occured in "+getClass().getName()+".doOperation:\n" 
			+ e.getMessage();
			throw new AuthorOperationException(msg, e);
			
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
		return "Outdent list items";
	}

}
