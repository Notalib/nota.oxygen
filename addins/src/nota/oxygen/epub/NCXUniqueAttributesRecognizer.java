package nota.oxygen.epub;

import ro.sync.ecss.extensions.commons.id.DefaultUniqueAttributesRecognizer;
import ro.sync.ecss.extensions.commons.id.GenerateIDElementsInfo;

public class NCXUniqueAttributesRecognizer extends
		DefaultUniqueAttributesRecognizer {
	
	public NCXUniqueAttributesRecognizer()
	{
		super("id");
	}

	
	/**
	 * The default {@link GenerateIDElementsInfo}
	 */
	public static GenerateIDElementsInfo GENERATE_ID_DEFAULTS = new GenerateIDElementsInfo(
			true, 
			GenerateIDElementsInfo.DEFAULT_ID_GENERATION_PATTERN, 
			new String[] {
					"navPoint",
					"navTarget",
					"pageTarget"});

	
	@Override
	public String getDescription() {
		return "Unique Attribute Recognizer for NCX";
	}

	@Override
	protected GenerateIDElementsInfo getDefaultOptions() {
		return GENERATE_ID_DEFAULTS;
	}

}
