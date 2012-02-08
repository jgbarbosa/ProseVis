package GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;


@SuppressWarnings("serial")
public class SearchPanel extends JPanel {
	private VizController vizCtr;
	private ProseVis proViz;
	private JTextField searchBox;
	private JComboBox phonemeLevels;
	private ButtonGroup searchGroup;
	private int searchSel = 0;

	public SearchPanel(VizController sourceViz, ProseVis sourcepViz) {
		vizCtr = sourceViz;
		proViz = sourcepViz;

		JLabel searchLabel = new JLabel("Search:");
			
		searchBox = new JTextField(10);

		JRadioButton wordButton = new JRadioButton("Word");
		wordButton.setSelected(true);
		wordButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	searchSel = 0;
            	phonemeLevels.setEnabled(false);
            }            	
        });

		JRadioButton phoButton = new JRadioButton("Sound");
		phoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	searchSel = 1;
            	phonemeLevels.setEnabled(true);
            }            	
        });

		JRadioButton posButton = new JRadioButton("POS");
		posButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	searchSel = 2;
            	phonemeLevels.setEnabled(false);
            }            	
        });

		JRadioButton sdxButton = new JRadioButton("Soundex");
		sdxButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	searchSel = 3;
            	phonemeLevels.setEnabled(false);
            }            	
        });
		
		//Group the radio buttons.
		searchGroup = new ButtonGroup();
		searchGroup.add(wordButton);
		searchGroup.add(phoButton);
		searchGroup.add(posButton);
		searchGroup.add(sdxButton);
		
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new java.awt.GridLayout(4, 1));
		searchPanel.add(wordButton);
		searchPanel.add(phoButton);
		searchPanel.add(posButton);
		searchPanel.add(sdxButton);
		
		phonemeLevels = new JComboBox();
		phonemeLevels.addItem("Full");
		phonemeLevels.addItem("Beginning");
		phonemeLevels.addItem("Vowel");
		phonemeLevels.addItem("End");
		phonemeLevels.setSelectedItem("Full");
		phonemeLevels.setEnabled(false);
		
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String searchString = searchBox.getText().trim().toLowerCase();
				
				if(searchString.length() > 0){
					vizCtr.setSearch(searchString, searchSel, phonemeLevels.getSelectedIndex());
					proViz.updateViz(true);
				}else{
					vizCtr.killSearch();
				}
			}
		});
		
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(searchLabel)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(searchBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(searchPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(phonemeLevels, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(searchButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);

		
		layout.setVerticalGroup(
	            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                	.addComponent(searchLabel)
	                	.addComponent(searchBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
	                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(searchPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(phonemeLevels, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(searchButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
		);
	}
}
