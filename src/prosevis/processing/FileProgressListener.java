package prosevis.processing;

import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class FileProgressListener implements WindowStateListener {
  private static final long serialVersionUID = -4051127437837695428L;
  private ApplicationModel model;
  private FileListModel fileList;
  private JLabel progressLabel;
  private JFrame frame;
  private JButton addBtn;

  public FileProgressListener(ApplicationModel model, FileListModel fileListModel, JLabel progressLabel, JFrame f, JButton btnAddFile) {
    this.model = model;
    this.fileList = fileListModel;
    this.progressLabel = progressLabel;
    this.frame = f;
    this.addBtn = btnAddFile;
  }

  @Override
  public void windowStateChanged(WindowEvent e) {
    if (e instanceof FileProgressEvent) {
      FileProgressEvent fpe = (FileProgressEvent)e;
      
      switch (fpe.getStatus()) {
      case FINISHED_SUCC:
        this.model.addData(fpe.getResult());
        fileList.refresh(model.getFileList());
        System.err.println("Finished parsing " + fpe.getResult().getName());
        progressLabel.setText("");
        break;

      case PROGRESS:
        progressLabel.setText(String.format("Progress: (%2.2f%%)", fpe.getProgress() * 100.0));
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
