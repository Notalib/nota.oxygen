package nota.oxygen.epub;

import ro.sync.ecss.extensions.api.AuthorExtensionStateListener;
import ro.sync.ecss.extensions.api.ExtensionsBundle;
import ro.sync.ecss.extensions.api.UniqueAttributesRecognizer;

public class NCXExtensionsBundle extends ExtensionsBundle {

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "ncx extension bundle";
	}

	@Override
	public String getDocumentTypeID() {
		// TODO Auto-generated method stub
		return "ncx";
	}

	@Override
	public AuthorExtensionStateListener createAuthorExtensionStateListener() {
		return getUniqueAttributesIdentifier();
	}
	
	NCXUniqueAttributesRecognizer uniqueAttributesRecognizer;

	@Override
	public UniqueAttributesRecognizer getUniqueAttributesIdentifier() {
		if (uniqueAttributesRecognizer == null) {
			uniqueAttributesRecognizer = new NCXUniqueAttributesRecognizer();
		}
		return uniqueAttributesRecognizer;
	}
	
	

}
