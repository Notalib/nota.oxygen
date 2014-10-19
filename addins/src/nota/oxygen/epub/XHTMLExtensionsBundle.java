package nota.oxygen.epub;

import ro.sync.ecss.extensions.api.AuthorExtensionStateListener;
import ro.sync.ecss.extensions.api.UniqueAttributesRecognizer;

public class XHTMLExtensionsBundle extends ro.sync.ecss.extensions.xhtml.XHTMLExtensionsBundle {
	
	@Override
	public AuthorExtensionStateListener createAuthorExtensionStateListener() {
		return getUniqueAttributesIdentifier();
	}
	
	XHTMLUniqueAttributesRecognizer uniqueAttributesRecognizer;

	@Override
	public UniqueAttributesRecognizer getUniqueAttributesIdentifier() {
		if (uniqueAttributesRecognizer == null) uniqueAttributesRecognizer = new XHTMLUniqueAttributesRecognizer();
		return uniqueAttributesRecognizer;
	}
}
