package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.*;

@SuppressWarnings("serial")
public class DataPanel extends JPanel {
	JComboBox selFileBox;
	private VizController vizCtr;
	private ProseVis proViz;
	public Navigator nav;
	
	JComboBox colorLevels;
	JComboBox phonemeLevels;
	
	public DataPanel(VizController sourceviz, ProseVis sourceproViz) {
		vizCtr = sourceviz;
		proViz = sourceproViz;
		nav = new Navigator(vizCtr);
		nav.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		vizCtr.setNav(nav);
		nav.setPreferredSize(new Dimension(200,100));
		
		JLabel tileLabel = new JLabel("Tile Files:");
		JComboBox tileLevels = new JComboBox();
		tileLevels.addItem("Horizontal");
		tileLevels.addItem("Vertical");
		tileLevels.setSelectedItem("Horizontal");
		
		tileLevels.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                int tile = ((JComboBox)evt.getSource()).getSelectedIndex();
                
                if(tile == 0){
                	proViz.updateTiling(true);
                }else{
                	proViz.updateTiling(false);
                }
                proViz.updateViz(false);
            }            	
        });
		
		JLabel zoomLabel = new JLabel("Zoom:");
		JComboBox zoomLevels = new JComboBox();
		zoomLevels.addItem("25");
		zoomLevels.addItem("50");
		zoomLevels.addItem("75");
		zoomLevels.addItem("100");
		zoomLevels.addItem("200");
		zoomLevels.addItem("400");
		zoomLevels.addItem("800");
		zoomLevels.setSelectedItem("25");
		
		zoomLevels.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                double scale = ((double)Integer.parseInt(((JComboBox)evt.getSource()).getSelectedItem().toString()))/25;
                vizCtr.setZoomFactor(scale);
            }            	
        }); 
		
		JLabel splitLabel = new JLabel("Line By:");
		JComboBox splitLevels = new JComboBox();
		splitLevels.addItem("Chapter");
		splitLevels.addItem("Section");
		splitLevels.addItem("Paragraph");
		splitLevels.addItem("Sentence");
		splitLevels.addItem("Phrase");
		splitLevels.setSelectedItem("Phrase");
		
		splitLevels.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	int lineLevel = ((JComboBox)evt.getSource()).getSelectedIndex();
            	vizCtr.setLineLevel(lineLevel);
            	proViz.updateViz(false);
            }            	
        });
		
		JLabel colorLabel = new JLabel("Color By:");
		colorLevels = new JComboBox();
		colorLevels.addItem("None");
		colorLevels.addItem("Sound");
		colorLevels.addItem("Stress");
		colorLevels.addItem("POS");
		colorLevels.addItem("Tone");
		colorLevels.addItem("Accent");
		colorLevels.addItem("Soundex");
		colorLevels.addItem("Word");
		colorLevels.setSelectedItem("None");
		
		colorLevels.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	String colorChoice = ((JComboBox)evt.getSource()).getSelectedItem().toString();
            	vizCtr.setColorRenderPolicy(colorChoice);
            	
            	if(colorChoice.equals("Sound")){
            		phonemeLevels.setEnabled(true);
            	}else{
            		phonemeLevels.setEnabled(false);
            	}
            	proViz.updateViz(false);
            }            	
        });
		
		
		phonemeLevels = new JComboBox();
		phonemeLevels.addItem("Full");
		phonemeLevels.addItem("Beginning");
		phonemeLevels.addItem("Vowel");
		phonemeLevels.addItem("End");
		phonemeLevels.setSelectedItem("Full");
		phonemeLevels.setEnabled(false);
		
		phonemeLevels.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	String phonemeChoice = ((JComboBox)evt.getSource()).getSelectedItem().toString();
            	vizCtr.setPhonemeRenderPolicy(phonemeChoice);
            	proViz.updateViz(false);
            }            	
        });
		
		//Rendering Options
        JLabel renderOptLabel = new JLabel("Render:");
		JComboBox renderLevels = new JComboBox();
		renderLevels.addItem("Text");
		renderLevels.addItem("POS");
		renderLevels.addItem("Sound");
		renderLevels.addItem("None");
		renderLevels.setSelectedItem("Text");
		
		renderLevels.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	String renderChoice = ((JComboBox)evt.getSource()).getSelectedItem().toString();
            	
            	if(renderChoice.equals("Text")){
            		vizCtr.setTextRenderPolicy(true);
            	}else if(renderChoice.equals("Sound")){
            		vizCtr.setTextRenderPolicy(true);
            		vizCtr.setTextRenderVar(1);
            	}else if(renderChoice.equals("POS")){
            		vizCtr.setTextRenderPolicy(true);
            		vizCtr.setTextRenderVar(2);
            	}else{
            		vizCtr.setTextRenderPolicy(false);
            	}
            	proViz.updateViz(false);
            }            	
        });
        
      
		//Tooltip Options
        JLabel tooltipLabel = new JLabel("Tooltip:");
		JComboBox tooltipLevels = new JComboBox();
		tooltipLevels.addItem("None");
		tooltipLevels.addItem("POS");
		tooltipLevels.addItem("Sound");
		tooltipLevels.addItem("Word");
		tooltipLevels.setSelectedItem("None");
		
		tooltipLevels.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	int tooltipChoice = ((JComboBox)evt.getSource()).getSelectedIndex();
            	
            	vizCtr.setTooltipPolicy(tooltipChoice);
            	proViz.updateViz(false);
            }            	
        });        
        
		
		JCheckBox renderTone = new JCheckBox("Render Accent");
		renderTone.setSelected(false);
		renderTone.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					vizCtr.setToneRenderPolicy(true);					
				} else {
					vizCtr.setToneRenderPolicy(false);
				}
				proViz.updateViz(false);
			}
		});
		
		JButton updateButton = new JButton("Reset");
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				vizCtr.renderDdata = false;
				vizCtr.renderText = true;
				vizCtr.renderColor = false;
				vizCtr.textRenderVar = 0;
				vizCtr.renderTone = false;
								
				proViz.updateViz(false);
			}
		});
		
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tileLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tileLevels, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nav)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))                
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(zoomLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(zoomLevels, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(splitLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitLevels, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(colorLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorLevels, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(colorLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(phonemeLevels, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(renderOptLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(renderLevels, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tooltipLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tooltipLevels, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(renderTone)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
            	.addContainerGap()
            	.addComponent(updateButton)
            	.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                	.addComponent(tileLabel)
                	.addComponent(tileLevels, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(nav)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                	.addComponent(zoomLabel)
                	.addComponent(zoomLevels, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                	.addComponent(splitLabel)
                	.addComponent(splitLevels, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                	.addComponent(colorLabel)
                	.addComponent(colorLevels, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                	.addComponent(phonemeLevels, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                	.addComponent(renderOptLabel)
                	.addComponent(renderLevels, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                	.addComponent(tooltipLabel)
                	.addComponent(tooltipLevels, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(renderTone)
                .addContainerGap()
                .addComponent(updateButton))
        );
	}
	
	
	
}
