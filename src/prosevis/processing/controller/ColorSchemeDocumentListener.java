package prosevis.processing.controller;

import java.awt.Color;
import java.util.HashMap;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.ColorUIResource;

import prosevis.processing.model.ApplicationModel;
import prosevis.processing.model.color.ColorSchemeUtil;
import prosevis.processing.model.color.WorkingColorScheme;

public class ColorSchemeDocumentListener implements DocumentListener {
  private WorkingColorScheme colorScheme = new WorkingColorScheme();
  private JTextField[] watchedFields;
  private ApplicationModel model;
  private ColorRefreshable refresher;
  
  public ColorSchemeDocumentListener(JTextField [] inputs, ApplicationModel model, ColorRefreshable refresher) {
    this.model = model;
    model.registerWorkingColorScheme(colorScheme);
    this.watchedFields = inputs;
    this.refresher = refresher;
  }
  @Override
  public void removeUpdate(DocumentEvent e) {
    //JOptionPane.showMessageDialog(null, "remove");
    updateWorkingColorScheme();
  }
  
  @Override
  public void insertUpdate(DocumentEvent e) {
    //JOptionPane.showMessageDialog(null, "insert");
    updateWorkingColorScheme();
  }
  
  @Override
  public void changedUpdate(DocumentEvent e) {
    //JOptionPane.showMessageDialog(null, "change");
    // unsure how to trigger this on textfields
    updateWorkingColorScheme();
  }
 
  public void setLabel(String label) {
    colorScheme.setLabel(label);
    model.setColorBy(colorScheme.getName());
    refresher.refreshColorSchemeElements();
  }
  
  private void updateWorkingColorScheme() {
    HashMap<String, Color> newMapping = new HashMap<String, Color>();
    for (int i = 0;i < watchedFields.length; i++) {
      JTextField t = watchedFields[i];
      if (t.getText() == null || t.getText().isEmpty()) {
        continue;
      }
      newMapping.put(t.getText(), t.getBackground());
    }
    
    colorScheme.setMapping(newMapping);
    model.setColorBy(colorScheme.getName());
    refresher.refreshColorSchemeElements();
  }
}
