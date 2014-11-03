package nota.oxygen.epub.figures;

import java.io.File;
import java.io.IOException;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.fs.FsSyncException;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import nota.oxygen.common.BaseAuthorOperation;

public class InsertFigureOperation extends BaseAuthorOperation {
	private static String ARG_IMAGE_FRAGMENT = "image fragment";
	private String imageFragment;
	
	private static String ARG_LOCATION = "location";
	private String location;
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] { 
				new ArgumentDescriptor(ARG_IMAGE_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Image fragment"),
				new ArgumentDescriptor(ARG_LOCATION, ArgumentDescriptor.TYPE_STRING, "Location")
		};
	}

	@Override
	protected void parseArguments(ArgumentsMap args) throws IllegalArgumentException {
		imageFragment = (String)args.getArgumentValue(ARG_IMAGE_FRAGMENT);
		location = (String)args.getArgumentValue(ARG_LOCATION);
	}
	
	@Override
	public String getDescription() {
		return "Inserts figure(s), with the side-effect of updating the ePub navigation documents";
	}

	@Override
	protected void doOperation() throws AuthorOperationException {		
		File[] imageFiles = getAuthorAccess().getWorkspaceAccess().chooseFiles(new File(""), "Select image file", new String[] {"jpg"}, "JPEG");
		if (imageFiles == null) {
			return;
		}
			
		if (imageFragment == null) {
			throw new AuthorOperationException(ARG_IMAGE_FRAGMENT + " argument is null");
		}
		
		String figureXml = "";
		String fragmentXml = null;
		
		if (imageFiles.length == 0) {
			// no image file selected
			return;
		} else if (imageFiles.length == 1) {
			// single image file selected
			insertImageToArchive(imageFiles[0]);
			String fileName = imageFiles[0].getName();
			if (fileName == null) {
				showMessage("No image file selected");
				return;
			}

			figureXml += "<img src=\"images/" + fileName + "\" alt=\"ALTTEXT\" />";
			figureXml += "<figcaption><p>CAPTIONPLACEHOLDER</p></figcaption>";
		} else {
			// multi image files selected
			figureXml = "<figcaption><p>GROUPCAPTIONPLACEHOLDER</p></figcaption>";
			for (int i = 0; i < imageFiles.length; i++) {
				insertImageToArchive(imageFiles[i]);
				String fileName = imageFiles[i].getName();
				if (fileName == null) {
					showMessage("No image files selected");
					return;
				}
				
				figureXml += "<figure class=\"image\">";
				figureXml += "<img src=\"images/" + fileName + "\" alt=\"ALTTEXT\" />";
				figureXml += "<figcaption><p>CAPTIONPLACEHOLDER</p></figcaption>";
				figureXml += "</figure>";
			}
		}

		// Inserts this fragment at the caret position.
		fragmentXml = imageFragment.replace("$content", figureXml);
		int caretPosition = getAuthorAccess().getEditorAccess().getCaretOffset();
		getAuthorAccess().getDocumentController().insertXMLFragment(fragmentXml, caretPosition);
	}
	
	@SuppressWarnings("deprecation")
	public void insertImageToArchive(File imageFile) {
		TArchiveDetector myDetector = new TArchiveDetector("epub", new JarDriver(IOPoolLocator.SINGLETON));
		TFile source = new TFile(imageFile);
		TFile destination = new TFile(location + "/EPUB/images", myDetector);

		try {
            TFile.umount();
        } catch (FsSyncException e) {
            e.printStackTrace();
        }
		
		try {
			if (destination.isArchive() || destination.isDirectory()) {
				destination = new TFile(destination, source.getName());
			}
			source.cp_rp(destination);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
            TFile.umount();
        } catch (FsSyncException e) {
            e.printStackTrace();
        }
	}
}
