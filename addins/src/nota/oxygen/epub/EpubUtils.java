package nota.oxygen.epub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import nota.oxygen.common.Utils;
import nota.oxygen.epub.headings.UpdateNavigationDocumentsOperation;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.fs.FsSyncException;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorWorkspaceAccess;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;

public class EpubUtils {	
	public static URL getEpubUrl(URL baseEpubUrl, String url)
	{
		URL epubUrl = Utils.getZipRootUrl(baseEpubUrl);
		if (epubUrl == null) return null;
		try {
			return new URL(epubUrl, url);
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	public static AuthorAccess getAuthorDocument(AuthorAccess authorAccess, URL docUrl)
	{
		AuthorWorkspaceAccess wa = authorAccess.getWorkspaceAccess();
		WSEditor editor = wa.getEditorAccess(docUrl);
		if (editor == null)
		{
			if (!wa.open(docUrl, WSEditor.PAGE_AUTHOR)) return null;
			editor = wa.getEditorAccess(docUrl);
			if (editor == null) return null;
		}
		if (editor.getCurrentPageID() != WSEditor.PAGE_AUTHOR) editor.changePage(WSEditor.PAGE_AUTHOR);
		WSEditorPage wep = editor.getCurrentPage();
		WSAuthorEditorPage aea = (wep instanceof WSAuthorEditorPage ? (WSAuthorEditorPage)wep : null);
		if (aea == null) return null;
		return aea.getAuthorAccess();
	}
	
	public static URL getPackageUrl(AuthorAccess authorAccess) {
		try {
			if (authorAccess == null) return null;
			URL docUrl = authorAccess.getDocumentController().getAuthorDocumentNode().getXMLBaseURL(); 
			AuthorAccess containerDocAccess = getAuthorDocument(authorAccess, getEpubUrl(docUrl, "META-INF/container.xml"));
			if (containerDocAccess == null) return null;
			Element rootElem = Utils.deserializeElement(Utils.serialize(
					containerDocAccess, 
					containerDocAccess.getDocumentController().getAuthorDocumentNode()));
			containerDocAccess.getEditorAccess().close(true);
			XPath xp = Utils.getXPath("ns", "urn:oasis:names:tc:opendocument:xmlns:container");
			try {
				String relUrl = xp.evaluate("//ns:rootfile[@media-type='application/oebps-package+xml']/@full-path", rootElem); 
				return getEpubUrl(docUrl, relUrl);
			} catch (XPathExpressionException e) {
				return null;
			}
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public static String getEpubFolder(AuthorAccess authorAccess) {
		try {
			if (authorAccess == null) return null;
			URL docUrl = authorAccess.getDocumentController().getAuthorDocumentNode().getXMLBaseURL(); 
			AuthorAccess containerDocAccess = getAuthorDocument(authorAccess, getEpubUrl(docUrl, "META-INF/container.xml"));
			if (containerDocAccess == null) return null;
			Element rootElem = Utils.deserializeElement(Utils.serialize(
					containerDocAccess, 
					containerDocAccess.getDocumentController().getAuthorDocumentNode()));
			containerDocAccess.getEditorAccess().close(true);
			XPath xp = Utils.getXPath("ns", "urn:oasis:names:tc:opendocument:xmlns:container");
			try {
				URL rootUrl = Utils.getZipRootUrl(docUrl);
				String epubUrl = xp.evaluate("//ns:rootfile[@media-type='application/oebps-package+xml']/@full-path", rootElem); 
				return rootUrl.toString() + epubUrl.substring(0, epubUrl.indexOf("/"));
			} catch (XPathExpressionException e) {
				return "";
			}
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public static URL getPackageItemURLByXPath(AuthorAccess opfAccess, String xpath) {
		try {
			AuthorNode[] res = opfAccess.getDocumentController().findNodesByXPath(xpath, true, true, true);
			if (res.length > 0)
			{
				AttrValue itemHref = ((AuthorElement)res[0]).getAttribute("href");
				if (itemHref != null)
				{
					return new URL(opfAccess.getEditorAccess().getEditorLocation(), itemHref.getValue());
				}
			}
		}
		catch (Exception e) {
			return null;
		}
		return null;
	}

	public static AuthorAccess getPackageItemDocumentByXPath(AuthorAccess opfAccess, String xpath) {
		URL itemUrl = getPackageItemURLByXPath(opfAccess, xpath);
		if (itemUrl != null) {
			return getAuthorDocument(opfAccess, itemUrl);
		}
		return null;
	}

	public static AuthorAccess getXHTMLNavDocument(AuthorAccess opfAccess) {
		return getPackageItemDocumentByXPath(opfAccess, "//item[@media-type='application/xhtml+xml' and @properties='nav']");
	}

	public static AuthorAccess getNCXDocument(AuthorAccess opfAccess) {
		return getPackageItemDocumentByXPath(opfAccess, "//item[@media-type='application/x-dtbncx+xml']");
	}
	
	public static URL[] getSpineUrls(AuthorAccess opfAccess, boolean includeNonLinear) throws AuthorOperationException {
		String xpath = "/package/spine/itemref" + (includeNonLinear ? "" : "[not(@linear='no')]");
		AuthorNode[] itemrefs = opfAccess.getDocumentController().findNodesByXPath(xpath, true, true, true);
		List<URL> res = new ArrayList<URL>();
		for (int i = 0; i < itemrefs.length; i++) {
			AuthorElement itemref = (AuthorElement)itemrefs[i];
			AttrValue idref = itemref.getAttribute("idref");
			if (idref != null) {
				URL spineUrl = getPackageItemURLByXPath(
						opfAccess, 
						String.format("/package/manifest/item[@id='%s']", idref.getValue()));
				if (spineUrl != null) res.add(spineUrl);
			}
		}
		return res.toArray(new URL[res.size()]);
	}
	
	public static AuthorAccess[] getSpine(AuthorAccess opfAccess, boolean includeNonLinear) throws AuthorOperationException {
		List<AuthorAccess> res = new ArrayList<AuthorAccess>();
		for (URL spineUrl : getSpineUrls(opfAccess, includeNonLinear)) {
			AuthorAccess spineAccess = getAuthorDocument(opfAccess, spineUrl);
			if (spineAccess != null) res.add(spineAccess);
		}
		return res.toArray(new AuthorAccess[0]);
	}

	public static boolean updateNavigationDocuments(AuthorAccess authorAccess) {
		ERROR_MESSAGE = "";
		
		try {
			UpdateNavigationDocumentsOperation navigationOperation = new UpdateNavigationDocumentsOperation();
			navigationOperation.setAuthorAccess(authorAccess);
			navigationOperation.doOperation();
		} catch (AuthorOperationException e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not update navigation documents - an error occurred: " + e.getMessage();
			return false;
		}
		
		return true;
	}
	
	/*********************************************************************************************************************************************
	 * OLD CONCAT & SPLIT METHODS (USING OXYGEN API)
	 *********************************************************************************************************************************************/
	public static WSTextEditorPage getTextDocument(AuthorAccess authorAccess, URL docUrl)
	{
		AuthorWorkspaceAccess wa = authorAccess.getWorkspaceAccess();
		WSEditor editor = wa.getEditorAccess(docUrl);
		if (editor == null)
		{
			if (!wa.open(docUrl, WSEditor.PAGE_TEXT)) return null;
			editor = wa.getEditorAccess(docUrl);
			if (editor == null) return null;
		}
		editor.close(true);
		if (editor.getCurrentPageID() != WSEditor.PAGE_TEXT) editor.changePage(WSEditor.PAGE_TEXT);
		WSEditorPage wep = editor.getCurrentPage();
		WSTextEditorPage editorPage = (wep instanceof WSTextEditorPage ? (WSTextEditorPage)wep : null);
		if (editorPage == null) return null;
		return editorPage;
	}
	
	public static boolean addUniqueIds(AuthorAccess xhtmlAccess) {
		ERROR_MESSAGE = "";
		
		try {
			AuthorElement rootElement = xhtmlAccess.getDocumentController().getAuthorDocumentNode().getRootElement();
			if (rootElement == null) {
				ERROR_MESSAGE = "Found no root in xhtml file";
				return false;
			}

			// insert unique ids for elements
			XHTMLUniqueAttributesRecognizer uniqueAttributesRecognizer = new XHTMLUniqueAttributesRecognizer();
			uniqueAttributesRecognizer.activated(xhtmlAccess);
			uniqueAttributesRecognizer.assignUniqueIDs(rootElement.getStartOffset(), rootElement.getEndOffset(), false);
			uniqueAttributesRecognizer.deactivated(xhtmlAccess);

			// save and close
			xhtmlAccess.getEditorAccess().save();
			xhtmlAccess.getEditorAccess().close(true);
		} catch (Exception e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not add ids to document - an error occurred: " + e.getMessage();
			return false;
		}
		
		return true;
	}
	
	private static AuthorElement getFirstElement(AuthorNode[] nodes) {
		for (int i=0; i<nodes.length; i++) {
			if (nodes[i] instanceof AuthorElement) return (AuthorElement)nodes[i];
		}
		return null;
	}
	
	public static boolean removeFallbackFromOpf(AuthorAccess opfAccess, String fileName) {
		ERROR_MESSAGE = "";
		
		if (opfAccess == null) {
			ERROR_MESSAGE = "Could not access opf document";
			return false;
		}
		
		AuthorDocumentController opfCtrl = opfAccess.getDocumentController();
		opfCtrl.beginCompoundEdit();
		
		try {
			AuthorElement manifest = getFirstElement(opfCtrl.findNodesByXPath("/package/manifest", true, true, true));
			if (manifest == null) {
				ERROR_MESSAGE = "Found no manifest in package file";
				return false;
			}
			
			AuthorElement item = getFirstElement(opfCtrl.findNodesByXPath(String.format("/package/manifest/item[contains(@href,'%s')]", fileName), true, true, true));
			if (item != null) {
				String idValue = item.getAttribute("id").getValue();
				opfCtrl.removeAttribute("fallback", item);
				
				AuthorElement itemRef = getFirstElement(opfCtrl.findNodesByXPath(String.format("/package/spine/itemref[@idref='%s']", idValue), true, true, true));
				if (itemRef != null) {
					opfCtrl.deleteNode(itemRef);
				}
			}
		}
		catch (Exception e) {
			opfCtrl.cancelCompoundEdit();
			ERROR_MESSAGE = "Could not add item to opf document - an error occurred: " + e.getMessage();
			return false;
		}
		
		opfCtrl.endCompoundEdit();
		
		return true;
	}
	
	public static boolean removeOpfItem(AuthorAccess opfAccess, String fileName) {
		ERROR_MESSAGE = "";
		
		if (opfAccess == null) {
			ERROR_MESSAGE = "Could not access opf document";
			return false;
		}
		
		AuthorDocumentController opfCtrl = opfAccess.getDocumentController();
		opfCtrl.beginCompoundEdit();
		
		try {
			AuthorElement manifest = getFirstElement(opfCtrl.findNodesByXPath("/package/manifest", true, true, true));
			if (manifest == null) {
				ERROR_MESSAGE = "Found no manifest in package file";
				return false;
			}
			
			AuthorElement item = getFirstElement(opfCtrl.findNodesByXPath(String.format("/package/manifest/item[@href='%s']", fileName), true, true, true));
			if (item != null) {
				String idValue = item.getAttribute("id").getValue();
				opfCtrl.deleteNode(item);
				
				AuthorElement itemRef = getFirstElement(opfCtrl.findNodesByXPath(String.format("/package/spine/itemref[@idref='%s']", idValue), true, true, true));
				if (itemRef != null) {
					opfCtrl.deleteNode(itemRef);
				}
			}
		}
		catch (Exception e) {
			opfCtrl.cancelCompoundEdit();
			ERROR_MESSAGE = "Could not add item to opf document - an error occurred: " + e.getMessage();
			return false;
		}
		
		opfCtrl.endCompoundEdit();
		
		return true;
	}
	
	public static boolean addOpfItem(AuthorAccess opfAccess, String fileName, boolean linear) {
		ERROR_MESSAGE = "";
		
		if (opfAccess == null) {
			ERROR_MESSAGE = "Could not access opf document";
			return false;
		}
		
		AuthorDocumentController opfCtrl = opfAccess.getDocumentController();
		opfCtrl.beginCompoundEdit();
		
		try {
			AuthorElement manifest = getFirstElement(opfCtrl.findNodesByXPath("/package/manifest", true, true, true));
			AuthorElement spine = getFirstElement(opfCtrl.findNodesByXPath("/package/spine", true, true, true));
			if (manifest == null) {
				ERROR_MESSAGE = "Found no manifest in package file";
				return false;
			}
		
			AuthorElement item = getFirstElement(opfCtrl.findNodesByXPath(String.format("/package/manifest/item[@href='%s']", fileName), true, true, true));
			if (item == null) {
				
				String itemXml = "<item xmlns='" + EpubUtils.OPF_NS + "' media-type='application/xhtml+xml' href='" + fileName + "'/>";
			
				
				opfCtrl.insertXMLFragment(itemXml, manifest.getEndOffset());
				opfCtrl.getUniqueAttributesProcessor().assignUniqueIDs(manifest.getStartOffset(), manifest.getEndOffset(), true);
				
				item = getFirstElement(opfCtrl.findNodesByXPath(String.format("/package/manifest/item[@href='%s']", fileName), true, true, true));
				if (item != null) {
					String idValue = item.getAttribute("id").getValue();

					AuthorElement itemRef = getFirstElement(opfCtrl.findNodesByXPath(String.format("/package/spine/itemref[@idref='%s']", idValue), true, true, true));
					if (itemRef == null) {
						
						String itemRefXml="";
						
						if(linear)
						{
							itemRefXml = "<itemref xmlns='" + EpubUtils.OPF_NS + "' idref='" + idValue + "'/>";
						}
						else
						{
							itemRefXml = "<itemref xmlns='" + EpubUtils.OPF_NS + "' idref='" + idValue + "' linear='no'/>";
						}
						
						opfCtrl.insertXMLFragment(itemRefXml, spine.getEndOffset());
					}
				}
			}
		} catch (Exception e) {
			opfCtrl.cancelCompoundEdit();
			ERROR_MESSAGE = "Could not add item to opf document - an error occurred: " + e.getMessage();
			return false;
		}
		
		opfCtrl.endCompoundEdit();
		
		return true;
	}
	
	public static Document createDocument() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			return doc;
		} catch (ParserConfigurationException e) {
		}
		
		return null;
	}
	
	public static boolean saveDocument(AuthorAccess authorAccess, Document doc, URL file) {
		ERROR_MESSAGE = "";
		
		try {
			// remove all empty lines in document
			removeEmptyLines(doc);
			
			// create transformer
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			
			StringWriter writer = new StringWriter();
			writer.write("<?xml version='1.0' encoding='UTF-8'?>\n");
			writer.write("<!DOCTYPE html>\n");
			StreamResult result = new StreamResult(writer);
			
			// transform concatenated document to xml content string
			transformer.transform(new DOMSource(doc), result);
			
			// open new editor with concatenated xml content
			AuthorWorkspaceAccess awa = authorAccess.getWorkspaceAccess();
			URL newEditorUrl = awa.createNewEditor("xhtml", "text/xml", writer.toString());

			// save content in editor into new concatenated xhtml file
			WSEditor editor = awa.getEditorAccess(newEditorUrl);
			editor.saveAs(file);
			editor.close(true);
			return true;
		} catch (TransformerException e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not save document - an error occurred: " + e.getMessage();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not save document - an error occurred: " + e.getMessage();
			return false;
		}
	}
	
	private static void removeEmptyLines(Document doc) {
		try {
			XPath xp = XPathFactory.newInstance().newXPath();
			NodeList nl = (NodeList) xp.evaluate("//text()[normalize-space(.)='']", doc, XPathConstants.NODESET);
			for (int i=0; i < nl.getLength(); ++i) {
			    Node node = nl.item(i);
			    node.getParentNode().removeChild(node);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}
	
	public static String getMetaNodeValue(Node meta) {
		String value = "";
		if (meta.getNodeName().equalsIgnoreCase("meta")) {
			Node metaName = meta.getAttributes().getNamedItem("name");
			if (metaName != null) {
				value = String.valueOf(meta.getAttributes().getNamedItem("name").getNodeValue());
			}
		}
		return value;
	}
	
	/*********************************************************************************************************************************************
	 * NEW CONCAT & SPLIT METHODS
	 *********************************************************************************************************************************************/
	public static void prepare(String workFolder, String backupExt) {
		// set working folder
		WORK_FOLDER = EPUB.getParent() + File.separator + EPUB.getName() + "." + workFolder;

		// set epub folder name
		EPUB_FOLDER = WORK_FOLDER + File.separator + EPUB_FOLDER.substring(EPUB_FOLDER.lastIndexOf("/")).replace("/", "");
		
		// set backup name
		BACKUP_EXT = "." + backupExt + ".bak";
	}
	
	public static boolean start(JTextArea taskOutput) {
		outputProcess("STARTING", false, taskOutput);

		// create working folder
		if (!new File(WORK_FOLDER).exists())
			new File(WORK_FOLDER).mkdir();

		// clean workfolder
		if (!cleanWorkfolder(WORK_FOLDER, taskOutput))
			return false;

		return true;
	}
	
	public static boolean backup(JTextArea taskOutput) {
		outputProcess("BACKING UP EPUB", true, taskOutput);

		// backup epub
		if (!backupEpub(EPUB, BACKUP_EXT, taskOutput))
			return false;

		return true;
	}
	
	public static boolean unzip(JTextArea taskOutput) {
		outputProcess("UNZIPPING", true, taskOutput);

		// unzip epub to workfolder
		if (!unzip(EPUB, WORK_FOLDER, taskOutput))
			return false;

		return true;
	}
	
	public static boolean finish(JTextArea taskOutput) {
		outputProcess("FINISHING", true, taskOutput);

		// clean working folder
		if (!cleanWorkfolder(WORK_FOLDER, taskOutput))
			return false;

		// delete folder
		if (new File(WORK_FOLDER).exists())
			new File(WORK_FOLDER).delete();

		outputMessage(taskOutput, "");

		return true;
	}

	public static void outputProcess(String process, boolean newLine, JTextArea taskOutput) {
		if (newLine) outputMessage(taskOutput, "");
		outputMessage(taskOutput, "********************************************************");
		outputMessage(taskOutput, process);
		outputMessage(taskOutput, "********************************************************");
	}
	
	public static void outputMessage(JTextArea taskOutput, String output) {
		taskOutput.append(output + "\n");
		taskOutput.setCaretPosition(taskOutput.getDocument().getLength());
	}
	
	public static boolean cleanWorkfolder(String workFolder, JTextArea taskOutput) {
		try {
			outputMessage(taskOutput, "Cleaning " + workFolder);
			FileUtils.cleanDirectory(new File(workFolder));
			return true;
		} catch (IOException ioe) {
			outputMessage(taskOutput, "Could not clean folder " + workFolder + ". An IOException occurred: " + ioe.getMessage());
		} catch (Exception e) {
			outputMessage(taskOutput, "Could not clean folder " + workFolder + ". An Exception occurred: " + e.getMessage());
		}
		
		return false;
	}
	
	public static boolean backupEpub(File epub, String extension, JTextArea taskOutput) {
		try {
			File epubBak = new File(epub.getParent() + File.separator + epub.getName() + extension);
			outputMessage(taskOutput, "Backing up epub " + epub.getPath() + " to " + epubBak.getPath());

			final FileInputStream fileInputStream  = new FileInputStream(epub);
			final FileOutputStream fileOutputStream  = new FileOutputStream(epubBak);

			
			final byte[] buffer = new byte[16 * 1024 * 1024];

			// copy the file content in bytes
			int length;
			while ((length = fileInputStream .read(buffer)) > 0) {
				fileOutputStream .write(buffer, 0, length);
			}

			fileInputStream .close();
			fileOutputStream .close();

			return true;
		} catch (IOException ioe) {
			outputMessage(taskOutput, "Could not backup epub " + epub.getParent() + ". An IOException occurred: " + ioe.getMessage());
		} catch (Exception e) {
			outputMessage(taskOutput, "Could not backup epub " + epub.getParent() + ". An Exception occurred: " + e.getMessage());
		}

		return false;
	}

	public static boolean unzip(File epub, String destDir, JTextArea taskOutput) {
		byte[] byteBuffer = new byte[16 * 1024 * 1024];

		try {
			ZipInputStream inZip = new ZipInputStream(new FileInputStream(epub.getPath()));
			ZipEntry inZipEntry = inZip.getNextEntry();
			while (inZipEntry != null) {
				String fileName = inZipEntry.getName();
				if (fileName.contains(".")) {
					String extension = fileName.substring(fileName.lastIndexOf(".")).replace(".", "");
					if (!extension.equals("xhtml") && !extension.equals("ncx") && !extension.equals("opf")) {
						inZipEntry = inZip.getNextEntry();
						continue;
					}
				}

				File unZippedFile = new File(destDir + File.separator + fileName);

				outputMessage(taskOutput, "Unzipping: " + unZippedFile.getAbsoluteFile());

				if (inZipEntry.isDirectory()) {
					unZippedFile.mkdirs();
				} else {
					new File(unZippedFile.getParent()).mkdirs();
					unZippedFile.createNewFile();
					FileOutputStream unZippedFileOutputStream = new FileOutputStream(unZippedFile);
					int length;
					while ((length = inZip.read(byteBuffer)) > 0) {
						unZippedFileOutputStream.write(byteBuffer, 0, length);
					}
					unZippedFileOutputStream.close();
				}

				inZipEntry = inZip.getNextEntry();
			}
			inZip.close();
			
			outputMessage(taskOutput, "Finished unzipping");
			return true;
		} catch (IOException ioe) {
			outputMessage(taskOutput, "Could not unzip epub " + epub.getParent() + " to " + destDir + ". An IOException occurred: " + ioe.getMessage());
		} catch (Exception e) {
			outputMessage(taskOutput, "Could not unzip epub " + epub.getParent() + " to " + destDir + ". An Exception occurred: " + e.getMessage());
		}

		return false;
	}
	
	public static boolean canConcat(JTextArea taskOutput) {
		outputMessage(taskOutput, "");
		
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xhtml") && !name.equals(NAVIGATION_FILENAME);
			}
		};
		
		File[] files = new File(EPUB_FOLDER).listFiles(filter);
		
		boolean exists = false;
		for (File file : files) {
			if (file.getName().equals(CONCAT_FILENAME)) {
				exists = true;
			}
		}
		
		if (files.length == 0) {
			outputMessage(taskOutput, "Cannot concat ePub - no documents found");
		} else if (exists) {
			outputMessage(taskOutput, "Cannot concat ePub - concat document already exists");
		} else if (files.length == 1) {
			outputMessage(taskOutput, "Cannot concat ePub - only one document exists");
		} else {
			return true;
		}
		
		outputMessage(taskOutput, "");
		return false;
	}
	
	public static boolean canSplit(JTextArea taskOutput) {
		outputMessage(taskOutput, "");
		
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xhtml") && !name.equals(NAVIGATION_FILENAME);
			}
		};
		
		File[] files = new File(EPUB_FOLDER).listFiles(filter);
		
		boolean exists = true;
		for (File file : files) {
			if (file.getName().equals(CONCAT_FILENAME)) {
				exists = true;
			}
		}
		
		if (files.length == 0) {
			outputMessage(taskOutput, "Cannot split ePub - no documents found");
		} else if (files.length > 1) {
			outputMessage(taskOutput, "Cannot split ePub - too many documents exists");
		} else if (!exists) {
			outputMessage(taskOutput, "Cannot split ePub - no concat document exists");
		} else {
			return true;
		}
		
		outputMessage(taskOutput, "");
		return false;
	}
	
	public static File[] getFiles(final boolean concat, final boolean split) {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (concat) return name.endsWith(".xhtml") && !name.equals(NAVIGATION_FILENAME) && name.equals(CONCAT_FILENAME);
				else if (split) return name.endsWith(".xhtml") && !name.equals(NAVIGATION_FILENAME) && !name.equals(CONCAT_FILENAME);
				else return name.endsWith(".xhtml") && !name.equals(NAVIGATION_FILENAME);
			}
		};
		return new File(EPUB_FOLDER).listFiles(filter);
	}
	
