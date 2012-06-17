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
				new ArgumentDescriptor(ARG_XPATH_STATEMENT, ArgumentDescriptor.TYPE_STRING, "The XPath statement"),
				new ArgumentDescriptor(ARG_DIRECTION, ArgumentDescriptor.TYPE_CONSTANT_LIST, "The XPath statement", DIRECTIONS, DIRECTIONS[0])
		};
	}
	
	private static String ARG_DIRECTION = "direction";
	private static String[] DIRECTIONS = new String[] {"forward", "reverse"};
	private String direction;

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
				showMessage("No node matches "+xpathStatement);
			}
			else {
				int currentCaretPos = getAuthorAccess().getEditorAccess().getCaretOffset();
				AuthorNode next = null;
				if (DIRECTIONS[0].equals(direction)) {//Search in forward direction
					for (int i=0; i<res.length; i++) {
						if (currentCaretPos<=res[i].getStartOffset()) {
							next = res[i];
							break;
						}
					}
					if (next==null) next = res[0];
				}
				else if (DIRECTIONS[1].equals(direction)) {//Search in reverse direction
					for (int i=res.length-1; i>=0; i--) {
						if (currentCaretPos>res[i].getEndOffset()) {
							next = res[i];
							break;
						}
					}
					if (next==null) next = res[res.length-1];
				}
				else {
					throw new AuthorOperationException("Invalid direction "+direction);
				}
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
		direction = (String)args.getArgumentValue(ARG_DIRECTION);

	}

}
