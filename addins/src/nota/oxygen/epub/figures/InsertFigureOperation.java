package nota.oxygen.epub.figures;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.fs.FsSyncException;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import nota.oxygen.common.BaseAuthorOperation;
import nota.oxygen.common.Utils;
import nota.oxygen.epub.EpubUtils;

public class InsertFigureOperation extends BaseAuthorOperation {
	private static String ARG_IMAGE_FRAGMENT = "image fragment";
	private String imageFragment;
	
	private static String ARG_LOCATION = "location";
	private String location;
	
	private static String ARG_FROM_ARCHIVE = "from archive";
	private static String[] YES_NO = new String[]{"yes", "no"};
	private boolean fromArchive;
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] { 
				new ArgumentDescriptor(ARG_IMAGE_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Image fragment"),
				new ArgumentDescriptor(ARG_LOCATION, ArgumentDescriptor.TYPE_STRING, "Location"),
				new ArgumentDescriptor(ARG_FROM_ARCHIVE, ArgumentDescriptor.TYPE_CONSTANT_LIST, "From archive", YES_NO, YES_NO[1])
		};
	}

	@Override
	protected void parseArguments(ArgumentsMap args) throws IllegalArgumentException {
		imageFragment = (String)args.getArgumentValue(ARG_IMAGE_FRAGMENT);
		location = (String)args.getArgumentValue(ARG_LOCATION);
		String temp = (String)args.getArgumentValue(ARG_FROM_ARCHIVE);
		fromArchive = YES_NO[0].equals(temp);
	}
	
	@Override
	public String getDescription() {
		return "Inserts figure(s), with the side-effect of updating the ePub navigation documents";
	}

	@Override
	protected void doOperation() throws AuthorOperationException {		
		File[] imageFiles = null;
		if (fromArchive) {
			URL imageURL = getAuthorAccess().getWorkspaceAccess().chooseURL(
					"Select image file", new String[] { "jpg" }, "JPEG");
			if (imageURL == null) {
				showMessage("No image file selected");
				return;
			}
			
			String relImageURL = getAuthorAccess().getUtilAccess().makeRelative(getAuthorAccess().getDocumentController().getAuthorDocumentNode().getXMLBaseURL(), imageURL);
			if (relImageURL==null) {
				showMessage("No image file selected");
				return;
			}
			
			imageFiles = new File[] { new File(relImageURL) };
		} else {
			imageFiles = getAuthorAccess().getWorkspaceAccess().chooseFiles(new File(""), "Select image file", new String[] {"jpg"}, "JPEG");
			if (imageFiles == null) {
				return;
			}
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
			if (!fromArchive) {
				insertImageToArchive(imageFiles[0]); 
			}

			String fileName = imageFiles[0].getName();
			if (fileName == null) {
				showMessage("No image file selected");
				return;
			}

			figureXml += "<img src=\"images/" + fileName + "\" alt=\"ALTTEXT\" />\n";
			figureXml += "<figcaption>\n<p>CAPTIONPLACEHOLDER</p>\n</figcaption>\n";
		} else {
			// multi image files selected
			figureXml = "<figcaption>\n<p>GROUPCAPTIONPLACEHOLDER</p>\n</figcaption>\n";
			for (int i = 0; i < imageFiles.length; i++) {
				insertImageToArchive(imageFiles[i]);
				String fileName = imageFiles[i].getName();
				if (fileName == null) {
					showMessage("No image files selected");
					return;
				}
				
				figureXml += "<figure class=\"image\">\n";
				figureXml += "<img src=\"images/" + fileName + "\" alt=\"ALTTEXT\" />\n";
				figureXml += "<figcaption>\n<p>CAPTIONPLACEHOLDER</p>\n</figcaption>\n";
				figureXml += "</figure>\n";
			}
		}

		// Inserts this fragment at the caret position.
		fragmentXml = imageFragment.replace("$content", figureXml);
		int caretPosition = getAuthorAccess().getEditorAccess().getCaretOffset();
		getAuthorAccess().getDocumentController().insertXMLFragment(fragmentXml, caretPosition);
		
		addToOpf(imageFiles);
		
		Utils.bringFocusToDocumentTab(getAuthorAccess());
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

	public void addToOpf(File[] imageFiles) throws AuthorOperationException {
		URL opfUrl = EpubUtils.getPackageUrl(getAuthorAccess());
		if (opfUrl == null) {
			showMessage("Could not find pagkage file for document");
			return;
		}
		
		AuthorAccess opfAccess = EpubUtils.getAuthorDocument(getAuthorAccess(), opfUrl);
		if (opfAccess == null) {
			throw new AuthorOperationException("Could not access pagkage file for document");
		}
		AuthorDocumentController opfCtrl = opfAccess.getDocumentController();
		opfCtrl.beginCompoundEdit();
		try {
			AuthorElement manifest = getFirstElement(opfCtrl.findNodesByXPath("/package/manifest", true, true, true));
			if (manifest == null) {
				throw new AuthorOperationException("Found no manifest in package file");
			}
		
		
		
			for (int i = 0; i < imageFiles.length; i++) {
				String fileName = imageFiles[i].getName();
				
				AuthorElement item = getFirstElement(opfCtrl.findNodesByXPath(String.format("/package/manifest/item[@href='images/%s']", fileName), true, true, true));
				if (item == null) {
					String itemXml = "<item xmlns='" + EpubUtils.EPUB_NS + "' media-type='image/jpeg' href='images/" + fileName + "'/>";
					opfCtrl.insertXMLFragment(itemXml, manifest.getEndOffset());
					
				}
			}
			opfCtrl.getUniqueAttributesProcessor().assignUniqueIDs(manifest.getStartOffset(), manifest.getEndOffset(), true);
		}
		catch (Exception e) {
			opfCtrl.cancelCompoundEdit();
			throw e;
		}
		opfCtrl.endCompoundEdit();
		
	}
}
