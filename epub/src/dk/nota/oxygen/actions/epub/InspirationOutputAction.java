package dk.nota.oxygen.actions.epub;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

import dk.nota.dtb.conversion.InspirationOutput;
import dk.nota.epub.EpubException;
import dk.nota.oxygen.EditorAccess;
import dk.nota.oxygen.ResultsListener;
import dk.nota.oxygen.ResultsView;
import dk.nota.oxygen.workers.epub.InspirationOutputWorker;
import net.sf.saxon.s9api.XdmNode;

public class InspirationOutputAction extends EpubAction {
	
	private InspirationOutput inspirationOutput;
	
	public InspirationOutputAction(InspirationOutput inspirationOutput) {
		super(inspirationOutput.getName(), false, false);
		this.inspirationOutput = inspirationOutput;
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
		URI outputUri;
		switch (inspirationOutput) {
		case INSP_PRINT:
			outputUri = epubAccess.getArchiveUri().resolve("tryk/");
			break;
		case INSP_PROOF:
			outputUri = epubAccess.getArchiveUri().resolve(
					"korrektur/korrektur.html");
			break;
		default:
			outputUri = epubAccess.getArchiveUri().resolve(inspirationOutput
				.getPrefix() + "/" + inspirationOutput.getPrefix() + epubAccess
				.getPid().replaceFirst("^(dk-nota-)*.{4}", "") + ".xml");
		}
		if (Files.exists(Paths.get(outputUri)) &&
				inspirationOutput != InspirationOutput.INSP_PRINT) {
			File outputFile = editorAccess.getPluginWorkspace().chooseFile(
					new File(outputUri), "Export [" + inspirationOutput
					.getName() + "]", null, "All files", true);
			if (outputFile == null) return;
			outputUri = outputFile.toURI();
		}
		InspirationOutputWorker inspirationOutputWorker =
				new InspirationOutputWorker(epubAccess, opfDocument,
						new ResultsListener(epubAccess.getPid() + " - Create "
								+ inspirationOutput.getName()),
						inspirationOutput, outputUri);
		inspirationOutputWorker.execute();
	}

}
