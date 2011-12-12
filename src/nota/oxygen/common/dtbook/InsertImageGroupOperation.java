package nota.oxygen.common.dtbook;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import nota.oxygen.common.BaseAuthorOperation;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;

/**
 * @author OHA
 *
 */
public class InsertImageGroupOperation extends BaseAuthorOperation {
	
	
	private static String ARG_IMGGROUP_FRAGMENT = "imggroup fragment";
	
	private String imggroupFragment;

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor(ARG_IMGGROUP_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Imagegroup fragment - $image is placeholder for the image file URL")
		};
	}
 
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Insert an image group";
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		URL imageURL = getAuthorAccess().getWorkspaceAccess().chooseURL("Select image file", new String[] {"jpg", "png"}, "JPEG|PNG");
		if (imageURL==null) return;
		String relImageURL = getAuthorAccess().getUtilAccess().makeRelative(
				getAuthorAccess().getDocumentController().getAuthorDocumentNode().getXMLBaseURL(),
				imageURL);
		String height = "";
		String width = "";
		Dimension imageDim = getImageDimension(imageURL);
		if (imageDim!=null) {
			height = String.format("%1$d", imageDim.height);
			width = String.format("%1$d", imageDim.width);
		}
		if (imggroupFragment==null) throw new AuthorOperationException(ARG_IMGGROUP_FRAGMENT+" argument is null");
		String imggroup = imggroupFragment.replace("$image", relImageURL).replace("$height", height).replace("$width", width);
		getAuthorAccess().getDocumentController().insertXMLFragment(imggroup, getAuthorAccess().getEditorAccess().getCaretOffset());
	}
	
	private String getExtension(String path) {
		int index = path.lastIndexOf('.');
		if (index>-1 && index+1<path.length()) {
			return path.substring(index+1);
		}
		return "";
	}
	
	private Dimension getImageDimension(URL imageURL) {
		Iterator<ImageReader> imageReaders = ImageIO.getImageReadersBySuffix(getExtension(imageURL.getPath()));
		if (imageReaders.hasNext()) {
			 ImageReader reader = imageReaders.next();
			 try {
				 File imageFile = new File(imageURL.toURI());
				 ImageInputStream imageStream = new FileImageInputStream(imageFile);
				 try  {
					 reader.setInput(imageStream);
					 return new Dimension(
							 reader.getWidth(reader.getMinIndex()),
							 reader.getHeight(reader.getMinIndex()));
				 }
				 finally {
					 imageStream.close();
				 }
			 }
			 catch (IOException e) {
				 return null;
			 }
			 catch (URISyntaxException e) {
				 return null;
			 }
		}
		return null;
	}

	@Override
	protected void parseArguments(ArgumentsMap args)
			throws IllegalArgumentException {
		imggroupFragment = (String)args.getArgumentValue(ARG_IMGGROUP_FRAGMENT);

	}

}
