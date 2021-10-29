package dk.nota.oxygen.listeners.html;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorExtensionStateAdapter;

public class XhtmlExtensionStateListener
		extends AuthorExtensionStateAdapter {
	
	private LangAuthorListener authorListener;

	@Override
	public void activated(AuthorAccess authorAccess) {
		authorListener = new LangAuthorListener(authorAccess);
		authorAccess.getDocumentController().addAuthorListener(authorListener);
	}

	@Override
	public void deactivated(AuthorAccess authorAccess) {
		authorAccess.getDocumentController().removeAuthorListener(
				authorListener);
		authorListener = null;
	}

}
