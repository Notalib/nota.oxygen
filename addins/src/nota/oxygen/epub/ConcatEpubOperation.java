package nota.oxygen.epub;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.fs.FsSyncException;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import nota.oxygen.common.BaseAuthorOperation;
import nota.oxygen.common.Utils;

public class ConcatEpubOperation extends BaseAuthorOperation {
	private String epubZipPath;
	private String workFolder;
	private String epubFolder;
	private String concatFile;
	private File[] listOfFiles;
	
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
			
			// get epub path
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

			// set working folder
			workFolder = new File(epubZipPath).getParent() + File.separator + "workfolder";

			// set epubFolder
			epubFolder = workFolder + File.separator + epubFolder.substring(epubFolder.lastIndexOf("/")).replace("/", "");

			// set concat file
			concatFile = epubFolder + File.separator + EpubUtils.CONCAT_FILENAME;
			    	
			// create working folder
			if (!new File(workFolder).exists()) new File(workFolder).mkdir();
			
			// clean working folder
			FileUtils.cleanDirectory(new File(workFolder));

			// backup epub
			if (!backupEpub()) {
				showMessage(ERROR_MESSAGE);
				return;
			}

			// unzip epub to work folder
			if (!unzip(epubZipPath, workFolder)) {
				showMessage(ERROR_MESSAGE);
				return;
			}
			
			// get all xhtml files from extracted zip file
	    	listOfFiles = getFiles();
			
			// create concat handler instance
			ConcatHandler concatHandler = new ConcatHandler();
			
			// parse each file
			for (int i = 0; i < listOfFiles.length; i++) {
				// prepare source file
				if (!prepare(listOfFiles[i])) {
					showMessage(ERROR_MESSAGE);
					return;
				}
				
				// parse source file
				String fileName = listOfFiles[i].getName();
				concatHandler.setFileEpubType(fileName.substring(fileName.lastIndexOf("-") + 1, fileName.lastIndexOf(".")));
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();
				saxParser.parse(listOfFiles[i], concatHandler);
			}

