package nota.oxygen.epub;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.beans.*;
import java.io.File;
import java.util.Map;

import nota.oxygen.common.Utils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;

@SuppressWarnings("serial")
public class Concatter extends JPanel implements ActionListener, PropertyChangeListener {
	private static JFrame frame;
	private JButton startButton;
	private static JTextArea taskOutput;
	private Task task;

	private static File epub;
	private static String epubFolder = "";
	private String workFolder = "";
	private File[] listOfFiles;
	private String concatFile = "";

	private static ConcatHandler concatHandler;
	
	class Task extends SwingWorker<Void, Void> {
		// Main task. Executed in background thread.
		@Override
		public Void doInBackground() {
			frame.setTitle("Concatenating " + epub.getName() + "...");
			
			EpubUtils.outputProcess("STARTING", false, taskOutput);
			
			// set working folder 
			workFolder = epub.getParent() + File.separator + epub.getName() + ".concatter";

			// set epubFolder
			epubFolder = workFolder + File.separator + epubFolder.substring(epubFolder.lastIndexOf("/")).replace("/", "");

			// set concat file
			concatFile = epubFolder + File.separator + EpubUtils.CONCAT_FILENAME;

			// create working folder
			if (!new File(workFolder).exists())	new File(workFolder).mkdir();

			// clean workfolder
			if(!EpubUtils.cleanWorkfolder(workFolder, taskOutput)) return null;
			
			EpubUtils.outputProcess("BACKING UP EPUB", true, taskOutput);
			
			// backup epub
			if (!EpubUtils.backupEpub(epub, ".concat.bak", taskOutput)) return null;
			
			EpubUtils.outputProcess("UNZIPPING", true, taskOutput);
			
			// unzip epub to workfolder
			if (!EpubUtils.unzip(epub, workFolder, taskOutput)) return null;

			EpubUtils.outputProcess("PREPARING AND PARSING", true, taskOutput);
			
			// get all xhtml files from extracted zip file
			listOfFiles = EpubUtils.getFiles(epubFolder, false);

			// create concat handler instance
			concatHandler = new ConcatHandler();

			for (int i = 0; i < listOfFiles.length; i++) {
				// prepare source file
				if (!EpubUtils.prepareFile(listOfFiles[i], taskOutput)) return null;
				
				// parse source file
				if (!EpubUtils.parseFile(listOfFiles[i], concatHandler, taskOutput)) return null;
			}
			
			EpubUtils.outputProcess("BUILDING CONCAT DOCUMENT", true, taskOutput);
			
			// build concat document
			Document concatDoc = buildConcatDocument();
			if (concatDoc == null) {
				return null;
			}
			
			// add unique ids to missing elements
			EpubUtils.addUniqueIds(concatDoc.getDocumentElement(), taskOutput);
			
			// clean references
			if (!cleanReferences(concatDoc.getElementsByTagName("a"), epubFolder, taskOutput)) return null;
			
			// save concat document
			if (!EpubUtils.saveDocument(concatDoc, new File(concatFile), taskOutput)) return null;
			
			EpubUtils.outputProcess("MODIFYING PACKAGE DOCUMENT", true, taskOutput);
			
			Document packageDoc = EpubUtils.createDocument(new File(epubFolder + File.separator + EpubUtils.PACKAGE_FILENAME), taskOutput);		
			if (packageDoc == null) {
				return null;
			}
			
			// add concat document to opf document
			if (!EpubUtils.addOpfItem(packageDoc, EpubUtils.CONCAT_FILENAME, 0, taskOutput)) return null;
			
			// remove non concat documents from opf document
			for (int i = 0; i < listOfFiles.length; i++) {
				if (!EpubUtils.removeOpfItem(packageDoc, listOfFiles[i].getName(), taskOutput)) return null;
			}

			// remove fallback from non xhtml spine elements
			if (!EpubUtils.removeFallbackFromOpf(packageDoc, epubFolder, taskOutput)) return null;
						
			// save opf document
			if (!EpubUtils.saveDocument(packageDoc, new File(epubFolder + File.separator + EpubUtils.PACKAGE_FILENAME), taskOutput)) return null;

			EpubUtils.outputProcess("MODIFYING EPUB", true, taskOutput);
			
			// modify epub file
			TConfig.get().setArchiveDetector(new TArchiveDetector("epub", new JarDriver(IOPoolLocator.SINGLETON)));
			
			TFile destination = new TFile(epub.getPath() + File.separator + epubFolder.substring(epubFolder.lastIndexOf(File.separator)).replace(File.separator, ""));
			
			if (!EpubUtils.addFileToEpub(new TFile(epubFolder + File.separator + EpubUtils.CONCAT_FILENAME), destination, taskOutput)) return null;
			
			if (!EpubUtils.addFileToEpub(new TFile(epubFolder + File.separator + EpubUtils.PACKAGE_FILENAME), destination, taskOutput)) return null;
			
			for (int i = 0; i < listOfFiles.length; i++) {
				if (!EpubUtils.removeFileFromEpub(new TFile(destination, listOfFiles[i].getName()), taskOutput)) return null;
			}
			
			if (!EpubUtils.commitChanges(taskOutput)) return null;

			EpubUtils.outputProcess("FINISHING", true, taskOutput);
			
			// clean working folder
			if (!EpubUtils.cleanWorkfolder(workFolder, taskOutput)) return null;

			// delete folder
			if (new File(workFolder).exists()) new File(workFolder).delete();
			
			EpubUtils.outputMessage(taskOutput, "");
			
			return null;
		}

