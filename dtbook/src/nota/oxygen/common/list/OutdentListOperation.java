package nota.oxygen.common.list;


import java.util.List;

import nota.oxygen.common.BaseAuthorOperation;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;


/**
 * Outdents lists by replacing the list with it's items
 * 
 * @author Ole Holst Andersen (oha@nota.nu)
 */
public class OutdentListOperation extends BaseAuthorOperation {

	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
			AuthorElement list = getNamedCommonParentElementOfSelection(listElement, null);
			List<AuthorNode> items = list.getContentNodes(); 
			//showMessage(list.getParent().getName()+"=="+itemElement);
			if (list.getParent().getName().equals(itemElement))
			{
				AuthorElement parentLiAElem = (AuthorElement)list.getParent();
				int index = parentLiAElem.getContentNodes().indexOf(list);
				if (items.size()>0)
				{
					Element parentLiElem = deserializeElement(serialize(parentLiAElem));
					Element listElem = (Element)parentLiElem.getChildNodes().item(index);
					String xml = "";
					NodeList listChildren = listElem.getChildNodes();
					for (int i=0; i<listChildren.getLength(); i++)
					{
						xml += serialize(listChildren.item(i));
					}
					parentLiElem.removeChild(listElem);
					if (parentLiElem.getChildNodes().getLength()>0)
					{
						xml = serialize(parentLiElem) + xml;
					}
					docCtrl.deleteNode(parentLiAElem);
					docCtrl.insertXMLFragment(xml, getAuthorAccess().getEditorAccess().getCaretOffset());
				}
			}
			else
			{
				Element listElem = deserializeElement(serialize(list));
				NodeList listChildren = listElem.getChildNodes();
				String xml = "";
				for (int i=0; i<listChildren.getLength(); i++)
				{
					Node node = listChildren.item(i);
					if (node.getLocalName().equals(itemElement))
					{
						Element repl = listElem.getOwnerDocument().createElementNS(listElem.getNamespaceURI(), itemReplacementElement);
						NodeList liChildren = listChildren.item(i).getChildNodes();
						for (int j=0; j<liChildren.getLength(); j++)
						{
							repl.appendChild(liChildren.item(j).cloneNode(true));
						}
						xml += serialize(repl);
					}
					else
					{
						xml += serialize(node);
					}
				}
				docCtrl.deleteNode(list);
				docCtrl.insertXMLFragment(xml, getAuthorAccess().getEditorAccess().getCaretOffset());
				
			}
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
		itemReplacementElement = (String)args.getArgumentValue(ARG_ITEM_REPLACEMENT_ELEMENT);
	}
	
	private static String ARG_LIST_ELEMENT = "list element";
	private static String ARG_ITEM_ELEMENT = "item element";
	private static String ARG_ITEM_REPLACEMENT_ELEMENT = "item replacement element";
	private String listElement;
	private String itemElement;
	private String itemReplacementElement;

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor(ARG_LIST_ELEMENT, ArgumentDescriptor.TYPE_STRING, "list element name"),
				new ArgumentDescriptor(ARG_ITEM_ELEMENT, ArgumentDescriptor.TYPE_STRING, "item element name"),
				new ArgumentDescriptor(ARG_ITEM_REPLACEMENT_ELEMENT, ArgumentDescriptor.TYPE_STRING, "item replacement element name")
		};
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Outdent list items";
	}

}
