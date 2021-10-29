package nota.oxygen.epub.notes;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.beans.*;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nota.oxygen.epub.EpubUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;

@SuppressWarnings("serial")
public class NoteRefInserter extends JPanel implements ActionListener, PropertyChangeListener {
	private static JFrame frame;
	private JButton startButton;
	private JTextArea taskOutput;
	private Task task;
	
	private static String fileName;
	private File[] listOfFiles;
	
	private static FindNoteRefHandler findNoteRefHandler;
	
	public static boolean ERRORS_FOUND;
	
	class Task extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			if (!EpubUtils.start(taskOutput))
				return null;

			if (!EpubUtils.unzip(taskOutput))
				return null;
			
			if (!EpubUtils.backup(taskOutput))
				return null;
			
			EpubUtils.outputProcess("PREPARING AND PARSING", true, taskOutput);
			
			// get all files where noterefs can be found
			listOfFiles = EpubUtils.getFiles(false, false);

			findNoteRefHandler = new FindNoteRefHandler();
			for (File file : listOfFiles) {
				String fileName = file.getName();
				if (fileName.endsWith("-footnotes.xhtml") || fileName.endsWith("-rearnotes.xhtml")) {
					continue;
				}
				
				// prepare source file
				if (!EpubUtils.prepareFile(file, taskOutput))
					return null;
				
				// set source filename
				findNoteRefHandler.setFileName(file.getName());
				
				// parse source file
				if (!EpubUtils.parseFile(file, findNoteRefHandler, taskOutput))
					return null;
			}
			
			if (findNoteRefHandler.getIDMap().size() == 0 || findNoteRefHandler.getFNMap().size() == 0) {
				EpubUtils.outputMessage(taskOutput, "Cannot insert noterefs, zero found");
				return null;
			}
			
			if (findNoteRefHandler.getFNMap().size() != findNoteRefHandler.getFNMap().size()) {
				EpubUtils.outputMessage(taskOutput, "Cannot insert noterefs, mismatch between ids and filenames");
				return null;
			}
			
			EpubUtils.outputProcess("ADDING NOTEREFS TO DOCUMENT", true, taskOutput);
			
			Document doc = EpubUtils.createDocument(new File(EpubUtils.EPUB_FOLDER + File.separator + fileName), taskOutput);
			if (doc == null) {
				return null;
			}
			
