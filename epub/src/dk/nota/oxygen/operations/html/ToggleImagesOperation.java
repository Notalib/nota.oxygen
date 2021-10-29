package dk.nota.oxygen.operations.html;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.editor.page.author.fold.AuthorFoldManager;

public class ToggleImagesOperation extends XhtmlEpubAuthorOperation {

	@Override
	protected void doOperation() throws AuthorOperationException {
		AuthorFoldManager foldManager = getAuthorEditor().getAuthorFoldManager();
		AuthorDocumentController controller = getDocumentController();
		for (AuthorNode imgNode : controller.findNodesByXPath(
				"//*[local-name() eq 'figure']", true, true, true))
			foldManager.expandFold(imgNode);
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Expands all folded image nodes";
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

}
