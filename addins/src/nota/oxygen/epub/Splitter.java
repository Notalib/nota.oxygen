package nota.oxygen.epub;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.beans.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import nota.oxygen.common.Utils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ro.sync.ecss.extensions.api.AuthorOperationException;
import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;

@SuppressWarnings("serial")
public class Splitter extends JPanel implements ActionListener, PropertyChangeListener {
	private static JFrame frame;
	private JButton startButton;
	private JTextArea taskOutput;
	private Task task;

	private File[] listOfFiles;
	
	private SplitHandler splitHandler;
	private int docNumber;
	private String idPrefix;
	private Map<String, Document> docList;
	private boolean stop;
	private Map<String, String> ids;
	
	class Task extends SwingWorker<Void, Void> {
		
		@Override
		public Void doInBackground() {
			if (!EpubUtils.start(taskOutput)) return null;
			if (!EpubUtils.unzip(taskOutput)) return null;
			if (!EpubUtils.canSplit(taskOutput)) return null;
			if (!EpubUtils.backup(taskOutput)) return null;

			EpubUtils.outputProcess("PREPARING AND PARSING", true, taskOutput);
			
			docNumber = 0;
			docList = new TreeMap<String, Document>();
			ids = new HashMap<String, String>();

			// get concat xhtml file from extracted zip file
			listOfFiles = EpubUtils.getFiles(true, false);
			
			// create split handler instance
			splitHandler = new SplitHandler();

			// prepare source file
			if (!EpubUtils.prepareFile(listOfFiles[0], taskOutput)) return null;

			// parse source file
			if (!EpubUtils.parseFile(listOfFiles[0], splitHandler, taskOutput)) return null;
			
			if (splitHandler.getMetaNodes().containsKey("dc:identifier")) {
				idPrefix = splitHandler.getMetaNodes().get("dc:identifier");
			}
			
			EpubUtils.outputProcess("BUILDING SPLIT DOCUMENTS", true, taskOutput);

			if (!splitConcatDocument(listOfFiles[0])) return null;
			if (!mapRefs()) return null;
			if (!saveDocs()) return null;
			
			EpubUtils.outputProcess("MODIFYING PACKAGE DOCUMENT", true, taskOutput);
			
			listOfFiles = EpubUtils.getFiles(false, true);
			if (listOfFiles.length == 0) {
				EpubUtils.outputMessage(taskOutput, "No split files found");
				return null;
			}
			else if (listOfFiles.length == 1) {
				EpubUtils.outputMessage(taskOutput, "Only one file found - concat file have not been splitted correctly");
				return null;
			}
			
			Document packageDoc = EpubUtils.createDocument(new File(EpubUtils.EPUB_FOLDER + File.separator + EpubUtils.PACKAGE_FILENAME), taskOutput);		
			if (packageDoc == null) {
				return null;
			}
			
			for (int i = 0; i < listOfFiles.length; i++) {
				// add split document to opf document
				if (!EpubUtils.addOpfItem(packageDoc, listOfFiles[i].getName(), i+1, taskOutput)) return null;
			}
			
			// remove concat document from opf document
			if (!EpubUtils.removeOpfItem(packageDoc, EpubUtils.CONCAT_FILENAME, taskOutput)) return null;

			// remove fallback from non xhtml spine elements
			if (!EpubUtils.removeFallbackFromOpf(packageDoc, taskOutput)) return null;

			// save opf document
			if (!EpubUtils.saveDocument(packageDoc, new File(EpubUtils.EPUB_FOLDER	+ File.separator + EpubUtils.PACKAGE_FILENAME), taskOutput)) return null;

			EpubUtils.outputProcess("MODIFYING EPUB", true, taskOutput);
			
			// modify epub file
			TConfig.get().setArchiveDetector(new TArchiveDetector("epub", new JarDriver(IOPoolLocator.SINGLETON)));

			TFile destination = new TFile(EpubUtils.EPUB.getPath() + File.separator + EpubUtils.EPUB_FOLDER.substring(EpubUtils.EPUB_FOLDER.lastIndexOf(File.separator)).replace(File.separator, ""));

			for (int i = 0; i < listOfFiles.length; i++) {
				if (!EpubUtils.addFileToEpub(new TFile(listOfFiles[i]), destination, taskOutput)) return null;
			}

			if (!EpubUtils.addFileToEpub(new TFile(EpubUtils.EPUB_FOLDER + File.separator + EpubUtils.PACKAGE_FILENAME), destination, taskOutput)) return null;

			if (!EpubUtils.removeFileFromEpub(new TFile(destination, EpubUtils.CONCAT_FILENAME), taskOutput)) return null;

			if (!EpubUtils.commitChanges(taskOutput)) return null;
			
			if (!EpubUtils.finish(taskOutput)) return null;
			return null;
		}

		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			startButton.setEnabled(true);
			setCursor(null);
			EpubUtils.outputMessage(taskOutput, "Done");
		}
	}

	public Splitter() {
		super(new BorderLayout());

		// Create the demo's UI.
		startButton = new JButton("Start");
		startButton.setActionCommand("start");
		startButton.addActionListener(this);
		startButton.setVisible(false);

		taskOutput = new JTextArea(30, 130);
		taskOutput.setMargin(new Insets(5, 5, 5, 5));
		taskOutput.setEditable(false);

		JPanel panel = new JPanel();
		panel.add(startButton);

		add(panel, BorderLayout.PAGE_START);
		add(new JScrollPane(taskOutput), BorderLayout.CENTER);
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		startButton.doClick();
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		startButton.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		task = new Task();
		task.addPropertyChangeListener(this);
		task.execute();
	}

	private static void createAndShowGUI() {
		JComponent newContentPane = new Splitter();
		newContentPane.setOpaque(true);
		
		frame = new JFrame("Splitting " + EpubUtils.EPUB.getName());
		frame.setContentPane(newContentPane);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		EpubUtils.EPUB = new File(args[0]);
		EpubUtils.EPUB_FOLDER = args[1];
		EpubUtils.prepare("splitter", "split");
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	private boolean splitConcatDocument(File concatFile) {
		try {
			EpubUtils.outputMessage(taskOutput, "Splitting concat document");
			
			// create concat document
			Document sourceDoc = EpubUtils.createDocument(concatFile, taskOutput);
			if (sourceDoc == null) {
				return false;
			}

			// Find Source body
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			Node sourceBody = (Node) xpath.evaluate("//*[local-name() = 'body']", sourceDoc.getDocumentElement(), XPathConstants.NODE);
			NodeList bodyNodes = sourceBody.getChildNodes();
			
			for (int i = 0; i < bodyNodes.getLength(); i++) {
				Node sectionNodeAtLevel1 = bodyNodes.item(i);
				if (sectionNodeAtLevel1.getNodeType() == Node.ELEMENT_NODE) {
					createNewEpubDoc(sectionNodeAtLevel1);
				}
			}
			return true;
		} catch (XPathExpressionException xpee) {
			EpubUtils.outputMessage(taskOutput, "Could not split concat document. An XPathExpressionException occurred: " + xpee.getMessage());
		} catch (AuthorOperationException aoe) {
			EpubUtils.outputMessage(taskOutput, "Could not split concat document. An AuthorOperationException occurred: " + aoe.getMessage());
		} catch (Exception e) {
			EpubUtils.outputMessage(taskOutput, "Could not split concat document. An Exception occurred: " + e.getMessage());
		}
					
		return false;
	}
	
	private void createNewEpubDoc(Node section) throws AuthorOperationException {
		docNumber = docNumber + 1;

		StringBuilder xmlTemplate = new StringBuilder();
		
		xmlTemplate.append("<html");
		
		for (Map.Entry<String, String> entry : splitHandler.getHtmlAttributes().entrySet()) {
			xmlTemplate.append(" " + entry.getKey() + "='" + entry.getValue() + "'");
		}
		
		xmlTemplate.append(">");
		
		xmlTemplate.append("<head>");
		xmlTemplate.append("<meta charset='UTF-8'/>");
		xmlTemplate.append("<title>" + splitHandler.getSourceTitle() + "</title>");
		
		for (Map.Entry<String, String> entry : splitHandler.getMetaNodes().entrySet()) {
			xmlTemplate.append("<meta content='" + entry.getValue() + "' name='" + entry.getKey() + "'/>");
		}
		
		for (String cssLink : splitHandler.getCssLinks()) {
			xmlTemplate.append("<link href='" + cssLink + "' rel='stylesheet' type='text/css'/>");
		}
		
		xmlTemplate.append("</head>");
		xmlTemplate.append("<body/>");
		xmlTemplate.append("</html>");

		Document template = Utils.deserializeDocument(xmlTemplate.toString(), null);

		Node templateBody = template.getElementsByTagName("body").item(0);

		// Read Section Node
		// Section Classname
		String className = getAttributeFromNode(section, "class");
		if (!className.equals("")) {
			// Add class to body
			Attr attrClass = template.createAttribute("class");
			attrClass.setValue(className);
			NamedNodeMap bodyAttrs = templateBody.getAttributes();
			bodyAttrs.setNamedItem(attrClass);
		}

		// Section Id
		String id = getAttributeFromNode(section, "id"); 
		if (!id.equals("")) {
			// Add id to body
			Attr attrID = template.createAttribute("id");
			attrID.setValue(id);
			NamedNodeMap bodyAttrs = templateBody.getAttributes();
			bodyAttrs.setNamedItem(attrID);
		}

		// Section epub:type
		String epubType = getAttributeFromNode(section, "epub:type");
		
		// Add epub:type to body
		Attr attrEpubType = template.createAttributeNS("http://www.idpf.org/2007/ops", "epub:type");
		attrEpubType.setValue(epubType);
		NamedNodeMap bodyAttrs = templateBody.getAttributes();
		bodyAttrs.setNamedItemNS(attrEpubType);

		// epub:type might be divided by spaces - only 1 value will be used

		String epubMainType = getEpubMainType(epubType);
		if(epubMainType.equals("")) {
			epubMainType = "unknown";
		}

		// Add Nodes to body
		NodeList sectionNodes = section.getChildNodes();
		for (int i = 0; i < sectionNodes.getLength(); i++) {
			Node sectionNode = sectionNodes.item(i);

			// Import the node
			Node importedNode = template.importNode(sectionNode, true);
			templateBody.appendChild(importedNode);
		}

		// Create FileName
		String strNum = "00" + Integer.toString(docNumber);
		strNum = strNum.substring(strNum.length() - 3);
		String newFileName = idPrefix + "-" + strNum + "-" + epubMainType + ".xhtml";

		docList.put(newFileName, template);
		stop = false;
		collectIds(newFileName, templateBody);
	}
	
	private String getAttributeFromNode(Node node, String attrName) {
		Element tmp = (Element) node;
		return tmp.getAttribute(attrName);
	}
	
	private String getEpubMainType(String epubType) {
		// Hvis der kun er en enkelt attributværdi returneres denne. Ellers
		// returnes de værdier der IKKE er frontmatter, bodymatter eller
		// rearmatter
		epubType = epubType.replace("  ", " ");

		if (epubType.contains(" ")) {
			epubType = epubType.replace("frontmatter", "");
			epubType = epubType.replace("bodymatter", "");
			epubType = epubType.replace("backmatter", "");
		}

		epubType = epubType.trim();
		epubType = epubType.replaceAll(" ", "-");
		return epubType;
	}
	
	private void collectIds(String fileName, Node node) {
		// Kør rekursivt
		if (stop) {
			return;
		}

		// Check on noden har et id
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			String id = getAttributeFromNode(node, "id");
			if (id != "") {
				// Læg i Map
				if (addId2Map(id, fileName) == false) {
					stop = true;
					return;
				}
			}
		}

		if (node.hasChildNodes()) {
			NodeList lst = node.getChildNodes();
			for (int i = 0; i < lst.getLength(); i++) {
				Node childNode = lst.item(i);
				collectIds(fileName, childNode);
			}
		}
	}
	
	private boolean addId2Map(String id, String fileName) {
		if (ids.containsKey(id)) {
			EpubUtils.outputMessage(taskOutput, "Id'et " + id + " findes flere gange i dokumentet");
			return false;
		}
		ids.put(id, fileName);
		return true;
	}
	
	private boolean mapRefs() {
		try {
			EpubUtils.outputMessage(taskOutput, "Editing references");
			
			for (Map.Entry<String, Document> entry : docList.entrySet()) {
				NodeList refNodes = null;
				Document doc = entry.getValue();

				// Get references
				XPathFactory factory = XPathFactory.newInstance();
				XPath xpath = factory.newXPath();
				try {
					refNodes = (NodeList) xpath.evaluate("//*[local-name() = 'a']", doc, XPathConstants.NODESET);
				} catch (Exception e) {
					EpubUtils.outputMessage(taskOutput, "Error in Xml document: " + e.getMessage());
					return false;
				}

				// Check referencer mod samlingen af Id'er
				for (int i = 0; i < refNodes.getLength(); i++) {
					Node ref = refNodes.item(i);
					String href = getAttributeFromNode(ref, "href");
					href = href.replace("#", "");

					String fileName = GetReferenceFileName(href);
					if (fileName != "") {
						// Change reference
						Attr attHref = doc.createAttribute("href");
						attHref.setValue(fileName + "#" + href);
						NamedNodeMap bodyAttrs = ref.getAttributes();
						bodyAttrs.setNamedItem(attHref);
					}
				}
			}
			return true;
		} 
		catch (Exception e) {
			EpubUtils.outputMessage(taskOutput, "Could not edit references. An Exception occurred: " + e.getMessage());
		}
		
		return false;
	}
	
	private String GetReferenceFileName(String ref) {
		if (ids.containsKey(ref)) {
			String fileName = (String) ids.get(ref);
			return fileName;
		}
		
		return "";
	}
	
	private boolean saveDocs() {
		try {
			// save split documents
			for (Map.Entry<String, Document> entry : docList.entrySet()) {
				if (!EpubUtils.saveDocument(entry.getValue(), new File(EpubUtils.EPUB_FOLDER + File.separator + entry.getKey()), taskOutput)) {
					return false;
				}
			}
			return true;
		}
		catch (Exception e) {
			EpubUtils.outputMessage(taskOutput, "Could not save documents. An Exception occurred: " + e.getMessage());
		}
		
		return false;
	}
}
