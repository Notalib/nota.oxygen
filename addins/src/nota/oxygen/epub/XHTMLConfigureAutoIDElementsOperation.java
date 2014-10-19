package nota.oxygen.epub;

import ro.sync.ecss.extensions.commons.id.ConfigureAutoIDElementsOperation;
import ro.sync.ecss.extensions.commons.id.GenerateIDElementsInfo;

public class XHTMLConfigureAutoIDElementsOperation extends
		ConfigureAutoIDElementsOperation {

	@Override
	public String getDescription() {
		return "Configure auto id elements for XHTML in epub";
	}

	@Override
	protected GenerateIDElementsInfo getDefaultOptions() {
		return XHTMLUniqueAttributesRecognizer.GENERATE_ID_DEFAULTS;
	}

	@Override
	protected String getListMessage() {
		return "Select elements for which you want auto id generation turned on";
	}

}
