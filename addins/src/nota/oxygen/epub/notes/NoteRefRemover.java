package nota.oxygen.epub.notes;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.beans.*;
import java.io.File;
import java.util.HashSet;
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
public class NoteRefRemover extends JPanel implements ActionListener, PropertyChangeListener {
	private static JFrame frame;
	private JButton startButton;
	private JTextArea taskOutput;
	private Task task;
	
	private static String fileName = "";
	
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

			EpubUtils.outputProcess("REMOVING NOTEREFS FROM DOCUMENT", true, taskOutput);
			
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
					EpubUtils.outputMessage(taskOutput, "Cannot remove noteref from list element " + liElement.getTextContent() + ", id not found");
					ERRORS_FOUND = true;
					continue;
				}
				
				String epubType = liElement.getAttribute("epub:type");
				if (epubType == null || epubType.equals("")) {
					EpubUtils.outputMessage(taskOutput, "Cannot remove noteref from list element with id " + id + ", epub:type not found");
					ERRORS_FOUND = true;
					continue;
				}
				
				if (!epubType.equals("footnote") && !epubType.equals("rearnote")) {
					EpubUtils.outputMessage(taskOutput, "Cannot remove noteref from list element with id " + id + ", epub:type should be either footnote or rearnote");
					ERRORS_FOUND = true;
					continue;
				}
				
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
				} else {
					EpubUtils.outputMessage(taskOutput, "Cannot remove noteref from list element, too many paragraphs");
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
			if (!EpubUtils.addFileToEpub(new TFile(EpubUtils.EPUB_FOLDER + File.separator + fileName), destination, taskOutput))
				return null;
			
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
	
	public NoteRefRemover() {
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
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		startButton.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		task = new Task();
		task.addPropertyChangeListener(this);
		task.execute();
	}
	
	private static void createAndShowGUI() {
		JComponent newContentPane = new NoteRefRemover();
		newContentPane.setOpaque(true);
		
		frame = new JFrame("Removing noterefs freom " + fileName);
		frame.setContentPane(newContentPane);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		EpubUtils.EPUB = new File(args[0]);
		EpubUtils.EPUB_FOLDER = args[1];
		EpubUtils.prepare("noterefremover", "noterefremove");
		fileName = args[2];
		
		// Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
