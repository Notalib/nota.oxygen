package nota.oxygen.dtbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import nota.oxygen.common.BaseAuthorOperation;
import nota.oxygen.common.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.content.TextContentIterator;
import ro.sync.ecss.extensions.api.content.TextContext;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

/**
 * Marks up words by lists of positive and possible words.
 * The possible words are marked up only after prompting the user, 
 * the positive words are marked up automatically.
 * Words and other settings are stored in an xml file, specified via. argument Settings file.
 * @author OHA
 */
public class MarkupByWordsOperation extends BaseAuthorOperation {

	private int startIndex = 0;
	
	@Override
	protected void doOperation() throws AuthorOperationException {
		loadSettingsDocument();
		markupByWords(getWords(WordTypes.Positive), false);
		markupByWords(getWords(WordTypes.Possible), true);
	}
	
	private void resetStart() throws AuthorOperationException {
		AuthorNode start = getStartNode();
		if (start==null) throw new AuthorOperationException("Found not find suitable start node");
		startIndex = start.getStartOffset();
	}
	
	private int[] findNextOccurence(String[] words) throws AuthorOperationException	{
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		TextContentIterator itr = docCtrl.getTextContentIterator(startIndex, docCtrl.getAuthorDocumentNode().getEndOffset());
		while (itr.hasNext()) {
			TextContext nextContext = itr.next();
			String next = nextContext.getText().toString();
			if (nextContext.getTextStartOffset()<startIndex) {
				next = next.substring(startIndex-nextContext.getTextStartOffset());
			}
			Map<Integer, String> foundWords = new HashMap<Integer, String>();
			for (int i=0; i<words.length; i++) {
				Pattern pat = Pattern.compile("\\b"+words[i]+"\\b");
				Matcher mat = pat.matcher(next);
				if (mat.find()) foundWords.put(mat.start(), words[i]);
			}
			if (foundWords.size()>0) {
				List<Integer> firstIndexes = new ArrayList<Integer>();
				firstIndexes.addAll(foundWords.keySet());
				Collections.sort(firstIndexes);
				int firstIndex = nextContext.getTextStartOffset()+firstIndexes.get(0);
				int lastIndex = firstIndex+foundWords.get(firstIndexes.get(0)).length()-1;
				startIndex = lastIndex;
				return new int[] {firstIndex, lastIndex};
			}
		}
		return new int[] {};
	}

	private void markupWord(int startOffset, int endOffset) throws AuthorOperationException
	{
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		docCtrl.surroundInFragment(getFragment(), startOffset, endOffset);
	}
	
	private void markupByWords(String[] words, boolean prompt) throws AuthorOperationException {
		resetStart();
		int count = 0;
		int res[] = findNextOccurence(words);
		while (res.length==2) {
			AuthorElement elem;
			try {
				elem = getElementAtOffset(res[0]);
			} catch (BadLocationException e) {
				throw new AuthorOperationException("Unexpectedly could not find element at offset "+res[0], e);
			}
			if (elem!=null) {
				if (!isElementExcluded(elem)) {
					getAuthorAccess().getEditorAccess().select(res[0], res[1]+1);
					boolean ok = true;
					if (prompt) {
						String msg = getMessage("POSSIBLE_QUESTION").replace("$word", getAuthorAccess().getEditorAccess().getSelectedText());
						int ans = showYesNoCancelMessage(getMessage("CAPTION"), msg, 1);
						if (ans==-1) break;
						if (ans!=1) ok = false;
					}
					if (ok) {
						markupWord(res[0], res[1]);
						count++;
					}
				}
			}
			res = findNextOccurence(words);
		}
		showMessage(getMessage("CAPTION"), getMessage(prompt?"POSSIBLE_RESULT":"POSITIVE_RESULT").replace("$count", ""+count));
		
	}
	
	protected String getMessage(String name) throws AuthorOperationException {
		try {
			String res = Utils.getXPath().evaluate("/MarkupByWords/Message[@name='"+name+"']", getSettingsDocument());
			if (res!=null) return res;
		} catch (XPathExpressionException e) {
			throw new AuthorOperationException("Could not find message "+name+":"+e.getMessage(), e);
		}
		return "";
	}
	
