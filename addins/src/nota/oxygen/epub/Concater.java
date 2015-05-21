package nota.oxygen.epub;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.beans.*;
import java.io.File;
import org.w3c.dom.Document;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;

@SuppressWarnings("serial")
public class Concater extends JPanel implements ActionListener, PropertyChangeListener {
	private JButton startButton;
	private JTextArea taskOutput;
	private Task task;

	private static String epubZipPath;
	private String workFolder = "";
	private static String epubFolder = "";
	private String concatFile = "";
	private File[] listOfFiles;
	
	class Task extends SwingWorker<Void, Void> {
		// Main task. Executed in background thread.
		@Override
		public Void doInBackground() {
			outputProcess("STARTING");
			
			// set working folder 
			workFolder = new File(epubZipPath).getParent() + File.separator + "workfolder";

			// set epubFolder
			epubFolder = workFolder + File.separator + epubFolder.substring(epubFolder.lastIndexOf("/")).replace("/", "");

			// set concat file
			concatFile = epubFolder + File.separator + EpubUtils.CONCAT_FILENAME;

			// create working folder
			if (!new File(workFolder).exists())	new File(workFolder).mkdir();

			// clean workfolder
			if(!EpubUtils.cleanWorkfolder(workFolder, taskOutput)) return null;
			
			outputProcess("BACKING UP EPUB");
			
			// backup epub
			if (!EpubUtils.backupEpub(epubZipPath, taskOutput)) return null;
			
			outputProcess("UNZIPPING");
			
			// unzip epub to workfolder
			if (!EpubUtils.unzip(epubZipPath, workFolder, taskOutput)) return null;

			outputProcess("PREPARING AND PARSING");
			
			// get all xhtml files from extracted zip file
			listOfFiles = EpubUtils.getFiles(epubFolder);

			// create concat handler instance
			ConcatHandler concatHandler = new ConcatHandler();

			for (int i = 0; i < listOfFiles.length; i++) {
				// prepare source file
				if (!EpubUtils.prepareFile(listOfFiles[i], taskOutput)) return null;
				
				// parse source file
				if (!EpubUtils.parseFile(listOfFiles[i], concatHandler, taskOutput)) return null;
			}
			
			outputProcess("BUILDING CONCAT DOCUMENT");
			
			// build concat file
			EpubUtils.buildConcatFile(concatFile, concatHandler, taskOutput);
						
			// create new concat document
			Document concatDoc = EpubUtils.createDocument(new File(concatFile), taskOutput);
			if (concatDoc == null) {
				EpubUtils.outputMessage(taskOutput, "Concat file was not found");
				return null;
			}
			
			// add unique ids to missing elements
			EpubUtils.addUniqueIds(concatDoc.getDocumentElement(), taskOutput);
			
			// clean references
			if (!EpubUtils.cleanReferences(concatDoc.getElementsByTagName("a"), epubFolder, taskOutput)) return null;
			
			// save concat document
			if (!EpubUtils.saveDocument(concatDoc, new File(concatFile), taskOutput)) return null;
			
			outputProcess("MODIFYING PACKAGE DOCUMENT");
			
			Document packageDoc = EpubUtils.createDocument(new File(epubFolder + File.separator + EpubUtils.PACKAGE_FILENAME), taskOutput);		
			if (packageDoc == null) {
				EpubUtils.outputMessage(taskOutput, "Package file was not found");
				return null;
			}
			
			// add concat document to opf document
			if (!EpubUtils.addOpfItem(packageDoc, EpubUtils.CONCAT_FILENAME, taskOutput)) return null;
			
			// remove fallback from non xhtml spine elements
			if (!EpubUtils.removeFallbackFromOpf(packageDoc, epubFolder, taskOutput)) return null;
			
			// remove non concat documents from opf document
			for (int i = 0; i < listOfFiles.length; i++) {
				String fileName = listOfFiles[i].getName();
				if (!EpubUtils.removeOpfItem(packageDoc, fileName, taskOutput)) return null;
			}

			// save opf document
			if (!EpubUtils.saveDocument(packageDoc, new File(epubFolder + File.separator + EpubUtils.PACKAGE_FILENAME), taskOutput)) return null;

			outputProcess("MODIFYING EPUB");
			
			// modify epub file
			TConfig.get().setArchiveDetector(new TArchiveDetector("epub", new JarDriver(IOPoolLocator.SINGLETON)));
			
			TFile source = null;
			TFile destination = new TFile(epubZipPath + File.separator + epubFolder.substring(epubFolder.lastIndexOf(File.separator)).replace(File.separator, ""));
			
			source = new TFile(concatFile);
			if (!EpubUtils.addFileToEpub(source, destination, taskOutput)) return null;
			
			source = new TFile(epubFolder + File.separator + EpubUtils.PACKAGE_FILENAME);
			if (!EpubUtils.addFileToEpub(source, destination, taskOutput)) return null;
			
			if (!EpubUtils.removeFilesFromEpub(destination, taskOutput)) return null;
			
			if (!EpubUtils.commitChanges(taskOutput)) return null;

			outputProcess("FINISHING");
			
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

	public Concater() {
		super(new BorderLayout());

		// Create the demo's UI.
		startButton = new JButton("Start");
		startButton.setActionCommand("start");
		startButton.addActionListener(this);
		startButton.setVisible(false);

		taskOutput = new JTextArea(20, 100);
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
		JFrame frame = new JFrame("ProgressBarDemo");
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		JComponent newContentPane = new Concater();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		epubZipPath = args[0];
		epubFolder = args[1];
		
		// Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	private void outputProcess(String process) {
		EpubUtils.outputMessage(taskOutput, "");
		EpubUtils.outputMessage(taskOutput, "********************************************************");
		EpubUtils.outputMessage(taskOutput, process);
		EpubUtils.outputMessage(taskOutput, "********************************************************");
	}
}
