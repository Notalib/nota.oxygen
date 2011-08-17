package nota.oxygen.common;

import java.util.ArrayList;
import java.util.List;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;

/**
 * Marks the selected text as a link, using the selected text as both link content and reference
 * @author OHA
 */
public class MarkupLinkOperation extends BaseAuthorOperation {

	@Override
	public String getDescription() {
		return "Marks the selected text as a link, using the selected text as both link content and reference";
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
			int startSel = getSelectionStart();
			int endSel = getSelectionEnd();
			String selectedText = getAuthorAccess().getEditorAccess().getSelectedText(); 
			AuthorElement firstAthElem = (AuthorElement)docCtrl.getNodeAtOffset(startSel+1);
			AuthorElement lastAthElem = (AuthorElement)docCtrl.getNodeAtOffset(endSel-1);
			if (firstAthElem!=lastAthElem)
			{
				String msg = "Current selection is not contained in a single node:\n";
				msg += "first element: "+firstAthElem.getLocalName()+"[@id='"+firstAthElem.getAttribute("id")+"']\n";
				msg += "last element: "+lastAthElem.getLocalName()+"[@id='"+lastAthElem.getAttribute("id")+"']\n";
				showMessage(msg);
				return;
			}
			docCtrl.surroundInFragment(linkFragment, startSel, endSel-1);
			AuthorElement linkElem = (AuthorElement)docCtrl.getNodeAtOffset(startSel+1);
			linkElem.setAttribute(refAttributeName, new AttrValue(selectedText));
		}
		catch (AuthorOperationException e)
		{
			throw e;
		}
		catch (Exception e) {
			throw new AuthorOperationException(
 					"Unexpected "+e.getClass().getName()+"occured: "+e.getMessage(),
					e);
		}
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		linkFragment = (String)args.getArgumentValue(ARG_LINK_FRAGMENT);
		refAttributeName = (String)args.getArgumentValue(ARG_REF_ATTRIBUTE_NAME);
	}
	
	private static String ARG_LINK_FRAGMENT = "link fragment";
	private static String ARG_REF_ATTRIBUTE_NAME = "reference attribute name";

	private String linkFragment;
	private String refAttributeName;

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor(ARG_LINK_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "link xml fragment"),
				new ArgumentDescriptor(ARG_REF_ATTRIBUTE_NAME, ArgumentDescriptor.TYPE_STRING, "name of reference attribute (href)")
		};
	}

}
