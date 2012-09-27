package nota.oxygen.common.links;

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
		// TODO Auto-generated method stub

	}
	
	private String[] getDomainList()
	{
		
		return null;
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		linkFragment = (String)args.getArgumentValue(ARG_LINK_FRAGMENT);
		refAttributeName = (String)args.getArgumentValue(ARG_REF_ATTRIBUTE_NAME);
		externalAttributeName = (String)args.getArgumentValue(ARG_EXTERNAL_ATTRIBUTE_NAME);
		if (externalAttributeName==null) externalAttributeName = "";
		externalAttributeName = externalAttributeName.trim();
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
				new ArgumentDescriptor(ARG_EXTERNAL_ATTRIBUTE_NAME, ArgumentDescriptor.TYPE_STRING, "name of external attribute (external)")
		};
	}

}
