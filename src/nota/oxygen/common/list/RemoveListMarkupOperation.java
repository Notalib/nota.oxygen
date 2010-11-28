package nota.oxygen.common.list;


import java.util.ArrayList;


import nota.oxygen.common.BaseAuthorOperation;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;


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
			Element list = deserializeElement(serialize(listAuthElem));
			String xml = "";
			for (int i=0; i<list.getChildNodes().getLength(); i++) {
				Node child = list.getChildNodes().item(i);
				if (child instanceof Element) {
					Element elemChild = (Element)child;
					if (elemChild.getLocalName().equals(itemElement)) {
						if (!elemChild.hasChildNodes()) continue;
						Node firstChild = elemChild.getChildNodes().item(0);
						if (isListSiblingElement(firstChild))						{
							for (int j=0; j<elemChild.getChildNodes().getLength(); j++) {
								xml += serialize(elemChild.getChildNodes().item(j));
							}
						}
						else {
							Element p = list.getOwnerDocument().createElementNS(list.getNamespaceURI(), "p");
							for (int j=0; j<elemChild.getChildNodes().getLength(); j++) {
								p.appendChild(elemChild.getChildNodes().item(j).cloneNode(true));
							}
							xml += serialize(p);
						}
						continue;
					}
				}
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
	
	private boolean isListSiblingElement(Node nod)
	{
		if (nod instanceof Element)
		{
			Element elem = (Element)nod;
			if (listSiblingElements.contains(elem.getLocalName())) return true;
		}
		return false;
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		listElement = (String)args.getArgumentValue(ARG_LIST_ELEMENT);
		itemElement = (String)args.getArgumentValue(ARG_ITEM_ELEMENT);
		String elems = (String)args.getArgumentValue(ARG_LIST_SIBLING_ELEMENTS);
		listSiblingElements = new ArrayList<String>();
		for (String e : elems.split(" ")) listSiblingElements.add(e);	}
	
	private static String ARG_LIST_ELEMENT = "list element";
	private static String ARG_ITEM_ELEMENT = "item element";
	private static String ARG_LIST_SIBLING_ELEMENTS = "list sibling elements";
	private String listElement;
	private String itemElement;
	private ArrayList<String> listSiblingElements;

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor(ARG_LIST_ELEMENT, ArgumentDescriptor.TYPE_STRING, "list element name"),
				new ArgumentDescriptor(ARG_ITEM_ELEMENT, ArgumentDescriptor.TYPE_STRING, "item element name"),
				new ArgumentDescriptor(ARG_LIST_SIBLING_ELEMENTS, ArgumentDescriptor.TYPE_STRING, "list sibling elements - space separated")
		};
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Removes list markup";
	}

}
