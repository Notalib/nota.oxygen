package nota.oxygen.common.dtbook.v110;


import ro.sync.ecss.extensions.api.AuthorExtensionStateListener;
import ro.sync.ecss.extensions.api.ExtensionsBundle;
import ro.sync.ecss.extensions.api.Extension;
import ro.sync.ecss.extensions.api.UniqueAttributesRecognizer;

/**
 * Extension bundle for dtbook v1.1.0
 * @author Ole Holst Andersen (oha@nota.nu)
 */
public class Dtbook110ExtensionBundle extends ExtensionsBundle {

	@Override
	public String getDocumentTypeID() {
		// TODO Auto-generated method stub 
		return "dtbook110";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "dtbook v1.1.0 extension bundle";
	}
	
	private UniqueAttributesRecognizer uniqueRecognizer;

	@Override
	public AuthorExtensionStateListener createAuthorExtensionStateListener() {
		uniqueRecognizer = new Dtbook110UniqueAttributesRecognizer();
		return uniqueRecognizer;
	}

	@Override
	public UniqueAttributesRecognizer getUniqueAttributesIdentifier() {
		return uniqueRecognizer;
	}
	
	

}
