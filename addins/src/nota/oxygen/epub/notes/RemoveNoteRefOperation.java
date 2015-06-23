package nota.oxygen.epub.notes;

import nota.oxygen.common.BaseAuthorOperation;
import nota.oxygen.common.Utils;
import nota.oxygen.epub.EpubUtils;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;

public class RemoveNoteRefOperation extends BaseAuthorOperation {
	private String epub;
	private String epubFolder;
	private String fileName;
	
	@Override
	public String getDescription() {
		return "Remove noteref(s)";
	}
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[]{};
	}
	
	@Override
	protected void parseArguments(ArgumentsMap args) throws IllegalArgumentException {
		// Nothing to parse!!!
	}
	
	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			getAuthorAccess().getWorkspaceAccess().closeAll();
			
			// get epub zip path
			epub = Utils.getZipPath(getAuthorAccess().getEditorAccess().getEditorLocation().toString());
			if (epub == null || epub.equals("")) {
				showMessage("Could not find epub zip path");
				return;
			}

			// get epub folder
			epubFolder = EpubUtils.getEpubFolder(getAuthorAccess());
			if (epubFolder == null || epubFolder.equals("")) {
				showMessage("Could not find epub folder");
				return;
			}
			
			// find document filename
			fileName = getAuthorAccess().getUtilAccess().getFileName(getAuthorAccess().getEditorAccess().getEditorLocation().getFile().toString());
			if (fileName == null || fileName.equals("")) {
				showMessage("Could not find document filename");
				return;
			}
			
			NoteRefRemover.main(new String[] { epub, epubFolder, fileName });
			
			if (NoteRefRemover.ERRORS_FOUND) {
				showMessage("Processed with errors in some list elements, see separate process window for further details");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage("Could not finalize RemoveNoteRefOperation. An Exception occurred: " + e.getMessage());
			return;
		}
	}
}
