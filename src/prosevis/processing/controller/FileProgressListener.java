package prosevis.processing.controller;

import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import prosevis.processing.model.ProseModelIF;


public class FileProgressListener implements WindowStateListener {
  private static final long serialVersionUID = -4051127437837695428L;
  private ProseModelIF model;
  private FileListModel fileList;
  private JLabel progressLabel;
  private JButton addBtn;

  public FileProgressListener(ProseModelIF model, FileListModel fileListModel, JLabel progressLabel, JButton btnAddFile) {
    this.model = model;
    this.fileList = fileListModel;
    this.progressLabel = progressLabel;
    this.addBtn = btnAddFile;
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
        this.model.addData(fpe.getResult());
        fileList.refresh(model.getFileList());
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
