package nota.oxygen.common;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;

public class RenameParentElementOperation extends BaseAuthorOperation {
	private static String ARG_NEW_NAME = "new name";
	private String newName;

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] { 
				new ArgumentDescriptor(ARG_NEW_NAME, ArgumentDescriptor.TYPE_STRING, "New name for the parent")
		};
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		newName = (String)args.getArgumentValue(ARG_NEW_NAME);
	}

	@Override
	public String getDescription() {
		return "Renames the parent element";
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			AuthorElement elementToRename = getCurrentElement();
			getAuthorAccess().getDocumentController().renameElement(elementToRename, newName);
		}
		catch (Exception e) {
			throw new AuthorOperationException(
					String.format("An unexpected %s occured while renaming element: %s", e.getClass().getName(), e.getMessage()), 
					e);
		}
		
	}

}
