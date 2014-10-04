package nota.oxygen.common.links;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;

import nota.oxygen.common.BaseAuthorOperation;

import org.apache.commons.validator.routines.EmailValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

/**
 * Operation to find and markup hyperlinks, including external http and https links, internal # links and e-mail links
 * @author OHA
 *
 */
public class MarkupLinksOperation extends BaseAuthorOperation {

	private int startIndex = 0;
	
	private int getStartOffset() throws AuthorOperationException {
		AuthorNode start = getStartNode();
		if (start==null) throw new AuthorOperationException("Found not find suitable start node");
		return start.getStartOffset();
	} 
	
	protected AuthorNode getStartNode() throws AuthorOperationException {
		AuthorNode res = getAuthorAccess().getDocumentController().getAuthorDocumentNode();
		AuthorNode[] candidates = getAuthorAccess().getDocumentController().findNodesByXPath(startNodeXPath, false, false, false);
		if (candidates.length>0) return candidates[0];
		return res;
	}

	@Override
	public String getDescription() {
		return "Find and markup hyperlinks";
	}
	
	private void markupLink(int startIndex, int endIndex) throws AuthorOperationException {
		getAuthorAccess().getEditorAccess().select(startIndex, endIndex);
		String text = getAuthorAccess().getEditorAccess().getSelectedText();
		URI link = expandLink(text);
		Element linkElem = getLinkElement();
		linkElem.setAttribute(refAttributeName, link.toString());
		if (!externalAttributeName.equals("")) {
			if (isInternalLink(link)) {
				linkElem.setAttribute(externalAttributeName, "false");
			}
			else {
				linkElem.setAttribute(externalAttributeName, "true");
			}
		}
		getAuthorAccess().getDocumentController().surroundInFragment(serialize(linkElem), startIndex, endIndex);
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		int foundCount = 0; 
		startIndex = getAuthorAccess().getEditorAccess().getCaretOffset();
		if (startIndex<getStartOffset()) startIndex = getStartOffset();
		int[] res = findNextOccurence();
		while (res.length==2) {
			foundCount++;
			getAuthorAccess().getEditorAccess().select(res[0], res[1]);
			int ans = showYesNoCancelMessage(getDescription(), "Do you wish to mark up link '"+getAuthorAccess().getEditorAccess().getSelectedText()+"'?", 1);
			if (ans==1) {
				markupLink(res[0], res[1]);
			}
			else if (ans==-1) {
				break;
			}
			res = findNextOccurence();
		}
		if (foundCount==0) showMessage(getDescription(), "Found no links to mark up");
	}
	
	String[] domainList;
	
	private String[] getDomainList() throws AuthorOperationException
	{
		if (domainList == null)
		{
			Element root = getDomainListDocument().getDocumentElement();
			if (root==null) 
			{
				domainList = new String[0];
			}
			else
			{
				NodeList domains = root.getElementsByTagName("domain");
				domainList = new String[domains.getLength()];
				for (int i=0; i<domainList.length; i++)
				{
					Node n = domains.item(i);
					Element d = (Element)n;
					Node suffix = d.getElementsByTagName("suffix").item(0);
					if (suffix==null)
					{
						domainList[i] = "";
					}
					else
					{
						domainList[i] = suffix.getTextContent().toLowerCase();
					}
				}
			}
		}
		return domainList;
	}
	
	private URI expandLink(URI link) throws AuthorOperationException {
		URI res;
		res = expandExternalLink(link);
		if (res!=null) return res;
		res = expandInternalLink(link);
		if (res!=null) return res;
		return expandMailLink(link);
	}
	
	private URI expandExternalLink(URI link) throws AuthorOperationException {
		String[] domains = getDomainList();
		if (link.getScheme()==null) {
			try
			{
				link = new URI("http://"+link.toString());
			}
			catch (URISyntaxException e)
			{
				return null;
			}
			if (!link.isAbsolute()) return null;
			if (link.getHost()==null) return null;
			if (link.getUserInfo()!=null) return null;
			for (int i=0; i<domains.length; i++) {
				if (link.getHost().toLowerCase().endsWith(domains[i])) return link; 
			}
			return null;
		}
		else if (link.getScheme().equals("http") || link.getScheme().equals("https")) {
			return link;
		}
		return null;
	}
	
	private boolean isInternalLink(URI link) throws AuthorOperationException {
		return expandInternalLink(link)!=null;
	}
	
	
	private URI expandInternalLink(URI link) throws AuthorOperationException {
		if (link.getScheme()!=null) return null;
		if (link.getSchemeSpecificPart()!=null) return null;
		if (link.getFragment()==null) return null;
		return link;
	}
	
	private URI expandMailLink(URI link) throws AuthorOperationException {
		if (link.getScheme()==null) {
			try
			{
				link = new URI("mailto:"+link.toString());
			}
			catch (URISyntaxException e)
			{
				return null;
			}
		}
		if (link.getScheme().equals("mailto"))
		{
			if (link.getFragment()!=null) return null;
			if (link.getSchemeSpecificPart()==null) return null;
			if (EmailValidator.getInstance(false).isValid(link.getSchemeSpecificPart())) return link; 
		}
		return null;
	}
	
