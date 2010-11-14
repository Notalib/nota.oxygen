package common.dtbook.v2005;


import ro.sync.ecss.extensions.api.AuthorExtensionStateListener;
import ro.sync.ecss.extensions.api.ExtensionsBundle;
import ro.sync.ecss.extensions.api.UniqueAttributesRecognizer;
import ro.sync.ecss.extensions.api.structure.AuthorOutlineCustomizer;

/**
 * Extension bundle for dtbook 2005
 * @author Ole Holst Andersen (oha@nota.nu)
 */
public class Dtbook2005ExtensionBundle extends ExtensionsBundle {

	@Override
	public String getDocumentTypeID() {
		return "dtbook2005";
	}

	@Override
	public String getDescription() {
		return "dtbook 2005 extension bundle";
	}
	
	private UniqueAttributesRecognizer uniqueRecognizer;
	

	@Override
	public AuthorExtensionStateListener createAuthorExtensionStateListener() {
		return new Dtbook2005UniqueAttributesRecognizer();
	}

	@Override
	public UniqueAttributesRecognizer getUniqueAttributesIdentifier() {
		return uniqueRecognizer;
	}

	@Override
	public AuthorOutlineCustomizer createAuthorOutlineCustomizer() {
		return new Dtbook2005AuthorOutlineCustomizer();
	}
	
	
	

}
