package GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.*;

import data.*;

@SuppressWarnings("serial")
public class ProseVis extends JPanel implements ActionListener, ItemListener {
	public static String OPEN_FILE_COMMAND = "Open File";
	public static String CLOSE_FILE_COMMAND = "Close File";
	public static String LINENO_VIEW_COMMAND = "Line Nos.";
	
	JMenuBar menuBar;
		
	private JFileChooser fileDialog; //File dialog
	private File lastDirectory;
	private Visualization viz1; //Main visualization panel
	private Visualization viz2; //Main visualization panel
	private JScrollPane leftVizPanel;
	private JScrollPane rightVizPanel;
	private static VizController vizPane;

	/* Tool Panels */ 
	protected DataPanel dataPanel;
	protected SearchPanel searchPanel;
	protected DavidDataPanel davidPanel;
	
	public ProseVis() {
		super(new BorderLayout());

		viz1 = new Visualization();
		viz2 = new Visualization();

		leftVizPanel = new JScrollPane(viz1);
		leftVizPanel.getViewport().addComponentListener(viz1);
		leftVizPanel.setPreferredSize(new Dimension(400, 480));
		leftVizPanel.getVerticalScrollBar().setUnitIncrement(16);
		leftVizPanel.getHorizontalScrollBar().setUnitIncrement(16);
				
		rightVizPanel = new JScrollPane(viz2);
		rightVizPanel.getViewport().addComponentListener(viz2);
		rightVizPanel.setPreferredSize(new Dimension(400, 480));
		rightVizPanel.getVerticalScrollBar().setUnitIncrement(16);
		rightVizPanel.getHorizontalScrollBar().setUnitIncrement(16);
				
		vizPane = new VizController(JSplitPane.HORIZONTAL_SPLIT, leftVizPanel, rightVizPanel, viz1, viz2);
	
		vizPane.setOneTouchExpandable(true);
		//vizPane.setResizeWeight(0.5);
				
		menuBar = PVToolPanel.makeMenuBar(this);
				
		/* Initialize the tool panels we need */
		dataPanel = new DataPanel(vizPane, this);
		searchPanel = new SearchPanel(vizPane, this);
		davidPanel = new DavidDataPanel(vizPane, this);
		
		/* Setting up the tool panel tabs */
		JTabbedPane toolPane = new JTabbedPane();
		toolPane.setMinimumSize(new Dimension(240, 480));
		toolPane.setPreferredSize(new Dimension(240, 480));

		toolPane.addTab("Data", null, dataPanel, "Data Tools");
		toolPane.setMnemonicAt(0, KeyEvent.VK_1);
		
		toolPane.addTab("Search", null, searchPanel, "Search Data");
		toolPane.setMnemonicAt(0, KeyEvent.VK_1);

		toolPane.addTab("Comparison", null, davidPanel, "Comparison");
		toolPane.setMnemonicAt(0, KeyEvent.VK_3);
		
		/* Setting up the tool panel */
		JPanel toolPanel = new JPanel(new BorderLayout());
		toolPanel.add(toolPane, BorderLayout.CENTER);
		toolPanel.add(menuBar, BorderLayout.NORTH);

		/* Adding The Split Pane */
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, vizPane, toolPanel);
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(1.0);
		add(splitPane, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (OPEN_FILE_COMMAND.equals(command)) {

			if(vizPane.getNumFiles() < 2){

				lastDirectory = null;
				try {
					FileInputStream fis = new FileInputStream(".lastpath.txt");
					ObjectInputStream istream = new ObjectInputStream(fis);
					lastDirectory = (File) istream.readObject();
					fis.close();
					istream.close();
				}
				catch (Exception eioo) {
					
				}
			
				if (lastDirectory == null)
					fileDialog = new JFileChooser("./Data/");
				else
					fileDialog = new JFileChooser(lastDirectory);

				int returnVal = fileDialog.showDialog(ProseVis.this, "Open File");

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileDialog.getSelectedFile();
					lastDirectory = file.getParentFile();
					/* Save the last selected directory */
					try {
						FileOutputStream fos = new FileOutputStream(".lastpath.txt");
						ObjectOutputStream ostream = new ObjectOutputStream(fos);
						ostream.writeObject(lastDirectory);
						ostream.close();
						fos.close();
					}
					catch (Exception eio) {
					}
					
					Graphics2D g = (Graphics2D)this.getGraphics();
					InputFile newFile;

					//If a file is already open, we pass it's variable lists to the new file
					if(vizPane.noFiles()){
						newFile = new InputFile(file, g);
					}else{
						InputFile template = vizPane.getFile();
						newFile = new InputFile(template, file, g);
					}
					
					vizPane.addFile(newFile);
					updateViz(false);

					PVToolPanel.addFileItem(newFile.getName(), this);
					this.revalidate();

				}
				fileDialog.setSelectedFile(null);
			}else{
				JOptionPane.showMessageDialog(this, "There are already two files open. To open a new file, please close one of the current files.");
			}

		} else if (CLOSE_FILE_COMMAND.equals(command)) {
			vizPane.closeFiles();
			this.revalidate();
			
			updateViz(false);
		} /*else if (LINENO_VIEW_COMMAND.equals(command)) {
			e.getActionCommand()
			System.out.println("came here");
			 viz1.setViewLineNumber(true);
			 viz2.setViewLineNumber(true);
			 
		} */else { 
			//Closing an individual file
			vizPane.closeFile(command);
			PVToolPanel.removeFileItem(command);
			this.revalidate();
			
			updateViz(false);
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			viz1.setViewLineNumber(true);					
			viz2.setViewLineNumber(true);
		} else {
			viz1.setViewLineNumber(false);					
			viz2.setViewLineNumber(false);
		}
		updateViz(false);
	}
	
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("ProseVis");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		/* Changes the look and feel according to the system */
		try {
			String cn = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(cn);
		} catch (Exception cnf) {
		}

		// Add content to the window.
		frame.add(new ProseVis());

		// Display the window.
		frame.pack();
		frame.setVisible(true);

		vizPane.initializeSizes();
	}
	
	/*
	 * Update the visualization based on changes in the data or search panel.
	 */
	public void updateViz(boolean search){
		//Search results are discarded after additional updates to the data panel
        if(!search){
        	vizPane.killSearch();
        }
		
		vizPane.repaint();
	}
	
	public void updateTiling(boolean horizontal){
		if(horizontal){
			vizPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		}else{
			vizPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		}
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = ProseVis.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	public static void main(String[] args) {

		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}	
}