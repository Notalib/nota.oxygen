package nota.oxygen.epub;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorExtensionStateAdapter;

public class XHTMLAuthorExtensionStateListener extends
		AuthorExtensionStateAdapter {
	
	XHTMLHeadingAuthorListener headingListener;

	@Override
	public void activated(AuthorAccess authorAccess) {
		super.activated(authorAccess);
		if (headingListener == null) headingListener = new XHTMLHeadingAuthorListener();
		authorAccess.getDocumentController().addAuthorListener(headingListener);
	}

	@Override
	public void deactivated(AuthorAccess authorAccess) {
		// TODO Auto-generated method stub
		super.deactivated(authorAccess);
		authorAccess.getDocumentController().removeAuthorListener(headingListener);
	}

}
