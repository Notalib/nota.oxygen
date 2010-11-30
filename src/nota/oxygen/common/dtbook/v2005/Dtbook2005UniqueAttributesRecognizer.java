package nota.oxygen.common.dtbook.v2005;

import java.util.Random;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.commons.id.DefaultUniqueAttributesRecognizer;
import ro.sync.ecss.extensions.commons.id.GenerateIDElementsInfo;

/**
 * {@link DefaultUniqueAttributesRecognizer} for dtbook v 1.1.0
 * @author Ole Holst Andersen (oha@nota.nu)
 */
public class Dtbook2005UniqueAttributesRecognizer extends
		DefaultUniqueAttributesRecognizer {
	
	private long nextLuidSource = -1;
	
	/* (non-Javadoc)
	 * @see ro.sync.ecss.extensions.commons.id.DefaultUniqueAttributesRecognizer#generateUniqueIDFor(java.lang.String, ro.sync.ecss.extensions.api.node.AuthorElement)
	 */
	@Override
	protected String generateUniqueIDFor(String idGenerationPattern,
			AuthorElement element) {
		AuthorDocumentController docCtrl = authorAccess.getDocumentController();
		String idVal = super.generateUniqueIDFor(idGenerationPattern, element);
		if (idVal.indexOf("${luid}")!=-1) {
			String luid = idVal.replace("${luid}", getNextLUID());
			try {
				while (docCtrl.findNodesByXPath("//*[@id='"+luid+"']", true, true, true).length>0) {
					luid = idVal.replace("${luid}", getNextLUID());
				}
			}
			catch (AuthorOperationException e) {}
			return luid;
		}
		return  idVal;
	}
	
	private String getNextLUID() {
		if (nextLuidSource==-1) {
			Random randomGenerator = new Random();
			nextLuidSource = randomGenerator.nextLong() & ~0xFFFF;
		}
		String luid = "00000000"+Long.toHexString(nextLuidSource);
		luid = luid.substring(0, 8);
		nextLuidSource++;
		return luid;
	}

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
					"bridgehead",
					"byline",
					"caption",
					"cite",
					"code",
					"col",
					"colgroup",
					"covertitle",
					"dateline",
					"dd",
					"dfn",
					"div",
					"dl",
					"docauthor",
					"doctitle",
					"dt",
					"em",
					"epigraph",
					"frontmatter",
					"h1",
					"h2",
					"h3",
					"h4",
					"h5",
					"h6",
					"hd",
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
					"li",
					"lic",
					"line",
					"linegroup",
					"linenum",
					"link",
					"list",
					"note",
					"noteref",
					"p",
					"pagenum",
					"poem",
					"prodnote",
					"q",
					"rearmatter",
					"samp",
					"sent",
					"sidebar",
					"span",
					"strong",
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
					"w"});
	
	/**
	 * Default constructor
	 */
	public Dtbook2005UniqueAttributesRecognizer()
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
