package nota.oxygen.epub.headings;

import javax.swing.text.BadLocationException;

import nota.oxygen.common.BaseAuthorOperation;
import nota.oxygen.common.Utils;
import nota.oxygen.epub.EpubUtils;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

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
		try {
			AuthorElement heading;
			try {
				 heading = getCurrentElement();
			} catch (BadLocationException e) {
				throw new AuthorOperationException("No element was selected");
			}
			AuthorElement section = (AuthorElement)heading.getParent();
			if (section == null) {
				throw new AuthorOperationException("The selected element is the document root");
			}
			if (section.getParent() == null) {
				throw new AuthorOperationException("The containing section of the heading to delete is the root of the document");
			}
			if (!section.getLocalName().equals(parentSectionLocalName)) {
				throw new AuthorOperationException(String.format(
						"The selected heading is not the child of a %s element", parentSectionLocalName));
			}
			getAuthorAccess().getDocumentController().surroundInFragment(
					headerReplacementFragment, 
					heading.getStartOffset()+1, 
					heading.getEndOffset()-1);
			AuthorDocumentFragment headingReplacementFragment = getAuthorAccess().getDocumentController().createDocumentFragment(
					heading.getStartOffset()+1,  
					heading.getEndOffset()-1);
			int offset = heading.getStartOffset();
			getAuthorAccess().getDocumentController().deleteNode(heading);
			getAuthorAccess().getDocumentController().insertFragment(offset, headingReplacementFragment);
			AuthorElement prevSiblingWhenSection = findElementByXPath(
					String.format("(preceding-sibling::*[1])[local-name()='%s']", parentSectionLocalName));
			if (prevSiblingWhenSection == null && section.getElementsByLocalName("section").length > 0) {
				throw new AuthorOperationException(
						"Removing the selected heading will lead to inconsistent heading heirachy, since the heading is the first of its parent and has child sections");
			}
			AuthorDocumentFragment sectionContentFragment = getAuthorAccess().getDocumentController().createDocumentFragment(
					section.getStartOffset()+1,  
					section.getEndOffset()-1);
			int sectionStartOffset = section.getStartOffset();
			if (!getAuthorAccess().getDocumentController().deleteNode(section)) {
				throw new AuthorOperationException("Could not delete old section");
			}
			if (prevSiblingWhenSection != null) {
				//if the preceding element of section is also a section, merge section with this
				getAuthorAccess().getDocumentController().insertFragment(prevSiblingWhenSection.getEndOffset(), sectionContentFragment);
			}
			else {
				//insert
				getAuthorAccess().getDocumentController().insertFragment(sectionStartOffset, sectionContentFragment);
			}
		}
		catch (AuthorOperationException e) {
			throw e;
		}
		catch (Exception e) {
			throw new AuthorOperationException(
					String.format("An unexpected %s occured: %s", 
							e.getClass().getName(),
							e.getMessage()),
					e);
		}
		
	}

}
