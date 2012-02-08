package GUI;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


@SuppressWarnings("serial")
public class DavidDataPanel extends JPanel {
	private VizController vizCtr;
	private ProseVis prosViz;
		
	public DavidDataPanel(VizController sourceViz, ProseVis sourcepViz) {
		vizCtr = sourceViz;
		prosViz = sourcepViz;
					
		JCheckBox file1 = new JCheckBox("Three Lives", true);
		file1.setForeground(Color.RED);
		file1.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					vizCtr.actFiles[0] = true; 
				} else {
					vizCtr.actFiles[0] = false;
				}
			}
		});
				
		JCheckBox file2 = new JCheckBox("Picasso Portrait", true);
		file2.setForeground(Color.BLUE);
		file2.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					vizCtr.actFiles[1] = true; 
				} else {
					vizCtr.actFiles[1] = false;
				}
			}
		});
		
		JCheckBox file3 = new JCheckBox("Odyssey", true);
		file3.setForeground(Color.GREEN);
		file3.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					vizCtr.actFiles[2] = true; 
				} else {
					vizCtr.actFiles[2] = false;
				}
			}
		});
		
		JCheckBox file4 = new JCheckBox("Illiad", true);
		file4.setForeground(Color.ORANGE);
		file4.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					vizCtr.actFiles[3] = true; 
				} else {
					vizCtr.actFiles[3] = false;
				}
			}
		});
		
		JCheckBox file5 = new JCheckBox("The Making of Americans", true);
		file5.setForeground(Color.DARK_GRAY);
		file5.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					vizCtr.actFiles[4] = true; 
				} else {
					vizCtr.actFiles[4] = false;
				}
			}
		});
		
		JCheckBox file6 = new JCheckBox("Tender Buttons", true);
		file6.setForeground(Color.YELLOW);
		file6.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					vizCtr.actFiles[5] = true; 
				} else {
					vizCtr.actFiles[5] = false;
				}
			}
		});
		
		JCheckBox file7 = new JCheckBox("Matisse Portrait", true);
		file7.setForeground(Color.CYAN);
		file7.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					vizCtr.actFiles[6] = true; 
				} else {
					vizCtr.actFiles[6] = false;
				}
			}
		});
		
		JCheckBox file8 = new JCheckBox("New England Cook Book", true);
		file8.setForeground(Color.MAGENTA);
		file8.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					vizCtr.actFiles[7] = true; 
				} else {
					vizCtr.actFiles[7] = false;
				}
			}
		});
		
		JButton updateButton = new JButton("Update");
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (vizCtr.getFile().hasComparisonData()) {
					vizCtr.renderDdata = true;
					vizCtr.renderColor = false;
				
					prosViz.updateViz(false);
				} else {
					String message = "There is no Comparison Data present\n"
									+ "in the file " + vizCtr.getFile().getName();
					JOptionPane.showMessageDialog(new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
		hGroup.addGroup(layout.createParallelGroup()
	            .addComponent(file1).addComponent(file2).addComponent(file3).addComponent(file4)
	            .addComponent(file5).addComponent(file6).addComponent(file7).addComponent(file8)
	            .addComponent(updateButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
	    
	    layout.setHorizontalGroup(hGroup);
		
	    GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
	    vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(file1));
	    vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(file2));
	    vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(file3));
	    vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(file4));
	    vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(file5));
	    vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(file6));
	    vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(file7));
	    vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(file8));
	    vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(updateButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
	    
	    layout.setVerticalGroup(vGroup); 	
	}
}