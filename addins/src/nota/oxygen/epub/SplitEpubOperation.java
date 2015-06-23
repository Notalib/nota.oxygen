package nota.oxygen.epub;

import nota.oxygen.common.BaseAuthorOperation;
import nota.oxygen.common.Utils;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;

public class SplitEpubOperation extends BaseAuthorOperation {
	private String epub;
	private String epubFolder;

	@Override
	public String getDescription() {
		return "Splits epub file";
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {};
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		// Nothing to parse!!!
	}
	
	@Override
	protected void doOperation() throws AuthorOperationException {
		try {
			getAuthorAccess().getWorkspaceAccess().closeAll();
			
			// get epub zip path
			epub = Utils.getZipPath(getAuthorAccess().getEditorAccess().getEditorLocation().toString());
			if (epub.equals("")) {
				showMessage("Could not access epub zip path");
				return;
			}

			// get epub folder
			epubFolder = EpubUtils.getEpubFolder(getAuthorAccess());
			if (epubFolder.equals("")) {
				showMessage("Could not access epub folder");
				return;
			}

			Splitter.main(new String[] { epub, epubFolder });
		} catch (Exception e) {
			e.printStackTrace();
			showMessage("Could not finalize SplitEpubOperation. An Exception occurred: " + e.getMessage());
			return;
		}
	}
}
