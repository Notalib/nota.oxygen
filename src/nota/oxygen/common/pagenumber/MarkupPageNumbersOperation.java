package nota.oxygen.common.pagenumber;

import java.util.ArrayList;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.access.AuthorWorkspaceAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import nota.oxygen.common.BaseAuthorOperation;

/**
 * Marks page number fully or semi-automatic
 * @author OHA
 *
 */
public class MarkupPageNumbersOperation extends BaseAuthorOperation {
	
	private static String ARG_PAGENUMBER_FRAGMENT = "pagenumber fragment";
	private String pagenumberFragment;
	
	private static String ARG_CANDIDATE_XPATH = "candidate xpath";
	private String candidateXPath = "candidate xpath";
	
	private static String ARG_MARKUP_MODE = "Markup mode";
	private static String[] MARKUP_MODES = new String[] {"Fully automatic", "Confirm when out-of-sequence", "Confirm all"};
	private String markupMode;

	private static String ARG_ID_PREFIX = "id prefix";
	private String idPrefix = "page_";
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[]{
				new ArgumentDescriptor(ARG_PAGENUMBER_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Pagenumber xml fragment - $pagenum is placeholder for the pagenumber, $id for pagenumber id"),
				new ArgumentDescriptor(ARG_CANDIDATE_XPATH, ArgumentDescriptor.TYPE_XPATH_EXPRESSION, "Candidate XPath"),
				new ArgumentDescriptor(ARG_MARKUP_MODE, ArgumentDescriptor.TYPE_CONSTANT_LIST, "Markup mode", MARKUP_MODES, MARKUP_MODES[0]),
				new ArgumentDescriptor(ARG_ID_PREFIX, ArgumentDescriptor.TYPE_STRING, "id prefix")
		};
	}

	@Override
	public String getDescription() {
		return "Find and markup pagenumbers";
	} 
	
	private void doOperation(boolean automatic, boolean confirmOnlyOutOfSequence) throws AuthorOperationException {
		AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
		AuthorEditorAccess edtAcc = getAuthorAccess().getEditorAccess();
		AuthorWorkspaceAccess wa = getAuthorAccess().getWorkspaceAccess();
		
		AuthorElement[] candidates = findCandidates();
		int nextExpected = 1;
		for (int i=0; i<candidates.length; i++) {
			int pagenumber = parseCandidate(candidates[i]);
			if (pagenumber==-1) continue;
			edtAcc.select(candidates[i].getStartOffset(), candidates[i].getEndOffset());
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				throw new AuthorOperationException("Unexpected InterruptException occured", e);
//			}
			boolean confirmed = false;
			if (automatic || (pagenumber==nextExpected && confirmOnlyOutOfSequence)) {
				confirmed = true;
			}
			else {
				int answer = showYesNoCancelMessage(getDescription(), "Do you wish to markup page "+pagenumber+"?", 1);
				if (answer==-1) break;
				if (answer==1) confirmed = true;
			}
			if (confirmed) {
				wa.showStatusMessage("Page "+pagenumber);
				String pnXml = pagenumberFragment.replace("$pagenum", String.format("%1$d", pagenumber));
				pnXml = pnXml.replace("$id", String.format("%1$s%2$d", idPrefix, pagenumber));
				docCtrl.insertXMLFragment(pnXml, candidates[i].getEndOffset()+1);
				docCtrl.deleteNode(candidates[i]);
				nextExpected = pagenumber+1;
			}
		}
	}
	
	private int parseCandidate(AuthorElement candidate) {
		try {
			return Integer.parseInt(candidate.getTextContent());
		}
		catch (NumberFormatException e) {
			return -1;
		}
		catch (BadLocationException e) {
			return -1;
		}
	}
	
	private AuthorElement[] findCandidates() throws AuthorOperationException {
		AuthorNode[] nRes = getAuthorAccess().getDocumentController().findNodesByXPath(candidateXPath, true, true, true);
		ArrayList<AuthorElement> res = new ArrayList<AuthorElement>();
		for (int i=0; i<nRes.length; i++) {
			if (nRes[i] instanceof AuthorElement) {
				res.add((AuthorElement)nRes[i]);
			}
		}
		return res.toArray(new AuthorElement[0]);
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		if (markupMode.equals(MARKUP_MODES[0])) {//Fully automatic
			doOperation(true, false);
		}
		else if (markupMode.equals(MARKUP_MODES[1])) {//Confirm when out-of-sequence
			doOperation(false, true);
		}
		else if (markupMode.equals(MARKUP_MODES[2])) {//Confirm always
			doOperation(false, false);
		}
		else {
			throw new AuthorOperationException("Unknown markup mode "+markupMode);
		}
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		pagenumberFragment = (String)args.getArgumentValue(ARG_PAGENUMBER_FRAGMENT);
		candidateXPath = (String)args.getArgumentValue(ARG_CANDIDATE_XPATH);
		markupMode = (String)args.getArgumentValue(ARG_MARKUP_MODE);
		idPrefix = (String)args.getArgumentValue(ARG_ID_PREFIX);
	}

}