			NodeList liNodeList = doc.getDocumentElement().getElementsByTagName("li");
			for (int i=0; i<liNodeList.getLength(); i++) {
				Element liElement = (Element) liNodeList.item(i);
				NodeList pNodeList = liElement.getElementsByTagName("p");
				
				String id = liElement.getAttribute("id");
				if (id == null || id.equals("")) {
					EpubUtils.outputMessage(taskOutput, "Cannot insert noteref to list element " + liElement.getTextContent() + ", id not found");
					ERRORS_FOUND = true;
					continue;
				}
				
				String epubType = liElement.getAttribute("epub:type");
				if (epubType == null || epubType.equals("")) {
					EpubUtils.outputMessage(taskOutput, "Cannot insert noteref to list element with id " + id + ", epub:type not found");
					ERRORS_FOUND = true;
					continue;
				}
				
				if (!epubType.equals("footnote") && !epubType.equals("rearnote")) {
					EpubUtils.outputMessage(taskOutput, "Cannot insert noteref to list element with id " + id + ", epub:type should be either footnote or rearnote");
					ERRORS_FOUND = true;
					continue;
				}
				
				List<String> idList = findNoteRefHandler.getIDMap().get(id);
				List<String> fnList = findNoteRefHandler.getFNMap().get(id);
				
				if (pNodeList.getLength() == 0) {
					NodeList aNodeList = liElement.getElementsByTagName("a");
					System.out.println("notes: " + aNodeList.getLength());
					
					Set<Node> removeNodes = new HashSet<Node>();
					for (int j = 0; j < aNodeList.getLength(); j++) {
						removeNodes.add(aNodeList.item(j));
					}
					
					for (Node node : removeNodes) {
						System.out.println("removing node with href " + ((Element)node).getAttribute("href"));
						liElement.removeChild(node);
					}
					
					if (aNodeList.getLength() == 0) {
						for (int j=0; j<idList.size(); j++) {
							Element aElement = doc.createElement("a");
							aElement.setAttribute("class", "noteref");
							aElement.setAttribute("epub:type", "noteref");
							aElement.setAttribute("href", fnList.get(j) + "#" + idList.get(j));
							aElement.setTextContent("*");
							liElement.appendChild(aElement);
							System.out.println("appended note with href " + fnList.get(j) + "#" + idList.get(j));
						}
					}
				} else if (pNodeList.getLength() == 1) {
					Element pElement = (Element) pNodeList.item(0);
					NodeList aNodeList = pElement.getElementsByTagName("a");
					System.out.println("notes: " + aNodeList.getLength());
					
					Set<Node> removeNodes = new HashSet<Node>();
					for (int j = 0; j < aNodeList.getLength(); j++) {
						removeNodes.add(aNodeList.item(j));
					}
					
					for (Node node : removeNodes) {
						System.out.println("removing node with href " + ((Element)node).getAttribute("href"));
						pElement.removeChild(node);
					}
					
					if (aNodeList.getLength() == 0) {
						for (int j=0; j<idList.size(); j++) {
							Element aElement = doc.createElement("a");
							aElement.setAttribute("class", "noteref");
							aElement.setAttribute("epub:type", "noteref");
							aElement.setAttribute("href", fnList.get(j) + "#" + idList.get(j));
							aElement.setTextContent("*");
							pElement.appendChild(aElement);
							System.out.println("appended note with href " + fnList.get(j) + "#" + idList.get(j));
						}
					}
				} else {
					EpubUtils.outputMessage(taskOutput, "Cannot insert noteref to list element, too many paragraphs");
					ERRORS_FOUND = true;
					continue;
				}
			}
			
			if (!EpubUtils.saveDocument(doc, new File(EpubUtils.EPUB_FOLDER + File.separator + fileName), taskOutput)) 
				return null;
			
			EpubUtils.outputProcess("MODIFYING EPUB", true, taskOutput);
			
			// obtain the global configuration
			TConfig config = TConfig.get();
			config.setArchiveDetector(new TArchiveDetector("epub", new JarDriver(IOPoolLocator.SINGLETON)));
						
			// get epub file destination
			String epubPath = EpubUtils.EPUB.getPath();
			String epubFolder = EpubUtils.EPUB_FOLDER.substring(EpubUtils.EPUB_FOLDER.lastIndexOf(File.separator)).replace(File.separator, "");
			TFile destination = new TFile(epubPath + File.separator + epubFolder);
						
			// modify epub file destination
			for (File file : listOfFiles) {
				if (!EpubUtils.addFileToEpub(new TFile(file), destination, taskOutput))
					return null;
			}
			
			// commit changes to epub file destination
			if (!EpubUtils.commitChanges(taskOutput))
				return null;
			
			if (!EpubUtils.finish(taskOutput))
				return null;
			
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
	
	public NoteRefInserter() {
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
	public void actionPerformed(ActionEvent e) {
		startButton.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		this.task = new Task();
		this.task.addPropertyChangeListener(this);
		this.task.execute();
	}
	
	private static void createAndShowGUI() {
		JComponent newContentPane = new NoteRefInserter();
		newContentPane.setOpaque(true);
		
		frame = new JFrame("Inserting noterefs into " + fileName);
		frame.setContentPane(newContentPane);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		EpubUtils.EPUB = new File(args[0]);
		EpubUtils.EPUB_FOLDER = args[1];
		EpubUtils.prepare("noterefinserter", "noterefinsert");
		fileName = args[2];
				
		// Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}	
}
