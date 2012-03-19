package prosevis.processing.controller;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    textByModel.setSelectedItem(TypeMap.kNoLabelLabel);


    JList list = new JList();
    final StringListModel fileListModel = new StringListModel();
    list.setModel(fileListModel);
    JLabel lblProgress = new JLabel("");
    final JButton btnAddFile = new JButton("Add File");

    frame = new JFrame();
    FileProgressListener fplistener =
        new FileProgressListener(theModel, fileListModel, lblProgress, btnAddFile, colorByModel, textByModel);
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
    dataPane.add(dataPaneButtonPanel, "2, 2, left, top");

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
        System.out.println("Not implemented: remove file");
      }
    });

    JButton btnClearFiles = new JButton("Clear Files");
    btnClearFiles.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        theModel.removeAllData();
        fileListModel.refresh(theModel.getFileList());
      }
    });

    JButton btnMoveToTop = new JButton("Move To Top");
    btnMoveToTop.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.out.println("Not implemented: move to top");
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

    JComboBox textByDropdown = new JComboBox(textByModel);
    textByDropdown.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();
        String labelStr = (String)cb.getSelectedItem();
        theModel.setTextBy(labelStr);
      }
    });

    colorByDropdown.setSelectedItem(TypeMap.kNoLabelLabel);
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

    JPanel colorBottom = new JPanel();
    GridBagConstraints colorBottomConstraints = new GridBagConstraints();
    colorBottomConstraints.anchor = GridBagConstraints.EAST;
    colorBottomConstraints.insets = new Insets(0, 0, 0, 5);
    colorBottomConstraints.gridx = 1;
    colorBottomConstraints.gridy = 2;
    colorPane.add(colorBottom, colorBottomConstraints);

    colorTop.setLayout(new FormLayout(new ColumnSpec[] {
        FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("left:min"),
        FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC,},
      new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC,}));

    JPanel colorTopButtons = new JPanel();
    colorTop.add(colorTopButtons, "2, 2, left, top");
    GridBagLayout gbl_colorTopButtons = new GridBagLayout();
    gbl_colorTopButtons.columnWidths = new int[]{0, 0};
    gbl_colorTopButtons.rowHeights = new int[]{0, 0, 0};
    gbl_colorTopButtons.columnWeights = new double[]{0.0, Double.MIN_VALUE};
    gbl_colorTopButtons.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
    colorTopButtons.setLayout(gbl_colorTopButtons);

    JLabel lblNewLabel = new JLabel("  Actions:  ");
    GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
    gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
    gbc_lblNewLabel.gridx = 0;
    gbc_lblNewLabel.gridy = 0;
    colorTopButtons.add(lblNewLabel, gbc_lblNewLabel);

    JScrollPane colorTopSchemasPanel = new JScrollPane();
    JLabel lblSchemas = new JLabel("Color Schemes:");
    colorTopSchemasPanel.setColumnHeaderView(lblSchemas);

    final JList colorTopSchemasList = new JList();
    colorTopSchemasList.setModel(new StringListModel());
    colorTopSchemasPanel.add(colorTopSchemasList);
    colorTop.add(colorTopSchemasPanel, "4, 2, fill, fill");

    JButton btnLoadScheme = new JButton("Load scheme");
    btnLoadScheme.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.err.println("not implemented: load scheme");
      }
    });

    JButton btnRemoveScheme = new JButton("Remove scheme");
    btnRemoveScheme.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.err.println("not implemented: remove scheme");
      }
    });

    JButton btnSaveScheme = new JButton("Save scheme");
    btnSaveScheme.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.err.println("not implemented: save scheme");
      }
    });

    GridBagConstraints gbc_btnSaveScheme = new GridBagConstraints();
    GridBagConstraints gbc_btnRemoveScheme = new GridBagConstraints();
    GridBagConstraints gbc_btnLoadScheme = new GridBagConstraints();
    gbc_btnLoadScheme.gridx = 0;
    gbc_btnLoadScheme.gridy = 1;
    gbc_btnSaveScheme.gridx = 0;
    gbc_btnSaveScheme.gridy = 2;
    gbc_btnRemoveScheme.gridx = 0;
    gbc_btnRemoveScheme.gridy = 3;
    colorTopButtons.add(btnLoadScheme, gbc_btnLoadScheme);
    colorTopButtons.add(btnSaveScheme, gbc_btnSaveScheme);
    colorTopButtons.add(btnRemoveScheme, gbc_btnRemoveScheme);


  }

  public void go() {
    this.frame.setVisible(true);
  }
}

