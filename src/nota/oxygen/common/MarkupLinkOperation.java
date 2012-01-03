package nota.oxygen.common;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
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
	
	protected String getRefAttributeValue(String text) {
		if (text.contains("://")) return text;
		if (text.startsWith("mailto:")) return text;
		if (text.startsWith("#")) return text;
		if (text.matches(".+@.+\\.[a-z]+")) return "mailto:"+text;
		return "http://"+text;
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
			String refValue = getRefAttributeValue(selectedText);
			linkElem.setAttribute(refAttributeName, new AttrValue(refValue));
			if (externalAttributeName!="") {
				String val = "true";
				if (refValue.startsWith("#")) val ="false";
				linkElem.setAttribute(externalAttributeName, new AttrValue(val));
			}
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
		externalAttributeName = (String)args.getArgumentValue(ARG_EXTERNAL_ATTRIBUTE_NAME);
		if (externalAttributeName==null) externalAttributeName = "";
		externalAttributeName = externalAttributeName.trim();
	}
	
	private static String ARG_LINK_FRAGMENT = "link fragment";
	private static String ARG_REF_ATTRIBUTE_NAME = "reference attribute name";
	private static String ARG_EXTERNAL_ATTRIBUTE_NAME = "external attribute name - leave empty if not available";

	private String linkFragment;
	private String refAttributeName;
	private String externalAttributeName;

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor(ARG_LINK_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "link xml fragment"),
				new ArgumentDescriptor(ARG_REF_ATTRIBUTE_NAME, ArgumentDescriptor.TYPE_STRING, "name of reference attribute (href)"),
				new ArgumentDescriptor(ARG_EXTERNAL_ATTRIBUTE_NAME, ArgumentDescriptor.TYPE_STRING, "name of external attribute (external)")
		};
	}

}
