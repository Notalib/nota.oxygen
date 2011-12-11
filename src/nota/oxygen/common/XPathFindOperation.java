package nota.oxygen.common;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorNode;

/**
 * Finds a Node by XPath
 * @author OHA
 *
 */
public class XPathFindOperation extends BaseAuthorOperation {
	
	private static String ARG_XPATH_STATEMENT = "XPath statement";
	
	private String xpathStatement;

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor(ARG_XPATH_STATEMENT, ArgumentDescriptor.TYPE_STRING, "The XPath statement")
		};
	}

	@Override
	public String getDescription() {
		return "Finds nodes by Xpath";
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		try
		{
			AuthorNode[] res = docCtrl.findNodesByXPath(xpathStatement, true, true, true);
			if (res.length==0) {
				showMessage("No nodes matches "+xpathStatement);
			}
			else {
				int currentCaretPos = getAuthorAccess().getEditorAccess().getCaretOffset();
				AuthorNode next = null;
				for (int i=0; i<res.length; i++) {
					if (currentCaretPos<=res[i].getStartOffset()) {
						next = res[i];
						break;
					}
				}
				if (next==null) next = res[0];
				getAuthorAccess().getEditorAccess().select(next.getStartOffset(), next.getEndOffset());
			}
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
		xpathStatement = (String)args.getArgumentValue(ARG_XPATH_STATEMENT);

	}

}
