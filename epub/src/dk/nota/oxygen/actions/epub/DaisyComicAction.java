package dk.nota.oxygen.actions.epub;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;

import dk.nota.epub.EpubException;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.ResultsListener;
import dk.nota.oxygen.ResultsView;
import dk.nota.oxygen.workers.epub.DaisyComicWorker;
import net.sf.saxon.s9api.XdmNode;

public class DaisyComicAction extends EpubAction {

	public DaisyComicAction() {
		super("DAISY comic", false);
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess,
			LinkedList<URL> affectedEditorUrls) {
		File outputDirectory = editorAccess.getPluginWorkspace()
				.chooseDirectory();
		if (outputDirectory == null) return;
		XdmNode opfDocument;
		try {
			opfDocument = epubAccess.getContentAccess().getOpfDocument();
		} catch (EpubException e) {
			editorAccess.showErrorMessage("Unable to get OPF document", e);
			return;
		}
		DaisyComicWorker daisyComicWorker = new DaisyComicWorker(epubAccess,
				opfDocument, new ResultsListener(epubAccess.getPid()
						+ " - Create DAISY comic"), outputDirectory.toURI());
		daisyComicWorker.execute();
	}

}
