package common.id.dtbook110;

import ro.sync.ecss.extensions.api.UniqueAttributesRecognizer;

/**
 * Generates id for the elements in the current selection using a {@link Dtbook110UniqueAttributesRecognizer}
 * @author Ole Holst Andersen (oha@nota.nu)
 */
public class GenerateIDsOperation extends
		ro.sync.ecss.extensions.commons.id.GenerateIDsOperation {

	@Override
	protected UniqueAttributesRecognizer getUniqueAttributesRecognizer() {
		return new Dtbook110UniqueAttributesRecognizer();
	}

}
