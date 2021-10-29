package dk.nota.oxygen.operations.html;

import java.util.LinkedList;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class ListOperation extends XhtmlEpubAuthorOperation {
	
	private ListOperationType listOperationType;
	
	private void createList(LinkedList<AuthorElement> blocks, int start,
			int end) throws AuthorOperationException, BadLocationException {
		LinkedList<AuthorDocumentFragment> fragments =
				new LinkedList<AuthorDocumentFragment>();
		for (AuthorElement block : blocks)
			fragments.addFirst(getDocumentController().createDocumentFragment(
					block, true));
		getDocumentController().delete(start, end);
		getDocumentController().insertXMLFragment(listOperationType
				.getListFragment(), start++);
		for (AuthorDocumentFragment fragment : fragments) {
			AuthorNode nodeInFragment = fragment.getContentNodes().get(0);
			if (nodeInFragment.getName().equals("p")) {
				((AuthorElement)nodeInFragment).setName("li");
				getDocumentController().insertFragment(start, fragment);
			} else {
				getDocumentController().insertXMLFragment(listOperationType
						.getListItemFragment(), start++);
				getDocumentController().insertFragment(start--, fragment);
			}
		}
	}
	
	private void dissolveList(LinkedList<AuthorElement> listItems, int start,
			int end) throws AuthorOperationException, BadLocationException {
		AuthorNode list = getDocumentController().getNodeAtOffset(start);
		AuthorNode listParent = list.getParent();
		if (listParent.getName().equals("li")) {
			if (start - list.getStartOffset() == 1)
				floatInterval(start--, end--);
			else floatInterval(start++, end++);
			floatInterval(start, end);
			
		} else {
			for (AuthorElement listItem : listItems) {
				if (hasBlockContent(listItem)) {
					dissolveElement(listItem);
					end -= 2;
				} else {
					getDocumentController().renameElement(listItem, "p");
				}
			}
			floatInterval(start, end);
		}
	}
	
	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			LinkedList<AuthorElement> blocks = new LinkedList<AuthorElement>();
			for (AuthorNode node : getSelectedNodes()) {
				AuthorElement block = getFirstElementByXpath(listOperationType
						.getXpathForBlocks(), node);
				if (block != null && !blocks.contains(block))
					blocks.add(block);
			}
			int start = blocks.getFirst().getStartOffset();
			int end = blocks.getLast().getEndOffset();
			switch (listOperationType) {
			case INDENT_OL: case INDENT_UL:
				indent(start, end);
				return;
			case CREATE_OL: case CREATE_UL:
				createList(blocks, start, end);
				return;
			case DISSOLVE:
				dissolveList(blocks, start, end);
			}
		} catch (BadLocationException e) {
			throw new AuthorOperationException(e.toString());
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor("operationType", ArgumentDescriptor
						.TYPE_CONSTANT_LIST, "The desired list operation type",
						new String[] {"createOrdered", "createUnordered",
						"indentOrdered", "indentUnordered", "dissolve"},
						"createOrdered")
		};
	}

	@Override
	public String getDescription() {
		return "Allows manipulation of lists and list items";
	}
	
	private void indent(int start, int end) throws AuthorOperationException {
		getDocumentController().surroundInFragment(listOperationType
				.getListFragment(), start, end);
		getDocumentController().surroundInFragment(listOperationType
				.getListItemFragment(), start, end);
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException {
		String typeString = (String)arguments.getArgumentValue("operationType");
		switch (typeString) {
		case "createOrdered":
			listOperationType = ListOperationType.CREATE_OL;
			break;
		case "createUnordered":
			listOperationType = ListOperationType.CREATE_UL;
			break;
		case "indentOrdered":
			listOperationType = ListOperationType.INDENT_OL;
			break;
		case "indentUnordered":
			listOperationType = ListOperationType.INDENT_UL;
			break;
		case "dissolve":
			listOperationType = ListOperationType.DISSOLVE;
		}
	}
	
	public enum ListOperationType {
		
		CREATE_OL, CREATE_UL, INDENT_OL, INDENT_UL, DISSOLVE;
		
		public String getListFragment() {
			switch (this) {
			case CREATE_OL: case INDENT_OL:
				return "<ol xmlns='http://www.w3.org/1999/xhtml'/>";
			case CREATE_UL: case INDENT_UL:
				return "<ul xmlns='http://www.w3.org/1999/xhtml'/>";
			default: return null;
			}
		}
		
		public String getListItemFragment() {
			return "<li xmlns='http://www.w3.org/1999/xhtml'/>";
		}
		
		public String getName() {
			switch (this) {
			case CREATE_OL: case INDENT_OL: return "ol";
			case CREATE_UL: case INDENT_UL: return "ul";
			default: return null;
			}
		}
		
		public String getXpathForBlocks() {
			switch (this) {
			case CREATE_OL: case CREATE_UL:
				return "(ancestor-or-self::ol|ancestor-or-self::p|" +
				    	"ancestor-or-self::ul)[1]";
			default: return "ancestor-or-self::li[1]";
			}
		}
		
	}

}
