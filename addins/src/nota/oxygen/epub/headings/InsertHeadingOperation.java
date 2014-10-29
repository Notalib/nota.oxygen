package nota.oxygen.epub.headings;

import java.util.List;

import javax.swing.text.BadLocationException;

import nota.oxygen.common.BaseAuthorOperation;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import sun.font.SunLayoutEngine;

public class InsertHeadingOperation extends BaseAuthorOperation {

	private static String ARG_HEADER_FRAGMENT = "header fragment";
	private String headerFragment;

	private static String ARG_PARENT_SECTION_XPATH = "parent section xpath";
	private String parentSectionXPath;
	
	private static String ARG_HEADING_OPERATION_TYPE = "heading operation type";
	private String headingOperationType;
	
	private static String SUBLEVEL_OPERATION_TYPE = "Sublevel";
	private static String SAME_LEVEL_OPERATION_TYPE = "Same level";

	private static String ARG_PREVIOUS_HEADING_XPATH = "previous heading xpath";
	private String previousHeadingXPath;
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[]{
				new ArgumentDescriptor(ARG_HEADER_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Header fragment"),
				new ArgumentDescriptor(ARG_PARENT_SECTION_XPATH, ArgumentDescriptor.TYPE_XPATH_EXPRESSION, "Parent section XPath"),
				new ArgumentDescriptor(ARG_HEADING_OPERATION_TYPE, ArgumentDescriptor.TYPE_CONSTANT_LIST, "Heading operation type", new String[] {SAME_LEVEL_OPERATION_TYPE, SUBLEVEL_OPERATION_TYPE}, "Same level"),
				new ArgumentDescriptor(ARG_PREVIOUS_HEADING_XPATH, ArgumentDescriptor.TYPE_XPATH_EXPRESSION, "Previous heading XPath")
				};
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		headerFragment = (String)args.getArgumentValue(ARG_HEADER_FRAGMENT);
		parentSectionXPath = (String)args.getArgumentValue(ARG_PARENT_SECTION_XPATH);
		headingOperationType = (String)args.getArgumentValue(ARG_HEADING_OPERATION_TYPE);
		previousHeadingXPath = (String)args.getArgumentValue(ARG_PREVIOUS_HEADING_XPATH);

	}

	@Override
	public String getDescription() {
		return "Inserts a heading, with the side-effect of updating the ePub navigation documents";
	}
	
	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			int selectionStartBefore = getAuthorAccess().getEditorAccess().getSelectionStart();
			int selectionEndBefore = getAuthorAccess().getEditorAccess().getSelectionEnd();
			int selectionOffset = 0;
			AuthorElement headingCandidate;
			try {
				 headingCandidate = getCurrentElement();
			} catch (BadLocationException e) {
				showMessage("No element is selected");
				return;
			}
			AuthorElement parentSection = findElementByXPath(parentSectionXPath, headingCandidate);
			if (parentSection != headingCandidate.getParent()) {
				showMessage("The selected element is not a direct child of a section");
				return;
			}
			try {
				if (findElementByXPath(previousHeadingXPath) == null) {
					showMessage("The selected element is before the heading of it's parent section");
					return;
				}
				AuthorDocumentController ctrl = getAuthorAccess().getDocumentController();
				ctrl.surroundInFragment(
						headerFragment, 
						headingCandidate.getStartOffset()+1, 
						headingCandidate.getEndOffset()-1);
				AuthorDocumentFragment headingFragment = ctrl.createDocumentFragment(headingCandidate.getContentNodes().get(0), true);
				int headingStartOffset = headingCandidate.getStartOffset();
				ctrl.deleteNode(headingCandidate);
				ctrl.insertFragment(headingStartOffset, headingFragment);
				List<AuthorNode> nodes = parentSection.getContentNodes();
				AuthorNode lastNode = nodes.get(nodes.size()-1);
				ctrl.surroundInFragment(
						String.format("<%s xmlns='%s'/>", parentSection.getLocalName(), parentSection.getNamespace()),
						headingStartOffset,
						lastNode.getEndOffset());
				AuthorElement newSection = (AuthorElement)lastNode.getParent();
				int offset = newSection.getStartOffset();
				if (SAME_LEVEL_OPERATION_TYPE.equals(headingOperationType)) {
					AuthorDocumentFragment newSectionFragment = ctrl.createDocumentFragment(newSection, true);
					ctrl.deleteNode(newSection);
					offset = parentSection.getEndOffset()+1;
					ctrl.insertFragment(offset, newSectionFragment);
					selectionOffset = 2;
				}
				else if (SUBLEVEL_OPERATION_TYPE.equals(headingOperationType)) {
					selectionOffset = 1;
				}
				getAuthorAccess().getEditorAccess().select(selectionStartBefore+selectionOffset, selectionEndBefore+selectionOffset);
			} 
			catch (BadLocationException e) {
				throw new AuthorOperationException(
						String.format("Unexpected BadLocationException while moving new level section: %s", e.getMessage()),
						e);
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