			// build concat file
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(concatFile), "UTF-8"));
			if (!buildConcatFile(bw, concatHandler.getTopLines())) {
				showMessage(ERROR_MESSAGE);
				return;
			}
			if (!buildConcatFile(bw, concatHandler.getHeadLines())) {
				showMessage(ERROR_MESSAGE);
				return;
			}
			if (!buildConcatFile(bw, concatHandler.getBodyLines())) {
				showMessage(ERROR_MESSAGE);
				return;
			}
			if (!buildConcatFile(bw, concatHandler.getBottomLines())) {
				showMessage(ERROR_MESSAGE);
				return;
			}
			bw.close();
			
			// create new concat document
			Document doc = createDocument();
			if (doc == null) {
				showMessage(ERROR_MESSAGE);
				return;
			}
			
			// add unique ids to missing elements
			addUniqueIds(doc.getDocumentElement());

			// clean references
			if (!cleanReferences(doc.getElementsByTagName("a"))) {
				showMessage(ERROR_MESSAGE);
				return;
			}

			// save concat document
			if (!saveDocument(doc, new File(concatFile))) {
				showMessage(ERROR_MESSAGE);
				return;
			}
			
			// add concat document to opf document
			if (!addOpfItem(EpubUtils.CONCAT_FILENAME)) {
				showMessage(ERROR_MESSAGE);
				return;
			}

			// remove fallback from non xhtml spine elements
			if (!removeFallbackFromOpf()) {
				showMessage(ERROR_MESSAGE);
				return;
			}

			// remove non concat documents from opf document
			for (int i = 0; i < listOfFiles.length; i++) {
				String fileName = listOfFiles[i].getName();
				if (!removeOpfItem(fileName)) {
					showMessage(ERROR_MESSAGE);
					return;
				}
				//listOfFiles[i].delete();
			}
			
			// modify epub file
			TFile destination = new TFile(epubZipPath + File.separator + epubFolder.substring(epubFolder.lastIndexOf(File.separator)).replace(File.separator, ""), new TArchiveDetector("epub", new JarDriver(IOPoolLocator.SINGLETON)));
			
			if (!addFileToEpub(new TFile(concatFile), destination)) {
				showMessage(ERROR_MESSAGE);
				return;
			}
			
			if (!addFileToEpub(new TFile(epubFolder + File.separator + EpubUtils.PACKAGE_FILENAME), destination)) {
				showMessage(ERROR_MESSAGE);
				return;
			}
			
			if (!removeFilesFromEpub(destination)) {
				showMessage(ERROR_MESSAGE);
				return;
			}
			
			if (!commitChanges()) {
				showMessage(ERROR_MESSAGE);
				return;
			}
			
			// clean working folder
			FileUtils.cleanDirectory(new File(workFolder));
			
			// delete folder
			if (new File(workFolder).exists()) new File(workFolder).delete();
		} catch (Exception e) {
			e.printStackTrace();
			showMessage("Could not finalize operation. An Exception occurred: " + e.getMessage());
			return;
		}
	}
	
	private String ERROR_MESSAGE = "";
	private boolean backupEpub() {
		ERROR_MESSAGE = "";
		
		InputStream inStream = null;
		OutputStream outStream = null;

		try {
			File epub = new File(epubZipPath);
			File epubBak = new File(epub.getParent() + File.separator + epub.getName() + ".concat.bak");

			inStream = new FileInputStream(epub);
			outStream = new FileOutputStream(epubBak);

			int length;
			byte[] buffer = new byte[1024];

			// copy the file content in bytes
			while ((length = inStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, length);
			}

			inStream.close();
			outStream.close();
			
			return true;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			ERROR_MESSAGE = "Could not backup epub " + epubZipPath + ". An IOException occurred: " + ioe.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not backup epub " + epubZipPath + ". An Exception occurred: " + e.getMessage();
		}
		
		return false;
	}
	
	private boolean unzip(String zipFilePath, String destDir) {
		ERROR_MESSAGE = "";
		
		byte[] byteBuffer = new byte[1024];

		try {
			ZipInputStream inZip = new ZipInputStream(new FileInputStream(
					zipFilePath));
			ZipEntry inZipEntry = inZip.getNextEntry();
			while (inZipEntry != null) {
				String fileName = inZipEntry.getName();
				File unZippedFile = new File(destDir + File.separator
						+ fileName);
				System.out.println("Unzipping: "
						+ unZippedFile.getAbsoluteFile());
				if (inZipEntry.isDirectory()) {
					unZippedFile.mkdirs();
				} else {
					new File(unZippedFile.getParent()).mkdirs();
					unZippedFile.createNewFile();
					FileOutputStream unZippedFileOutputStream = new FileOutputStream(
							unZippedFile);
					int length;
					while ((length = inZip.read(byteBuffer)) > 0) {
						unZippedFileOutputStream.write(byteBuffer, 0, length);
					}
					unZippedFileOutputStream.close();
				}
				inZipEntry = inZip.getNextEntry();
			}
			inZip.close();
			System.out.println("Finished Unzipping");
			
			return true;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			ERROR_MESSAGE = "Could not unzip epub " + zipFilePath + " to " + destDir + ". An IOException occurred: " + ioe.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not unzip epub " + zipFilePath + " to " + destDir + ". An Exception occurred: " + e.getMessage();
		}
		
		return false;
	}
	
	private File[] getFiles() {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xhtml") && !name.equals("nav.xhtml") && !name.equals("concat.xhtml");
			}
		};

		return new File(epubFolder).listFiles(filter);
	}
	
	private boolean prepare(File file) {
		ERROR_MESSAGE = "";
		
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(file.getPath());

			// add unique ids to missing elements
			addUniqueIds(doc.getDocumentElement());

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);
			
			return true;
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
			ERROR_MESSAGE = "Could not prepare file " + file.getPath() + ". An ParserConfigurationException occurred: " + pce.getMessage();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
			ERROR_MESSAGE = "Could not prepare file " + file.getPath() + ". An TransformerException occurred: " + tfe.getMessage();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			ERROR_MESSAGE = "Could not prepare file " + file.getPath() + ". An IOException occurred: " + ioe.getMessage();
		} catch (SAXException sae) {
			sae.printStackTrace();
			ERROR_MESSAGE = "Could not prepare file " + file.getPath() + ". An SAXException occurred: " + sae.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not prepare file " + file.getPath() + ". An Exception occurred: " + e.getMessage();
		}
		
		return false;
	}
	
	private boolean buildConcatFile(BufferedWriter bw, List<String> lines) {
		ERROR_MESSAGE = "";
		
		try {
			for (String line : lines) {
				line = line.replace("&", "&amp;");
				bw.write(line);
			}
			return true;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			ERROR_MESSAGE = "Could not build concat file " + lines.toString() + ". An IOException occurred: " + ioe.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not build concat file " + lines.toString() + ". An Exception occurred: " + e.getMessage();
		}
		
		return false;
	}
	
	public Document createDocument() {
    	ERROR_MESSAGE = "";
    
    	try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(concatFile);
			return doc;
		} catch (Exception e) {
			e.printStackTrace(); 
			ERROR_MESSAGE = "Could not create document. An Exception occurred: " + e.getMessage();
	    }
		
		return null;
	}
    
	private void addUniqueIds(Node node) {
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
						((Element) currentNode).setAttribute("id", currentNode.getNodeName() + "_" + UUID.randomUUID().toString());
					}
				}

				// calls this method for all the children which is Element
				addUniqueIds(currentNode);
			}
		}
	}
	
	private boolean cleanReferences(NodeList refNodes) {
		ERROR_MESSAGE = "";
		
		try {
			for (int i = 0; i < refNodes.getLength(); i++) {
				Node refNode = refNodes.item(i);
				NamedNodeMap attrs = refNode.getAttributes();
				for (int j = 0; j < attrs.getLength(); j++) {
					Attr attr = (Attr) attrs.item(j);
					if (attr.getNodeName().equalsIgnoreCase("href")) {
						if (!attr.getNodeValue().contains("www")
								&& attr.getNodeValue().contains("#")) {
							// remove file reference
							attr.setNodeValue(attr.getNodeValue().substring(
									attr.getNodeValue().indexOf("#")));
						} else if (!attr.getNodeValue().contains("www")
								&& !attr.getNodeValue().contains("#")
								&& attr.getNodeValue().contains(".xhtml")) {
							String fileRef = attr.getNodeValue();

							// create default handler instance
							FindIdHandler findIdHandler = new FindIdHandler();

							SAXParserFactory factory = SAXParserFactory
									.newInstance();
							SAXParser saxParser = factory.newSAXParser();
							saxParser.parse(new File(epubFolder + File.separator + fileRef), findIdHandler);

							String bodyId = findIdHandler.getId();
							if (bodyId == null || bodyId.equals("")) {

							}

							attr.setNodeValue("#" + bodyId);
						}
					}
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not clean references. An Exception occurred: " + e.getMessage();
		}
		
		return false;
	}
	
    public boolean saveDocument(Document doc, File file) {
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
			te.printStackTrace();
			ERROR_MESSAGE = "Could not save document to file " + file.getPath() + ". An TransformerException occurred: " + te.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not save document to file " + file.getPath() + ". An Exception occurred: " + e.getMessage();
		}
		
		return false;
	}
	
	private void removeEmptyLines(Document doc) {
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
    
	private boolean addOpfItem(String fileName) {
		ERROR_MESSAGE = "";
		
		try {
			// create instance of DocumentBuilderFactory
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();

			// get the DocumentBuilder
			DocumentBuilder docBuilder = factory.newDocumentBuilder();

			// parsing XML Document
			Document opfDoc = docBuilder.parse(new File(epubFolder
					+ File.separator + EpubUtils.PACKAGE_FILENAME));

			NodeList manifest = opfDoc.getElementsByTagName("manifest");
			NodeList spine = opfDoc.getElementsByTagName("spine");

			Element manifestItem = opfDoc.createElement("item");
			manifestItem.setAttribute("href", fileName);
			manifestItem.setAttribute("id", "item_concat");
			manifestItem.setAttribute("media-type", "application/xhtml+xml");
			manifest.item(0).appendChild(manifestItem);

			Element spineItem = opfDoc.createElement("itemref");
			spineItem.setAttribute("idref", "item_concat");
			spine.item(0).appendChild(spineItem);

			saveDocument(opfDoc, new File(epubFolder + File.separator + EpubUtils.PACKAGE_FILENAME));
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not add file " + fileName + " to opf document. An Exception occurred: " + e.getMessage();
		}
		
		return false;
	}

	private boolean removeOpfItem(String fileName) {
		ERROR_MESSAGE = "";
		
		try {
			// create instance of DocumentBuilderFactory
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();

			// get the DocumentBuilder
			DocumentBuilder docBuilder = factory.newDocumentBuilder();

			// parsing XML Document
			Document opfDoc = docBuilder.parse(new File(epubFolder
					+ File.separator + EpubUtils.PACKAGE_FILENAME));

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

			saveDocument(opfDoc, new File(epubFolder + File.separator + EpubUtils.PACKAGE_FILENAME));
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not remove file " + fileName + " from opf document. An Exception occurred: " + e.getMessage();
		}
		
		return false;
	}
	
	private boolean removeFallbackFromOpf() {
		ERROR_MESSAGE = "";
		
		try {
			List<String> nonSpineElements = new ArrayList<String>();
			getNonSpineElements(new File(epubFolder), "", nonSpineElements);
			
			// create instance of DocumentBuilderFactory
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();

			// get the DocumentBuilder
			DocumentBuilder docBuilder = factory.newDocumentBuilder();

			// parsing XML Document
			Document opfDoc = docBuilder.parse(new File(epubFolder + File.separator + EpubUtils.PACKAGE_FILENAME));

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

			saveDocument(opfDoc, new File(epubFolder + File.separator + EpubUtils.PACKAGE_FILENAME));
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not remove fallback from opf document. An Exception occurred: " + e.getMessage();
		}
		
		return false;
	}
	
	private void getNonSpineElements(File dir, String subFolder, List<String> nonSpineElements) {
		File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();

            if (fileName.endsWith("nav.xhtml") || (fileName.contains(".") && !fileName.substring(fileName.lastIndexOf(".")).equals(".xhtml"))) {
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

	private boolean addFileToEpub(TFile source, TFile destination) {
		ERROR_MESSAGE = "";
		
		try {
			source.cp_rp(new TFile(destination, source.getName()));
			return true;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			ERROR_MESSAGE = "Could not add file " + new File(destination, source.getName()).getPath() + " to epub. An IOException occurred: " + ioe.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not add file " + new File(destination, source.getName()).getPath() + " to epub. An Exception occurred: " + e.getMessage();
		}
		
		return false;
	}
	
	private boolean removeFilesFromEpub(TFile destination) {
		ERROR_MESSAGE = "";
		
		try {
			for (TFile source : destination.listFiles()) {
				if (source.isFile()
						&& source.getName().substring(source.getName().lastIndexOf(".")).equals(".xhtml")
						&& !source.getName().equals("concat.xhtml")
						&& !source.getName().equals("nav.xhtml")) {
					removeFileFromEpub(source);
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not remove files from destination " + destination.getPath() + ". An Exception occurred: " + e.getMessage();
		}
		
		return false;
	}
	
	private boolean removeFileFromEpub(TFile source) {
		ERROR_MESSAGE = "";
		
		try {
			source.rm();
			return true;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			ERROR_MESSAGE = "Could not remove file " + source.getPath() + ". An IOException occurred: " + ioe.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not remove file " + source.getPath() + ". An Exception occurred: " + e.getMessage();
		}
		
		return false;
	}

	private boolean commitChanges() {
		ERROR_MESSAGE = "";
		
		try {
			TVFS.umount();
			return true;
		} catch (FsSyncException fsse) {
			fsse.printStackTrace();
			ERROR_MESSAGE = "Could not commit changes to epub file. An FsSyncException occurred: " + fsse.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			ERROR_MESSAGE = "Could not commit changes to epub file. An Exception occurred: " + e.getMessage();
		}
		
		return false;
	}

}
