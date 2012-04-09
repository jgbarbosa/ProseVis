package prosevis.processing.controller;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import prosevis.data.BreakLinesBy;
import prosevis.data.TypeMap;
import prosevis.processing.model.ApplicationModel;
import prosevis.processing.model.ColorScheme;
import prosevis.processing.model.DataTreeView;
import prosevis.processing.model.ProseModelIF;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;


public class ControllerGUI implements WindowStateListener {
  static final String kXmlTag = " (with XML)";
  private final ProseModelIF theModel;
  private JFrame frame;
  private JLabel lblProgress;
  private JButton btnAddFile;
  private DefaultComboBoxModel<String> colorByModel;
  private DefaultComboBoxModel<String> textByModel;
  private DefaultComboBoxModel<String> fileListModel;
  private DefaultComboBoxModel<String> searchListModel;

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
    colorByModel = new DefaultComboBoxModel<String>();
    colorByModel.addElement(TypeMap.kNoLabelLabel);
    textByModel = new DefaultComboBoxModel<String>();
    textByModel.addElement(TypeMap.kNoLabelLabel);
    textByModel.setSelectedItem(TypeMap.kNoLabelLabel);

    fileListModel = new DefaultComboBoxModel<String>();
    searchListModel = new DefaultComboBoxModel<String>();
    final JList<String> dataFilesList = new JList<String>();
    dataFilesList.setModel(fileListModel);
    lblProgress = new JLabel("");
    btnAddFile = new JButton("Add File");

    frame = new JFrame();
    frame.addWindowStateListener(this);
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

