package gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

public class LdaOptions  extends JFrame  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2892621741924015116L;
	public static final String LDA_BAYES = "CVB0LDA";
	public static final String LDA_GIBBS = "Gibbs";
	
	private ButtonGroup buttonGroupLdaAlgoGroup;
	private JSpinner spinnerTopicSmoothing;
	private JSpinner spinnerTermSmoothing;
	private JSpinner spinnerNumberOfTopics;
	private JSpinner spinnerNumberOfIterations;
	private JSpinner spinnerTrainingData;
	private JTextField textFieldSliceColumns;
	private JTextField textFieldDataColumn;
	private CsvViewer csvViewer1 = null;
	private CsvViewer csvViewer2 = null;
	private JButton buttonOk;
	private String filepath;
	private JButton buttonSelectSliceColumns;
	private JButton buttonSelectDataColumn;
	private JRadioButton bayesButton;
	private JRadioButton gibbsButton;
	
	private void setAllEnabled() {
//		buttonGroupLdaAlgoGroup;
		bayesButton.setEnabled(true);
		gibbsButton.setEnabled(true);
		buttonSelectSliceColumns.setEnabled(true);
		buttonSelectDataColumn.setEnabled(true);
		spinnerTopicSmoothing.setEnabled(true);
		spinnerTermSmoothing.setEnabled(true);
		spinnerNumberOfTopics.setEnabled(true);
		spinnerNumberOfIterations.setEnabled(true);
		spinnerTrainingData.setEnabled(true);
		textFieldSliceColumns.setEnabled(true);
		textFieldDataColumn.setEnabled(true);		
	}
	
	/**
	 * @return the spinnerTrainingData
	 */
	public JSpinner getSpinnerTrainingData() {
		return spinnerTrainingData;
	}

	/**
	 * @return the spinnerNumberOfIterations
	 */
	public JSpinner getSpinnerNumberOfIterations() {
		return spinnerNumberOfIterations;
	}


	
	/**
	 * @return the filepath
	 */
	public String getFilepath() {
		return filepath;
	}

	/**
	 * @param filepath the filepath to set
	 */
	public void setFilepath(String filepath) {
		this.filepath = filepath;
		if (!this.filepath.isEmpty()) {
			csvViewer1 = new CsvViewer(filepath);
			csvViewer2 = new CsvViewer(filepath);
		}
	}

	/**
	 * @return the buttonGroupLdaAlgoGroup
	 */
	public ButtonGroup getButtonGroupLdaAlgoGroup() {
		return buttonGroupLdaAlgoGroup;
	}

	/**
	 * @return the spinnerTopicSmoothing
	 */
	public JSpinner getSpinnerTopicSmoothing() {
		return spinnerTopicSmoothing;
	}

	/**
	 * @return the spinnerTermSmoothing
	 */
	public JSpinner getSpinnerTermSmoothing() {
		return spinnerTermSmoothing;
	}

	/**
	 * @return the spinnerNumberOfTopics
	 */
	public JSpinner getSpinnerNumberOfTopics() {
		return spinnerNumberOfTopics;
	}

	/**
	 * @return the textFieldDataColumn
	 */
	public JTextField getTextFieldDataColumn() {
		return textFieldDataColumn;
	}

	/**
	 * @return the textFieldSliceColumns
	 */
	public JTextField getTextFieldSliceColumns() {
		return textFieldSliceColumns;
	}
	
	/**
	 * @return the buttonOk
	 */
	public JButton getButtonOk() {
		return buttonOk;
	}
	
	
	
	/**
	 * @return the buttonSelectSliceColumns
	 */
	public JButton getButtonSelectSliceColumns() {
		return buttonSelectSliceColumns;
	}

	/**
	 * @return the buttonSelectDataColumn
	 */
	public JButton getButtonSelectDataColumn() {
		return buttonSelectDataColumn;
	}

	/**
	 * @return the bayesButton
	 */
	public JRadioButton getBayesButton() {
		return bayesButton;
	}

	/**
	 * @return the gibbsButton
	 */
	public JRadioButton getGibbsButton() {
		return gibbsButton;
	}

	public LdaOptions() {
		
		// title for the frame
		super("LDA options");
		
		/*
		 * IMPLEMENTATION
		 */
		
		// Radiobuttons to select the inference algorithm for LDAs
		bayesButton = new JRadioButton(LDA_BAYES);
		bayesButton.setActionCommand(LDA_BAYES);
		gibbsButton = new JRadioButton(LDA_GIBBS);
		gibbsButton.setActionCommand(LDA_GIBBS);
		bayesButton.setSelected(true);
		buttonGroupLdaAlgoGroup = new ButtonGroup();
		buttonGroupLdaAlgoGroup.add(bayesButton);
	    buttonGroupLdaAlgoGroup.add(gibbsButton);
	    JPanel ldaAlgoPanel = new JPanel();
	    ldaAlgoPanel.setLayout(new BoxLayout(ldaAlgoPanel, BoxLayout.Y_AXIS));	    
	    ldaAlgoPanel.add(bayesButton);
	    ldaAlgoPanel.add(gibbsButton);
	    JPanel panelImplementation = new JPanel();
	    panelImplementation.setBorder(BorderFactory.createLineBorder(Color.black));
	    panelImplementation.add(new JLabel("Implementation"));	    
	    panelImplementation.add(ldaAlgoPanel);
	    
	    
	    /*
	     * PARAMETER
	     */
	    
	    SpinnerNumberModel spinnerTopicSmoothingModel = new SpinnerNumberModel(0.1, 0, 10, 0.01);
	    SpinnerNumberModel spinnerTermSmoothingModel = new SpinnerNumberModel(0.1, 0, 10, 0.01);
	    SpinnerNumberModel spinnerTopicModel = new SpinnerNumberModel(30, 1, 1000, 1);	 
	    SpinnerNumberModel spinnerIterationsModel = new SpinnerNumberModel(500, 100, 2000, 100);
	    SpinnerNumberModel spinnerTrainingDataModel = new SpinnerNumberModel(0.8, 0.1, 1.0, 0.1);
	    spinnerTopicSmoothing = new JSpinner(spinnerTopicSmoothingModel);
	    spinnerTermSmoothing = new JSpinner(spinnerTermSmoothingModel);
	    spinnerNumberOfTopics = new JSpinner(spinnerTopicModel);
	    spinnerNumberOfIterations = new JSpinner(spinnerIterationsModel);
	    spinnerTrainingData = new JSpinner(spinnerTrainingDataModel);
	    
	    JPanel topicSmoothingPanel = new JPanel();
	    topicSmoothingPanel.add(new JLabel("topic smoothing:"));
	    topicSmoothingPanel.add(spinnerTopicSmoothing);
	    JPanel termSmoothingPanel = new JPanel();
	    termSmoothingPanel.add(new JLabel("term smoothing:"));
	    termSmoothingPanel.add(spinnerTermSmoothing);
	    JPanel numberOfTopicsPanel = new JPanel();
	    numberOfTopicsPanel.add(new JLabel("number of topics:"));
	    numberOfTopicsPanel.add(spinnerNumberOfTopics);
	    JPanel numberOfIterationsPanel = new JPanel();
	    numberOfIterationsPanel.add(new JLabel("number of iterations:"));
	    numberOfIterationsPanel.add(spinnerNumberOfIterations);
	    JPanel numberOfTrainingDataPanel = new JPanel();
	    numberOfTrainingDataPanel.add(new JLabel("percentage of data for training:"));
	    numberOfTrainingDataPanel.add(spinnerTrainingData);
	    
	    // LDA-Parameter
	    JPanel panelLdaParam = new JPanel();
	    panelLdaParam.setLayout(new BoxLayout(panelLdaParam, BoxLayout.Y_AXIS));	    
	    panelLdaParam.add(topicSmoothingPanel);
	    panelLdaParam.add(termSmoothingPanel);
	    panelLdaParam.add(numberOfTopicsPanel);	
	    panelLdaParam.add(numberOfIterationsPanel);
	    panelLdaParam.add(numberOfTrainingDataPanel);
	    JPanel panelParams = new JPanel();
	    panelParams.setBorder(BorderFactory.createLineBorder(Color.black));
	    panelParams.add(new JLabel("Parameter"));
	    panelParams.add(panelLdaParam);
	    
	    /*
	     * SETTINGS
	     */
	    
	    // datacolumn panel
		JLabel labelDataColumn = new JLabel("Data");
		textFieldDataColumn = new JTextField();
		textFieldDataColumn.setColumns(30);
		buttonSelectDataColumn = new JButton("select columns");
		
		buttonSelectDataColumn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				if (filepath != null) {
					if (csvViewer1 == null) {
						setFilepath(filepath);
					}
					csvViewer1.setVisible(true);
					csvViewer1.getTableData().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					csvViewer1.getButtonOk().addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							textFieldDataColumn.setText(csvViewer1.getSelectedColumnsAsString());						
							csvViewer1.setVisible(false);
						}}
					);
				}
				
				
			}});	
		JPanel panelDataColumn = new JPanel();
		panelDataColumn.add(labelDataColumn);
		panelDataColumn.add(textFieldDataColumn);
		panelDataColumn.add(buttonSelectDataColumn);
		
		// slicecolumn panel
		JLabel labelSliceColumns = new JLabel("Slice");
		textFieldSliceColumns = new JTextField();
		textFieldSliceColumns.setColumns(30);
		buttonSelectSliceColumns = new JButton("select columns");
		buttonSelectSliceColumns.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if (filepath != null) {
					if (csvViewer2 == null) {
						setFilepath(filepath);
					}
					csvViewer2.setVisible(true);
					csvViewer2.getTableData().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					csvViewer2.getButtonOk().addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							textFieldSliceColumns.setText(csvViewer2.getSelectedColumnsAsString());						
							csvViewer2.setVisible(false);
						}}
					);					
				}								
				
			}});
		JPanel panelSliceColumns = new JPanel();
		panelSliceColumns.add(labelSliceColumns);
		panelSliceColumns.add(textFieldSliceColumns);
		panelSliceColumns.add(buttonSelectSliceColumns);
		
		// settings panel
		JPanel panelSettings = new JPanel();
		panelSettings.setLayout(new BoxLayout(panelSettings, BoxLayout.Y_AXIS));
		panelSettings.setBorder(BorderFactory.createLineBorder(Color.black));
		panelSettings.add(panelDataColumn);
		panelSettings.add(panelSliceColumns);
		
		/*
		 *  Panel for the ok and cancel button
		 */
		buttonOk = new JButton("OK");
		buttonOk.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setAllEnabled();	
			}
		});
		JButton buttonCancel = new JButton("Cancel");
		buttonCancel.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				setAllEnabled();
			}
		});
		buttonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);				
			}
		});
		JPanel panelButtonOkAndCancel = new JPanel();
		panelButtonOkAndCancel.add(buttonOk);
		panelButtonOkAndCancel.add(buttonCancel);
		
	    // main panel
	    JPanel panelMain = new JPanel();
	    panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.Y_AXIS));
	    panelMain.add(panelImplementation);
	    panelMain.add(panelParams);
	    panelMain.add(panelSettings);
	    panelMain.add(panelButtonOkAndCancel);
	    
	    add(panelMain);
	    
		// set position
		setLocation(50,50);
		
		// set size
//		setSize(800,600);
		pack(); // set size automatically
		
		//Das JFrame anzeigen
//		setVisible(true);
	    
	}
	
}
