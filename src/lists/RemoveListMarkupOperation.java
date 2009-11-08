package lists;


import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;

import common.BaseAuthorOperation;

/**
 * Out-indents nested lists
 * 
 * @author Ole Holst Andersen (oha@nota.nu)
 */
public class RemoveListMarkupOperation extends BaseAuthorOperation {

	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
			AuthorElement listAuthElem = getNamedCommonParentElementOfSelection(listElement, null);
			if (listAuthElem == null) {
				throw new AuthorOperationException(
						"The current selection is not inside a list");
			}
			showMessage("Removing list[@id='"+listAuthElem.getAttribute("id").getValue()+"']");
			Element list = deserialize(serialize(listAuthElem));
			String xml = "";
			for (int i=0; i<list.getChildNodes().getLength(); i++) {
				Node child = list.getChildNodes().item(i);
				if (child instanceof Element) {
					Element elemChild = (Element)child;
					if (elemChild.getLocalName().equals(itemElement)) {
						for (int j=0; j<elemChild.getChildNodes().getLength(); j++) {
							xml += serialize(elemChild.getChildNodes().item(j));
						}
						continue;
					}
				}
				showMessage("serializing non item child "+child.getLocalName());
				xml += serialize(child);
				
			}
			docCtrl.deleteNode(listAuthElem);
			docCtrl.insertXMLFragment(xml, getAuthorAccess().getEditorAccess().getCaretOffset());
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
		return "Removes list markup";
	}

}