    btnAddFile.addActionListener(new FileListActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        TypeMap typeMap = theModel.getTypeMapCopy();
        btnAddFile.setEnabled(false);
        Thread workerThread = new Thread(new FileLoader(frame, typeMap));
        workerThread.start();
      }
    });

    JButton btnRemoveFiles = new JButton("Remove Files");
    btnRemoveFiles.addActionListener(new FileListActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        List<String> selectedFiles =
            stripXMLMetaData(dataFilesList.getSelectedValuesList());
        theModel.removeData(selectedFiles);
        updateFileLists();
      }
    });

    JButton btnClearFiles = new JButton("Clear Files");
    btnClearFiles.addActionListener(new FileListActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        theModel.removeAllData();
        updateFileLists();
      }
    });

    JButton btnMoveToTop = new JButton("Move To Top");
    btnMoveToTop.addActionListener(new FileListActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        List<String> selectedFiles =
            stripXMLMetaData(dataFilesList.getSelectedValuesList());
        theModel.moveFilesToTop(selectedFiles);
        updateFileLists();
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

    dataPaneFilePanel.setViewportView(dataFilesList);

    JLabel lblFiles = new JLabel("Files");
    dataPaneFilePanel.setColumnHeaderView(lblFiles);

    final ButtonGroup searchButtons = new ButtonGroup();

    JPanel searchButtonsPanel = new JPanel();
    JRadioButton rdbtnSound = new JRadioButton("Sound");
    rdbtnSound.setActionCommand("sound");
    searchButtons.add(rdbtnSound);

    final JComboBox<String> searchSoundOptions = new JComboBox<String>();
    JRadioButton rdbtnSoundex = new JRadioButton("Soundex");
    rdbtnSoundex.setActionCommand("soundex");
    searchButtons.add(rdbtnSoundex);
    JRadioButton rdbtnPos = new JRadioButton("POS");
    rdbtnPos.setActionCommand("pos");
    searchButtons.add(rdbtnPos);

    JRadioButton rdbtnWord = new JRadioButton("Word", true);
    rdbtnWord.setActionCommand("word");
    searchButtons.add(rdbtnWord);

    JLabel lblSearch = new JLabel("Search:");

    final JTextField searchTermBox = new JTextField();
    searchTermBox.setColumns(10);

    JButton btnNext = new JButton("Find Next");
    GroupLayout gl_searchButtonsPanel = new GroupLayout(searchButtonsPanel);
    gl_searchButtonsPanel.setHorizontalGroup(
      gl_searchButtonsPanel.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_searchButtonsPanel.createSequentialGroup()
          .addContainerGap()
          .addGroup(gl_searchButtonsPanel.createParallelGroup(Alignment.LEADING)
            .addGroup(gl_searchButtonsPanel.createSequentialGroup()
              .addComponent(rdbtnSound)
              .addPreferredGap(ComponentPlacement.UNRELATED)
              .addComponent(searchSoundOptions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addComponent(rdbtnSoundex)
            .addComponent(rdbtnPos)
            .addGroup(gl_searchButtonsPanel.createSequentialGroup()
              .addComponent(lblSearch)
              .addPreferredGap(ComponentPlacement.RELATED)
              .addComponent(searchTermBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addComponent(rdbtnWord)
            .addComponent(btnNext))
          .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    gl_searchButtonsPanel.setVerticalGroup(
      gl_searchButtonsPanel.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_searchButtonsPanel.createSequentialGroup()
          .addGap(5)
          .addGroup(gl_searchButtonsPanel.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblSearch)
            .addComponent(searchTermBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(rdbtnWord)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(rdbtnPos)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(rdbtnSoundex)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addGroup(gl_searchButtonsPanel.createParallelGroup(Alignment.BASELINE)
            .addComponent(rdbtnSound)
            .addComponent(searchSoundOptions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(btnNext)
          .addContainerGap(310, Short.MAX_VALUE))
    );
    searchButtonsPanel.setLayout(gl_searchButtonsPanel);

    JScrollPane searchScrollPanel = new JScrollPane();
    final JList<String> searchFilesList = new JList<String>();
    searchFilesList.setModel(searchListModel);
    searchScrollPanel.setViewportView(searchFilesList);
    JLabel whichFilesLabel = new JLabel("Files");
    searchScrollPanel.setColumnHeaderView(whichFilesLabel);



    btnNext.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String label = searchButtons.getSelection().getActionCommand();
        if ("sound".equals(label)) {
          label += '-' + (String)searchSoundOptions.getSelectedItem();
        }
        String searchTerm = searchTermBox.getText();
        List<String> selectedFiles = searchFilesList.getSelectedValuesList();
        theModel.searchForTerm(searchTerm, label, selectedFiles);
      }
    });
    searchSoundOptions.addItem("Full");
    searchSoundOptions.addItem("Initial");
    searchSoundOptions.addItem("Vowel");
    searchSoundOptions.addItem("Final");

    JPanel navigationPane = new JPanel();
    navigationPane.setLayout(new FormLayout(new ColumnSpec[] {
        FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("left:min"),
        FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC,},
      new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC,}));

    controllerTabGroup.addTab("Navigation", null, navigationPane, null);
    navigationPane.add(searchButtonsPanel, "2, 2, left, top");
    navigationPane.add(searchScrollPanel, "4, 2, fill, fill");


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

    JComboBox<String> breakLineByDropdown = new JComboBox<String>();
    for (BreakLinesBy breakType : BreakLinesBy.values()){
      breakLineByDropdown.addItem(breakType.toString());
    }
    breakLineByDropdown.setSelectedItem(theModel.getBreakLevel().toString());
    breakLineByDropdown.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        @SuppressWarnings("unchecked")
        JComboBox<String> cb = (JComboBox<String>)e.getSource();
        String typeStr = (String)cb.getSelectedItem();
        theModel.setBreakLevel(BreakLinesBy.valueOf(typeStr));
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

    JComboBox<String> colorByDropdown = new JComboBox<String>(colorByModel);
    colorByDropdown.setSelectedItem(TypeMap.kNoLabelLabel);
    colorByDropdown.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        @SuppressWarnings("unchecked")
        JComboBox<String> cb = (JComboBox<String>)e.getSource();
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

    JComboBox<String> textByDropdown = new JComboBox<String>(textByModel);
    textByDropdown.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        @SuppressWarnings("unchecked")
        JComboBox<String> cb = (JComboBox<String>)e.getSource();
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

    JLabel lblNewLabel = new JLabel("  Actions:  ");

    JScrollPane colorTopSchemasPanel = new JScrollPane();
    JLabel lblSchemas = new JLabel("Color Schemes:");
    colorTopSchemasPanel.setColumnHeaderView(lblSchemas);

    final JList<String> colorTopSchemasList = new JList<String>();
    final DefaultComboBoxModel<String> colorTopSchemasListModel = new DefaultComboBoxModel<String>();
    colorTopSchemasList.setModel(colorTopSchemasListModel);
    colorTopSchemasPanel.setViewportView(colorTopSchemasList);
    colorTop.add(colorTopSchemasPanel, "4, 2, fill, fill");
    JButton btnLoadScheme = new JButton("Load scheme");
    btnLoadScheme.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        File file = FileLoader.loadColorSchemeFile();
        if (file == null) {
          return;
        }
        try {
          ColorScheme colorScheme = ColorScheme.loadFromFile(file);
          theModel.addColorScheme(colorScheme);
          colorTopSchemasListModel.removeAllElements();
          for (String s: theModel.getColorSchemeList()) {
            colorTopSchemasListModel.addElement(s);
          }
        } catch (InstantiationException e1) {
          JOptionPane.showMessageDialog(frame, e1.getMessage());
        }
      }
    });

    JButton btnRemoveScheme = new JButton("Remove scheme");
    btnRemoveScheme.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String selectedItem = (String)colorTopSchemasListModel.getSelectedItem();
        if (selectedItem == null) {
          JOptionPane.showMessageDialog(frame, "Please select a color scheme to remove");
          return;
        }
        theModel.removeColorScheme(selectedItem);
        colorTopSchemasListModel.removeAllElements();
        for (String s: theModel.getColorSchemeList()) {
          colorTopSchemasListModel.addElement(s);
        }
      }
    });

    JButton btnSaveScheme = new JButton("Save scheme");
    btnSaveScheme.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String selectedItem = (String)colorTopSchemasListModel.getSelectedItem();
        if (selectedItem == null) {
          JOptionPane.showMessageDialog(frame, "Please select a color scheme to save");
          return;
        }
        ColorScheme colorScheme = theModel.getColorScheme(selectedItem);
        colorScheme.saveToFile();
      }
    });

    GroupLayout gl_colorTopButtons = new GroupLayout(colorTopButtons);
    gl_colorTopButtons.setHorizontalGroup(
      gl_colorTopButtons.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_colorTopButtons.createSequentialGroup()
          .addContainerGap()
          .addGroup(gl_colorTopButtons.createParallelGroup(Alignment.LEADING)
            .addComponent(lblNewLabel)
            .addComponent(btnLoadScheme)
            .addComponent(btnSaveScheme)
            .addComponent(btnRemoveScheme))
          .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    gl_colorTopButtons.setVerticalGroup(
      gl_colorTopButtons.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_colorTopButtons.createSequentialGroup()
          .addContainerGap()
          .addComponent(lblNewLabel)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(btnLoadScheme)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(btnSaveScheme)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(btnRemoveScheme)
          .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    colorTopButtons.setLayout(gl_colorTopButtons);


  }

   public void go() {
    this.frame.setVisible(true);
  }

  @Override
  public void windowStateChanged(WindowEvent e) {
    if (e instanceof FileProgressEvent) {
      FileProgressEvent fpe = (FileProgressEvent)e;

      switch (fpe.getStatus()) {
      case PROGRESS:
        lblProgress.setText(String.format("Progress: (%2.2f%%)", fpe.getProgress() * 100.0));
        break;
      case FINISHED_SUCC:
        theModel.addData(fpe.getResult(), fpe.getResultingTypeMap());
        updateFileLists();
        TypeMap typeMap = fpe.getResultingTypeMap();
        for (String l: TypeMap.kPossibleColorByLabels) {
          if (typeMap.hasLabel(l.toLowerCase()) && colorByModel.getIndexOf(l.toLowerCase()) < 0) {
            colorByModel.addElement(l);
          }
        }
        for (String l: TypeMap.kPossibleTextByLabels) {
          if (typeMap.hasLabel(l.toLowerCase()) && textByModel.getIndexOf(l.toLowerCase()) < 0) {
            textByModel.addElement(l);
          }
        }

        System.err.println("Finished parsing " + fpe.getResult().getName());
        lblProgress.setText("");
        btnAddFile.setEnabled(true);
        break;
      default:
        lblProgress.setText("");
        btnAddFile.setEnabled(true);
        break;
      }
    }
  }

  protected void updateFileLists() {
    updateNaviFileList();
    updateDataFileList();
  }


  private void updateNaviFileList() {
    searchListModel.removeAllElements();
    for (DataTreeView s: theModel.getRenderingData()) {
      String line = s.getData().getPath();
      searchListModel.addElement(line);
    }
  }

  private void updateDataFileList() {
    fileListModel.removeAllElements();
    for (DataTreeView s: theModel.getRenderingData()) {
      String line = s.getData().getPath();
      if (s.getData().hasXML()) {
        line += ControllerGUI.kXmlTag;
      }
      fileListModel.addElement(line);
    }
  }
}

abstract class FileListActionListener implements ActionListener {
  protected List<String> stripXMLMetaData(List<String> rawInput) {
    List<String> strippedPaths = new ArrayList<String>(rawInput.size());
    for (String in: rawInput) {
      if (in.endsWith(ControllerGUI.kXmlTag)) {
        strippedPaths.add(in.substring(0, in.lastIndexOf(ControllerGUI.kXmlTag)));
      } else {
        strippedPaths.add(in);
      }
    }
    return strippedPaths;
  }
}
