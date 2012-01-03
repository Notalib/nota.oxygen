package nota.oxygen.common.notes;

import nota.oxygen.common.BaseAuthorOperation;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;

/**
 * Markup a nota and search for references to it.
 * @author OHA
 *
 */
public class MarkupNoteOperation extends BaseAuthorOperation {

	@Override
	public String getDescription() {
		return "Does note and noteref markup";
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
			int startSel = getSelectionStart();
			int endSel = getSelectionEnd();
			
			AuthorElement firstAthElem = (AuthorElement)docCtrl.getNodeAtOffset(startSel+1);
			AuthorElement lastAthElem = (AuthorElement)docCtrl.getNodeAtOffset(endSel-1);
			if (firstAthElem.getParent()!=lastAthElem.getParent()) {
				String msg = "Selected nodes are not siblings:\n";
				msg += "first element: "+firstAthElem.getLocalName()+"[@id='"+firstAthElem.getAttribute("id")+"']\n";
				msg += "last element: "+lastAthElem.getLocalName()+"[@id='"+lastAthElem.getAttribute("id")+"']\n";
				showMessage(msg);
				return;
			}
			startSel = firstAthElem.getStartOffset();
			endSel = lastAthElem.getEndOffset();
			docCtrl.surroundInFragment(noteFragment, startSel, endSel);
			AuthorElement note = getElementAtOffset(getAuthorAccess().getEditorAccess().getBalancedSelectionStart());
			tryToFindNoteIdentifier(note);
			getAuthorAccess().getEditorAccess().setCaretPosition(note.getEndOffset()-2);
		}
		catch (AuthorOperationException e) {
			throw e;
		}
		catch (Exception e) {
			throw new AuthorOperationException( 
					"Unexpected "+e.getClass().getName()+"occured: "+e.getMessage(),
					e);
		}
	}
	
	private void tryToFindNoteIdentifier(AuthorElement note) throws AuthorOperationException {
		if (note==null) return;
		Element noteElem = deserializeElement(serialize(note));
		Node firstChildElement = noteElem.getFirstChild();
		if (firstChildElement==null) return;
		if (firstChildElement.getNodeType()!=Node.ELEMENT_NODE) return;
		Node firstText = firstChildElement.getFirstChild();
		if (firstText==null) return;
		if (firstText.getNodeType()!=Node.TEXT_NODE) return;
		if (firstText.getTextContent().trim().isEmpty()) return;
		firstChildElement.removeChild(firstText);
		Element noteIdentifierElement = (Element)noteElem.getOwnerDocument().importNode( deserializeElement(noteIdentifierFragment), true);
		String text = firstText.getTextContent();
		if (text.indexOf(' ')!=-1) {
			Text restTextNode = noteElem.getOwnerDocument().createTextNode(text.substring(text.indexOf(' ')));
			firstChildElement.insertBefore(restTextNode, firstChildElement.getFirstChild());
			text = text.substring(0, text.indexOf(' '));
		}
		noteIdentifierElement.insertBefore(noteElem.getOwnerDocument().createTextNode(text), noteIdentifierElement.getFirstChild());
		firstChildElement.insertBefore(noteIdentifierElement, firstChildElement.getFirstChild());
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		docCtrl.insertXMLFragment(serialize(noteElem), note, AuthorConstants.POSITION_BEFORE);
		docCtrl.deleteNode(note);
	}
	
	private static String ARG_NOTE_FRAGMENT = "note fragment";
	private String noteFragment;
	
	private static String ARG_NOTE_IDENTIFIER_FRAGMENT = "note identifier fragment";
	private String noteIdentifierFragment;

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor(ARG_NOTE_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "The note fragment"),
				new ArgumentDescriptor(ARG_NOTE_IDENTIFIER_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "The note identifier fragment"),
		};
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		noteFragment = (String)args.getArgumentValue(ARG_NOTE_FRAGMENT);
		noteIdentifierFragment = (String)args.getArgumentValue(ARG_NOTE_IDENTIFIER_FRAGMENT);
	}

}
