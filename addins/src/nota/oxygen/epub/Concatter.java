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

	private File[] listOfFiles;

	private static PackageHandler packageHandler;
	private static ConcatHandler concatHandler;
	
	class Task extends SwingWorker<Void, Void> {
		// Main task. Executed in background thread.
		@Override
		public Void doInBackground() {
			if (!EpubUtils.start(taskOutput)) return null;
			if (!EpubUtils.unzip(taskOutput)) return null;
			if (!EpubUtils.canConcat(taskOutput)) return null;
			if (!EpubUtils.backup(taskOutput)) return null;
			
			EpubUtils.outputProcess("PREPARING AND PARSING", true, taskOutput);
			
			// create package handler instance
			packageHandler = new PackageHandler();
			if (!EpubUtils.parseFile(new File(EpubUtils.EPUB_FOLDER + File.separator + EpubUtils.PACKAGE_FILENAME), packageHandler, taskOutput)) return null;
			
			// get all xhtml files from extracted zip file
			listOfFiles = EpubUtils.getFiles(false, true);
						
			// create concat handler instance
			concatHandler = new ConcatHandler();

			for (File file : listOfFiles) {
				// prepare source file
				if (!EpubUtils.prepareFile(file, taskOutput)) return null;
				
				// parse source file
				if (!EpubUtils.parseFile(file, concatHandler, taskOutput)) return null;
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
			if (!cleanReferences(concatDoc.getElementsByTagName("a"), EpubUtils.EPUB_FOLDER, taskOutput)) return null;
			
			// save concat document
			if (!EpubUtils.saveDocument(concatDoc, new File(EpubUtils.EPUB_FOLDER + File.separator + EpubUtils.CONCAT_FILENAME), taskOutput)) return null;
			
			EpubUtils.outputProcess("MODIFYING PACKAGE DOCUMENT", true, taskOutput);
			
			Document packageDoc = EpubUtils.createDocument(new File(EpubUtils.EPUB_FOLDER + File.separator + EpubUtils.PACKAGE_FILENAME), taskOutput);		
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
			if (!EpubUtils.removeFallbackFromOpf(packageDoc, taskOutput)) return null;
						
			// save opf document
			if (!EpubUtils.saveDocument(packageDoc, new File(EpubUtils.EPUB_FOLDER + File.separator + EpubUtils.PACKAGE_FILENAME), taskOutput)) return null;

			EpubUtils.outputProcess("MODIFYING EPUB", true, taskOutput);
			
			// modify epub file
			TConfig.get().setArchiveDetector(new TArchiveDetector("epub", new JarDriver(IOPoolLocator.SINGLETON)));
			
			TFile destination = new TFile(EpubUtils.EPUB.getPath() + File.separator + EpubUtils.EPUB_FOLDER.substring(EpubUtils.EPUB_FOLDER.lastIndexOf(File.separator)).replace(File.separator, ""));
			
			if (!EpubUtils.addFileToEpub(new TFile(EpubUtils.EPUB_FOLDER + File.separator + EpubUtils.CONCAT_FILENAME), destination, taskOutput)) return null;
			
			if (!EpubUtils.addFileToEpub(new TFile(EpubUtils.EPUB_FOLDER + File.separator + EpubUtils.PACKAGE_FILENAME), destination, taskOutput)) return null;
			
			for (int i = 0; i < listOfFiles.length; i++) {
				if (!EpubUtils.removeFileFromEpub(new TFile(destination, listOfFiles[i].getName()), taskOutput)) return null;
			}
			
			if (!EpubUtils.commitChanges(taskOutput)) return null;

			if (!EpubUtils.finish(taskOutput)) return null;
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
	
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		// Invoked when the user presses the start button.
		startButton.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		// Instances of javax.swing.SwingWorker are not reusuable, so we create new instances as needed.
		task = new Task();
		task.addPropertyChangeListener(this);
		task.execute();
	}

	private static void createAndShowGUI() {
		JComponent newContentPane = new Concatter();
		newContentPane.setOpaque(true);
		
		frame = new JFrame("Concatenating " + EpubUtils.EPUB.getName());
		frame.setContentPane(newContentPane);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		EpubUtils.EPUB = new File(args[0]);
		EpubUtils.EPUB_FOLDER = args[1];
		EpubUtils.prepare("concatter", "concat");
		
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
			
			if (packageHandler.getTitle() != null) {
				xmlTemplate.append("<title>" + packageHandler.getTitle() + "</title>");
			} else {
				xmlTemplate.append("<title>" + concatHandler.getTitle() + "</title>");
			}
			
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

			//System.out.println(xmlTemplate.toString());
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
