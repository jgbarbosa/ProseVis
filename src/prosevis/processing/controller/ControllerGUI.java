package prosevis.processing.controller;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
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
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import prosevis.data.TypeMap;
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
    StringListModel colorByModel = new StringListModel();
    colorByModel.addItem(TypeMap.kNoLabelLabel);
    StringListModel textByModel = new StringListModel();
    textByModel.addItem(TypeMap.kNoLabelLabel);


    JList list = new JList();
    final FileListModel listModel = new FileListModel();
    list.setModel(listModel);
    JLabel lblProgress = new JLabel("");
    final JButton btnAddFile = new JButton("Add File");

    frame = new JFrame();
    FileProgressListener fplistener =
        new FileProgressListener(theModel, listModel, lblProgress, btnAddFile, colorByModel, textByModel);
    frame.addWindowStateListener(fplistener);
    frame.setBounds(100, 100, 693, 558);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JTabbedPane controllerTabGroup = new JTabbedPane(JTabbedPane.TOP);
    frame.getContentPane().add(controllerTabGroup, BorderLayout.CENTER);

    JPanel dataPane = new JPanel();
    controllerTabGroup.addTab("Data", null, dataPane, null);
    dataPane.setLayout(new FormLayout(new ColumnSpec[] {
        FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("left:min"),
        FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC,},
      new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC,}));

    JPanel dataPaneButtonPanel = new JPanel();
    dataPane.add(dataPaneButtonPanel, "2, 2, left, fill");

    JLabel lblActions = new JLabel("Actions");

    btnAddFile.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        TypeMap typeMap = theModel.getTypeMapCopy();
        btnAddFile.setEnabled(false);
        Thread workerThread = new Thread(new FileLoader(frame, typeMap));
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

    GroupLayout gl_dataPaneButtonPanel = new GroupLayout(dataPaneButtonPanel);
    gl_dataPaneButtonPanel.setHorizontalGroup(
      gl_dataPaneButtonPanel.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_dataPaneButtonPanel.createSequentialGroup()
          .addContainerGap()
          .addGroup(gl_dataPaneButtonPanel.createParallelGroup(Alignment.LEADING)
            .addComponent(lblActions)
            .addComponent(btnAddFile)
            .addComponent(btnRemoveFiles)
            .addComponent(btnClearFiles)
            .addComponent(btnMoveToTop)
            .addComponent(lblProgress))
          .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    gl_dataPaneButtonPanel.setVerticalGroup(
      gl_dataPaneButtonPanel.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_dataPaneButtonPanel.createSequentialGroup()
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
    dataPaneButtonPanel.setLayout(gl_dataPaneButtonPanel);

    JScrollPane dataPaneFilePanel = new JScrollPane();
    dataPane.add(dataPaneFilePanel, "4, 2, fill, fill");

    dataPaneFilePanel.setViewportView(list);

    JLabel lblFiles = new JLabel("Files");
    dataPaneFilePanel.setColumnHeaderView(lblFiles);

    JPanel navigationPane = new JPanel();
    controllerTabGroup.addTab("Navigation", null, navigationPane, null);

    JPanel renderPane = new JPanel();
    controllerTabGroup.addTab("Render", null, renderPane, null);
    GridBagLayout gbl_renderPane = new GridBagLayout();
    gbl_renderPane.columnWidths = new int[]{0, 0, 0, 0};
    gbl_renderPane.rowHeights = new int[]{0, 0, 0, 0, 0};
    gbl_renderPane.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
    gbl_renderPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
    renderPane.setLayout(gbl_renderPane);

    JLabel lblLineBreaksBy = new JLabel("  Line breaks by:  ");
    GridBagConstraints gbc_lblLineBreaksBy = new GridBagConstraints();
    gbc_lblLineBreaksBy.anchor = GridBagConstraints.EAST;
    gbc_lblLineBreaksBy.fill = GridBagConstraints.VERTICAL;
    gbc_lblLineBreaksBy.insets = new Insets(0, 0, 5, 5);
    gbc_lblLineBreaksBy.gridx = 1;
    gbc_lblLineBreaksBy.gridy = 1;
    renderPane.add(lblLineBreaksBy, gbc_lblLineBreaksBy);

    JComboBox breakLineByDropdown = new JComboBox();
    for (RenderBy breakType : RenderBy.values()){
      breakLineByDropdown.addItem(breakType.toString().toLowerCase());
    }
    breakLineByDropdown.setSelectedItem(theModel.getBreakLevel().toString().toLowerCase());
    breakLineByDropdown.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();
        String typeStr = (String)cb.getSelectedItem();
        theModel.setBreakLevel(RenderBy.valueOf(typeStr.toUpperCase()));
      }
    });

    GridBagConstraints breakLineConstraints = new GridBagConstraints();
    breakLineConstraints.anchor = GridBagConstraints.WEST;
    breakLineConstraints.insets = new Insets(0, 0, 5, 0);
    breakLineConstraints.gridx = 2;
    breakLineConstraints.gridy = 1;
    renderPane.add(breakLineByDropdown, breakLineConstraints);

    JLabel lblColorBy = new JLabel("  Color by:  ");
    GridBagConstraints gbc_lblColorBy = new GridBagConstraints();
    gbc_lblColorBy.anchor = GridBagConstraints.EAST;
    gbc_lblColorBy.insets = new Insets(0, 0, 5, 5);
    gbc_lblColorBy.gridx = 1;
    gbc_lblColorBy.gridy = 2;
    renderPane.add(lblColorBy, gbc_lblColorBy);

    JComboBox colorByDropdown = new JComboBox(colorByModel);
    colorByDropdown.setSelectedItem(TypeMap.kNoLabelLabel);
    colorByDropdown.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();
        String labelStr = (String)cb.getSelectedItem();
        theModel.setColorBy(labelStr);
      }
    });

    GridBagConstraints colorByConstraints = new GridBagConstraints();
    colorByConstraints.insets = new Insets(0, 0, 5, 0);
    colorByConstraints.anchor = GridBagConstraints.WEST;
    colorByConstraints.gridx = 2;
    colorByConstraints.gridy = 2;
    renderPane.add(colorByDropdown, colorByConstraints);

    JLabel lblText = new JLabel("  Text:  ");
    GridBagConstraints gbc_lblText = new GridBagConstraints();
    gbc_lblText.anchor = GridBagConstraints.EAST;
    gbc_lblText.insets = new Insets(0, 0, 0, 5);
    gbc_lblText.gridx = 1;
    gbc_lblText.gridy = 3;
    renderPane.add(lblText, gbc_lblText);

    JComboBox textByDropdown = new JComboBox();
    GridBagConstraints textByConstraints = new GridBagConstraints();
    textByConstraints.anchor = GridBagConstraints.WEST;
    textByConstraints.gridx = 2;
    textByConstraints.gridy = 3;
    renderPane.add(textByDropdown, textByConstraints);

    JPanel colorPane = new JPanel();
    controllerTabGroup.addTab("Color", null, colorPane, null);
    colorPane.setLayout(new GridLayout(2, 1, 0, 0));

    JPanel colorTop = new JPanel();
    GridBagConstraints gbc_colorTop = new GridBagConstraints();
    gbc_colorTop.anchor = GridBagConstraints.EAST;
    gbc_colorTop.insets = new Insets(0, 0, 0, 5);
    gbc_colorTop.gridx = 1;
    gbc_colorTop.gridy = 1;
    colorPane.add(colorTop, gbc_colorTop);
    colorTop.setLayout(new GridLayout(1, 0, 0, 0));

    JPanel colorTopLeft = new JPanel();
    colorTop.add(colorTopLeft);

    JList colorSchemes = new JList();
    colorTopLeft.add(list_1);

    JPanel colorTopRight = new JPanel();
    colorTop.add(colorTopRight);

    JPanel colorBottom = new JPanel();
    GridBagConstraints colorBottomConstraints = new GridBagConstraints();
    colorBottomConstraints.anchor = GridBagConstraints.EAST;
    colorBottomConstraints.insets = new Insets(0, 0, 0, 5);
    colorBottomConstraints.gridx = 1;
    colorBottomConstraints.gridy = 2;
    colorPane.add(colorBottom, colorBottomConstraints);
  }

  public void go() {
    this.frame.setVisible(true);
  }
}

