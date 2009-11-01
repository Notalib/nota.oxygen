package automarkup;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;

import common.BaseAuthorOperation;

public class MarkupWordFromDictionaryOperation extends
		BaseAuthorOperation {

	@Override
	public void doOperation()
			throws AuthorOperationException {
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		try {
			docCtrl.surroundInFragment(
					fragment, 
					getAuthorAccess().getSelectionStart(), 
					getAuthorAccess().getSelectionEnd()-1);
		}
		catch (AuthorOperationException e) {
			throw e;
		}
		catch (Exception e) {
			throw new AuthorOperationException(
				"An unexpected "+e.getClass().getName()+" occured: "+e.getMessage(),
				e);
		}
		
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
						ArgumentDescriptor.TYPE_FRAGMENT, 
						"Xml fragment to surround words with",
						null,
						"<arconym></acronym>"),
				new ArgumentDescriptor(
						ARG_PARENTS, 
						ArgumentDescriptor.TYPE_STRING, 
						"A space separated list of allowed parent element local-names",
						null,
						"address author notice prodnote sidebar line a em strong dfn kbd code samp cite abbr acronym sub sup span bdo sent w q p doctitle docauthor levelhd h1 h2 h3 h4 h5 h6 hd dt dd li lic caption th td")
			};
	}

	@Override
	public String getDescription() {
		return "Markup word from dictionary";
	}
	
	private static String ARG_CONNECTION = "connection";
	private static String ARG_TABLE = "table";
	private static String ARG_FRAGMENT = "fragment";
	private static String ARG_PARENTS = "allowed parent elements";
	
	private String connection;
	private String table;
	private String fragment;
	private Set<String> parents;

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		connection = (String)args.getArgumentValue(ARG_CONNECTION);
		table = (String)args.getArgumentValue(ARG_TABLE);
		fragment = (String)args.getArgumentValue(ARG_FRAGMENT);
		parents = new HashSet<String>();
		String pArg = (String)args.getArgumentValue(ARG_PARENTS);
		for (String p : pArg.split(" ")) parents.add(p);
	}

}
