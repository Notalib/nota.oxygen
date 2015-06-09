package nota.oxygen.epub.notes;

import javax.swing.text.BadLocationException;
import nota.oxygen.common.BaseAuthorOperation;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class RemoveNoteRefOperation extends BaseAuthorOperation {
	private static String ARG_SINGLE_NOTE = "single note";
	private static String[] YES_NO = new String[] {"yes", "no"};
	private boolean singleNote;
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] { 
				new ArgumentDescriptor(ARG_SINGLE_NOTE, ArgumentDescriptor.TYPE_CONSTANT_LIST, "Single note", YES_NO, YES_NO[1])
		};
	}
	
	@Override
	protected void parseArguments(ArgumentsMap args) throws IllegalArgumentException {
		String temp = (String)args.getArgumentValue(ARG_SINGLE_NOTE);
		singleNote = YES_NO[0].equals(temp);
	}
	
	@Override
	public String getDescription() {
		return "Remove noteref(s)";
	}
	
	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			// remove noteref(s)
			if (singleNote) removeSingleNoteRef();
			else removeMultiNoteRefs();
		} catch (AuthorOperationException e) {
			throw e;
		} catch (Exception e) {
			throw new AuthorOperationException(String.format("An unexpected %s occured: %s", e.getClass().getName(), e.getMessage()), e);
		}
	}
	
	private void removeSingleNoteRef() throws AuthorOperationException {
		// get current and parent element
		AuthorElement currentElement, parentElement, listElement;
		try {
			currentElement = getCurrentElement();
			parentElement = (AuthorElement) getCurrentElement().getParent();
		} catch (BadLocationException e) {
			showMessage("No element is selected");
			return;
		}

		// find li element
		if (currentElement.getName().equals("li")) {
			listElement = currentElement;
		} else if (currentElement.getName().equals("a") && parentElement.getName().equals("li")) {
			currentElement = parentElement;
		} else if (parentElement.getName().equals("li")) {
			listElement = parentElement;
		} else {
			showMessage("Noteref can not be removed from here");
			return;
		}

		// check if noteref exists
		AuthorElement[] noterefs = currentElement.getElementsByLocalName("a");
		if (noterefs.length > 0) {
			for (AuthorNode noteref : noterefs) {
				removeNoteRef(noteref);
			}
		}
	}
	
	private void removeMultiNoteRefs() throws AuthorOperationException {
		AuthorNode[] notes = getAuthorAccess().getDocumentController().findNodesByXPath("/html/body/ol/li", true, true, true);
		if (notes == null) {
			throw new AuthorOperationException("Found no notes in document");
		}
		
		for (AuthorNode note : notes) {
			AuthorElement listElement = ((AuthorElement) note);

			// find current element
			AuthorElement currentElement;
			if (listElement.getElementsByLocalName("p").length == 0) {
				currentElement = listElement;
			} else {
				currentElement = listElement.getElementsByLocalName("p")[0];
			}

			// check if noteref exists
			AuthorElement[] noterefs = currentElement.getElementsByLocalName("a");
			if (noterefs.length > 0) {
				for (AuthorNode noteref : noterefs) {
					removeNoteRef(noteref);
				}
			}
		}
	}
	
	private void removeNoteRef(AuthorNode noteref) throws AuthorOperationException {
		AuthorDocumentController ctrl = getAuthorAccess().getDocumentController();
		try {
			ctrl.beginCompoundEdit();
			ctrl.deleteNode(noteref);
			ctrl.endCompoundEdit();
		} catch (Exception e) {
			ctrl.cancelCompoundEdit();
			throw new AuthorOperationException(String.format("An unexpected %s occured: %s", e.getClass().getName(), e.getMessage()), e);
		}
	}
}
