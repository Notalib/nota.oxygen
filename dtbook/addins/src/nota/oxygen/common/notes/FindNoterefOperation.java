package nota.oxygen.common.notes;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import nota.oxygen.common.BaseAuthorOperation;

/**
 * Finds and marks up noterefs for the current note
 * @author OHA
 *
 */
public class FindNoterefOperation extends BaseAuthorOperation {

	@Override
	public String getDescription() {
		String res = "Find note references";
		if (noteIdentifier!=null) res += " for "+noteIdentifier;
		return res;
	}
	
	private AuthorNode findNextCandidate() throws AuthorOperationException {
		
		AuthorNode[] res = getAuthorAccess().getDocumentController().findNodesByXPath(noterefCandidateXPath, false, true, true);
		int currentCaretPos = getAuthorAccess().getEditorAccess().getBalancedSelectionStart();
		if (res.length==0) return null;
		for (int i=res.length-1; i>=0; i--) {
			if (currentCaretPos>res[i].getEndOffset()) {
				return res[i];
			}
		}
		return res[res.length-1];
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
			AuthorEditorAccess edtAcc = getAuthorAccess().getEditorAccess();
			AuthorNode[] noteCandts = docCtrl.findNodesByXPath(noteXPath, getCurrentElement(), true, true, true, true);
			AuthorElement note = null;
			if (noteCandts.length>0) {
				if (noteCandts[0] instanceof AuthorElement) note = (AuthorElement)noteCandts[0];
			}
			if (note==null) {
				throw new AuthorOperationException("Could not find an ancestor note element");
			}
			String noteId = null;
			if (note.getAttribute("id")!=null) noteId = note.getAttribute("id").getValue();
			if (noteId == null) throw new AuthorOperationException("Ancestor note element has no id");
			noterefFragment = noterefFragment.replace("$idref", noteId);
			noteIdentifier = null;
			AuthorNode[] noteIdentifierCandts = docCtrl.findNodesByXPath(noteIdentifierXPath, note, true, true, true, true);
			if (noteIdentifierCandts.length>0) {
				noteIdentifier = noteIdentifierCandts[0].getTextContent();
			}
			if (noteIdentifier==null) throw new AuthorOperationException("Fould no note identifier");
			noterefCandidateXPath = noterefCandidateXPath.replace("$noteident", noteIdentifier);
			boolean found = false;
			edtAcc.select(note.getStartOffset(), note.getEndOffset()+1);
			int offset = note.getStartOffset();
			AuthorNode next = findNextCandidate();
			
			boolean hasTurned = false;//flag indicating if the search has reached the start of the document and continued from the end (searching backwards)
			while (next != null) {
				found = true;
				if ((offset<next.getStartOffset())) {
					if (hasTurned) break;
					hasTurned = true;
				}
				offset = next.getStartOffset();
				if (hasTurned && (offset < note.getStartOffset())) break;
				if (next.getTextContent().indexOf(noteIdentifier)==-1) continue;
				offset += next.getTextContent().indexOf(noteIdentifier); 
				edtAcc.setCaretPosition(next.getStartOffset());
				edtAcc.select(offset, offset+noteIdentifier.length());
				int answer = showYesNoCancelMessage(getDescription(), "Is the current selection a reference to the note?\n(Note: "+note.getTextContent()+")", 1);
				if (answer == -1) break;
				if (answer == 1) { 
					docCtrl.surroundInFragment(noterefFragment, offset, offset+noteIdentifier.length());
				}
				next = findNextCandidate();
			}
			if (!found)	showMessage("Found no suitable candidates for note identifier '"+noteIdentifier+"'");
			edtAcc.setCaretPosition(note.getEndOffset()-2);
						
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
	
	private String noteIdentifier;
	
	private static String ARG_NOTEREF_FRAGMENT = "noteref fragment";
	private String noterefFragment;
	
	private static String ARG_NOTE_XPATH = "note xpath";
	private String noteXPath;
	
	private static String ARG_NOTE_IDENTIFIER_XPATH = "note identifier xpath";
	private String noteIdentifierXPath; 
	
	private static String ARG_NOTEREF_CANDIDATE_XPATH = "noteref candidate xpath";
	private String noterefCandidateXPath; 

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor(ARG_NOTEREF_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "The noteref fragment - $idref is placeholder for the id of the referenced note"),
				new ArgumentDescriptor(ARG_NOTE_XPATH, ArgumentDescriptor.TYPE_XPATH_EXPRESSION, "The XPath used to find the ancestor note"),
				new ArgumentDescriptor(ARG_NOTE_IDENTIFIER_XPATH, ArgumentDescriptor.TYPE_XPATH_EXPRESSION, "The XPath used to find the note identifier"),
				new ArgumentDescriptor(ARG_NOTEREF_CANDIDATE_XPATH, ArgumentDescriptor.TYPE_XPATH_EXPRESSION, "The XPath used to find noteref candidates - $noteident is placeholder for the note identifier")
		};
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		noterefFragment = (String)args.getArgumentValue(ARG_NOTEREF_FRAGMENT);
		noteXPath = (String)args.getArgumentValue(ARG_NOTE_XPATH);
		noteIdentifierXPath = (String)args.getArgumentValue(ARG_NOTE_IDENTIFIER_XPATH);
		noterefCandidateXPath = (String)args.getArgumentValue(ARG_NOTEREF_CANDIDATE_XPATH);

	}

}
