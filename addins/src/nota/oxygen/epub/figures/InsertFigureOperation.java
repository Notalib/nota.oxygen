package nota.oxygen.epub.figures;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.fs.FsSyncException;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import nota.oxygen.common.BaseAuthorOperation;
import nota.oxygen.common.Utils;
import nota.oxygen.epub.EpubUtils;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;

public class InsertFigureOperation extends BaseAuthorOperation {
	private static String ARG_IMAGE_FRAGMENT = "image fragment";
	private String imageFragment;
	
	private static String ARG_IMAGE_COMTAINER_FRAGMENT = "image container fragment";
	private String imageContainerFragment;
	
	private static String ARG_FROM_ARCHIVE = "from archive";
	private static String[] YES_NO = new String[]{"yes", "no"};
	private boolean fromArchive;
	
	private String epubFilePath;
	
	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] { 
				new ArgumentDescriptor(ARG_IMAGE_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Image fragment - use $image as placeholder for image url"),
				new ArgumentDescriptor(ARG_IMAGE_COMTAINER_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Image container fragment - use $images as placeholder for images"),
				new ArgumentDescriptor(ARG_FROM_ARCHIVE, ArgumentDescriptor.TYPE_CONSTANT_LIST, "From archive", YES_NO, YES_NO[1])
		};
	}

	@Override
	protected void parseArguments(ArgumentsMap args) throws IllegalArgumentException {
		imageFragment = (String)args.getArgumentValue(ARG_IMAGE_FRAGMENT);
		imageContainerFragment = (String)args.getArgumentValue(ARG_IMAGE_COMTAINER_FRAGMENT);
		String temp = (String)args.getArgumentValue(ARG_FROM_ARCHIVE);
		fromArchive = YES_NO[0].equals(temp);
	}
	
	@Override
	public String getDescription() {
		return "Inserts figure(s), with the side-effect of updating the ePub navigation documents";
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		epubFilePath = Utils.getZipPath(getAuthorAccess().getEditorAccess().getEditorLocation().toString());
		if (epubFilePath == null) epubFilePath = "";
		File[] imageFiles = null;
		if (fromArchive) {
			List<File> files = new ArrayList<File>();
			while (true) {
				URL imageURL = getAuthorAccess()
						.getWorkspaceAccess()
						.chooseURL(
								"Select image file",
								new String[] { "jpg" },
								"JPEG",
								EpubUtils
										.getEpubUrl(
												EpubUtils
														.getPackageUrl(getAuthorAccess()),
												"images/").toString());
				if (imageURL == null) {
					break;
				}
				if (!imageURL.toString().contains("zip:file:")) {
					showMessage("The choosen file is not contained in an archive");
					continue;
				}

				if (!epubFilePath.equals(Utils.getZipPath(imageURL.toString()))) {
					showMessage("The choosen file is from another archive");
					continue;
				}

				String relImageURL = getAuthorAccess().getUtilAccess()
						.makeRelative(
								getAuthorAccess().getDocumentController()
										.getAuthorDocumentNode()
										.getXMLBaseURL(), imageURL);
				if (relImageURL == null) {
					break;
				}
				files.add(new File(relImageURL));
			}
			if (files.size() == 0) {
				return;
			}
			imageFiles = files.toArray(new File[0]);
		} else {
			imageFiles = getAuthorAccess().getWorkspaceAccess().chooseFiles(new File(""), "Select image file", new String[] {"jpg"}, "JPEG");
			if (imageFiles == null) {
				return;
			}
		}
			
		
			
		if (imageFragment == null) {
			throw new AuthorOperationException(ARG_IMAGE_FRAGMENT + " argument is null");
		}
		
		String fragmentXml = "";
		for (File imageFile : imageFiles) {
			if (!fromArchive) {
				insertImageToArchive(imageFile); 
			}
			fragmentXml += imageFragment.replace("$imageUrl", "images/"+imageFile.getName());			
		}
		if (imageContainerFragment == null) imageContainerFragment = "";
		if (imageContainerFragment.length()>0 && imageFiles.length>1) {
			fragmentXml = imageContainerFragment.replace("$images", fragmentXml);
		}
		int caretPosition = getAuthorAccess().getEditorAccess().getCaretOffset();
		getAuthorAccess().getDocumentController().insertXMLFragment(fragmentXml, caretPosition);
		
		addToOpf(imageFiles);
		
		Utils.bringFocusToDocumentTab(getAuthorAccess());
	}
	
	public void insertImageToArchive(File imageFile) throws AuthorOperationException {
		TArchiveDetector myDetector = new TArchiveDetector("epub", new JarDriver(IOPoolLocator.SINGLETON));
		TFile source = new TFile(imageFile);
		TFile destination = new TFile(epubFilePath + "/EPUB/images", myDetector);

		try {
			TVFS.umount();
        } catch (FsSyncException e) {
        	throw new AuthorOperationException(e.getMessage(), e);
        }
		
		try {
			destination = new TFile(destination, source.getName());
			source.cp_rp(destination);
		} catch (IOException e) {
			throw new AuthorOperationException(e.getMessage(), e);
		}
		
		try {
			TVFS.umount();
        } catch (FsSyncException e) {
        	throw new AuthorOperationException(e.getMessage(), e);
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
					String itemXml = "<item xmlns='" + EpubUtils.OPF_NS + "' media-type='image/jpeg' href='images/" + fileName + "'/>";
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