class StringListModel implements ComboBoxModel<String> {
  private final ArrayList<String> labels = new ArrayList<String>();
  private final ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();
  private int selectedIdx = -1;
  @Override
  public void addListDataListener(ListDataListener l) {
    listeners.add(l);
  }

  @Override
  public String getElementAt(int index) {
    return labels.get(index);
  }

  @Override
  public int getSize() {
    return labels.size();
  }

  @Override
  public void removeListDataListener(ListDataListener l) {
    listeners.remove(l);
  }

  @Override
  public Object getSelectedItem() {
    return (selectedIdx == -1)?null:labels.get(selectedIdx);
  }

  @Override
  public void setSelectedItem(Object anItem) {
    if (!(anItem instanceof String)) {
      return;
    }
    String str = (String)anItem;
    for (int i = 0; i < labels.size(); i++) {
      if (str.equals(labels.get(i))) {
        selectedIdx = i;
        return;
      }
    }
  }

  public void addItem(String item) {
    if (!labels.contains(item)) {
      labels.add(item);
      for (ListDataListener l: this.listeners) {
        l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, labels.size() - 1, labels.size()));
      }
    }
  }
}

class FileListModel extends AbstractListModel {
  private static final long serialVersionUID = 8940049949482647158L;
  private final ArrayList<String> files = new ArrayList<String>();

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
