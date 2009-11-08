package automarkup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorWorkspaceAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import common.BaseAuthorOperation;

/**
 * Automatically applies markup to words from a dictionary
 * @author Ole Holst Andersen (oha@nota.nu)
 */
public class AutoMarkupWordsFromDictionaryOperation extends BaseAuthorOperation {
	
	private static Set<String> getWords(String connection, String table)
			throws IllegalArgumentException {
		Set<String> words = new HashSet<String>();
		try	{
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			Connection con = DriverManager.getConnection(connection);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select * from ["+table+"]");
			while (rs.next()) {
				words.add(rs.getString("word"));
			}
				
		}
		catch (Exception e) {
			throw new IllegalArgumentException(
					"Unexpected "+e.getClass().getName()
					+" occured while retrieving word dictionary from database:\n"+e.getMessage(),
					e);
		}
		return words;
	}
	
	private List<Node> applyToTextNode(Node textNode) {
		List<Node> res = new ArrayList<Node>();
		String text = textNode.getTextContent();
		Matcher wdMatcher = wordPattern.matcher(text);
		while (wdMatcher.find()) {
			String word = text.substring(wdMatcher.start(), wdMatcher.end());
			if (words.contains(word)) {
				insertCount++;
				if (wdMatcher.start()>0) {
					res.addAll(applyToTextNode(ownerDoc.createTextNode(text.substring(0, wdMatcher.start()))));
				}
				Element e = (Element)ownerDoc.importNode(fragment, true);
				e.setTextContent(text.substring(wdMatcher.start(), wdMatcher.end()));
				res.add(e);
				if (wdMatcher.end()<text.length()-1) {
					res.addAll(applyToTextNode(ownerDoc.createTextNode(text.substring(wdMatcher.end()))));
				}
				return res;
			}
		}
		res.add(textNode);
		return res;
	}
	
	private void applyMarkup(Node nod) {
		switch (nod.getNodeType()){
		case Node.ELEMENT_NODE:
			NodeList children = nod.getChildNodes();
			for (int i=0; i<children.getLength(); i++) {
				applyMarkup(children.item(i));
			}
			break;
		case Node.TEXT_NODE:
			if (parents.contains(nod.getParentNode().getLocalName())) {
				List<Node> nodes = applyToTextNode(nod);
				if (!nodes.contains(nod)) {
					for (Node c : nodes) nod.getParentNode().insertBefore(c, nod);
					nod.getParentNode().removeChild(nod);
				}
			}
			break;
		}
	}
	

