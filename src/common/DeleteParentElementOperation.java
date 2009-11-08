package common;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

/**
 *	Operation that deleted the parent xml tag of the current selection
 * @author Ole Holst Andersen (oha@nota.nu)
 */
public class DeleteParentElementOperation extends BaseAuthorOperation {

	@Override
	protected void doOperation() throws AuthorOperationException {
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		try
		{
			AuthorNode aNode = docCtrl.getNodeAtOffset(getAuthorAccess().getCaretOffset());
			while (aNode != null) {
				if (aNode instanceof AuthorElement) break;
				aNode = aNode.getParent();
			}
			if (aNode == null) {
				throw new AuthorOperationException("Unexpectedly could not find parent element");
			}
			Element xElem = (Element)deserialize(serialize(aNode));
			String xml = "";
			NodeList children = xElem.getChildNodes();
			for (int i=0; i<children.getLength(); i++) {
				xml += serialize(children.item(i));
			}
			//showMessage("Deleting element "+xElem.getLocalName());
			docCtrl.deleteNode(aNode);
			docCtrl.insertXMLFragment(xml, getAuthorAccess().getCaretOffset());
			getAuthorAccess().getEditorAccess().select(getAuthorAccess().getCaretOffset()-xml.length(), getAuthorAccess().getCaretOffset());
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
		// No arguments to parse

	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[]{};
	}

	@Override
	public String getDescription() {
		return "Deletes the parent element of the current context";
	}

}
