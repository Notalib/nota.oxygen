package nota.oxygen.common.links;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.xml.xpath.XPathExpressionException;

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
import ro.sync.ecss.extensions.api.node.AuthorNode;
import nota.oxygen.common.BaseAuthorOperation;

public class MarkupLinksOperation extends BaseAuthorOperation {

	private int startIndex = 0;
	
	private void resetStart() throws AuthorOperationException {
		AuthorNode start = getStartNode();
		if (start==null) throw new AuthorOperationException("Found not find suitable start node");
		startIndex = start.getStartOffset();
	}
	
	protected AuthorNode getStartNode() throws AuthorOperationException {
		AuthorNode res = getAuthorAccess().getDocumentController().getAuthorDocumentNode();
		AuthorNode[] candidates = getAuthorAccess().getDocumentController().findNodesByXPath(startNodeXPath, false, false, false);
		if (candidates.length>0) return candidates[0];
		return res;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Find and markup hyperlinks";
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		resetStart();
		int[] res = findNextOccurence();
		while (res.length==2) {
			showMessage("Found an occurance");
			String link;
			try {
				link = getAuthorAccess().getDocumentController().getText(res[0], res[1]-res[0]);
				if (!showOkCancelMessage("Found link "+link)) break;
			} catch (BadLocationException e) {
				throw new AuthorOperationException(e.getMessage());
			}
			res = findNextOccurence();
		}
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
					Node name = d.getElementsByTagName("name").item(0);
					if (name==null)
					{
						domainList[i] = "";
					}
					else
					{
						domainList[i] = name.getTextContent();
					}
				}
			}
		}
		return domainList;
	}
	
	
	private boolean isExternalLink(URI link, String[] domains) {
		if (!link.isAbsolute()) return false;
		if (link.getScheme()==null) {
			if (link.getPath()!=null) return true;
			for (int i=0; i<domains.length; i++){
				if (link.getHost().toLowerCase().endsWith("."+domains[i])) return true; 
			}
			return false;
		}
		else if (link.getScheme().equals("http") || link.getScheme().equals("https")) {
			return true;
		}
		return false;
	}
	
	private boolean isInternalLink(URI link)
	{
		if (link.getScheme()!=null) return false;
		if (link.getSchemeSpecificPart()!=null) return false;
		if (link.getFragment()==null) return false;
		return true;
	}
	
	private boolean isMailLink(URI link)
	{
		if (link.getScheme()==null || link.getScheme().equals("mailto"))
		{
			if (link.isOpaque() && link.getHost()!=null && link.getPort()==-1 && link.getUserInfo()!=null)
			{
				if (!link.getUserInfo().contains(":")) return true;
			}
		}
		return false;
	}
	
	private boolean isLink(String linkCandidate, String[] domains) {
		if (linkCandidate==null) return false;
		URI link;
		try
		{
			link = new URI(linkCandidate);
		}
		catch (URISyntaxException e)
		{
			return false;
		}
		if (isExternalLink(link, domains)) return true;
		if (isInternalLink(link)) return true;
		if (isMailLink(link)) return true;
		return false;
	}

	
	private int[] findNextOccurence() throws AuthorOperationException
	{
		String[] domains = getDomainList();
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		TextContentIterator itr = docCtrl.getTextContentIterator(startIndex, docCtrl.getAuthorDocumentNode().getEndOffset());
		while (itr.hasNext()) {
			TextContext nextContext = itr.next();
			String next = nextContext.getText().toString();
			if (nextContext.getTextStartOffset()<startIndex) {
				next = next.substring(startIndex-nextContext.getTextStartOffset());
			}
			Pattern pat = Pattern.compile("\\b\\w[\\w\\./@;:]+\\w\\b");
			Matcher mat = pat.matcher(next);
			while (mat.find()) {
				if (show) {
					if (!showOkCancelMessage("Checking candidate "+mat.group())) show = false;
				}
						
				if (isLink(mat.group(), domains)) {
					return new int[] {
							nextContext.getTextStartOffset()+mat.start(), 
							nextContext.getTextStartOffset()+mat.end()};
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


	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		linkFragment = (String)args.getArgumentValue(ARG_LINK_FRAGMENT);
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
