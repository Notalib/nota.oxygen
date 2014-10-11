package nota.oxygen.epub.headings;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import nota.oxygen.common.BaseAuthorOperation;

public class InsertHeadingOperation extends BaseAuthorOperation {

	@Override
	public ArgumentDescriptor[] getArguments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Inserts a heading, with the side-effect of updating the ePub navigation documents";
	}
	
	@Override
	protected void doOperation() throws AuthorOperationException {
		showMessage(getAuthorAccess().getDocumentController().getAuthorDocumentNode().getXMLBaseURL().toString());

	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

}