	public static boolean prepareFile(File file, JTextArea taskOutput) {
		try {
			outputMessage(taskOutput, "Preparing " + file.getPath());
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(file.getPath());

			// add unique ids to missing elements
			addUniqueIds(doc.getDocumentElement(), taskOutput);

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);
			
			return true;
		} catch (ParserConfigurationException pce) {
			outputMessage(taskOutput, "Could not prepare " + file.getPath() + ". An ParserConfigurationException occurred: " + pce.getMessage());
		} catch (TransformerException tfe) {
			outputMessage(taskOutput, "Could not prepare " + file.getPath() + ". An ParserConfigurationException occurred: " + tfe.getMessage());
		} catch (IOException ioe) {
			outputMessage(taskOutput, "Could not prepare " + file.getPath() + ". An ParserConfigurationException occurred: " + ioe.getMessage());
		} catch (SAXException sae) {
			outputMessage(taskOutput, "Could not prepare " + file.getPath() + ". An ParserConfigurationException occurred: " + sae.getMessage());
		} catch (Exception e) {
			outputMessage(taskOutput, "Could not prepare " + file.getPath() + ". An ParserConfigurationException occurred: " + e.getMessage());
		}
		
		return false;
	}
	
	public static boolean parseFile(File file, DefaultHandler handler, JTextArea taskOutput) {
		try {
			// parse source file
			EpubUtils.outputMessage(taskOutput, "Parsing " + file.getPath());
			String fileName = file.getName();
			
			if (handler.getClass().isAssignableFrom(ConcatHandler.class)) {
				((ConcatHandler)handler).setFileEpubType(fileName.substring(fileName.lastIndexOf("-") + 1, fileName.lastIndexOf(".")));
			}
			
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser;
			saxParser = factory.newSAXParser();
			saxParser.parse(file, handler);
			return true;
		} catch (ParserConfigurationException pce) {
			EpubUtils.outputMessage(taskOutput, "Could not parse " + file.getPath() + ". An ParserConfigurationException occurred: " + pce.getMessage());
		} catch (SAXException se) {
			EpubUtils.outputMessage(taskOutput, "Could not parse " + file.getPath() + ". An SAXException occurred: " + se.getMessage());
		} catch (IOException ioe) {
			EpubUtils.outputMessage(taskOutput, "Could not parse " + file.getPath() + ". An IOException occurred: " + ioe.getMessage());
		} catch (Exception e) {
			EpubUtils.outputMessage(taskOutput, "Could not parse " + file.getPath() + ". An Exception occurred: " + e.getMessage());
		}
		
		return false;
	}
	
	public static void addUniqueIds(Node node, JTextArea taskOutput) {
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				if (currentNode.getNodeName().equals("a")
						|| currentNode.getNodeName().equals("aside")
						|| currentNode.getNodeName().equals("body")
						|| currentNode.getNodeName().equals("div")
						|| currentNode.getNodeName().equals("h1")
						|| currentNode.getNodeName().equals("h2")
						|| currentNode.getNodeName().equals("h3")
						|| currentNode.getNodeName().equals("h4")
						|| currentNode.getNodeName().equals("h5")
						|| currentNode.getNodeName().equals("h6")
						|| currentNode.getNodeName().equals("hd")
						|| currentNode.getNodeName().equals("img")
						|| currentNode.getNodeName().equals("section")
						|| currentNode.getNodeName().equals("span")
						|| currentNode.getNodeName().equals("td")
						|| currentNode.getNodeName().equals("th")
						|| currentNode.getNodeName().equals("tr")) {
					if (!((Element)currentNode).hasAttribute("id")) {
						String uniqueId = UUID.randomUUID().toString();
						((Element) currentNode).setAttribute("id", currentNode.getNodeName() + "_" + uniqueId);
						outputMessage(taskOutput, "Added unique id " + uniqueId + " to element " + currentNode.getNodeName());
					}
				}

				// calls this method for all the children which is Element
				addUniqueIds(currentNode, taskOutput);
			}
		}
	}
	
	public static Document createDocument(File file, JTextArea taskOutput) {
		try {
			outputMessage(taskOutput, "Creating document");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(file);
		} catch (ParserConfigurationException pce) {
			EpubUtils.outputMessage(taskOutput, "File " + file.getPath() + " could not be created. An ParserConfigurationException occurred: " + pce.getMessage());
		} catch (SAXException se) {
			EpubUtils.outputMessage(taskOutput, "File " + file.getPath() + " could not be created. An SAXException occurred: " + se.getMessage());
		} catch (IOException ioe) {
			EpubUtils.outputMessage(taskOutput, "File " + file.getPath() + " could not be created. An IOException occurred: " + ioe.getMessage());
		} catch (Exception e) {
			EpubUtils.outputMessage(taskOutput, "File " + file.getPath() + " could not be created. An Exception occurred: " + e.getMessage());
		}

		return null;
	}
	
    public static boolean saveDocument(Document doc, File file, JTextArea taskOutput) {
		try {
			outputMessage(taskOutput, "Saving document to " + file.getPath());
			
			// remove all empty lines in document
			removeEmptyLines(doc, taskOutput);
			
			// create transformer
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			//transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "about:legacy-compat");
			
			FileOutputStream  fop = new FileOutputStream(file);
			String xmlDeclaration = "<?xml version='1.0' encoding='UTF-8'?>\n";
			String doctype = "<!DOCTYPE html>\n";
			fop.write(xmlDeclaration.getBytes());
			fop.write(doctype.getBytes());
			StreamResult result = new StreamResult(fop);
			
			// transform concatenated document to xml content string
			transformer.transform(new DOMSource(doc), result);
			fop.close();
			
			return true;
		} catch (TransformerException te) {
			outputMessage(taskOutput, "Could not save document to " + file.getPath() + ". An TransformerException occurred: " + te.getMessage());
		} catch (Exception e) {
			outputMessage(taskOutput, "Could not save document to " + file.getPath() + ". An Exception occurred: " + e.getMessage());
		}
		
		return false;
	}
	
    private static void removeEmptyLines(Document doc, JTextArea taskOutput) {
		try {
			XPath xp = XPathFactory.newInstance().newXPath();
			NodeList nl = (NodeList) xp.evaluate("//text()[normalize-space(.)='']", doc, XPathConstants.NODESET);
			for (int i=0; i < nl.getLength(); ++i) {
			    Node node = nl.item(i);
			    node.getParentNode().removeChild(node);
			}
		} catch (XPathExpressionException xpee) {
			outputMessage(taskOutput, "Could not remove empty lines in document. An XPathExpressionException occurred: " + xpee.getMessage());
		}
	}
	
    public static boolean addOpfItem(Document opfDoc, String fileName, int number, JTextArea taskOutput) {
		try {
			outputMessage(taskOutput, "Adding " + fileName + " to package document");
			
			NodeList manifest = opfDoc.getElementsByTagName("manifest");
			NodeList spine = opfDoc.getElementsByTagName("spine");

			String id = "item_cs_" + number;
			Element manifestItem = opfDoc.createElement("item");
			manifestItem.setAttribute("href", fileName);
			manifestItem.setAttribute("id", id);
			manifestItem.setAttribute("media-type", "application/xhtml+xml");
			manifest.item(0).appendChild(manifestItem);

			Element spineItem = opfDoc.createElement("itemref");
			spineItem.setAttribute("idref", id);
			spine.item(0).appendChild(spineItem);

			return true;
		} catch (Exception e) {
			outputMessage(taskOutput, "Could not add " + fileName + " to package document. An Exception occurred: " + e.getMessage());
		}
		
		return false;
	}

    public static boolean removeOpfItem(Document opfDoc, String fileName, JTextArea taskOutput) {
		try {
			outputMessage(taskOutput, "Removing " + fileName + " from package document");
			
			NodeList items = opfDoc.getElementsByTagName("item");
			NodeList itemRefs = opfDoc.getElementsByTagName("itemref");

			if (items != null && items.getLength() > 0) {
				for (int i = 0; i < items.getLength(); i++) {
					if (items.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Element item = (Element) items.item(i);
						String id = item.getAttribute("id");
						if (item.getAttribute("href").equals(fileName)) {
							item.getParentNode().removeChild(item);

							if (itemRefs != null && itemRefs.getLength() > 0) {
								for (int j = 0; j < itemRefs.getLength(); j++) {
									if (itemRefs.item(j).getNodeType() == Node.ELEMENT_NODE) {
										Element itemRef = (Element) itemRefs
												.item(j);
										if (itemRef.getAttribute("idref")
												.equals(id)) {
											itemRef.getParentNode()
													.removeChild(itemRef);
										}
									}
								}
							}
						}
					}
				}
			}
			
			return true;
		} catch (Exception e) {
			outputMessage(taskOutput, "Could not remove " + fileName + " from package document. An Exception occurred: " + e.getMessage());
		}
		
		return false;
	}
	
	public static boolean removeFallbackFromOpf(Document opfDoc, JTextArea taskOutput) {
		try {
			outputMessage(taskOutput, "Removing fallback from package document");
			
			List<String> nonSpineElements = new ArrayList<String>();
			getNonSpineElements(new File(EPUB_FOLDER), "", nonSpineElements);

			NodeList items = opfDoc.getElementsByTagName("item");
			NodeList itemRefs = opfDoc.getElementsByTagName("itemref");

			if (items != null && items.getLength() > 0) {
				for (int i = 0; i < items.getLength(); i++) {
					if (items.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Element item = (Element) items.item(i);
						String id = item.getAttribute("id");
						for (String nonSpineElement : nonSpineElements) {
							if (item.getAttribute("href").equals(nonSpineElement)) {
								item.removeAttribute("fallback");

								if (itemRefs != null && itemRefs.getLength() > 0) {
									for (int j = 0; j < itemRefs.getLength(); j++) {
										if (itemRefs.item(j).getNodeType() == Node.ELEMENT_NODE) {
											Element itemRef = (Element) itemRefs
													.item(j);
											if (itemRef.getAttribute("idref")
													.equals(id)) {
												itemRef.getParentNode()
														.removeChild(itemRef);
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
			return true;
		} catch (Exception e) {
			outputMessage(taskOutput, "Could not remove fallback from package document. An Exception occurred: " + e.getMessage());
		}
		
		return false;
	}
	
	private static void getNonSpineElements(File dir, String subFolder, List<String> nonSpineElements) {
		File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();

            if (fileName.endsWith(NAVIGATION_FILENAME) || (fileName.contains(".") && !fileName.substring(fileName.lastIndexOf(".")).equals(".xhtml"))) {
                if (files[i].isFile()) {
                	if (!subFolder.equals("")) nonSpineElements.add(subFolder + "/" + files[i].getName());
                	else nonSpineElements.add(files[i].getName());
                }
            }
            
            if (files[i].isDirectory()) {
            	getNonSpineElements(files[i], files[i].getName(), nonSpineElements);
            }
        }
	}

	public static boolean addFileToEpub(TFile source, TFile destination, JTextArea taskOutput) {
		try {
			outputMessage(taskOutput, "Adding file " + source.getName() + " to epub");
			source.cp_rp(new TFile(destination, source.getName()));
			return true;
		} catch (IOException ioe) {
			outputMessage(taskOutput, "Could not add file " + source.getName() + " to epub. An IOException occurred: " + ioe.getMessage());
		} catch (Exception e) {
			outputMessage(taskOutput, "Could not add file " + source.getName() + " to epub. An Exception occurred: " + e.getMessage());
		}
		
		return false;
	}
	
	public static boolean removeFileFromEpub(TFile destination, JTextArea taskOutput) {
		try {
			outputMessage(taskOutput, "Removing file " + destination.getName() + " from epub");
			destination.rm();
			return true;
		} catch (IOException ioe) {
			outputMessage(taskOutput, "Could not remove file " + destination.getName() + " from epub. An IOException occurred: " + ioe.getMessage());
		} catch (Exception e) {
			outputMessage(taskOutput, "Could not remove file " + destination.getName() + " from epub. An Exception occurred: " + e.getMessage());
		}
		
		return false;
	}

	public static boolean commitChanges(JTextArea taskOutput) {
		try {
			outputMessage(taskOutput, "Committing changes to epub");
			TVFS.umount();
			return true;
		} catch (FsSyncException fsse) {
			outputMessage(taskOutput, "Could not commit changes to epub. An FsSyncException occurred: " + fsse.getMessage());
		} catch (Exception e) {
			outputMessage(taskOutput, "Could not commit changes to epub. An Exception occurred: " + e.getMessage());
		}
		
		return false;
	}
	
	public static String XHTML_NS = "http://www.w3.org/1999/xhtml";
	public static String NCX_NS = "http://www.daisy.org/z3986/2005/ncx/";
	public static String EPUB_NS = "http://www.idpf.org/2007/ops";
	public static String OPF_NS = "http://www.idpf.org/2007/opf";
	
	public static String ERROR_MESSAGE;
	
	public static String PACKAGE_FILENAME = "package.opf";
	public static String CONCAT_FILENAME = "concat.xhtml";
	public static String NAVIGATION_FILENAME = "nav.xhtml";
	
	public static File EPUB;
	public static String EPUB_FOLDER = "";
	public static String WORK_FOLDER = "";
	public static String BACKUP_EXT = "";
}
