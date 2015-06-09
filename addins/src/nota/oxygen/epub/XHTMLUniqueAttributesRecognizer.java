package nota.oxygen.epub;

import ro.sync.ecss.extensions.commons.id.DefaultUniqueAttributesRecognizer;
import ro.sync.ecss.extensions.commons.id.GenerateIDElementsInfo;

public class XHTMLUniqueAttributesRecognizer extends
		DefaultUniqueAttributesRecognizer {
	
	public XHTMLUniqueAttributesRecognizer()
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
					"a", 
					"aside",
					"body",
					"div",
					"h1", 
					"h2", 
					"h3", 
					"h4", 
					"h5", 
					"h6", 
					"hd", 
					"img",
					"section",
					"span",
					"td", 
					"th", 
					"tr"});

	
	@Override
	public String getDescription() {
		return "Unique Attribute Recognizer for XHTML in epub";
	}

	@Override
	protected GenerateIDElementsInfo getDefaultOptions() {
		return GENERATE_ID_DEFAULTS;
	}

}
