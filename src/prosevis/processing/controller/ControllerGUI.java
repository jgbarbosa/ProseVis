package prosevis.processing.controller;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle.ComponentPlacement;

import prosevis.processing.model.ApplicationModel;
import prosevis.processing.model.DataTreeView.RenderBy;
import prosevis.processing.model.ProseModelIF;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;


public class ControllerGUI {
  private final ProseModelIF theModel;
  private JFrame frame;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          ControllerGUI window = new ControllerGUI(new ApplicationModel());
          window.frame.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the application.
   */
  public ControllerGUI(ProseModelIF model) {
    theModel = model;
    initialize();
  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize() {
    JList<String> list = new JList<String>();
    final FileListModel listModel = new FileListModel();
    list.setModel(listModel);
    JLabel lblProgress = new JLabel("");
    final JButton btnAddFile = new JButton("Add File");

    frame = new JFrame();
    FileProgressListener fplistener =
        new FileProgressListener(theModel, listModel, lblProgress, btnAddFile);
    frame.addWindowStateListener(fplistener);
    frame.setBounds(100, 100, 693, 558);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JTabbedPane dataPane = new JTabbedPane(JTabbedPane.TOP);
    frame.getContentPane().add(dataPane, BorderLayout.CENTER);

    JPanel dataPaneSubPanel1 = new JPanel();
    dataPane.addTab("Data", null, dataPaneSubPanel1, null);
    dataPaneSubPanel1.setLayout(new FormLayout(new ColumnSpec[] {
        FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("left:min"),
        FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC,},
      new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC,}));

    JPanel dataPaneSubPanel2 = new JPanel();
    dataPaneSubPanel1.add(dataPaneSubPanel2, "2, 2, left, fill");

    JLabel lblActions = new JLabel("Actions");

    btnAddFile.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        btnAddFile.setEnabled(false);
        Thread workerThread = new Thread(new FileLoader(frame));
        workerThread.start();
      }
    });

    JButton btnRemoveFiles = new JButton("Remove Files");
    btnRemoveFiles.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.out.println("Removing files from Jlist");
      }
    });

    JButton btnClearFiles = new JButton("Clear Files");
    btnClearFiles.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        theModel.removeAllData();
        listModel.refresh(theModel.getFileList());
      }
    });

    JButton btnMoveToTop = new JButton("Move To Top");
    btnMoveToTop.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.out.println("Moving selected files to top.");
      }
    });

    GroupLayout gl_dataPaneSubPanel2 = new GroupLayout(dataPaneSubPanel2);
    gl_dataPaneSubPanel2.setHorizontalGroup(
      gl_dataPaneSubPanel2.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_dataPaneSubPanel2.createSequentialGroup()
          .addContainerGap()
          .addGroup(gl_dataPaneSubPanel2.createParallelGroup(Alignment.LEADING)
            .addComponent(lblActions)
            .addComponent(btnAddFile)
            .addComponent(btnRemoveFiles)
            .addComponent(btnClearFiles)
            .addComponent(btnMoveToTop)
            .addComponent(lblProgress))
          .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    gl_dataPaneSubPanel2.setVerticalGroup(
      gl_dataPaneSubPanel2.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_dataPaneSubPanel2.createSequentialGroup()
          .addComponent(lblActions)
          .addGap(18)
          .addComponent(btnAddFile)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(btnRemoveFiles)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(btnClearFiles)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(btnMoveToTop)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(lblProgress)
          .addContainerGap(298, Short.MAX_VALUE))
    );
    dataPaneSubPanel2.setLayout(gl_dataPaneSubPanel2);

    JScrollPane dataPaneFilePanel = new JScrollPane();
    dataPaneSubPanel1.add(dataPaneFilePanel, "4, 2, fill, fill");

    dataPaneFilePanel.setViewportView(list);

    JLabel lblFiles = new JLabel("Files");
    dataPaneFilePanel.setColumnHeaderView(lblFiles);

    JPanel navigationPane = new JPanel();
    dataPane.addTab("Navigation", null, navigationPane, null);

    JPanel renderPane = new JPanel();
    dataPane.addTab("Render", null, renderPane, null);
    GridBagLayout gbl_renderPane = new GridBagLayout();
    gbl_renderPane.columnWidths = new int[]{0, 0, 0, 0};
    gbl_renderPane.rowHeights = new int[]{0, 0, 0, 0};
    gbl_renderPane.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
    gbl_renderPane.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
    renderPane.setLayout(gbl_renderPane);

    JLabel lblLineBreaksBy = new JLabel("  Line breaks by:  ");
    GridBagConstraints gbc_lblLineBreaksBy = new GridBagConstraints();
    gbc_lblLineBreaksBy.anchor = GridBagConstraints.EAST;
    gbc_lblLineBreaksBy.fill = GridBagConstraints.VERTICAL;
    gbc_lblLineBreaksBy.insets = new Insets(0, 0, 5, 5);
    gbc_lblLineBreaksBy.gridx = 1;
    gbc_lblLineBreaksBy.gridy = 1;
    renderPane.add(lblLineBreaksBy, gbc_lblLineBreaksBy);

    JComboBox<String> comboBox = new JComboBox<String>();
    for (RenderBy breakType : RenderBy.values()){
      comboBox.addItem(breakType.toString().toLowerCase());
    }
    comboBox.setSelectedItem(theModel.getBreakLevel().toString().toLowerCase());
    final ProseModelIF model = this.theModel;
    comboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JComboBox<String> cb = (JComboBox)e.getSource();
        String typeStr = (String)cb.getSelectedItem();
        model.setBreakLevel(RenderBy.valueOf(typeStr.toUpperCase()));
      }
    });

    GridBagConstraints gbc_comboBox = new GridBagConstraints();
    gbc_comboBox.anchor = GridBagConstraints.WEST;
    gbc_comboBox.insets = new Insets(0, 0, 5, 0);
    gbc_comboBox.gridx = 2;
    gbc_comboBox.gridy = 1;
    renderPane.add(comboBox, gbc_comboBox);
  }

  public void go() {
    this.frame.setVisible(true);
  }
}

class FileListModel extends AbstractListModel<String> {
  private static final long serialVersionUID = 8940049949482647158L;
  private ArrayList<String> files = new ArrayList<String>();

  @Override
  public String getElementAt(int index) {
    String fullPath = files.get(index);
    return fullPath.substring(fullPath.lastIndexOf(File.pathSeparator) + 1);
  }

  @Override
  public int getSize() {
    return files.size();
  }

  public void addFile(String fullPath) {
    files.add(fullPath);
    this.fireContentsChanged(this, files.size() - 1, files.size() - 1);
  }

  public void refresh(ArrayList<String> fileList) {
    files.clear();
    files.addAll(fileList);
    fireContentsChanged(this, 0, files.size() - 1);
  }
}
