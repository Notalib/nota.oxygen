package nota.oxygen.epub.figures;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;
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
		return "Inserts a image, with the side-effect of updating the ePub navigation documents";
	}

	@Override
	protected void doOperation() throws AuthorOperationException {		
		
		/*File imageFile = getAuthorAccess().getWorkspaceAccess().chooseFile("Select image file", new String[] {"jpg"}, "JPEG");
		
		TFile source = new TFile(imageFile);
		
		TArchiveDetector myDetector = new TArchiveDetector("epub", new JarDriver(IOPoolLocator.SINGLETON));
		//File destination = new File(location + "/EPUB/images");
		TFile destination = new TFile(location);
		
		try {
			if (destination.isArchive() || destination.isDirectory())
			{
				destination = new TFile(destination, source.getName());
			}
			
			//destination.rm_r();
			//if (!destination.exists()) {
				//TVFS.umount(destination);
				TFile.cp_rp(imageFile, destination, myDetector);
			//}
			
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}*/
		
		URL imageURL = getAuthorAccess().getWorkspaceAccess().chooseURL("Select image file", new String[] {"jpg"}, "JPEG");
		if (imageURL==null) {
			showMessage("No image file selected");
			return;
		}
		
		if (!imageURL.toString().contains("zip:file:"))
		{
			showMessage("filen kan ikke vælges herfra");
			return;
		}
		
		if (!imageURL.toString().contains(location.replace("\\", "/")))
		{
			showMessage("billeder kan kun vælges fra samme epub");
			return;
		}
		
		String relImageURL = getAuthorAccess().getUtilAccess().makeRelative(getAuthorAccess().getDocumentController().getAuthorDocumentNode().getXMLBaseURL(), imageURL);
		if (relImageURL==null) {
			showMessage("No image file selected");
			return;
		}
		
		String imageName = getAuthorAccess().getUtilAccess().getFileName(relImageURL);
		if (imageName==null) {
			showMessage("Image filename could not be set");
			return;
		}
		
		if (imageFragment==null) throw new AuthorOperationException(ARG_IMAGE_FRAGMENT+" argument is null");
		
		// Inserts this fragment at the caret position.
		String imggroupXml = imageFragment.replace("$image", relImageURL);
		int caretPosition  = getAuthorAccess().getEditorAccess().getCaretOffset();
		getAuthorAccess().getDocumentController().insertXMLFragment(imggroupXml, caretPosition);
		

	}
}
