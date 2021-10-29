package dk.nota.oxygen.operations.html;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;

public class HierarchyOperation extends XhtmlEpubAuthorOperation {

	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			normaliseToDepth(getFirstElementByXpath("/html/body"),
					editingConcatDocument() ? -1 : 0);
		} catch (BadLocationException e) {
			throw new AuthorOperationException(e.toString());
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return null; // No arguments to return
	}

	@Override
	public String getDescription() {
		return "Normalises heading hierarchy throughout document";
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException {
		// No arguments to parse
	}

}
