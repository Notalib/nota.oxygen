package nota.oxygen.epub;

import ro.sync.ecss.extensions.api.AuthorExtensionStateListener;
import ro.sync.ecss.extensions.api.AuthorExtensionStateListenerDelegator;
import ro.sync.ecss.extensions.api.UniqueAttributesRecognizer;

public class XHTMLExtensionsBundle extends ro.sync.ecss.extensions.xhtml.XHTMLExtensionsBundle {
	
	@Override
	public AuthorExtensionStateListener createAuthorExtensionStateListener() {
		if (stateListener == null) {
			stateListener = new AuthorExtensionStateListenerDelegator();
			stateListener.addListener(getUniqueAttributesIdentifier());
			stateListener.addListener(getLangStateListener());
		}
		return stateListener;
	}
	
	XHTMLUniqueAttributesRecognizer uniqueAttributesRecognizer;
	
	LangExtensionStateListener langStateListener;
	
	AuthorExtensionStateListenerDelegator stateListener;

	@Override
	public UniqueAttributesRecognizer getUniqueAttributesIdentifier() {
		if (uniqueAttributesRecognizer == null) uniqueAttributesRecognizer = new XHTMLUniqueAttributesRecognizer();
		return uniqueAttributesRecognizer;
	}
	
	public LangExtensionStateListener getLangStateListener() {
		if (langStateListener == null) langStateListener = new LangExtensionStateListener();
		return langStateListener;
	}
	
}
