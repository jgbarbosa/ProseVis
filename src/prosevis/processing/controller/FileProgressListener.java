package prosevis.processing.controller;

import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;

import prosevis.data.TypeMap;
import prosevis.processing.model.ProseModelIF;


public class FileProgressListener implements WindowStateListener {
  private final ProseModelIF model;
  private final DefaultComboBoxModel<String> fileList;
  private final JLabel progressLabel;
  private final JButton addBtn;
  private final DefaultComboBoxModel<String> colorByModel;
  private final DefaultComboBoxModel<String> textByModel;

  public FileProgressListener(ProseModelIF model, DefaultComboBoxModel<String> fileListModel, JLabel progressLabel, JButton btnAddFile, DefaultComboBoxModel<String> colorByModel, DefaultComboBoxModel<String> textByModel) {
    this.model = model;
    this.fileList = fileListModel;
    this.progressLabel = progressLabel;
    this.addBtn = btnAddFile;
    this.colorByModel = colorByModel;
    this.textByModel = textByModel;
  }

  @Override
  public void windowStateChanged(WindowEvent e) {
    if (e instanceof FileProgressEvent) {
      FileProgressEvent fpe = (FileProgressEvent)e;

      switch (fpe.getStatus()) {
      case PROGRESS:
        progressLabel.setText(String.format("Progress: (%2.2f%%)", fpe.getProgress() * 100.0));
        break;
      case FINISHED_SUCC:
        this.model.addData(fpe.getResult(), fpe.getResultingTypeMap());
        fileList.removeAllElements();
        for (String s: model.getFileList()) {
          fileList.addElement(s);
        }
        TypeMap typeMap = fpe.getResultingTypeMap();
        for (String l: TypeMap.kPossibleColorByLabels) {
          if (typeMap.hasLabel(l.toLowerCase())) {
            colorByModel.addElement(l);
          }
        }
        for (String l: TypeMap.kPossibleTextByLabels) {
          if (typeMap.hasLabel(l.toLowerCase())) {
            textByModel.addElement(l);
          }
        }

        System.err.println("Finished parsing " + fpe.getResult().getName());
        progressLabel.setText("");
        addBtn.setEnabled(true);
        break;
      default:
        progressLabel.setText("");
        addBtn.setEnabled(true);
        break;
      }
    }
  }

}
