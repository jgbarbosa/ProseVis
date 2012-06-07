package prosevis.processing.controller;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.HashMap;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;
import prosevis.data.BreakLinesBy;
import prosevis.data.TypeMap;
import prosevis.processing.model.ApplicationModel;
import prosevis.processing.model.DataTreeView;
import prosevis.processing.model.color.ColorScheme;
import prosevis.processing.model.color.ColorSchemeDB;
import prosevis.processing.model.color.ColorSchemeUtil;
import prosevis.processing.model.color.CustomColorScheme;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class ControllerGUI implements WindowStateListener, ColorRefreshable {
  static final String kXmlTag = " (with XML)";
  private final ApplicationModel theModel;
  private JFrame frame;
  private JLabel lblProgress;
  private JButton btnAddFile;
  private DefaultComboBoxModel colorByModel;
  private DefaultComboBoxModel textByModel;
  private DefaultComboBoxModel fileListModel;
  private DefaultComboBoxModel searchListModel;
  private final List<JCheckBox> comparisonsEnabled = new ArrayList<JCheckBox>();
  private JLabel lblNoComparisonData;
  private JPanel comparisonContent;
  private final DefaultComboBoxModel colorTopSchemasListModel = new DefaultComboBoxModel();
  private JTextField txtCust_0;
  private JTextField txtCust_1;
  private JTextField txtCust_2;
  private JTextField txtCust_3;
  private JTextField txtCust_4;
  private JTextField txtCust_5;
  private JTextField txtCust_6;
  private JTextField txtCust_7;
  private JTextField txtSchemeName;

  /**
   * Create the application.
   * 
   * @param wC
   */
  public ControllerGUI(ApplicationModel model) {
    theModel = model;
    initialize();
  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize() {
    colorByModel = new DefaultComboBoxModel();
    colorByModel.addElement(TypeMap.kNoLabelLabel);
    textByModel = new DefaultComboBoxModel();
    textByModel.addElement(TypeMap.kNoLabelLabel);
    textByModel.setSelectedItem(TypeMap.kNoLabelLabel);

    fileListModel = new DefaultComboBoxModel();
    searchListModel = new DefaultComboBoxModel();
    final JList dataFilesList = new JList();
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
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:min"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, }));

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
        List<String> selectedFiles = stripXMLMetaData(dataFilesList
            .getSelectedValues());
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
        List<String> selectedFiles = stripXMLMetaData(dataFilesList
            .getSelectedValues());
        theModel.moveFilesToTop(selectedFiles);
        updateFileLists();
      }
    });

    GroupLayout gl_dataPaneButtonPanel = new GroupLayout(dataPaneButtonPanel);
    gl_dataPaneButtonPanel.setHorizontalGroup(gl_dataPaneButtonPanel
        .createParallelGroup(Alignment.LEADING).addGroup(
            gl_dataPaneButtonPanel
                .createSequentialGroup()
                .addContainerGap()
                .addGroup(
                    gl_dataPaneButtonPanel
                        .createParallelGroup(Alignment.LEADING)
                        .addComponent(lblActions).addComponent(btnAddFile)
                        .addComponent(btnRemoveFiles)
                        .addComponent(btnClearFiles).addComponent(btnMoveToTop)
                        .addComponent(lblProgress))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    gl_dataPaneButtonPanel.setVerticalGroup(gl_dataPaneButtonPanel
        .createParallelGroup(Alignment.LEADING).addGroup(
            gl_dataPaneButtonPanel.createSequentialGroup()
                .addComponent(lblActions).addGap(18).addComponent(btnAddFile)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(btnRemoveFiles)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(btnClearFiles)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(btnMoveToTop)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(lblProgress)
                .addContainerGap(298, Short.MAX_VALUE)));
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

    final JComboBox searchSoundOptions = new JComboBox();
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
    gl_searchButtonsPanel.setHorizontalGroup(gl_searchButtonsPanel
        .createParallelGroup(Alignment.LEADING).addGroup(
            gl_searchButtonsPanel
                .createSequentialGroup()
                .addContainerGap()
                .addGroup(
                    gl_searchButtonsPanel
                        .createParallelGroup(Alignment.LEADING)
                        .addGroup(
                            gl_searchButtonsPanel
                                .createSequentialGroup()
                                .addComponent(rdbtnSound)
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addComponent(searchSoundOptions,
                                    GroupLayout.PREFERRED_SIZE,
                                    GroupLayout.DEFAULT_SIZE,
                                    GroupLayout.PREFERRED_SIZE))
                        .addComponent(rdbtnSoundex)
                        .addComponent(rdbtnPos)
                        .addGroup(
                            gl_searchButtonsPanel
                                .createSequentialGroup()
                                .addComponent(lblSearch)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(searchTermBox,
                                    GroupLayout.PREFERRED_SIZE,
                                    GroupLayout.DEFAULT_SIZE,
                                    GroupLayout.PREFERRED_SIZE))
                        .addComponent(rdbtnWord).addComponent(btnNext))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    gl_searchButtonsPanel.setVerticalGroup(gl_searchButtonsPanel
        .createParallelGroup(Alignment.LEADING).addGroup(
            gl_searchButtonsPanel
                .createSequentialGroup()
                .addGap(5)
                .addGroup(
                    gl_searchButtonsPanel
                        .createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblSearch)
                        .addComponent(searchTermBox,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(rdbtnWord)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(rdbtnPos)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(rdbtnSoundex)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(
                    gl_searchButtonsPanel
                        .createParallelGroup(Alignment.BASELINE)
                        .addComponent(rdbtnSound)
                        .addComponent(searchSoundOptions,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(btnNext).addContainerGap(310, Short.MAX_VALUE)));
    searchButtonsPanel.setLayout(gl_searchButtonsPanel);

    JScrollPane searchScrollPanel = new JScrollPane();
    final JList searchFilesList = new JList();
    searchFilesList.setModel(searchListModel);
    searchScrollPanel.setViewportView(searchFilesList);
    JLabel whichFilesLabel = new JLabel("Files");
    searchScrollPanel.setColumnHeaderView(whichFilesLabel);
    JLabel lblColorBy = new JLabel("  Color by:  ");
    final JComboBox colorByDropdown = new JComboBox(colorByModel);

    ActionListener searchActionListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String label = searchButtons.getSelection().getActionCommand();
        if ("sound".equals(label)) {
          label += '-' + (String) searchSoundOptions.getSelectedItem();
        }
        label = label.toLowerCase();
        String searchTerm = searchTermBox.getText();
        List<String> selectedFiles = new ArrayList<String>();
        Object[] selected = searchFilesList.getSelectedValues();
        for (int i = 0; i < selected.length; i++) {
          selectedFiles.add((String) selected[i]);
        }
        theModel.searchForTerm(searchTerm, label, selectedFiles);
        colorByDropdown.setSelectedItem(TypeMap.kNoLabelLabel);
      }
    };
    searchTermBox.addActionListener(searchActionListener);
    btnNext.addActionListener(searchActionListener);
    searchSoundOptions.addItem("Full");
    searchSoundOptions.addItem("Initial");
    searchSoundOptions.addItem("Vowel");
    searchSoundOptions.addItem("Final");

    JPanel navigationPane = new JPanel();
    navigationPane.setLayout(new FormLayout(new ColumnSpec[] {
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:min"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, }));

    controllerTabGroup.addTab("Search", null, navigationPane, null);
    navigationPane.add(searchButtonsPanel, "2, 2, left, top");
    navigationPane.add(searchScrollPanel, "4, 2, fill, fill");

    JPanel renderPane = new JPanel();
    controllerTabGroup.addTab("Render", null, renderPane, null);
    GridBagLayout gbl_renderPane = new GridBagLayout();
    gbl_renderPane.columnWidths = new int[] { 0, 0, };
    gbl_renderPane.rowHeights = new int[] { 0, };
    gbl_renderPane.columnWeights = new double[] { 0.0, 0.0 };
    gbl_renderPane.rowWeights = new double[] { 1.0 };
    renderPane.setLayout(gbl_renderPane);
    JComboBox breakLineByDropdown = new JComboBox();
    for (BreakLinesBy breakType : BreakLinesBy.values()) {
      breakLineByDropdown.addItem(breakType.toString());
    }

    JPanel panel = new JPanel();
    GridBagConstraints gbc_panel = new GridBagConstraints();
    gbc_panel.insets = new Insets(0, 0, 5, 5);
    gbc_panel.fill = GridBagConstraints.BOTH;
    gbc_panel.gridx = 0;
    gbc_panel.gridy = 0;
    renderPane.add(panel, gbc_panel);

    JLabel lblLineBreaksBy = new JLabel("  Line breaks by:  ");

    breakLineByDropdown.setSelectedItem(theModel.getBreakLevel().toString());
    breakLineByDropdown.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox) e.getSource();
        String typeStr = (String) cb.getSelectedItem();
        theModel.setBreakLevel(BreakLinesBy.valueOf(typeStr));
      }
    });

    colorByDropdown.setSelectedItem(TypeMap.kNoLabelLabel);
    colorByDropdown.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox) e.getSource();
        String labelStr = (String) cb.getSelectedItem();
        theModel.setColorBy(labelStr);
        notifyWorkingColorsSelected(ColorSchemeUtil.kWorkingLabel.equals(labelStr));
      }
    });

    colorByDropdown.setSelectedItem(TypeMap.kNoLabelLabel);

    JLabel lblText = new JLabel("  Text:  ");

    JComboBox textByDropdown = new JComboBox(textByModel);
    textByDropdown.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox) e.getSource();
        String labelStr = (String) cb.getSelectedItem();
        theModel.setTextBy(labelStr);
      }
    });
    GroupLayout gl_panel = new GroupLayout(panel);
    gl_panel
        .setHorizontalGroup(gl_panel
            .createParallelGroup(Alignment.LEADING)
            .addGroup(
                gl_panel
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                        gl_panel
                            .createParallelGroup(Alignment.LEADING)
                            .addGroup(
                                gl_panel
                                    .createSequentialGroup()
                                    .addGroup(
                                        gl_panel
                                            .createParallelGroup(
                                                Alignment.LEADING)
                                            .addGroup(
                                                gl_panel
                                                    .createSequentialGroup()
                                                    .addComponent(
                                                        lblLineBreaksBy)
                                                    .addPreferredGap(
                                                        ComponentPlacement.RELATED)
                                                    .addComponent(
                                                        breakLineByDropdown,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE))
                                            .addGroup(
                                                gl_panel
                                                    .createSequentialGroup()
                                                    .addComponent(lblColorBy)
                                                    .addPreferredGap(
                                                        ComponentPlacement.RELATED,
                                                        33, Short.MAX_VALUE)
                                                    .addComponent(
                                                        colorByDropdown,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE)))
                                    .addContainerGap(10, Short.MAX_VALUE))
                            .addGroup(
                                gl_panel
                                    .createSequentialGroup()
                                    .addComponent(lblText)
                                    .addPreferredGap(
                                        ComponentPlacement.RELATED, 51,
                                        Short.MAX_VALUE)
                                    .addComponent(textByDropdown,
                                        GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap()))));
    gl_panel.setVerticalGroup(gl_panel.createParallelGroup(Alignment.LEADING)
        .addGroup(
            gl_panel
                .createSequentialGroup()
                .addContainerGap()
                .addGroup(
                    gl_panel
                        .createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblLineBreaksBy)
                        .addComponent(breakLineByDropdown,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(
                    gl_panel
                        .createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblColorBy)
                        .addComponent(colorByDropdown,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(
                    gl_panel
                        .createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblText)
                        .addComponent(textByDropdown,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                .addContainerGap(254, Short.MAX_VALUE)));
    panel.setLayout(gl_panel);

    JPanel panel_1 = new JPanel();
    GridBagConstraints gbc_panel_1 = new GridBagConstraints();
    gbc_panel_1.insets = new Insets(0, 0, 5, 5);
    gbc_panel_1.fill = GridBagConstraints.BOTH;
    gbc_panel_1.gridx = 1;
    gbc_panel_1.gridy = 0;
    renderPane.add(panel_1, gbc_panel_1);

    JLabel lblCustomColorScheme = new JLabel("Custom Color Scheme Builder");

    JLabel lblAttributeType = new JLabel("Attribute type:");

    JLabel lblGroup_0 = new JLabel("  Group 1:  ");
    JLabel lblGroup_1 = new JLabel("  Group 2:  ");
    JLabel lblGroup_2 = new JLabel("  Group 3:  ");
    JLabel lblGroup_3 = new JLabel("  Group 4:  ");
    JLabel lblGroup_4 = new JLabel("  Group 5:  ");
    JLabel lblGroup_5 = new JLabel("  Group 6:  ");
    JLabel lblGroup_6 = new JLabel("  Group 7:  ");
    JLabel lblGroup_7 = new JLabel("  Group 8:  ");

    final JComboBox whichAttrBox = new JComboBox();
    for (String s: TypeMap.kPossibleColorByLabels) {
      if (!TypeMap.kColorByComparison.equals(s)) {
        whichAttrBox.addItem(s);
      }
    }

    txtCust_0 = new JTextField();
    txtCust_0.setColumns(10);

    txtCust_1 = new JTextField();
    txtCust_1.setColumns(10);

    txtCust_2 = new JTextField();
    txtCust_2.setColumns(10);

    txtCust_3 = new JTextField();
    txtCust_3.setColumns(10);

    txtCust_4 = new JTextField();
    txtCust_4.setColumns(10);

    txtCust_5 = new JTextField();
    txtCust_5.setColumns(10);

    txtCust_6 = new JTextField();
    txtCust_6.setColumns(10);

    txtCust_7 = new JTextField();
    txtCust_7.setColumns(10);

    txtCust_0.setBackground(Color.white);
    txtCust_1.setBackground(Color.white);
    txtCust_2.setBackground(Color.white);
    txtCust_3.setBackground(Color.white);
    txtCust_4.setBackground(Color.white);
    txtCust_5.setBackground(Color.white);
    txtCust_6.setBackground(Color.white);
    txtCust_7.setBackground(Color.white);
    
    final JTextField[] custTxts = { txtCust_0, txtCust_1, txtCust_2, txtCust_3,
        txtCust_4, txtCust_5, txtCust_6, txtCust_7, };
    final ColorSchemeDocumentListener custUpdateListener = new ColorSchemeDocumentListener(custTxts, theModel, this);
    whichAttrBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        JComboBox src = (JComboBox)(arg0.getSource());
        custUpdateListener.setLabel((String)src.getSelectedItem());
      }
    });
    
    txtCust_0.getDocument().addDocumentListener(custUpdateListener);
    txtCust_1.getDocument().addDocumentListener(custUpdateListener);
    txtCust_2.getDocument().addDocumentListener(custUpdateListener);
    txtCust_3.getDocument().addDocumentListener(custUpdateListener);
    txtCust_4.getDocument().addDocumentListener(custUpdateListener);
    txtCust_5.getDocument().addDocumentListener(custUpdateListener);
    txtCust_6.getDocument().addDocumentListener(custUpdateListener);
    txtCust_7.getDocument().addDocumentListener(custUpdateListener);
    
    JLabel lblName = new JLabel("Name:");

    txtSchemeName = new JTextField();
    txtSchemeName.setText("MyCustomColors1");
    txtSchemeName.setColumns(10);

    JButton btnSaveSchemeToFile = new JButton("Save to file");
    btnSaveSchemeToFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String schemeName = txtSchemeName.getText();
        if (schemeName == null || schemeName.isEmpty()) {
          JOptionPane.showMessageDialog(frame,
              "Please enter a name for this custom color scheme.");
          return;
        }
        String schemeType = (String) (whichAttrBox.getSelectedItem());
        if (TypeMap.kNoLabelLabel.equals(schemeType)) {
          JOptionPane.showMessageDialog(frame,
              "I can't color anything for the 'none' attribute.  Select a different attribute to look for.");
          return;
        }
        HashMap<String, Color> colors = new HashMap<String, Color>();
        for (JTextField t : custTxts) {
          if (t.getText() == null || t.getText().isEmpty()) {
            continue;
          }
          colors.put(t.getText(), t.getBackground());
        }
        if (colors.isEmpty()) {
          JOptionPane.showMessageDialog(frame,
              "Refusing to save an empty color scheme.  Pick something interesting to color first!");
          return;
        }
        colors.put(ColorSchemeUtil.kDefaultLabel, Color.white);
        File f = FileLoader.getColorSchemeSaveFile();
        if (f == null) {
          return;
        }
        CustomColorScheme cs = new CustomColorScheme(schemeName, schemeType,
            colors, f.getAbsolutePath());
        cs.saveToFile();
        theModel.addColorScheme(cs);
        refreshColorSchemeElements();
        for (JTextField t : custTxts) {
          t.setText("");
        }
        colorByDropdown.setSelectedItem(cs.getName());
      }
    });
    GroupLayout gl_panel_1 = new GroupLayout(panel_1);
    gl_panel_1
        .setHorizontalGroup(gl_panel_1
            .createParallelGroup(Alignment.LEADING)
            .addGroup(
                gl_panel_1
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                        gl_panel_1
                            .createParallelGroup(Alignment.LEADING)
                            .addComponent(lblCustomColorScheme)
                            .addGroup(
                                gl_panel_1
                                    .createSequentialGroup()
                                    .addGap(10)
                                    .addGroup(
                                        gl_panel_1
                                            .createParallelGroup(
                                                Alignment.LEADING, false)
                                            .addGroup(
                                                gl_panel_1
                                                    .createSequentialGroup()
                                                    .addGroup(
                                                        gl_panel_1
                                                            .createParallelGroup(
                                                                Alignment.LEADING)
                                                            .addComponent(
                                                                lblAttributeType)
                                                            .addComponent(
                                                                lblGroup_0)
                                                            .addComponent(
                                                                lblGroup_3)
                                                            .addComponent(
                                                                lblGroup_4)
                                                            .addComponent(
                                                                lblGroup_5)
                                                            .addComponent(
                                                                lblGroup_6)
                                                            .addComponent(
                                                                lblGroup_7)
                                                            .addGroup(
                                                                gl_panel_1
                                                                    .createParallelGroup(
                                                                        Alignment.TRAILING,
                                                                        false)
                                                                    .addComponent(
                                                                        lblGroup_1,
                                                                        Alignment.LEADING,
                                                                        GroupLayout.DEFAULT_SIZE,
                                                                        GroupLayout.DEFAULT_SIZE,
                                                                        Short.MAX_VALUE)
                                                                    .addComponent(
                                                                        lblGroup_2,
                                                                        Alignment.LEADING,
                                                                        GroupLayout.DEFAULT_SIZE,
                                                                        GroupLayout.DEFAULT_SIZE,
                                                                        Short.MAX_VALUE)))
                                                    .addGap(18)
                                                    .addGroup(
                                                        gl_panel_1
                                                            .createParallelGroup(
                                                                Alignment.LEADING)
                                                            .addComponent(
                                                                txtCust_7,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(
                                                                txtCust_6,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(
                                                                txtCust_5,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(
                                                                txtCust_4,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(
                                                                txtCust_3,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(
                                                                txtCust_2,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(
                                                                txtCust_1,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(
                                                                whichAttrBox,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(
                                                                txtCust_0,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)))
                                            .addGroup(
                                                gl_panel_1
                                                    .createSequentialGroup()
                                                    .addComponent(lblName)
                                                    .addGap(18)
                                                    .addGroup(
                                                        gl_panel_1
                                                            .createParallelGroup(
                                                                Alignment.LEADING)
                                                            .addComponent(
                                                                btnSaveSchemeToFile)
                                                            .addComponent(
                                                                txtSchemeName))))))
                    .addContainerGap(14, Short.MAX_VALUE)));
    gl_panel_1.setVerticalGroup(gl_panel_1.createParallelGroup(
        Alignment.LEADING).addGroup(
        gl_panel_1
            .createSequentialGroup()
            .addContainerGap()
            .addComponent(lblCustomColorScheme)
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addGroup(
                gl_panel_1
                    .createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblAttributeType)
                    .addComponent(whichAttrBox, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(
                gl_panel_1
                    .createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblGroup_0)
                    .addComponent(txtCust_0, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(
                gl_panel_1
                    .createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblGroup_1)
                    .addComponent(txtCust_1, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(
                gl_panel_1
                    .createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblGroup_2)
                    .addComponent(txtCust_2, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(
                gl_panel_1
                    .createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblGroup_3)
                    .addComponent(txtCust_3, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(
                gl_panel_1
                    .createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblGroup_4)
                    .addComponent(txtCust_4, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(
                gl_panel_1
                    .createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblGroup_5)
                    .addComponent(txtCust_5, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(
                gl_panel_1
                    .createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblGroup_6)
                    .addComponent(txtCust_6, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(
                gl_panel_1
                    .createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblGroup_7)
                    .addComponent(txtCust_7, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addGap(18)
            .addGroup(
                gl_panel_1
                    .createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(txtSchemeName, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addComponent(btnSaveSchemeToFile)
            .addContainerGap(25, Short.MAX_VALUE)));
    panel_1.setLayout(gl_panel_1);

    JPanel colorPane = new JPanel();
    controllerTabGroup.addTab("Settings", null, colorPane, null);
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
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:min"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, }));

    JPanel colorTopButtons = new JPanel();
    colorTop.add(colorTopButtons, "2, 2, left, top");

    JLabel lblNewLabel = new JLabel("  Actions:  ");

    JScrollPane colorTopSchemasPanel = new JScrollPane();
    JLabel lblSchemas = new JLabel("Color Schemes:");
    colorTopSchemasPanel.setColumnHeaderView(lblSchemas);

    final JList colorTopSchemasList = new JList();
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
          CustomColorScheme colorScheme = ColorSchemeUtil.loadFromFile(file);
          if (colorScheme == null) {
            // Must have hit an error when reading from the file
            return;
          }
          theModel.addColorScheme(colorScheme);
          refreshColorSchemeElements();
          refreshComparisonDataCheckboxes();
        } catch (InstantiationException e1) {
          JOptionPane.showMessageDialog(frame, e1.getMessage());
        }
      }
    });

    JButton btnRemoveScheme = new JButton("Remove scheme");
    btnRemoveScheme.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String selectedItem = (String) colorTopSchemasListModel
            .getSelectedItem();
        if (selectedItem == null) {
          JOptionPane.showMessageDialog(frame,
              "Please select a color scheme to remove");
          return;
        }
        theModel.removeColorScheme(selectedItem);
        colorTopSchemasListModel.removeAllElements();
        for (String s : theModel.getCustomColorSchemeList()) {
          colorTopSchemasListModel.addElement(s);
        }
      }
    });

    JButton btnSaveScheme = new JButton("Save scheme");
    btnSaveScheme.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.err.println("Unimplemented");
        // String selectedItem =
        // (String)colorTopSchemasListModel.getSelectedItem();
        // if (selectedItem == null) {
        // JOptionPane.showMessageDialog(frame,
        // "Please select a color scheme to save");
        // return;
        // }
      }
    });

    GroupLayout gl_colorTopButtons = new GroupLayout(colorTopButtons);
    gl_colorTopButtons.setHorizontalGroup(gl_colorTopButtons
        .createParallelGroup(Alignment.LEADING).addGroup(
            gl_colorTopButtons
                .createSequentialGroup()
                .addContainerGap()
                .addGroup(
                    gl_colorTopButtons.createParallelGroup(Alignment.LEADING)
                        .addComponent(lblNewLabel).addComponent(btnLoadScheme)
                        .addComponent(btnSaveScheme)
                        .addComponent(btnRemoveScheme))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    gl_colorTopButtons.setVerticalGroup(gl_colorTopButtons.createParallelGroup(
        Alignment.LEADING).addGroup(
        gl_colorTopButtons.createSequentialGroup().addContainerGap()
            .addComponent(lblNewLabel)
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addComponent(btnLoadScheme)
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addComponent(btnSaveScheme)
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addComponent(btnRemoveScheme)
            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    colorTopButtons.setLayout(gl_colorTopButtons);

    JPanel comparisonPane = new JPanel();
    controllerTabGroup.addTab("Comparisons", null, comparisonPane, null);
    comparisonPane.setLayout(new FormLayout(new ColumnSpec[] {
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:min"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, }));

    JPanel comparisonPaneLeftPanel = new JPanel();
    comparisonPane.add(comparisonPaneLeftPanel, "2, 2, left, fill");

    JLabel lblSmoothingWindow = new JLabel("Smoothing window:");

    JComboBox smoothingWindowCombo = new JComboBox();
    smoothingWindowCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox) e.getSource();
        int selected = (Integer) cb.getSelectedItem();
        theModel.setSmoothingWindow(selected);
      }
    });
    for (int i = 0; i < 10; i++) {
      smoothingWindowCombo.addItem(i * 2 + 1);
    }
    smoothingWindowCombo.setSelectedItem(theModel.getSmoothingWindow());
    
    JCheckBox chckbxAllowSelfSimilarity = new JCheckBox("Allow self similarity");
    chckbxAllowSelfSimilarity.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JCheckBox src = (JCheckBox)e.getSource();
        theModel.setAllowSelfSimilarity(src.isSelected());
      }
    });
    chckbxAllowSelfSimilarity.setSelected(theModel.getAllowSelfSimilarity());
    
    JButton btnRefresh = new JButton("Refresh");
    btnRefresh.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        theModel.setColorBy(ColorSchemeDB.kRandomComparision);
        colorByModel.setSelectedItem(TypeMap.kNoLabelLabel);
      }
    });
    GroupLayout gl_comparisonPaneLeftPanel = new GroupLayout(
        comparisonPaneLeftPanel);
    gl_comparisonPaneLeftPanel.setHorizontalGroup(
      gl_comparisonPaneLeftPanel.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_comparisonPaneLeftPanel.createSequentialGroup()
          .addContainerGap()
          .addGroup(gl_comparisonPaneLeftPanel.createParallelGroup(Alignment.LEADING)
            .addGroup(gl_comparisonPaneLeftPanel.createSequentialGroup()
              .addComponent(lblSmoothingWindow)
              .addPreferredGap(ComponentPlacement.RELATED)
              .addComponent(smoothingWindowCombo, GroupLayout.PREFERRED_SIZE, 82, GroupLayout.PREFERRED_SIZE))
            .addComponent(chckbxAllowSelfSimilarity)
            .addComponent(btnRefresh))
          .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    gl_comparisonPaneLeftPanel.setVerticalGroup(
      gl_comparisonPaneLeftPanel.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_comparisonPaneLeftPanel.createSequentialGroup()
          .addContainerGap()
          .addGroup(gl_comparisonPaneLeftPanel.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblSmoothingWindow)
            .addComponent(smoothingWindowCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(chckbxAllowSelfSimilarity)
          .addPreferredGap(ComponentPlacement.RELATED, 385, Short.MAX_VALUE)
          .addComponent(btnRefresh)
          .addContainerGap())
    );
    comparisonPaneLeftPanel.setLayout(gl_comparisonPaneLeftPanel);

    JScrollPane comparisonPaneRightPanel = new JScrollPane();
    comparisonPane.add(comparisonPaneRightPanel, "4, 2, fill, fill");

    comparisonContent = new JPanel();
    comparisonPaneRightPanel.setViewportView(comparisonContent);
    comparisonContent.setLayout(new MigLayout("", "[left]", "[]"));

    lblNoComparisonData = new JLabel("No comparison data loaded.");
    comparisonContent.add(lblNoComparisonData, "cell 0 0");

  }

  public void go() {
    this.frame.setVisible(true);
  }

  @Override
  public void windowStateChanged(WindowEvent e) {
    if (e instanceof FileProgressEvent) {
      FileProgressEvent fpe = (FileProgressEvent) e;

      switch (fpe.getStatus()) {
      case PROGRESS:
        lblProgress.setText(String.format("Progress: (%2.2f%%)",
            fpe.getProgress() * 100.0));
        break;
      case FINISHED_SUCC:
        theModel.addData(fpe.getResult(), fpe.getResultingTypeMap());
        updateFileLists();
        TypeMap typeMap = fpe.getResultingTypeMap();
        refreshColorSchemeElements();

        for (String l : TypeMap.kPossibleTextByLabels) {
          if (typeMap.hasLabel(l.toLowerCase())
              && textByModel.getIndexOf(l.toLowerCase()) < 0) {
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

  public void refreshColorSchemeElements() {
    List<String> customColorSchemes = theModel.getCustomColorSchemeList();
    List<String> builtInColorSchemes = theModel.getBuiltInColorSchemeList();
    
    // Remove comparison color scheme from builtin color schemes
    builtInColorSchemes.remove(ColorSchemeDB.kRandomComparision);
    
    colorTopSchemasListModel.removeAllElements();
    for (String s : customColorSchemes) {
      colorTopSchemasListModel.addElement(s);
    }
    String lastSelected = (String)theModel.getColorBy();
    colorByModel.removeAllElements();
    for (String key : customColorSchemes) {
      colorByModel.addElement(key);
    }
    for (String key : builtInColorSchemes) {
      colorByModel.addElement(key);
    }
    colorByModel.setSelectedItem(lastSelected);
  }

  protected void updateFileLists() {
    searchListModel.removeAllElements();
    DataTreeView[] views = theModel.getRenderingData().views;
    for (DataTreeView s : views) {
      String line = s.getData().getPath();
      searchListModel.addElement(line);
    }

    fileListModel.removeAllElements();
    for (DataTreeView s : views) {
      String line = s.getData().getPath();
      if (s.canRenderByProseLines()) {
        line += ControllerGUI.kXmlTag;
      }
      fileListModel.addElement(line);
    }

    refreshComparisonDataCheckboxes();
  }

  private void refreshComparisonDataCheckboxes() {
    if (!theModel.hasComparisonData()) {
      if (comparisonsEnabled.size() > 0) {
        // remove checkboxes from Comparison panel
        comparisonContent.removeAll();
        // remove our references
        comparisonsEnabled.clear();
        // enable no comparison data label
        comparisonContent.add(lblNoComparisonData, "cell 0 0");
        lblNoComparisonData.setVisible(true);
      }
      return;
    }
    ComparisonState[] state = theModel.getComparisonState();
    boolean isSame = state.length == comparisonsEnabled.size();
    for (int i = 0; i < state.length && isSame; i++) {
      isSame = state[i].getName().equals(comparisonsEnabled.get(i).getText());
    }
 
    if (!isSame) {
      // on change, remove all the buttons, add them all again, refresh the values
      lblNoComparisonData.setVisible(false);
      comparisonContent.removeAll();
      comparisonsEnabled.clear();
      for (int i = 0; i < state.length; i++) {
        JCheckBox box = new JCheckBox(state[i].getName());
        box.setSelected(state[i].getEnabled());
        box.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent arg0) {
            JCheckBox box = (JCheckBox) arg0.getSource();
            theModel.setComparisonEnabled(box.isSelected(), box.getText());
          }
        });
        JTextField textField = new JTextField();
        textField.setEditable(false);
        textField.setBackground(state[i].getColor());
        textField.setOpaque(true);
        textField.setColumns(10);
        comparisonContent.add(textField, "cell 0 " + i);
        comparisonContent.add(box, "cell 1 " + i);
      }
    } else {
      for (int i = 0; i < state.length; i++) {
        comparisonsEnabled.get(i).setSelected(state[i].getEnabled());
      }
    }
  }

  private void notifyWorkingColorsSelected(boolean selected) {
    if (txtCust_0 == null) {
      return;
    }
    if (!selected) {
      txtCust_0.setBackground(Color.white);
      txtCust_1.setBackground(Color.white);
      txtCust_2.setBackground(Color.white);
      txtCust_3.setBackground(Color.white);
      txtCust_4.setBackground(Color.white);
      txtCust_5.setBackground(Color.white);
      txtCust_6.setBackground(Color.white);
      txtCust_7.setBackground(Color.white);
    } else {
      txtCust_0.setBackground(ColorSchemeUtil.goodColors[0]);
      txtCust_1.setBackground(ColorSchemeUtil.goodColors[1]);
      txtCust_2.setBackground(ColorSchemeUtil.goodColors[2]);
      txtCust_3.setBackground(ColorSchemeUtil.goodColors[3]);
      txtCust_4.setBackground(ColorSchemeUtil.goodColors[4]);
      txtCust_5.setBackground(ColorSchemeUtil.goodColors[5]);
      txtCust_6.setBackground(ColorSchemeUtil.goodColors[5]);
      txtCust_7.setBackground(ColorSchemeUtil.goodColors[7]);
    }
  }
}

abstract class FileListActionListener implements ActionListener {
  protected List<String> stripXMLMetaData(Object[] rawInput) {
    List<String> strippedPaths = new ArrayList<String>(rawInput.length);
    for (Object inObj : rawInput) {
      String in = (String) inObj;
      if (in.endsWith(ControllerGUI.kXmlTag)) {
        strippedPaths
            .add(in.substring(0, in.lastIndexOf(ControllerGUI.kXmlTag)));
      } else {
        strippedPaths.add(in);
      }
    }
    return strippedPaths;
  }
}
