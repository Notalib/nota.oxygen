package nota.oxygen.dtbook.v110;

import ro.sync.ecss.extensions.commons.id.DefaultUniqueAttributesRecognizer;
import ro.sync.ecss.extensions.commons.id.GenerateIDElementsInfo;

/**
 * {@link DefaultUniqueAttributesRecognizer} for dtbook v 1.1.0
 * @author Ole Holst Andersen (oha@nota.nu)
 */
public class Dtbook110UniqueAttributesRecognizer extends
		DefaultUniqueAttributesRecognizer {
	
	/**
	 * The default {@link GenerateIDElementsInfo}
	 */
	public static GenerateIDElementsInfo GENERATE_ID_DEFAULTS = new GenerateIDElementsInfo(
			true, 
			GenerateIDElementsInfo.DEFAULT_ID_GENERATION_PATTERN, 
			new String[] {
					"a", 
					"abbr", 
					"acronym", 
					"address", 
					"annoref", 
					"annotation", 
					"author", 
					"bdo", 
					"blockquote", 
					"bodymatter", 
					"br", 
					"caption", 
					"cite", 
					"code", 
					"col", 
					"colgroup", 
					"dd", 
					"dfn", 
					"div", 
					"dl", 
					"docauthor", 
					"doctitle", 
					"dt", 
					"em", 
					"frontmatter", 
					"h1", 
					"h2", 
					"h3", 
					"h4", 
					"h5", 
					"h6", 
					"hd", 
					"head", 
					"hr", 
					"img", 
					"imggroup", 
					"kbd", 
					"level", 
					"level1", 
					"level2", 
					"level3", 
					"level4", 
					"level5", 
					"level6", 
					"levelhd", 
					"li", 
					"lic", 
					"line", 
					"linenum", 
					"link", 
					"list", 
					"note", 
					"noteref", 
					"notice", 
					"p", 
					"pagenum", 
					"prodnote", 
					"q", 
					"rearmatter", 
					"samp", 
					"sent", 
					"sidebar", 
					"span", 
					"strong", 
					"style", 
					"sub", 
					"sup", 
					"table", 
					"tbody", 
					"td", 
					"tfoot", 
					"th", 
					"thead", 
					"tr", 
					"w"});
	
	/**
	 * Default constructor
	 */
	public Dtbook110UniqueAttributesRecognizer()
	{
		super("id");
	}
	
	

	@Override
	protected GenerateIDElementsInfo getDefaultOptions() {
		return GENERATE_ID_DEFAULTS;
	}

	@Override
	public String getDescription() {
		return "Unique Attribute Recognizer for dtbook";
	}

}