	private boolean isLink(String linkCandidate) throws AuthorOperationException {
		return expandLink(linkCandidate)!=null;
	}
	
	private URI expandLink(String linkCandidate) throws AuthorOperationException {
		if (linkCandidate==null) return null;
		URI link;
		try
		{
			link = new URI(linkCandidate);
		}
		catch (URISyntaxException e)
		{
			return null;
		}
		return expandLink(link);
	}
	
	private int[] findNextOccurence() throws AuthorOperationException
	{
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		TextContentIterator itr = docCtrl.getTextContentIterator(startIndex, docCtrl.getAuthorDocumentNode().getEndOffset());
		while (itr.hasNext()) {
			TextContext nextContext = itr.next();
			try {
				AuthorElement elem = getElementAtOffset(nextContext.getTextStartOffset());
				if (elem.getLocalName().equals(linkLocalName)) continue;
			} catch (BadLocationException e) {
				throw new AuthorOperationException(e.getMessage(), e);
			}
			String next = nextContext.getText().toString();
			if (nextContext.getTextStartOffset()<startIndex) {
				next = next.substring(startIndex-nextContext.getTextStartOffset());
			}
			Pattern pat = Pattern.compile("\\b\\w[\\w.;/?:@=&$-_+!*'(),]+\\w\\b");
			Matcher mat = pat.matcher(next);
			while (mat.find()) {
				if (isLink(mat.group())) {
					startIndex = nextContext.getTextStartOffset()+mat.end();
					return new int[] {
							nextContext.getTextStartOffset()+mat.start(), 
							startIndex};
				}
			}
		}
		return new int[] {};
	}
	
	private Document domainListDocument;
	
	protected void loadDomainListDocument() throws AuthorOperationException {
		InputStream is;
		try {
			is = new  FileInputStream(domainListFile);
		} catch (FileNotFoundException e) {
			throw new AuthorOperationException("Could not find domain list file "+domainListFile);
		}
		DOMImplementationLS impl = getDOMImplementation();
		LSParser builder = impl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
		LSInput input = impl.createLSInput();
		input.setByteStream(is);
		input.setEncoding("UTF-8");
		domainListDocument =  builder.parse(input);
	}
	
	protected Document getDomainListDocument() throws AuthorOperationException {
		if (domainListDocument==null) {//Lazy loading of settings document
			loadDomainListDocument();
		} 
		return domainListDocument;
	}

	private Element getLinkElement() throws AuthorOperationException{
		return deserializeElement(linkFragment);
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		linkFragment = (String)args.getArgumentValue(ARG_LINK_FRAGMENT);
		try {
			Element linkElem = deserializeElement(linkFragment);
			linkLocalName = linkElem.getLocalName();
		}
		catch (AuthorOperationException e) {
			linkLocalName = "";
		}
		refAttributeName = (String)args.getArgumentValue(ARG_REF_ATTRIBUTE_NAME);
		externalAttributeName = (String)args.getArgumentValue(ARG_EXTERNAL_ATTRIBUTE_NAME);
		if (externalAttributeName==null) externalAttributeName = "";
		externalAttributeName = externalAttributeName.trim();
		domainListFile = (String)args.getArgumentValue(ARG_DOMAIN_LIST_FILE);
		startNodeXPath = (String)args.getArgumentValue(ARG_START_NODE_XPATH);
	}
	
	private static String ARG_LINK_FRAGMENT = "link fragment";
	private static String ARG_REF_ATTRIBUTE_NAME = "reference attribute name";
	private static String ARG_EXTERNAL_ATTRIBUTE_NAME = "external attribute name - leave empty if not available";
	private static String ARG_DOMAIN_LIST_FILE = "domain list file";
	private static String ARG_START_NODE_XPATH = "start node xpath";

	private String linkFragment;
	private String linkLocalName;
	private String refAttributeName;
	private String externalAttributeName;
	private String domainListFile;
	private String startNodeXPath;

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor(ARG_LINK_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "link xml fragment"),
				new ArgumentDescriptor(ARG_REF_ATTRIBUTE_NAME, ArgumentDescriptor.TYPE_STRING, "name of reference attribute (href)"),
				new ArgumentDescriptor(ARG_EXTERNAL_ATTRIBUTE_NAME, ArgumentDescriptor.TYPE_STRING, "name of external attribute (extenal)"),
				new ArgumentDescriptor(ARG_DOMAIN_LIST_FILE, ArgumentDescriptor.TYPE_STRING, "name of external attribute (external)"),
				new ArgumentDescriptor(ARG_START_NODE_XPATH, ArgumentDescriptor.TYPE_XPATH_EXPRESSION, "xpath statement specifying the node at which to start the search for links")
		};
	}

}
