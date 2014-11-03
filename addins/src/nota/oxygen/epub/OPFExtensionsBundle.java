package nota.oxygen.epub;

import ro.sync.ecss.extensions.api.AuthorExtensionStateListener;
import ro.sync.ecss.extensions.api.ExtensionsBundle;
import ro.sync.ecss.extensions.api.UniqueAttributesRecognizer;

public class OPFExtensionsBundle extends ExtensionsBundle {

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "opf extension bundle";
	}

	@Override
	public String getDocumentTypeID() {
		// TODO Auto-generated method stub
		return "opf";
	}

	@Override
	public AuthorExtensionStateListener createAuthorExtensionStateListener() {
		return getUniqueAttributesIdentifier();
	}
	
	OPFUniqueAttributesRecognizer uniqueAttributesRecognizer;

	@Override
	public UniqueAttributesRecognizer getUniqueAttributesIdentifier() {
		if (uniqueAttributesRecognizer == null) {
			uniqueAttributesRecognizer = new OPFUniqueAttributesRecognizer();
		}
		return uniqueAttributesRecognizer;
	}

}