		// Executed in event dispatching thread
		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			startButton.setEnabled(true);
			setCursor(null); // turn off the wait cursor
			EpubUtils.outputMessage(taskOutput, "Done");
		}
	}

	public Concatter() {
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

	// Invoked when the user presses the start button.
	public void actionPerformed(ActionEvent evt) {
		startButton.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		// Instances of javax.swing.SwingWorker are not reusuable, so we create new instances as needed.
		task = new Task();
		task.addPropertyChangeListener(this);
		task.execute();
	}

	// Invoked when task's progress property changes.
	public void propertyChange(PropertyChangeEvent evt) {
	}

	// Create the GUI and show it. As with all GUI code, this must run on the event-dispatching thread.
	private static void createAndShowGUI() {
		// Create and set up the window.
		frame = new JFrame("Concatenating ePub...");
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		JComponent newContentPane = new Concatter();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		epub = new File(args[0]);
		epubFolder = args[1];
		
		// Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	public static Document buildConcatDocument() {
		try {
			
			StringBuilder xmlTemplate = new StringBuilder();
			
			xmlTemplate.append("<html");
			
			for (Map.Entry<String, String> entry : concatHandler.getHtmlAttributes().entrySet()) {
				xmlTemplate.append(" " + entry.getKey() + "='" + entry.getValue() + "'");
			}
			
			xmlTemplate.append(">");
			
			xmlTemplate.append("<head>");
			xmlTemplate.append("<meta charset='UTF-8'/>");
			xmlTemplate.append("<title>" + concatHandler.getSourceTitle() + "</title>");
			
			for (Map.Entry<String, String> entry : concatHandler.getMetaNodes().entrySet()) {
				xmlTemplate.append("<meta content='" + entry.getValue() + "' name='" + entry.getKey() + "'/>");
			}
			
			for (String cssLink : concatHandler.getCssLinks()) {
				xmlTemplate.append("<link href='" + cssLink + "' rel='stylesheet' type='text/css'/>");
			}
			
			xmlTemplate.append("</head>");
			xmlTemplate.append("<body>");
			
			for (String line : concatHandler.getBodyLines()) {
				xmlTemplate.append(line);
			}
			
			xmlTemplate.append("</body>");
			xmlTemplate.append("</html>");

			return Utils.deserializeDocument(xmlTemplate.toString(), null);
		} catch (Exception e) {
			EpubUtils.outputMessage(taskOutput, "Could not build concat document. An Exception occurred: " + e.getMessage());
		}
		
		return null;
	}
	
	public static boolean cleanReferences(NodeList refNodes, String epubFolder, JTextArea taskOutput) {
		try {
			EpubUtils.outputMessage(taskOutput, "Cleaning references");
			for (int i = 0; i < refNodes.getLength(); i++) {
				Node refNode = refNodes.item(i);
				NamedNodeMap attrs = refNode.getAttributes();
				for (int j = 0; j < attrs.getLength(); j++) {
					Attr attr = (Attr) attrs.item(j);
					if (attr.getNodeName().equalsIgnoreCase("href")) {
						if (!attr.getNodeValue().contains("www") && attr.getNodeValue().contains("#")) {
							// remove file reference
							attr.setNodeValue(attr.getNodeValue().substring(attr.getNodeValue().indexOf("#")));
						} else if (!attr.getNodeValue().contains("www") && !attr.getNodeValue().contains("#") && attr.getNodeValue().contains(".xhtml")) {
							String fileRef = attr.getNodeValue();

							// create default handler instance
							FindIdHandler findIdHandler = new FindIdHandler();

							SAXParserFactory factory = SAXParserFactory.newInstance();
							SAXParser saxParser = factory.newSAXParser();
							saxParser.parse(new File(epubFolder	+ File.separator + fileRef), findIdHandler);

							String bodyId = findIdHandler.getId();
							if (bodyId == null || bodyId.equals("")) {
								EpubUtils.outputMessage(taskOutput, "Reference " + attr.getNodeValue() + " could not be changed, no id found");
								return false;
							}
							
							String tempNodeValue = attr.getNodeValue();
							attr.setNodeValue("#" + bodyId);
							EpubUtils.outputMessage(taskOutput, "Reference " + tempNodeValue + " changed to " + attr.getNodeValue());
						}
					}
				}
			}
			return true;
		} catch (Exception e) {
			EpubUtils.outputMessage(taskOutput, "Could not clean references. An Exception occurred: " + e.getMessage());
		}

		return false;
	}
}