	@Override
	public void doOperation()
			throws AuthorOperationException {
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		AuthorWorkspaceAccess authorWrksp = getAuthorAccess().getWorkspaceAccess();
		docCtrl.beginCompoundEdit();
		try {
			AuthorNode nod = getCommonParentNodeOfSelection();
			if (nod==docCtrl.getAuthorDocumentNode()) {
				AuthorElement root = docCtrl.getAuthorDocumentNode().getRootElement();
				if (root.getLocalName()==contentRoot) {
					nod = root;
				}
				else {
					nod = root.getChild(contentRoot);
					if (nod == null) throw new AuthorOperationException("Could not find content root");
				}
			}
			String xml = docCtrl.serializeFragmentToXML(docCtrl.createDocumentFragment(nod, true));
			Element elem = deserialize(xml);
			ownerDoc = elem.getOwnerDocument();
			applyMarkup(elem);
			xml = serialize(elem);
			docCtrl.deleteNode(nod);
			docCtrl.insertXMLFragment(xml, getAuthorAccess().getCaretOffset());
			authorWrksp.showInformationMessage("Applied markup "+fragment.getLocalName()+" to "+insertCount+" words");
		}
		catch (AuthorOperationException e) {
			throw e;
		}
		catch (Exception e) {
			docCtrl.endCompoundEdit();
			docCtrl.getUndoManager().undo();
			throw new AuthorOperationException(
					"An unexpected "+e.getClass().getName()+" occured: "+e.getMessage(), 
					e);
		}
		docCtrl.endCompoundEdit();
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
			new ArgumentDescriptor(
					ARG_CONNECTION, 
					ArgumentDescriptor.TYPE_STRING, 
					"Database connection string",
					null,
					"jdbc:sqlserver://ntsdbb09;database=dtbook;integratedSecurity=true;"),
			new ArgumentDescriptor(
					ARG_TABLE, 
					ArgumentDescriptor.TYPE_STRING, 
					"Name of dictionary table",
					null,
					"arconyms"),
			new ArgumentDescriptor(
					ARG_FRAGMENT, 
					ArgumentDescriptor.TYPE_STRING, 
					"Xml fragment to surround words with",
					null,
					"<arconym></acronym>"),
			new ArgumentDescriptor(
					ARG_PARENTS, 
					ArgumentDescriptor.TYPE_STRING, 
					"A space separated list of allowed parent element local-names",
					null,
					"address author notice prodnote sidebar line a em strong dfn kbd code samp cite abbr acronym sub sup span bdo sent w q p doctitle docauthor levelhd h1 h2 h3 h4 h5 h6 hd dt dd li lic caption th td"),
			new ArgumentDescriptor(
					ARG_IGNORE_CASE, 
					ArgumentDescriptor.
					TYPE_CONSTANT_LIST, 
					"Indicates if the dictionary of words is case sensitive or not (true or false)", 
					new String[] {"true", "false"}, 
					"true"),
			new ArgumentDescriptor(
					ARG_WORD_CHARS, 
					ArgumentDescriptor.TYPE_STRING, 
					"The possible characters in words (regex notation)",
					null,
					"0-9a-zÂ‰ÊËÈÍÎ¯ÚÛÙˆ¸Á·‡‚ÌÏÔÒ˙„ÓA-Z≈ƒ∆»… Àÿ”‘÷‹¡⁄"),
			new ArgumentDescriptor(
					ARG_CONTENT_ROOT, 
					ArgumentDescriptor.TYPE_STRING, 
					"The root element of the content part of the document",
					null,
					"book"),
		};
	}
	
	//jdbc:sqlserver://ntsdbb09;integratedSecurity=true;
	private static String ARG_CONNECTION = "connection";
	private static String ARG_TABLE = "table";
	private static String ARG_FRAGMENT = "fragment";
	private static String ARG_PARENTS = "allowed parent elements";
	private static String ARG_IGNORE_CASE = "ignore case";
	private static String ARG_WORD_CHARS = "word characters";
	private static String ARG_CONTENT_ROOT = "content root";
	
	
	private Set<String> words;
	private Set<String> parents;
	private boolean ignoreCase;
	private Pattern wordPattern;
	private String wordChars;
	private Document ownerDoc;
	private Element fragment;
	private int insertCount;
	private String contentRoot;

	@Override
	public String getDescription() {
		return "Auto applies inline markup to words from a database stored dictionary";
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		String connection = (String)args.getArgumentValue(ARG_CONNECTION);
		String table = (String)args.getArgumentValue(ARG_TABLE);
		String f = (String)args.getArgumentValue(ARG_FRAGMENT);
		try {
			fragment = deserialize(f);
			
		} catch (AuthorOperationException e) {
			throw new IllegalArgumentException(
					"Could not deserialize fragment \""+f+"\":\n"+e.getMessage(),
					e);
		}
		if (fragment == null) {
			throw new IllegalArgumentException("Could not load fragment \""+f+"\""); 
		}
		insertCount = 0;
		words = getWords(connection, table);
		ignoreCase = (((String)args.getArgumentValue(ARG_IGNORE_CASE))=="true");
		wordChars = (String)args.getArgumentValue(ARG_WORD_CHARS);
		wordPattern = Pattern.compile("\\b["+wordChars+"]+\\b", ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
		contentRoot = (String)args.getArgumentValue(ARG_CONTENT_ROOT);
		parents = new HashSet<String>();
		String pArg = (String)args.getArgumentValue(ARG_PARENTS);
		for (String p : pArg.split(" ")) parents.add(p);
	}

}
