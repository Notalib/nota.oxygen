package dk.nota.oxygen.operations.html;

import java.util.List;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.AuthorSelectionModel;
import ro.sync.ecss.extensions.api.ContentInterval;
import ro.sync.ecss.extensions.api.SelectionInterpretationMode;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class ConvertTableCellsOperation extends XhtmlEpubAuthorOperation {
	
	private String cellType;

	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			AuthorSelectionModel selectionModel = getAuthorEditor()
					.getAuthorSelectionModel();
			if (selectionModel.getSelectionInterpretationMode() ==
					SelectionInterpretationMode.TABLE_ROW) {
				for (ContentInterval c : selectionModel.getSelectionIntervals())
					convertRows(getDocumentController().getNodesToSelect(
							c.getStartOffset(), c.getEndOffset()));
				return;
			}
			for (ContentInterval c : selectionModel.getSelectionIntervals())
				convertCells(getDocumentController().getNodesToSelect(
						c.getStartOffset(), c.getEndOffset()));
		} catch (BadLocationException e) {
			throw new AuthorOperationException(e.getMessage());
		}
	}
	
	private void convertCells(List<AuthorNode> nodes) {
		for (AuthorNode node : nodes) {
			if (node.getName().matches("t[dh]"))
				getDocumentController().renameElement((AuthorElement)node,
						cellType);
		}
	}
	
	private void convertRows(List<AuthorNode> nodes) {
		for (AuthorNode node : nodes) {
			if (node.getName().equals("tr"))
				convertCells(((AuthorElement)node).getContentNodes());
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor("cellType", ArgumentDescriptor
						.TYPE_CONSTANT_LIST, "The desired cell type",
						new String[] {"td", "th"}, "th"),
		};
	}

	@Override
	public String getDescription() {
		return "Convert selected cells to data or header cells";
	}

	@Override
	protected void parseArguments(ArgumentsMap arguments)
			throws IllegalArgumentException {
		cellType = (String)arguments.getArgumentValue("cellType");
	}

}
