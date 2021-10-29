package dk.nota.oxygen.actions.epub;

import java.net.URL;
import java.util.LinkedList;

import dk.nota.epub.EpubException;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.ResultsListener;
import dk.nota.oxygen.ResultsView;
import dk.nota.oxygen.workers.epub.ConcatWorker;
import net.sf.saxon.s9api.XdmNode;

public class ConcatAction extends EpubAction {
	
	public ConcatAction() {
		super("Concat", true);
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess,
			LinkedList<URL> affectedEditorUrls) {
		XdmNode opfDocument;
		try {
			opfDocument = epubAccess.getContentAccess().getOpfDocument();
		} catch (EpubException e) {
			editorAccess.showErrorMessage("Unable to get OPF document", e);
			return;
		}
		ConcatWorker concatWorker = new ConcatWorker(epubAccess,
				opfDocument, new ResultsListener(epubAccess.getPid()
						+ " - Concat"), affectedEditorUrls);
		concatWorker.execute();
	}

}
