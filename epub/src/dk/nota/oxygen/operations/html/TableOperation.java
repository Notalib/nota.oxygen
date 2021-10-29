package dk.nota.oxygen.operations.html;

import java.util.LinkedList;
import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class TableOperation extends XhtmlEpubAuthorOperation {
	
	private String cellFragment = "<td xmlns='http://www.w3.org/1999/xhtml'/>";
	private int columns;
	private boolean dissolve;
	private int rows;
	private String rowFragment = "<tr xmlns='http://www.w3.org/1999/xhtml'/>";
	private String tableFragment =
			"<table xmlns='http://www.w3.org/1999/xhtml'/>";
	
	private void determineTableLayout(int cells)
			throws AuthorOperationException {
		if (columns == 0) columns = (int)Math.ceil((double)cells / rows);
		else if (rows == 0) rows = (int)Math.ceil((double)cells / columns);
		else if (columns * rows < cells) throw new AuthorOperationException(
				String.format("A %s-by-%s table cannot contain %s cells",
						columns, rows, cells));
	}
	
	private void dissolveTable(AuthorElement table)
			throws AuthorOperationException, BadLocationException {
		AuthorElement caption = getFirstElementByXpath("caption", table);
		AuthorDocumentFragment captionFragment = null;
		if (caption != null) {
			if (hasBlockContent(caption)) captionFragment =
					getDocumentController().createDocumentFragment(caption
							.getStartOffset() + 1, caption.getEndOffset() - 1);
			else {
				captionFragment = getDocumentController()
						.createDocumentFragment(caption, true);
				((AuthorElement)captionFragment.getContentNodes().get(0))
						.setName("p");
			}
		}
		LinkedList<AuthorDocumentFragment> cellFragments =
				new LinkedList<AuthorDocumentFragment>();
		for (AuthorElement cell : getElementsByXpath(".//(td|th)", table)) {
			if (hasBlockContent(cell)) cellFragments.add(getDocumentController()
						.createDocumentFragment(cell.getStartOffset() + 1,
								cell.getEndOffset() - 1));
			else if (cell.getEndOffset() - cell.getStartOffset() != 1) {
				getDocumentController().removeAttribute("colspan", cell);
				getDocumentController().removeAttribute("rowspan", cell);
				getDocumentController().renameElement(cell, "p");
				cellFragments.add(getDocumentController()
						.createDocumentFragment(cell, true));
			}
		}
		int offset = table.getStartOffset();
		getDocumentController().deleteNode(table);
		if (captionFragment != null) {
			getDocumentController().insertFragment(offset, captionFragment);
			offset += captionFragment.getAcceptedLength();
		}
		for (AuthorDocumentFragment cellFragment : cellFragments) { 
			getDocumentController().insertFragment(offset, cellFragment);
			offset += cellFragment.getAcceptedLength();
		}
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			if (dissolve) { // If dissolve is set, dissolve table and return
				dissolveTable(getFirstElementByXpath("ancestor-or-self::table"));
				return;
			}
			// Get selected paragraphs
			LinkedList<AuthorElement> paragraphs =
					new LinkedList<AuthorElement>();
			for (AuthorNode node : getSelectedNodes()) {
				AuthorElement paragraph = getFirstElementByXpath(
						"ancestor-or-self::p", node);
				if (paragraph != null && !paragraphs.contains(paragraph))
					paragraphs.add(paragraph);
			}
			LinkedList<AuthorDocumentFragment> cellFragments =
					new LinkedList<AuthorDocumentFragment>();
			// Establish initial offset before deletion
			int offset = paragraphs.getFirst().getStartOffset();
			determineTableLayout(paragraphs.size());
			// For each selected paragraph: convert to table cell, create
			// document fragment, delete
			for (AuthorElement paragraph : paragraphs) {
				getDocumentController().renameElement(paragraph, "td");
				cellFragments.addLast(getDocumentController()
						.createDocumentFragment(paragraph, true));
				getDocumentController().deleteNode(paragraph);
			}
			// Insert an empty table and advance the offset to within the table
			getDocumentController().insertXMLFragment(tableFragment, offset++);
			for (int i = 1; i <= rows; i++) {
				// Insert an empty row and advance the offset to within the row
				getDocumentController().insertXMLFragment(rowFragment,
						offset++);
				// Insert cells; if no converted cells are left on the stack,
				// insert empty cells to complete row
				for (int j = 1; j <= columns; j++) {
					if (!cellFragments.isEmpty()) {
						AuthorDocumentFragment cellFragment =
								cellFragments.pop();
						getDocumentController().insertFragment(offset,
								cellFragment);
						// Advance offset by length of cell fragment
						offset += cellFragment.getAcceptedLength();
					} else {
						getDocumentController().insertXMLFragment(cellFragment,
								offset);
						// Advance offset by length of empty cell
						offset += 2;
					}
				}
				// Advance offset for next row (if any)
				offset++;
			}
		} catch (BadLocationException e) {
			throw new AuthorOperationException(e.toString());
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor("columns", ArgumentDescriptor
						.TYPE_STRING, "The desired number of columns"),
				new ArgumentDescriptor("rows", ArgumentDescriptor.TYPE_STRING,
						"The desired number of rows"),
				new ArgumentDescriptor("dissolve", ArgumentDescriptor
						.TYPE_CONSTANT_LIST, "Dissolve existing table",
						new String[] {"true", "false"}, "false")
		};
	}

	@Override
	public String getDescription() {
		return "Allows paragraphs to be converted to table cells";
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException {
		dissolve = Boolean.parseBoolean((String)arguments.getArgumentValue(
				"dissolve"));
		if (dissolve) return;
		int columns = Integer.parseInt((String)arguments.getArgumentValue(
				"columns"));
		int rows = Integer.parseInt((String)arguments.getArgumentValue("rows"));
		if (columns < 0 || rows < 0) throw new IllegalArgumentException(
				"Negative column or row count not allowed");
		if (columns == 0 && rows == 0) throw new IllegalArgumentException(
				"Column and row count cannot both be zero");
		this.columns = columns;
		this.rows = rows;
	}

}