	protected AuthorNode getStartNode() throws AuthorOperationException {
		String startNodeXpath;
		AuthorNode res = getAuthorAccess().getDocumentController().getAuthorDocumentNode();
		try {
			startNodeXpath = (String)Utils.getXPath().evaluate("/MarkupByWords/StartNode/@xpath", getSettingsDocument());
		} catch (XPathExpressionException e) {
			return res;
		}
		AuthorNode[] candidates = getAuthorAccess().getDocumentController().findNodesByXPath(startNodeXpath, false, false, false);
		if (candidates.length>0) return candidates[0];
		return res;
	}
	
	private Document settingsDocument;
	
	protected void loadSettingsDocument() throws AuthorOperationException {
		InputStream is;
		try {
			is = new  FileInputStream(settingsFile);
		} catch (FileNotFoundException e) {
			throw new AuthorOperationException("Could not find settings file "+settingsFile);
		}
		DOMImplementationLS impl = Utils.getDOMImplementation();
		LSParser builder = impl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
		LSInput input = impl.createLSInput();
		input.setByteStream(is);
		input.setEncoding("UTF-8");
		settingsDocument =  builder.parse(input);
	}
	
	protected Document getSettingsDocument() throws AuthorOperationException {
		if (settingsDocument==null) {//Lazy loading of settings document
			loadSettingsDocument();
		} 
		return settingsDocument;
	}
	
	protected String getFragment() throws AuthorOperationException {
		Document settDoc = getSettingsDocument();
		NodeList list = settDoc.getDocumentElement().getElementsByTagName("Fragment");
		if (list.getLength()>0) {
			Element frag = (Element)list.item(0);
			for (int i=0; 0<frag.getChildNodes().getLength();i++) {
				if (frag.getChildNodes().item(i) instanceof Element) return Utils.serialize(frag.getChildNodes().item(i));
			}
		}
		throw new AuthorOperationException("Found no usable Fragment in settings file "+settingsFile);
	}
	
	private boolean isElementExcluded(AuthorElement element) throws AuthorOperationException {
		String ns = element.getNamespace();
		String xpathStatement = "/MarkupByWords/ExcludedParentElements/ExcludedParentElement/"+(ns==""?"":"mns:")+element.getLocalName();
		AttrValue classAttrVal = element.getAttribute("class");
		if (classAttrVal!=null) xpathStatement += "[class='"+classAttrVal.getRawValue()+"']";
		xpathStatement = "count("+xpathStatement+")";
		XPath xpath;
		if (ns=="") {
			xpath = Utils.getXPath();
		}
		else {
			xpath = Utils.getXPath("mns", ns);
		}
		try {
			return ((Double)xpath.evaluate(xpathStatement, getSettingsDocument(), XPathConstants.NUMBER))>0;
		} catch (XPathExpressionException e) {
			throw new AuthorOperationException("Unexpected XPathExpressionException exception occured while checking for element exclusion: "+e.getMessage());
		}
	}
	
	private enum WordTypes
	{
		Positive,
		Possible
	}
	
	protected String[] getWords(WordTypes type) throws AuthorOperationException
	{
		String elementName;
		switch (type)
		{
		case Positive:
			elementName = "PositiveWords";
			break;
		case Possible:
			elementName = "PossibleWords";
			break;
		default:
			throw new AuthorOperationException("Unexpected word type "+type.toString());
		}
		NodeList l = getSettingsDocument().getElementsByTagName(elementName);
		if (l.getLength()==0) return new String[] {};
		return getWords((Element)l.item(0));
	}
	
	private String[] getWords(Element wordsContainer) {
		if (wordsContainer==null) return new String[] {};
		NodeList wordsList = wordsContainer.getElementsByTagName("Word");
		String[] res = new String[wordsList.getLength()];
		for (int i=0; i<res.length; i++) {
			res[i] = wordsList.item(i).getTextContent();
		}
		return res;
	}
	
	private static String ARG_SETTINGS_FILE = "settings file";
	private static String settingsFile;

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor(ARG_SETTINGS_FILE, ArgumentDescriptor.TYPE_STRING, "Settings file")
		};
	}
 
	@Override
	public String getDescription() {
		return "Markup by words";
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		settingsFile = (String)args.getArgumentValue(ARG_SETTINGS_FILE);
	}

}
