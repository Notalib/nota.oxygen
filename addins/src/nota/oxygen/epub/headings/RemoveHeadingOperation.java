package nota.oxygen.epub.headings;

import nota.oxygen.common.BaseAuthorOperation;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;

public class RemoveHeadingOperation extends BaseAuthorOperation {

	private static String ARG_HEADER_REPLACEMENT_FRAGMENT = "header replacement fragment";
	private String headerReplacementFragment;

	private static String ARG_PARENT_SECTION_LOCALNAME = "parent section localname";
	private String parentSectionLocalName;
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[]{
				new ArgumentDescriptor(ARG_HEADER_REPLACEMENT_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Header replacement fragment"),
				new ArgumentDescriptor(ARG_PARENT_SECTION_LOCALNAME, ArgumentDescriptor.TYPE_STRING, "Parent section localname")
				};
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		headerReplacementFragment = (String)args.getArgumentValue(ARG_HEADER_REPLACEMENT_FRAGMENT);
		parentSectionLocalName = (String)args.getArgumentValue(ARG_PARENT_SECTION_LOCALNAME);
	}

	@Override
	public String getDescription() {
		return "Remove heading (and parent section), with the side-effect of updating the ePub navigation documents";
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
//		try {
//			AuthorElement headingCandidate;
//			try {
//				 headingCandidate = getCurrentElement();
//			} catch (BadLocationException e) {
//				showMessage("No element is selected");
//				return;
//			}
//			AuthorElement parentSection = (AuthorElement)headingCandidate.getParent();
//			if (parentSection == null) {
//				showMessage("The selected heading the root element");
//				return;
//			}
//			if (!parentSection.getLocalName().equals(parentSectionLocalName)) {
//				showMessage(String.format("The selected heading is not the child of a %s element", parentSectionLocalName));
//				return;
//			}
//			AttrValue val = parentSection.getAttribute("id");
//			if (val == null) {
//				showMessage(String.format("The selected heading has a parent %s element with no id", parentSectionLocalName));
//				return;
//			}
//			String sectionId = val.getValue();
//			AuthorElement parentSection = findElementByXPath(parentSectionXPath, headingCandidate);
//			if (parentSection != headingCandidate.getParent()) {
//				showMessage("The selected element is not a direct child of a section");
//				return;
//			}
//		}
//		catch (AuthorOperationException e) {
//			throw e;
//		}
//		catch (Exception e) {
//			throw new AuthorOperationException(
//					String.format("An unexpected %s occured: %s", 
//							e.getClass().getName(),
//							e.getMessage()),
//					e);
//		}
		
	}

}
