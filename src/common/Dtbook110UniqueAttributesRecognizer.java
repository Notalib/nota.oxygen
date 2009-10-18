package common;

import ro.sync.ecss.extensions.commons.id.DefaultUniqueAttributesRecognizer;
import ro.sync.ecss.extensions.commons.id.GenerateIDElementsInfo;

public class Dtbook110UniqueAttributesRecognizer extends
		DefaultUniqueAttributesRecognizer {
	
	public Dtbook110UniqueAttributesRecognizer()
	{
		super("id");
	}

	@Override
	protected GenerateIDElementsInfo getDefaultOptions() {
		String[] elems = new String[] {
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
			"book", 
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
			"dtbook", 
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
			"meta", 
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
			"title", 
			"tr", 
			"w"		
		};
		return new GenerateIDElementsInfo(true, GenerateIDElementsInfo.DEFAULT_ID_GENERATION_PATTERN, elems);
	}

	@Override
	public String getDescription() {
		return "Unique Attribute Recognizer for dtbook";
	}

}
