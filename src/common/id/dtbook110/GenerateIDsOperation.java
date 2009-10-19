package common.id.dtbook110;

import ro.sync.ecss.extensions.api.UniqueAttributesRecognizer;

public class GenerateIDsOperation extends
		ro.sync.ecss.extensions.commons.id.GenerateIDsOperation {

	@Override
	protected UniqueAttributesRecognizer getUniqueAttributesRecognizer() {
		return new Dtbook110UniqueAttributesRecognizer();
	}

}
