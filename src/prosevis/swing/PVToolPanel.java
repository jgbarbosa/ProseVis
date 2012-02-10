package prosevis.swing;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class PVToolPanel {
	private static ArrayList<String> fileNames = new ArrayList<String>();
	private static JMenuBar menuBar;
	private static JMenu fileMenu;
	private static JMenu closeMenu;
	private static JMenu viewMenu;
				
	public static JMenuBar makeMenuBar(ProseVis panel) {
		menuBar = new JMenuBar();
		
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);

		JMenuItem openItem = new JMenuItem("Open");
		openItem.setActionCommand(ProseVis.OPEN_FILE_COMMAND);
		openItem.addActionListener(panel);
		fileMenu.add(openItem);
		
		fileMenu.addSeparator();
		
		closeMenu = new JMenu("Close");
		
		
		JMenuItem fileItem = new JMenuItem("(None)");
		fileItem.setEnabled(false);
		closeMenu.add(fileItem);
		
		
		fileMenu.add(closeMenu);
		
		JMenuItem closeAllItem = new JMenuItem("Close All");
		closeAllItem.setActionCommand(ProseVis.CLOSE_FILE_COMMAND);
		closeAllItem.addActionListener(panel);
		fileMenu.add(closeAllItem);
	
		viewMenu = new JMenu("View");
		viewMenu.setMnemonic(KeyEvent.VK_V);
		menuBar.add(viewMenu);
		
		JCheckBoxMenuItem lineNoItem = new JCheckBoxMenuItem("Line Nos.");
		//lineNoItem.setActionCommand(ProseVis.LINENO_VIEW_COMMAND);
		lineNoItem.addItemListener(panel);
		viewMenu.add(lineNoItem);
		
		return menuBar;
	}
	
	public static void addFileItem(String fileName, ProseVis panel){
		if(fileNames.size() == 0){
			closeMenu.removeAll();
		}
		
		fileNames.add(fileName);
		
		JMenuItem fileItem = new JMenuItem(fileName);
		fileItem.setActionCommand(fileName);
		fileItem.addActionListener(panel);
		closeMenu.add(fileItem);
	}
	
	public static void removeFileItem(String fileName){
		int index = fileName.indexOf(fileName);
		fileNames.remove(index);
		closeMenu.remove(index);
		
		if(fileNames.size() == 0){
			JMenuItem fileItem = new JMenuItem("(None)");
			fileItem.setEnabled(false);
			closeMenu.add(fileItem);
		}
	}
}
