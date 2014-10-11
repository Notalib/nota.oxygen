package nota.oxygen.epub;

import ro.sync.ecss.extensions.api.AuthorExtensionStateListener;
import ro.sync.ecss.extensions.api.AuthorExtensionStateListenerDelegator;

public class XHTMLExtensionsBundle extends ro.sync.ecss.extensions.xhtml.XHTMLExtensionsBundle {
	@Override
	public AuthorExtensionStateListener createAuthorExtensionStateListener() {
		AuthorExtensionStateListenerDelegator result = new AuthorExtensionStateListenerDelegator();
		result.addListener(super.createAuthorExtensionStateListener());
		result.addListener(new XHTMLAuthorExtensionStateListener());
		return result;
	}
}
