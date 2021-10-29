package dk.nota.oxygen.actions.epub;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;

import dk.nota.epub.EpubException;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.ResultsListener;
import dk.nota.oxygen.ResultsView;
import dk.nota.oxygen.workers.epub.EpubToDocxWorker;
import net.sf.saxon.s9api.XdmNode;

public class EpubToDocxAction extends EpubAction {

	public EpubToDocxAction() {
		super("Docx", false);
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
		String docxFileName = epubAccess.getPid().replaceFirst("dk-nota-", "")
				+ ".docx";
		File docxFile = null;
		docxFile = editorAccess.getPluginWorkspace().chooseFile(new File(
				epubAccess.getArchiveUri().resolve(docxFileName)),
				"Export [Docx]", new String[] { "docx" }, "Word documents",
				true);
		if (docxFile == null) return;
		EpubToDocxWorker epubToDocxWorker = new EpubToDocxWorker(epubAccess,
				opfDocument, new ResultsListener(epubAccess.getPid()
						+ " - Convert to Docx"), docxFile);
		epubToDocxWorker.execute();
	}

}
