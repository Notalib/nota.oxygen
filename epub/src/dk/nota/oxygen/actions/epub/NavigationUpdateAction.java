package dk.nota.oxygen.actions.epub;

import java.net.URL;
import java.util.LinkedList;

import dk.nota.epub.EpubException;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.ResultsListener;
import dk.nota.oxygen.ResultsView;
import dk.nota.oxygen.workers.epub.NavigationUpdateWorker;
import net.sf.saxon.s9api.XdmNode;

public class NavigationUpdateAction extends EpubAction {
	
	public NavigationUpdateAction() {
		super("Update navigation", true);
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
		NavigationUpdateWorker navigationUpdateWorker =
				new NavigationUpdateWorker(epubAccess, opfDocument,
				new ResultsListener(epubAccess.getPid()
						+ " - Update navigation"), affectedEditorUrls);
		navigationUpdateWorker.execute();
	}

}
