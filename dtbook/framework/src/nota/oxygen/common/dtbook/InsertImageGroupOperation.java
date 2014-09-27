package nota.oxygen.common.dtbook;

import java.awt.Dimension;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.swing.text.BadLocationException;

import nota.oxygen.common.BaseAuthorOperation;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

/**
 * @author OHA
 *
 */
public class InsertImageGroupOperation extends BaseAuthorOperation {
	
	
	private static String ARG_IMGGROUP_FRAGMENT = "imggroup fragment";
	private String imggroupFragment;
	
	private static String ARG_DO_FIXUP = "fixup mode";
	private static String[] YES_NO = new String[]{"yes", "no"};
	private boolean doFixup;

	@Override
	public ArgumentDescriptor[] getArguments() {
		return new ArgumentDescriptor[] {
				new ArgumentDescriptor(ARG_IMGGROUP_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "Imagegroup fragment - $image is placeholder for the image file URL, $height and $width for image height and width respectively"),
				new ArgumentDescriptor(ARG_DO_FIXUP, ArgumentDescriptor.TYPE_CONSTANT_LIST, "Choose if the imageref attribute should be updated on the inserted caption element", YES_NO, YES_NO[1])
		};
	}
 
	@Override
	public String getDescription() {
		return "Insert an image group";
	}
	 
	private void insertImageGroup(String relImageURL) throws AuthorOperationException
	{
		URL imageURL;
		try {
			imageURL = new URL(getAuthorAccess().getDocumentController().getAuthorDocumentNode().getXMLBaseURL(), relImageURL);
		} catch (MalformedURLException e1) {
			return;
		}
		if (relImageURL.contains("*")||relImageURL.contains("?"))
		{
			
		}
		String height = "";
		String width = "";
		Dimension imageDim = getImageDimension(imageURL);
		if (imageDim!=null) {
			height = String.format("%1$d", imageDim.height);
			width = String.format("%1$d", imageDim.width);
		}
		if (imggroupFragment==null) throw new AuthorOperationException(ARG_IMGGROUP_FRAGMENT+" argument is null");
		String imggroupXml = imggroupFragment.replace("$image", relImageURL).replace("$height", height).replace("$width", width);
		int offset = getAuthorAccess().getEditorAccess().getCaretOffset();
		getAuthorAccess().getDocumentController().insertXMLFragment(imggroupXml, offset);
		if (doFixup) {
			try {
				AuthorNode aNode = getAuthorAccess().getDocumentController().getNodeAtOffset(offset+1);
				if (aNode instanceof AuthorElement) {
					AuthorElement imggroup = getSelfOrAncestorElementByLocalName((AuthorElement)aNode, "imggroup");
					if (imggroup!=null) {
 						AuthorElement[] captions = imggroup.getElementsByLocalName("caption");
						if (captions.length==1) {
							AuthorElement[] imgs = imggroup.getElementsByLocalName("img");
							String imgrefAttrValue = "";
							for (int i=0; i<imgs.length; i++) {
								String val = imgs[i].getAttribute("id").getValue();
								if (val!=null) imgrefAttrValue += val+" ";
							}
							if (!imgrefAttrValue.trim().equals("")) {
								getAuthorAccess().getDocumentController().setAttribute("imgref", new AttrValue(imgrefAttrValue), captions[0]);
							}
						}
						else {
							showMessage("Cannot set caption idref, when the image group contains multiple captions");
						}
					}
				}
			}
			catch (BadLocationException e) {
				//Ignore
			}
		}
		try {
			AuthorNode aNode = getAuthorAccess().getDocumentController().getNodeAtOffset(offset+1);
			if (aNode instanceof AuthorElement)
			{
				getAuthorAccess().getEditorAccess().setCaretPosition(aNode.getEndOffset()+1);
			}
		}
		catch (BadLocationException e) {
			//Ignore
		}
	}
	
	private String[] getURLsFromPattern(String relPattern) throws AuthorOperationException
	{
		URL patternURL;
		try {
			patternURL = new URL(getAuthorAccess().getDocumentController().getAuthorDocumentNode().getXMLBaseURL(), relPattern);
		} catch (MalformedURLException e1) {
			throw new AuthorOperationException("Could not construct URL from "+relPattern, e1);
		}
		String patternPath = patternURL.getPath();
		if (patternPath=="") throw new AuthorOperationException("Patterns are only supported for file URLs");
		File relF = new File(patternPath);
		final String regEx = relF.getName().replace(".", "\\.").replace("*", ".*").replace("?", ".");
		File d = relF.getParentFile();
		if (!d.isDirectory()) throw new AuthorOperationException("Parent is unexpectedly not a directory");
		if (!d.exists()) throw new AuthorOperationException("Parent directory does not exist");
		FilenameFilter ff = new FilenameFilter() 
		{
			public boolean accept(File f, String n) 
			{
				return n.matches(regEx);
			}
		};
		return d.list(ff);
	}

	@Override
	protected void doOperation() throws AuthorOperationException {
		URL imageURL = getAuthorAccess().getWorkspaceAccess().chooseURL("Select image file (use */? for wildcards)", new String[] {"jpg", "png"}, "JPEG|PNG");
		if (imageURL==null) return;
		String relImageURL = getAuthorAccess().getUtilAccess().makeRelative(
				getAuthorAccess().getDocumentController().getAuthorDocumentNode().getXMLBaseURL(),
				imageURL);
		//String relImageURL = ro.sync.ecss.extensions.commons.ImageFileChooser.chooseImageFile(getAuthorAccess());
		if (relImageURL==null) return;
		if (relImageURL.contains("*")||relImageURL.contains("?"))
		{
			String[] relImageURLs = getURLsFromPattern(relImageURL);
			if (relImageURLs.length==0)
			{
				showMessage("No images matches pattern "+relImageURL);
				return;
			}
			
			int res = getAuthorAccess().getWorkspaceAccess().showConfirmDialog(
					getDescription(), "Do you wish to insert "+relImageURLs.length+" images matching "+relImageURL, new String[] {"Yes", "No"}, new int[] {1, 0});
			if (res==0) return;
			for (int i=0; i<relImageURLs.length; i++)
			{
				insertImageGroup(relImageURLs[i]);
			}
			
		}
		else
		{
			insertImageGroup(relImageURL);
		}
	}
	
	private AuthorElement getSelfOrAncestorElementByLocalName(AuthorElement elem, String localName) {
		if (elem.getLocalName().equals(localName)) return elem;
		if (elem.getParent() instanceof AuthorElement) {
			return getSelfOrAncestorElementByLocalName((AuthorElement)elem.getParent(), localName);
		}
		return null;
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
		String fixupMode = (String)args.getArgumentValue(ARG_DO_FIXUP);
		doFixup = YES_NO[0].equals(fixupMode);

	}

}
