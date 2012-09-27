package nota.oxygen.common.links;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import nota.oxygen.common.BaseAuthorOperation;

public class MarkupLinksOperation extends BaseAuthorOperation {

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Find and markup hyperlinks";
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		
	}
	
	String[] domainList;
	
	private String[] getDomainList() throws AuthorOperationException
	{
		if (domainList == null)
		{
			Element root = getDomainListDocument().getDocumentElement();
			if (root==null) 
			{
				domainList String[0];
			}
			else
			{
				NodeList domains = getDomainListDocument().getElementsByTagName("domain");
				domainList = new String[domains.getLength()];
				for (int i=0; i<domainList.length; i++)
				{
					NodeList l = ((Element)domains.item(i)).getElementsByTagName("name");
					if (l.getLength()>0)
					{
						domainList[i] = "";
					}
					else
					{
						domainList[i] = l.item(0).getTextContent();
					}
				}
			}
		}
		return domainList;
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
	}
	
	private static String ARG_LINK_FRAGMENT = "link fragment";
	private static String ARG_REF_ATTRIBUTE_NAME = "reference attribute name";
	private static String ARG_EXTERNAL_ATTRIBUTE_NAME = "external attribute name - leave empty if not available";
	private static String ARG_DOMAIN_LIST_FILE = "domain list file";

	private String linkFragment;
	private String refAttributeName;
	private String externalAttributeName;
	private String domainListFile;

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor(ARG_LINK_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "link xml fragment"),
				new ArgumentDescriptor(ARG_REF_ATTRIBUTE_NAME, ArgumentDescriptor.TYPE_STRING, "name of reference attribute (href)"),
				new ArgumentDescriptor(ARG_EXTERNAL_ATTRIBUTE_NAME, ArgumentDescriptor.TYPE_STRING, "name of external attribute (external)"),
				new ArgumentDescriptor(ARG_DOMAIN_LIST_FILE, ArgumentDescriptor.TYPE_STRING, "name of external attribute (external)")
		};
	}

}
