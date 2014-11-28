package nota.oxygen.epub;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorExtensionStateListener;

public class LangExtensionStateListener implements AuthorExtensionStateListener {

	@Override
	public String getDescription() {
		return "xml:lang to lang copy AuthorExtensionStateListener";
	}

	@Override
	public void activated(AuthorAccess authorAccess) {
		LangAuthorListener listener = new LangAuthorListener(authorAccess);
		listener.updateAllLangsFromXmlLangs();
		authorAccess.getDocumentController().addAuthorListener(listener);
	}

	@Override
	public void deactivated(AuthorAccess authorAccess) {
		//Nothing to do...
	}

}
