package nota.oxygen.epub;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import nota.oxygen.common.BaseAuthorOperation;
import nota.oxygen.common.Utils;

public class ConcatEpubOperation extends BaseAuthorOperation {
	private String epubZipPath;
	private String epubFolder;
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[]{};
	}

	@Override
	public String getDescription() {
		return "Concats epub files";
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
			epubZipPath = Utils.getZipPath(getAuthorAccess().getEditorAccess().getEditorLocation().toString());
			if (epubZipPath.equals("")) {
				showMessage("Could not access epub zip path");
				return;
			}
			
			// get epub folder
			epubFolder = EpubUtils.getEpubFolder(getAuthorAccess());
			if (epubFolder.equals("")) {
				showMessage("Could not access epub folder");
				return;
			}

			Concater.main(new String[] {epubZipPath, epubFolder});
		} catch (Exception e) {
			e.printStackTrace();
			showMessage("Could not finalize operation. An Exception occurred: " + e.getMessage());
			return;
		}
	}
}
