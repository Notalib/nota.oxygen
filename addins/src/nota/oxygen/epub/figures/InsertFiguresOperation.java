package nota.oxygen.epub.figures;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import nota.oxygen.common.BaseAuthorOperation;

public class InsertFiguresOperation extends BaseAuthorOperation {
	private static String ARG_IMAGE_FRAGMENT = "image fragment";
	private String imageFragment;

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] { 
				new ArgumentDescriptor(ARG_IMAGE_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Image fragment")
		};
	}

	@Override
	protected void parseArguments(ArgumentsMap args) throws IllegalArgumentException {
		imageFragment = (String)args.getArgumentValue(ARG_IMAGE_FRAGMENT);
	}
	
	@Override
	public String getDescription() {
		return "Inserts a image, with the side-effect of updating the ePub navigation documents";
	}

	@Override
	protected void doOperation() throws AuthorOperationException {		
		if (imageFragment==null) throw new AuthorOperationException(ARG_IMAGE_FRAGMENT+" argument is null");
		
		// Inserts this fragment at the caret position.
		int caretPosition  = getAuthorAccess().getEditorAccess().getCaretOffset();
		getAuthorAccess().getDocumentController().insertXMLFragment(imageFragment, caretPosition);
	}
}
