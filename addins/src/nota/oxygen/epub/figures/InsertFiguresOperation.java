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
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import nota.oxygen.common.BaseAuthorOperation;

public class InsertFiguresOperation extends BaseAuthorOperation 
{
	private static String ARG_IMAGE_FRAGMENT = "imageFragment";
	private String imageFragment;
	
	private static String ARG_IMAGEGROUP_FRAGMENT_FIRST = "imageGroupFragmentFirst";
	private String imageGroupFragmentFirst;
	
	private static String ARG_LOCATION = "location";
	private String location;

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] { 
				new ArgumentDescriptor(ARG_IMAGE_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "imageFragment"),
				new ArgumentDescriptor(ARG_IMAGEGROUP_FRAGMENT_FIRST, ArgumentDescriptor.TYPE_FRAGMENT, "imageGroupFragmentFirst"),
				new ArgumentDescriptor(ARG_LOCATION, ArgumentDescriptor.TYPE_STRING, "Location")
		};
	}

	@Override
	protected void parseArguments(ArgumentsMap args) throws IllegalArgumentException {
		imageFragment = (String)args.getArgumentValue(ARG_IMAGE_FRAGMENT);
		imageGroupFragmentFirst = (String)args.getArgumentValue(ARG_IMAGEGROUP_FRAGMENT_FIRST);
		location = (String)args.getArgumentValue(ARG_LOCATION);
	}
	
	@Override
	public String getDescription() {
		return "Inserts a figuregroup, with the side-effect of updating the ePub navigation documents";
	}

	@Override
	protected void doOperation() throws AuthorOperationException 
	{		
		File[] imageFiles = getAuthorAccess().getWorkspaceAccess().chooseFiles(new File("C:\\"), "Select image files", new String[] {"jpg"}, "JPEG");
		
		if (imageFiles.length == 0) 
		{
			return;
		}
		
		String FigureGroup = imageGroupFragmentFirst;
		
		if (imageFiles.length > 1) 
		{
			for(int i=0; i<imageFiles.length; i++) 
			{
				TArchiveDetector myDetector = new TArchiveDetector("epub", new JarDriver(IOPoolLocator.SINGLETON));
				TFile source = new TFile(imageFiles[i]);
				TFile destination = new TFile(location + "/EPUB/images", myDetector);

				try 
				{
		            TFile.umount();
		        } 
				catch (FsSyncException e1) 
				{
		            e1.printStackTrace();
		        }
				
				try 
				{
					if (destination.isArchive() || destination.isDirectory())
					{
						destination = new TFile(destination, source.getName());
					}
					source.cp_rp(destination);				
				} 
				catch (IOException ex) 
				{
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
				
				try 
				{
		            TFile.umount();
		        } 
				
				catch (FsSyncException e) 
				{
		            e.printStackTrace();
		        }
				
				String fileName = imageFiles[i].getName();
				
				String imggroupXml = imageFragment.replace("$image", "images/" + fileName);
				
				FigureGroup = FigureGroup + imggroupXml;
				
			}
			
			FigureGroup = FigureGroup + "</figure>";
			
			int caretPosition  = getAuthorAccess().getEditorAccess().getCaretOffset();
			getAuthorAccess().getDocumentController().insertXMLFragment(FigureGroup, caretPosition);
		}
		else
		{
			return;
		}
	}
}
