package dk.nota.oxygen.actions.epub;

import java.net.URL;
import java.util.LinkedList;

import dk.nota.epub.EpubException;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.ResultsListener;
import dk.nota.oxygen.ResultsView;
import dk.nota.oxygen.workers.epub.DocxToEpubWorker;
import net.sf.saxon.s9api.XdmNode;

public class ImportDocxAction extends EpubAction {

	public ImportDocxAction() {
		super("Docx", true);
	}

	@Override
	public void actionPerformed(EditorAccess editorAccess,
			LinkedList<URL> affectedEditorUrls) {
		java.io.File[] sourceFiles = editorAccess.getPluginWorkspace()
				.chooseFiles(null, "Import", new String[] {"docx", "kat"},
				"Word documents, cat lists");
		if (sourceFiles == null) return;
		XdmNode opfDocument;
		try {
			opfDocument = epubAccess.getContentAccess().getOpfDocument();
		} catch (EpubException e) {
			editorAccess.showErrorMessage("Unable to get OPF document", e);
			return;
		}
		DocxToEpubWorker docxToEpubWorker = new DocxToEpubWorker(
				epubAccess, opfDocument, new ResultsListener(epubAccess.getPid()
						+ " - Import Docx"), sourceFiles,
				affectedEditorUrls);
		docxToEpubWorker.execute();
	}
	
	

}
