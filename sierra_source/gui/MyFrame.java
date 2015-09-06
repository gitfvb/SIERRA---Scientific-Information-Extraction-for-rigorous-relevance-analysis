package gui;


import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import main.ExecuteLda;
import main.ExtractOptions;
import preparation.PrepareDomain;
import reports.ReportUtilities;
import utilities.DateTimeUtilities;
import utilities.FileUtilities;
import utilities.MessageConsole;
import utilities.SummaryUtilities;
import au.com.bytecode.opencsv.CSVReader;
import convert.ConvertPdf2Txt;
import convert.ConvertTxt2Csv;
import convert.ImportTxt;
import database.DataSource;
import database.KeyValue;
import database.KeyValueMutableModel;
import download.HttpCrawler;
import download.Normalize;
import events.CustomEvent;
import events.CustomEventListener;

public class MyFrame extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4088592765557611436L;
	public static String DATAFILE = "converted_file.csv";
	public static String LINE_SEPARATOR = "---------------------------------------------";
	
	private HttpCrawler httpCrawler;
	private ConvertTxt2Csv converter;
	private ConvertPdf2Txt txtConverter;
	private ReportUtilities ru;
	
    private Thread threadDownload;
    private Thread threadConvert;    
    private Thread threadConsolidate;
    private Thread threadLdaLearn;
    private Thread threadLdaExecute;
    private Thread threadLdaCalculate;
    private Thread threadLdaSlice;
    
    private JTextArea textAreaBottomOutput;
    private JTextField textFieldDirectory;
    private JTextField textFieldNumberOfTopicsToTest; 
    private JScrollPane scrollPaneBottom;	
    private JSpinner spinnerLowestDownloadId;
    private JSpinner spinnerHighestDownloadId;
    private JCheckBoxMenuItem checkBoxMenuItemAutoScroll;
    
    private LdaOptions ldaLearnOptions;
    private LdaSelect ldaChooseData;
    private CsvEditor csvEditor;
    private About about;
    private MessageConsole messageConsole;    
    private java.util.List<DataSource> downloadStack;
    private JComboBox comboBoxLanguage;
    private JCheckBox useEnglishStopwords;
    private JTextField textFieldModelDirectoryExecute;
    private JTextField textFieldModelDirectorySlice;
    private String chosenLdaData = "";
    private String chosenLdaModel = "";
	private String modelPathForReport = "";
    
    /**
     * creates the main frame of this program
     */
	public MyFrame(){		
		
		// title for the frame
		super("SIERRA - scientific information extraction for rigorous relevance analysis");
		
		// close the thread, if the JFrame closes
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// add the menuBar to the frame
		setJMenuBar(this.createMenuBar());
		
		// create Panels
		textFieldDirectory = new JTextField();
		JPanel chooseDirectoryPanel = createChooseDirectoryPanel(textFieldDirectory, "Domain directory:");
		JPanel preparationPanel = createPreparationPanel();
		JPanel downloadPanel = createDownloadPanel();
		JPanel convertPanel = createConvertPanel();
		JPanel ldaPanel = createLdaPanel();
		
		// create and add Tabs		
		JTabbedPane tabbedPaneProcesses = new JTabbedPane();
		tabbedPaneProcesses.add(preparationPanel.getName(), preparationPanel);
		tabbedPaneProcesses.add(downloadPanel.getName(), downloadPanel);
	    tabbedPaneProcesses.add(convertPanel.getName(), convertPanel);
	    tabbedPaneProcesses.add(ldaPanel.getName(), ldaPanel);	    
	    
		// the panel at the top
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.add(chooseDirectoryPanel);
		topPanel.add(tabbedPaneProcesses);

		// create a panel for the top
		JPanel top = new JPanel();
		top.add(topPanel);	
		
		// The output at the bottom
		textAreaBottomOutput = new JTextArea(10, 60);
        textAreaBottomOutput.setMargin(new Insets(5,5,5,5));
        textAreaBottomOutput.setEditable(true);
        messageConsole = new MessageConsole(textAreaBottomOutput);
        messageConsole.redirectOut();
        messageConsole.redirectErr(Color.RED, null);
        
		// initialise crawler
		httpCrawler = new HttpCrawler();
        httpCrawler.addCustomEventListener(new CustomEventListener() {
			@Override
			public void customEventOccurred(CustomEvent evt) {
				appendMessage(evt.getMessage());
			}}
        );
        
        // initialise converter
//        converter = new ConvertPdf2Csv();
        converter = new ConvertTxt2Csv();
        converter.addCustomEventListener(new CustomEventListener() {
			@Override
			public void customEventOccurred(CustomEvent evt) {
				appendMessage(evt.getMessage());
			}}
        );
        txtConverter = new ConvertPdf2Txt();
        txtConverter.addCustomEventListener(new CustomEventListener() {
			@Override
			public void customEventOccurred(CustomEvent evt) {
				appendMessage(evt.getMessage());
			}}
        );
        
        // initialise reporter
        ru = new ReportUtilities();
		ru.addCustomEventListener(new CustomEventListener() {
			@Override
			public void customEventOccurred(CustomEvent evt) {
				appendMessage(evt.getMessage());
			}}
        );
        
        // initialisiere LDA
        ldaLearnOptions = new LdaOptions();
        about = new About();
        
		// create a scrollpane for the bottom
		scrollPaneBottom = new JScrollPane(textAreaBottomOutput);

		// create the general layout
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,top,scrollPaneBottom);
		add(splitPane);		
				
		// set position
		setLocation(50,50);
		
		// set size
		setSize(800,600);
//		pack(); // set size automatically
		
		//Das JFrame anzeigen
		setVisible(true);
		
		// Ready-Message!
		appendMessage("Program is ready!");
		
	}
	
	private JMenuBar createMenuBar() {

    	// create menubar
    	JMenuBar menuBar = new JMenuBar();
    	
    	// create first part of menus
    	JMenu menuView = new JMenu("View");
		JMenu menuFile = new JMenu("File");
		JMenu menuReport = new JMenu("Report");
		JMenu menuHelp = new JMenu("Help");
		
		/*
		 * FILE-MENU
		 */
		
		// items for the menu
		JMenuItem menuItemFileExit = new JMenuItem("Close");
				
		// actions for the menu
		menuItemFileExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		
		// add items to the menu
		menuFile.add(menuItemFileExit);
		
		
		/*
		 * VIEW-MENU
		 */
		
		// items for the menu
		checkBoxMenuItemAutoScroll = new JCheckBoxMenuItem("autoscroll");
		
		// settings fot the menu
		checkBoxMenuItemAutoScroll.setSelected(true);
		
		// add items to the menu
		menuView.add(checkBoxMenuItemAutoScroll);
		
		/*
		 * REPORT-MENU
		 */
		
		// items for the menu
		JMenuItem menuItemShowMostUsedWordsByN = new JMenuItem("top n words");
		JMenuItem menuItemShowMostUsedWordsbyPercentage = new JMenuItem("top words by cumulation");
		JMenuItem menuItemShowLogProbability = new JMenuItem("log probability values");
		JMenuItem menuItemSaveTopTopicTerms = new JMenuItem("top topic terms");
		JMenuItem menuItemSaveAllMetaData = new JMenuItem("save all meta data");
		JMenuItem menuItemShowTMTStopwords = new JMenuItem("TMT english stopwords");
		
		// actions for the menu
		menuItemShowMostUsedWordsByN.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {				
			    
				setModelForReport();
				ldaChooseData.getButtonOk().addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {						
						ldaChooseData.setVisible(false);
						modelPathForReport = ldaChooseData.getSelectedFolder();
						if (modelPathForReport != "") {
							String s = (String) JOptionPane.showInputDialog(
						    		getParent(),
						    		"Enter the number of top used words you want to see:", // label
			                        "50");
							if ((s != null) && (s.length() > 0))										
								ru.printTopNWords(modelPathForReport, Integer.valueOf(s.trim()));
						}
					}}
				);
				
				
									
				
			}}
		);		
		menuItemShowMostUsedWordsbyPercentage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				setModelForReport();
				ldaChooseData.getButtonOk().addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {						
						ldaChooseData.setVisible(false);
						modelPathForReport = ldaChooseData.getSelectedFolder();
						if (modelPathForReport != "") {
							String s = (String) JOptionPane.showInputDialog(
						    		getParent(),
						    		"Enter the cumulative percentage of words you want to see:", // label
			                        "0.2");
							if ((s != null) && (s.length() > 0))										
								ru.printWordsByCumulation(modelPathForReport, Double.valueOf(s.trim()));
						}
					}}
				);
				
								
				
			}}
		);
		menuItemShowLogProbability.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				setModelForReport();				
				ldaChooseData.getButtonOk().addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {						
						ldaChooseData.setVisible(false);
						modelPathForReport = ldaChooseData.getSelectedFolder();
						if (modelPathForReport != "") {
							ru.printLogValues(modelPathForReport);
						}
					}}
				);
				
				
				
			}}
		);		
		menuItemSaveTopTopicTerms.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				setModelForReport();
				ldaChooseData.getButtonOk().addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {						
						ldaChooseData.setVisible(false);
						modelPathForReport = ldaChooseData.getSelectedFolder();
						if (modelPathForReport != "") {
							String filepath = SummaryUtilities.saveTopicTermsByCumulation(modelPathForReport);
							JOptionPane.showMessageDialog(getParent(), "Saved at ".concat(filepath));
						}
					}}
				);
				
												
				
			}}
		);
		menuItemSaveAllMetaData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				
				java.util.List<String> fileFolders = new ArrayList<String>();
				fileFolders.add(PrepareDomain.PDF_DOWNLOAD_FOLDER);
				fileFolders.add(PrepareDomain.PDF_IMPORT_FOLDER);
				fileFolders.add(PrepareDomain.PDF_CHANGE_FOLDER);
				fileFolders.add(PrepareDomain.PDF_ERROR_FOLDER);
				fileFolders.add(PrepareDomain.TXT_CONVERT_FOLDER);
				fileFolders.add(PrepareDomain.TXT_IMPORT_FOLDER);
				fileFolders.add(PrepareDomain.TXT_CHANGE_FOLDER);
				fileFolders.add(PrepareDomain.TXT_ERROR_FOLDER);
				
				for (String folder : fileFolders) {
					ru.printXmlFilesToCsv(FileUtilities.checkDirectoryEnding(textFieldDirectory.getText()).concat(folder));
				}
				
			}}
		);		
		menuItemShowTMTStopwords.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				ru.printTMTStopWords();								
				
			}}
		);
		
		
		// add items to the menu
		menuReport.add(menuItemShowMostUsedWordsByN);
		menuReport.add(menuItemShowMostUsedWordsbyPercentage);
		menuReport.add(menuItemShowLogProbability);
		menuReport.add(menuItemSaveTopTopicTerms);
		menuReport.add(menuItemSaveAllMetaData);
		menuReport.add(menuItemShowTMTStopwords);
		
		/*
		 * HELP-MENU
		 */
		
		// items for the menu
		JMenuItem menuItemAbout = new JMenuItem("About");
				
		// actions for the menu
		menuItemAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				about.setVisible(true);
			}
		});
		menuHelp.add(menuItemAbout);
		
		/*
		 * MENU-BAR
		 */
		
		// add menus to the menubar
		menuBar.add(menuFile);
		menuBar.add(menuReport);
		menuBar.add(menuView);
		menuBar.add(menuHelp);
		
		// return the menubar
    	return menuBar;
    	
    }
	

	
	private void setModelForReport() {
		if (textFieldDirectory.getText() != null) {
	        
			String ldaFolder = FileUtilities.checkDirectoryEnding(textFieldDirectory.getText()).concat(PrepareDomain.LDA_FOLDER);
			List<String> dataFolder = FileUtilities.getAllFoldersInFolder(ldaFolder);
			ldaChooseData = new LdaSelect(dataFolder);
			
			if (dataFolder.size() > 1) {
				ldaChooseData.setVisible(true);
			} else if (dataFolder.size() == 1) {
				modelPathForReport = dataFolder.get(0);
			}
			
		}
	}

	private JPanel createChooseDirectoryPanel(final JTextField textFieldDirectoryChooser, String labelText) {

		// label for choosing a folder
		JLabel labelChooseDirectory = new JLabel(labelText);

		// textfield for the directory
		textFieldDirectoryChooser.setColumns(30);
		textFieldDirectoryChooser.setText("");

		// button to select the directory
		JButton buttonOpenDirectoryChooser = new JButton();
		buttonOpenDirectoryChooser.setText("...");
		buttonOpenDirectoryChooser.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				textFieldDirectoryChooser.setText(FileDialogForDirectory.chooseDirectoryDialog(MyFrame.this, "Choose Directory..."));
			}

		});

		// panel for selecting a target directory
		JPanel panelSelectDirectory = new JPanel();
		panelSelectDirectory.setName("select directory");
		panelSelectDirectory.add(labelChooseDirectory);
		panelSelectDirectory.add(textFieldDirectoryChooser);
		panelSelectDirectory.add(buttonOpenDirectoryChooser);

		return panelSelectDirectory;
	}	

	
	
	
private JPanel createPreparationPanel() {
	
	JButton createStructure = new JButton("Create domain");
	createStructure.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			appendMessage(LINE_SEPARATOR);
			if (textFieldDirectory.getText().length() > 0) {
				// create the folders for all steps
				PrepareDomain pd = new PrepareDomain(new File(textFieldDirectory.getText()));
				pd.addCustomEventListener(new CustomEventListener() {
					@Override
					public void customEventOccurred(CustomEvent evt) {
						appendMessage(evt.getMessage());
					}}
				);
				pd.createFilesAndFolders();
			} else {
				// Message to select a directory for domain
				JOptionPane.showMessageDialog(getParent(), "Please select a domain directory.");
			}
		}});
	
	
	JPanel panelPreparation = new JPanel();
	panelPreparation.setName("Preparation");
	panelPreparation.add(createStructure);	
	
	return panelPreparation;
}

private JPanel createDownloadPanel() {
	
	// Panel for IDs
	JLabel labelLowestDownloadId = new JLabel("lowest ID");
	JLabel labelHighestDownloadId = new JLabel("highest ID");	
	SpinnerNumberModel spinnerLowestDownloadIdModel = new SpinnerNumberModel(10000000, 10000000, 69999999, 1);
    SpinnerNumberModel spinnerHighestDownloadIdModel = new SpinnerNumberModel(79999999, 20000000, 79999999, 1);
    spinnerLowestDownloadId = new JSpinner(spinnerLowestDownloadIdModel);
    spinnerHighestDownloadId = new JSpinner(spinnerHighestDownloadIdModel);
	JPanel panelLowestDownloadId = new JPanel();
	panelLowestDownloadId.add(labelLowestDownloadId);
	panelLowestDownloadId.add(spinnerLowestDownloadId);
	JPanel panelHighestDownloadId = new JPanel();
	panelHighestDownloadId.add(labelHighestDownloadId);
	panelHighestDownloadId.add(spinnerHighestDownloadId);
	JPanel panelDownloadIds = new JPanel();
	panelDownloadIds.setLayout(new BoxLayout(panelDownloadIds, BoxLayout.Y_AXIS));
	panelDownloadIds.add(panelLowestDownloadId);
	panelDownloadIds.add(panelHighestDownloadId);
		
		JButton buttonEditLinks = new JButton("edit links");
		buttonEditLinks.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				File downloadLinks = new File(FileUtilities.checkDirectoryEnding(textFieldDirectory.getText()).concat(PrepareDomain.DOWNLOAD_LINKS_FILE));
				if (downloadLinks.exists()) {
					csvEditor = new CsvEditor(downloadLinks.getAbsolutePath());
					String[] columnNames = {"link", "conference", "year"};
					csvEditor.setColumnNames(columnNames);
					csvEditor.loadData();
					csvEditor.setVisible(true);
				} else {
					// Message that the download-file does not exist
					JOptionPane.showMessageDialog(getParent(), "The csv-file for download-links does not exist.\n Please execute preparation first.");
				}				
				
				
			}});
	
		// download-button		
		JButton buttonDownload = new JButton("Download PDFs");
		buttonDownload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {				
				appendMessage(LINE_SEPARATOR);
				if ((Integer) spinnerLowestDownloadId.getValue() < (Integer) spinnerHighestDownloadId.getValue()) {



					threadDownload = new Thread(){
						public void run() {
							try {								

								// fill download stack
								downloadStack = new ArrayList<DataSource>();
								try {
									CSVReader reader = new CSVReader(new FileReader(FileUtilities.checkDirectoryEnding(textFieldDirectory.getText()).concat(PrepareDomain.DOWNLOAD_LINKS_FILE)));
									List<String[]> data = reader.readAll();
									for (String[] newLine : data) {
										DataSource ds = new DataSource(newLine[0]); // index 0 is the link
										ds.setConference(newLine[1]); // index 1 is the conference
										ds.setYear(newLine[2]); // index 2 is the year
										downloadStack.add(ds);
									}										
								} catch (FileNotFoundException e) {
									JOptionPane.showMessageDialog(getParent(), "The csv-file for download-links does not exist.\n Please execute preparation first.");
								} catch (IOException e) {
									e.printStackTrace();
								}



								// execute download stack
								if (downloadStack.size() > 0) {

									// just for a download queue
									int counter = 0;
									for (DataSource ds : downloadStack) {
										
										httpCrawler = new HttpCrawler();
										httpCrawler.addCustomEventListener(new CustomEventListener() {
											@Override
											public void customEventOccurred(CustomEvent evt) {
												appendMessage(evt.getMessage());
											}}
										);
										
										// post it to the GUI
										appendMessage(ds.getLink());				            			
										appendMessage("Starting crawling... ".concat(Integer.toString(++counter)).concat(" of ").concat(Integer.toString(downloadStack.size())));

										// download all data
										httpCrawler.setLowestId((Integer) spinnerLowestDownloadId.getValue());
										httpCrawler.setHighestId((Integer) spinnerHighestDownloadId.getValue());
										httpCrawler.getAllPdfLinks(ds, FileUtilities.checkDirectoryEnding(textFieldDirectory.getText()).concat(PrepareDomain.PDF_DOWNLOAD_FOLDER));
									}			            			
									appendMessage("finished :-)");			            		

								}


							} finally {

							}
						}
					};
					threadDownload.start();
				} else {
					JOptionPane.showMessageDialog(getParent(), "The csv-file for download-links does not exist.\n Please execute preparation first.");
				}
			}
			
		});
		
		// cancel download-button
		JButton buttonCancelDownload = new JButton("cancel");
		buttonCancelDownload.addActionListener(new ActionListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void actionPerformed(ActionEvent arg0) {
				threadDownload.stop();
			}
		});
		
		// button normalize
		JButton buttonNormalize = new JButton("normalize");
		buttonNormalize.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				appendMessage(LINE_SEPARATOR);
				Normalize norm = new Normalize();				
				norm.addCustomEventListener(new CustomEventListener() {
					@Override
					public void customEventOccurred(CustomEvent evt) {
						appendMessage(evt.getMessage());
					}}
				);
				
				try {
					norm.normalizeDownloadedData(textFieldDirectory.getText());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		});
		
		// JPanel - 1st row
		JPanel panelDownloadProcess = new JPanel();
		panelDownloadProcess.add(panelDownloadIds);
		panelDownloadProcess.add(buttonEditLinks);
		panelDownloadProcess.add(buttonDownload);
		panelDownloadProcess.add(buttonCancelDownload);
		
		// JPanel - 2nd row
		JPanel panelNormalize = new JPanel();
		panelNormalize.add(buttonNormalize);
		
		// download row
		JPanel panelDownload = new JPanel();	
		panelDownload.setName("Download");
		panelDownload.setLayout(new BoxLayout(panelDownload, BoxLayout.Y_AXIS));
		panelDownload.add(panelDownloadProcess);
		panelDownload.add(panelNormalize);
		
		
		
		return panelDownload;
    }
    
    private JPanel createConvertPanel() {
    	
    	// convert label
		JLabel labelConvert = new JLabel("Convert:");
		JLabel labelConsolidate = new JLabel("Consolidate:");
		
		// convert-button
		JButton buttonConvert = new JButton("Convert PDF to TXT");
		buttonConvert.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				appendMessage(LINE_SEPARATOR);
				threadConvert = new Thread(){
		            public void run() {
		            	try {
		            		appendMessage("Starting conversion...");
		            		txtConverter.convertPdf2TxtInAFolder(textFieldDirectory.getText());
		                } finally {
		                  
		                }
		            }
			 };
			 threadConvert.start();
				
			}
		});
				
		// cancel convert button		
		JButton buttonCancelConvert = new JButton("cancel");
		buttonCancelConvert.addActionListener(new ActionListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void actionPerformed(ActionEvent arg0) {
				threadConvert.stop();
			}
		});
		
		// import-button
		JLabel labelImport = new JLabel("Import:");
		JButton buttonImport = new JButton("Import txt-files");
		buttonImport.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				appendMessage(LINE_SEPARATOR);
				// Klasse ImportTXT bereits angelegt
				ImportTxt it = new ImportTxt();
				it.addCustomEventListener(new CustomEventListener() {
					@Override
					public void customEventOccurred(CustomEvent evt) {
						appendMessage(evt.getMessage());
					}}
				);
				try {
					it.normalizeTextData(textFieldDirectory.getText());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		// set language for consolidation
		comboBoxLanguage = new JComboBox(new KeyValueMutableModel());
 
        //Language-Objekte werden in die JComboBox eingetragen
		KeyValue english = new KeyValue("en", "English");
		comboBoxLanguage.addItem(english);
		comboBoxLanguage.addItem(new KeyValue("de", "German"));
		comboBoxLanguage.addItem(new KeyValue("nl", "Dutch"));		
		comboBoxLanguage.addItem(new KeyValue("da", "Danish"));
		comboBoxLanguage.addItem(new KeyValue("es", "Spanish"));
		comboBoxLanguage.addItem(new KeyValue("sv", "Swedish"));
		comboBoxLanguage.addItem(new KeyValue("it", "Italian"));
		comboBoxLanguage.addItem(new KeyValue("fr", "French"));
		comboBoxLanguage.addItem(new KeyValue("pt", "Portuguese"));
		comboBoxLanguage.addItem(new KeyValue("fi", "Finnish"));
		comboBoxLanguage.addItem(new KeyValue("pl", "Polish"));
		comboBoxLanguage.addItem(new KeyValue("et", "Estonian"));
		comboBoxLanguage.addItem(new KeyValue("sk", "Slovak"));
		comboBoxLanguage.addItem(new KeyValue("cs", "Czech"));
		comboBoxLanguage.addItem(new KeyValue("hu", "Hungarian"));
		comboBoxLanguage.addItem(new KeyValue("sl", "Slovene"));
		comboBoxLanguage.addItem(new KeyValue("lt", "Lithuanian"));
		comboBoxLanguage.addItem(new KeyValue("lv", "Latvian"));
		comboBoxLanguage.addItem(new KeyValue("ro", "Romanian"));
		comboBoxLanguage.addItem(new KeyValue("el", "Greek"));
		comboBoxLanguage.addItem(new KeyValue("bg", "Bulgarian"));
		comboBoxLanguage.setSelectedItem(english.getKeyValue());
		
		// consolidate-button
		JButton buttonConsolidate = new JButton("Convert TXTs in folder to CSV");
		buttonConsolidate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				appendMessage(LINE_SEPARATOR);
				threadConsolidate = new Thread(){
		            public void run() {
		            	try {
		            		appendMessage("Starting consolidation...");
		            		converter.convertTxt2CsvInAFolder(textFieldDirectory.getText(), KeyValue.getKey((String)comboBoxLanguage.getSelectedItem()));
		                } finally {
		                  
		                }
		            }
			 };
			 threadConsolidate.start();
				
			}
		});
		
		// cancel consolidate button
		JButton buttonCancelConsolidate = new JButton("cancel");
		buttonCancelConsolidate.addActionListener(new ActionListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void actionPerformed(ActionEvent arg0) {
				threadConsolidate.stop(); 
			}
		});
		
		// Panel for the conversion
		JPanel panelConvert = new JPanel();
		panelConvert.add(labelConvert);
		panelConvert.add(buttonConvert);
		panelConvert.add(buttonCancelConvert);
		
		// Panel for import		
		JPanel panelImport = new JPanel();
		panelImport.add(labelImport);
		panelImport.add(buttonImport);
		
		// Panel for the consolidation		
		JPanel panelConsolidate = new JPanel();
		panelConsolidate.add(labelConsolidate);
		panelConsolidate.add(comboBoxLanguage);
		panelConsolidate.add(buttonConsolidate);
		panelConsolidate.add(buttonCancelConsolidate);
		
		// Panel for this tab
		JPanel panelConvertAll = new JPanel();
		panelConvertAll.setName("Convert");
		panelConvertAll.setLayout(new BoxLayout(panelConvertAll, BoxLayout.Y_AXIS));
		panelConvertAll.add(panelConvert);
		panelConvertAll.add(panelImport);
		panelConvertAll.add(panelConsolidate);
		
		return panelConvertAll;
    	
    }
	
    
    /**
     * creates the panel for setting and starting up the LDA-process
     * @return JPanel with settings and buttons for the LDA-process
     */
    private JPanel createLdaLearnPanel() {
    	
    	// Label for LDA
		JLabel labelLda = new JLabel("Learn LDA:");
		
	    // Button for LDA
	    JButton startLda = new JButton("learn LDA");
	    startLda.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				threadLdaLearn = new Thread(){
		            public void run() {
		            	try {
		            		appendMessage("Starting lda...");
		            		
		            		ExecuteLda.ldaLearnModelOnData(FileUtilities.checkDirectoryEnding(chosenLdaData),
		            				ldaLearnOptions.getButtonGroupLdaAlgoGroup().getSelection().getActionCommand(),
		            				(Integer)ldaLearnOptions.getSpinnerNumberOfTopics().getValue(),
		            				(Double)ldaLearnOptions.getSpinnerTopicSmoothing().getValue(),
		            				(Double)ldaLearnOptions.getSpinnerTermSmoothing().getValue(),
		            				Integer.valueOf(ldaLearnOptions.getTextFieldDataColumn().getText()),
		            				(Integer)ldaLearnOptions.getSpinnerNumberOfIterations().getValue(),
		            				createStopwordList(),
		            				useEnglishStopwordList());

		            		appendMessage("done.");
		                } finally {
		                  
		                }
		            }
			 };
			 threadLdaLearn.start();
				
			}
		});
	    
	    // Cancel-Button for LDA
	    JButton cancelLda = new JButton("cancel");
	    cancelLda.addActionListener(new ActionListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void actionPerformed(ActionEvent arg0) {
				threadLdaLearn.stop(); 
			}
		});  
	    
	    JButton buttonLdaOptions = new JButton("Options");
	    
	    buttonLdaOptions.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				// disable unused parameters
//				ldaLearnOptions.getBayesButton().setEnabled(false);
//				ldaLearnOptions.getGibbsButton().setEnabled(false);
				ldaLearnOptions.getButtonSelectSliceColumns().setEnabled(false);
//				ldaLearnOptions.getButtonSelectDataColumn().setEnabled(false);
//				ldaLearnOptions.getSpinnerTopicSmoothing().setEnabled(false);
//				ldaLearnOptions.getSpinnerTermSmoothing().setEnabled(false);
//				ldaLearnOptions.getSpinnerNumberOfTopics().setEnabled(false);
//				ldaLearnOptions.getSpinnerNumberOfIterations().setEnabled(false);
				ldaLearnOptions.getSpinnerTrainingData().setEnabled(false);
				ldaLearnOptions.getTextFieldSliceColumns().setEnabled(false);
//				ldaLearnOptions.getTextFieldDataColumn().setEnabled(false);
				
				ldaLearnOptions.setVisible(true);
				if (textFieldDirectory.getText() != null) {
					ldaLearnOptions.setFilepath(FileUtilities.checkDirectoryEnding(chosenLdaData).concat(MyFrame.DATAFILE));
				}
				ldaLearnOptions.getButtonOk().addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {						
						ldaLearnOptions.setVisible(false);
					}});
				
			}});
	    
	    // Panel for LDA
		JPanel ldaPanel = new JPanel();		
		ldaPanel.setName("LDA - learn");
		ldaPanel.add(labelLda);
		ldaPanel.add(buttonLdaOptions);
		ldaPanel.add(startLda);
		ldaPanel.add(cancelLda);
		
		return ldaPanel;
    	
    }
    

    
    /**
     * creates the panel for setting and starting up the LDA-process
     * @return JPanel with settings and buttons for the LDA-process
     */
    private JPanel createLdaExecutePanel() {
    	
    	// Label for LDA
		JLabel labelLda = new JLabel("Execute LDA:");
		
		textFieldModelDirectoryExecute = new JTextField();
		JPanel chooseDirectoryPanel = createChooseDirectoryPanel(textFieldModelDirectoryExecute, "Model directory:");
		
	    // Button for LDA
	    JButton startLda = new JButton("learn LDA");
	    startLda.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				threadLdaExecute = new Thread(){
		            public void run() {
		            	try {
		            		appendMessage("Starting lda...");
		            		
		            		ExtractOptions ea = new ExtractOptions(FileUtilities.checkDirectoryEnding(textFieldModelDirectoryExecute.getText()));
		            		String algo = ea.getLdaAlgorithm();
		            		
		            		ExecuteLda.ldaExecuteModelOnData(FileUtilities.checkDirectoryEnding(chosenLdaData),
		            				algo,
		            				Integer.valueOf(ldaLearnOptions.getTextFieldDataColumn().getText()),
		            				FileUtilities.checkDirectoryEnding(textFieldModelDirectoryExecute.getText()),
		            				createStopwordList(),
		            				useEnglishStopwordList());
		            		
		            		appendMessage("done.");
		                } finally {
		                  
		                }
		            }
			 };
			 threadLdaExecute.start();
				
			}
		});
	    
	    // Cancel-Button for LDA
	    JButton cancelLda = new JButton("cancel");
	    cancelLda.addActionListener(new ActionListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void actionPerformed(ActionEvent arg0) {
				threadLdaExecute.stop(); 
			}
		});  
	    
	    JButton buttonLdaOptions = new JButton("Options");
	    
	    buttonLdaOptions.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				// set the algo in options panel
				ExtractOptions ea = new ExtractOptions(FileUtilities.checkDirectoryEnding(textFieldModelDirectoryExecute.getText()));
        		String algo = ea.getLdaAlgorithm();
        		if (algo == LdaOptions.LDA_BAYES) {
        			ldaLearnOptions.getBayesButton().setSelected(true);
        		} else if (algo == LdaOptions.LDA_GIBBS) {
        			ldaLearnOptions.getGibbsButton().setSelected(true);
        		}
        		        		
				// disable unused parameters
				ldaLearnOptions.getBayesButton().setEnabled(false);
				ldaLearnOptions.getGibbsButton().setEnabled(false);
				ldaLearnOptions.getButtonSelectSliceColumns().setEnabled(false);
//				ldaLearnOptions.getButtonSelectDataColumn().setEnabled(false);
				ldaLearnOptions.getSpinnerTopicSmoothing().setEnabled(false);
				ldaLearnOptions.getSpinnerTermSmoothing().setEnabled(false);
				ldaLearnOptions.getSpinnerNumberOfTopics().setEnabled(false);
				ldaLearnOptions.getSpinnerNumberOfIterations().setEnabled(false);
				ldaLearnOptions.getSpinnerTrainingData().setEnabled(false);
				ldaLearnOptions.getTextFieldSliceColumns().setEnabled(false);
//				ldaLearnOptions.getTextFieldDataColumn().setEnabled(false);
				
				ldaLearnOptions.setVisible(true);
				if (textFieldDirectory.getText() != null) {
					ldaLearnOptions.setFilepath(FileUtilities.checkDirectoryEnding(chosenLdaData).concat(MyFrame.DATAFILE));
				}
				ldaLearnOptions.getButtonOk().addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {						
						ldaLearnOptions.setVisible(false);
					}});
				
			}});
	    
	    // Panel top
	    
	    
	    // Panel bottom
	    JPanel ldaPanelBottom = new JPanel();
	    ldaPanelBottom.add(labelLda);				
	    ldaPanelBottom.add(buttonLdaOptions);
	    ldaPanelBottom.add(startLda);
	    ldaPanelBottom.add(cancelLda);
	    
	    // Panel for LDA
		JPanel ldaPanel = new JPanel();
		ldaPanel.setLayout(new BoxLayout(ldaPanel, BoxLayout.Y_AXIS));
		ldaPanel.setName("LDA - execute");
		ldaPanel.add(chooseDirectoryPanel);
		ldaPanel.add(ldaPanelBottom);
		
		return ldaPanel;
    	
    }
    
    /**
     * creates the panel for setting and starting up the LDA-process
     * @return JPanel with settings and buttons for the LDA-process
     */
    private JPanel createLdaSlicePanel() {
    	    	
    	textFieldModelDirectorySlice = new JTextField();
		JPanel chooseDirectoryPanel = createChooseDirectoryPanel(textFieldModelDirectorySlice, "Model directory:");
    	
    	// Label for LDA
		JLabel labelLda = new JLabel("Execute LDA:");
				
		// Button to choose model
		JButton buttonChooseLdaModel = new JButton("choose lda model");
		buttonChooseLdaModel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if (textFieldDirectory.getText() != null) {
			        
					String ldaFolder = FileUtilities.checkDirectoryEnding(chosenLdaData);
					List<String> dataFolder = FileUtilities.getAllFoldersInFolder(ldaFolder);					
					ldaChooseData = new LdaSelect(dataFolder);				
					
					if (dataFolder.size() > 1) {
						ldaChooseData.setVisible(true);
					} else if (dataFolder.size() == 1) {
						chosenLdaModel = dataFolder.get(0);
					}
					
					
				}
				ldaChooseData.getButtonOk().addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {						
						ldaChooseData.setVisible(false);
						chosenLdaModel = ldaChooseData.getSelectedFolder();
					}});
				
			}});
		
	    // Button for LDA
	    JButton startLda = new JButton("learn LDA");
	    startLda.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				threadLdaSlice = new Thread(){
		            public void run() {
		            	try {
		            		appendMessage("Starting lda...");
		            		
		            		ExtractOptions ea = new ExtractOptions(FileUtilities.checkDirectoryEnding(textFieldModelDirectorySlice.getText()));
		            		String algo = ea.getLdaAlgorithm();
		            		
		            		FileUtilities fu = new FileUtilities();
		            		java.util.List<String> dtdFiles = fu.searchForFiles(textFieldDirectory.getText(), "document-topic-distributions.csv");
		            		if (dtdFiles.size() > 0) {
		            			
		            			ExecuteLda.sliceData(FileUtilities.checkDirectoryEnding(chosenLdaData),
			            				Integer.valueOf(ldaLearnOptions.getTextFieldDataColumn().getText()),
			            				Integer.valueOf(ldaLearnOptions.getTextFieldSliceColumns().getText()),
			            				createStopwordList(),
			            				FileUtilities.checkDirectoryEnding(textFieldModelDirectorySlice.getText()),
			            				algo,
			            				useEnglishStopwordList());
		            		}
		            		
		            		appendMessage("done.");
		                } finally {
		                  
		                }
		            }
			 };
			 threadLdaSlice.start();
				
			}
		});
	    
	    // Cancel-Button for LDA
	    JButton cancelLda = new JButton("cancel");
	    cancelLda.addActionListener(new ActionListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void actionPerformed(ActionEvent arg0) {
				threadLdaSlice.stop(); 
			}
		});  
	    
	    JButton buttonLdaOptions = new JButton("Options");
	    
	    buttonLdaOptions.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				// set the algo in options panel
				ExtractOptions ea = new ExtractOptions(FileUtilities.checkDirectoryEnding(textFieldModelDirectorySlice.getText()));
        		String algo = ea.getLdaAlgorithm();
        		if (algo == LdaOptions.LDA_BAYES) {
        			ldaLearnOptions.getBayesButton().setSelected(true);
        		} else {
        			ldaLearnOptions.getGibbsButton().setSelected(true);
        		}
				
				// disable unused parameters
				ldaLearnOptions.getBayesButton().setEnabled(false);
				ldaLearnOptions.getGibbsButton().setEnabled(false);
//				ldaLearnOptions.getButtonSelectSliceColumns().setEnabled(false);
//				ldaLearnOptions.getButtonSelectDataColumn().setEnabled(false);
				ldaLearnOptions.getSpinnerTopicSmoothing().setEnabled(false);
				ldaLearnOptions.getSpinnerTermSmoothing().setEnabled(false);
				ldaLearnOptions.getSpinnerNumberOfTopics().setEnabled(false);
				ldaLearnOptions.getSpinnerNumberOfIterations().setEnabled(false);
				ldaLearnOptions.getSpinnerTrainingData().setEnabled(false);
//				ldaLearnOptions.getTextFieldSliceColumns().setEnabled(false);
//				ldaLearnOptions.getTextFieldDataColumn().setEnabled(false);
				
				ldaLearnOptions.setVisible(true);
				if (textFieldDirectory.getText() != null) {
					ldaLearnOptions.setFilepath(FileUtilities.checkDirectoryEnding(chosenLdaData).concat(MyFrame.DATAFILE));
				}
				ldaLearnOptions.getButtonOk().addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {						
						ldaLearnOptions.setVisible(false);
					}});
				
			}});
	    
	    // Panel bottom
	    JPanel ldaPanelBottom = new JPanel();
	    ldaPanelBottom.add(labelLda);				
	    ldaPanelBottom.add(buttonLdaOptions);
	    ldaPanelBottom.add(startLda);
	    ldaPanelBottom.add(cancelLda);
	    
	    // Panel for LDA
		JPanel ldaPanel = new JPanel();
		ldaPanel.setLayout(new BoxLayout(ldaPanel, BoxLayout.Y_AXIS));
		ldaPanel.setName("LDA - slice");
		ldaPanel.add(chooseDirectoryPanel);
		ldaPanel.add(ldaPanelBottom);
		
		return ldaPanel;

    	
    }
    
    private JPanel createLdaPanel() {
    	
		// Select the Lda-Data
		JButton buttonChooseLdaData = new JButton("choose data");
		buttonChooseLdaData.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if (textFieldDirectory.getText() != null) {
			        
					String ldaFolder = FileUtilities.checkDirectoryEnding(textFieldDirectory.getText()).concat(PrepareDomain.LDA_FOLDER);
					List<String> dataFolder = FileUtilities.getAllFoldersInFolder(ldaFolder);
					ldaChooseData = new LdaSelect(dataFolder);
					
					if (dataFolder.size() > 1) {
						ldaChooseData.setVisible(true);
					} else if (dataFolder.size() == 1) {
						chosenLdaData = dataFolder.get(0);
					}
					
				}
				ldaChooseData.getButtonOk().addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {						
						ldaChooseData.setVisible(false);
						chosenLdaData = ldaChooseData.getSelectedFolder();
						
						String ldaFolder = FileUtilities.checkDirectoryEnding(chosenLdaData);
						List<String> dataFolder = FileUtilities.getAllFoldersInFolder(ldaFolder);
						String modelPath = "";
						if (dataFolder.size() == 1) {
							modelPath = dataFolder.get(0);
						}
																		
						textFieldModelDirectoryExecute.setText(FileUtilities.checkDirectoryEnding(modelPath));
						textFieldModelDirectorySlice.setText(FileUtilities.checkDirectoryEnding(modelPath));
					}});
				
			}});
    	
		// decide to use the TMT English Stopwords
		useEnglishStopwords = new JCheckBox("Use english stopword-filter", true);
		
    	// create top panel
		JPanel ldaSettingsPanel = new JPanel();
		ldaSettingsPanel.add(useEnglishStopwords);
		ldaSettingsPanel.add(buttonChooseLdaData);
		
		// create bottom panels
    	JPanel ldaLearnPanel = createLdaLearnPanel();
		JPanel ldaExecutePanel = createLdaExecutePanel();
		JPanel ldaCalculatePanel = createLdaCalculatePanel();
		JPanel ldaSlicePanel = createLdaSlicePanel();
		
		// create and add tabs for lda	    
		JTabbedPane tabbedLdaProcesses = new JTabbedPane();
	    tabbedLdaProcesses.add(ldaCalculatePanel.getName(), ldaCalculatePanel);
	    tabbedLdaProcesses.add(ldaLearnPanel.getName(), ldaLearnPanel);
	    tabbedLdaProcesses.add(ldaExecutePanel.getName(), ldaExecutePanel);
	    tabbedLdaProcesses.add(ldaSlicePanel.getName(), ldaSlicePanel);
	    JPanel panelLdaProcesses = new JPanel();
	    panelLdaProcesses.setName("LDA");
	    panelLdaProcesses.setLayout(new BoxLayout(panelLdaProcesses, BoxLayout.Y_AXIS));
	    panelLdaProcesses.add(ldaSettingsPanel);
	    panelLdaProcesses.add(tabbedLdaProcesses);
	    
	    return panelLdaProcesses;
    }
    
    
    
    private String useEnglishStopwordList() {
    	return useEnglishStopwords.isSelected()?"en":"";
    }
    
    /**
     * creates the panel for setting and starting up the LDA-process
     * @return JPanel with settings and buttons for the LDA-process
     */
    private JPanel createLdaCalculatePanel() {
    	
    	// Label for LDA
		JLabel labelLda = new JLabel("Calculate LDA:");
		
		// Textfield for the numbers of topics to calculate
		textFieldNumberOfTopicsToTest = new JTextField();
		textFieldNumberOfTopicsToTest.setColumns(10);
	
		
	    // Button for LDA
	    JButton startLda = new JButton("learn LDA");
	    startLda.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				threadLdaCalculate = new Thread(){
		            public void run() {
		            	try {
		            		appendMessage("Starting lda...");
   		
		            		String s = textFieldNumberOfTopicsToTest.getText();
		            		String[] a = s.split(",");
		            		int[] ia = new int[a.length];
		            		for (int i = 0; i < a.length; i++) {
		            			ia[i] = Integer.decode(a[i].trim());
		            		}
		            		ExecuteLda.calculateNumberOfTopics(FileUtilities.checkDirectoryEnding(chosenLdaData),
		            				ia,
		            				Integer.valueOf(ldaLearnOptions.getTextFieldDataColumn().getText()),
		            				(Integer)ldaLearnOptions.getSpinnerNumberOfIterations().getValue(),
		            				(Double)ldaLearnOptions.getSpinnerTrainingData().getValue(),
		            				(Double)ldaLearnOptions.getSpinnerTopicSmoothing().getValue(),
		            				(Double)ldaLearnOptions.getSpinnerTermSmoothing().getValue(),
		            				ldaLearnOptions.getButtonGroupLdaAlgoGroup().getSelection().getActionCommand(),
		            				createStopwordList(),
		            				useEnglishStopwordList()
		            				);
		            		appendMessage("done.");
		            		
		                } finally {
		                  
		                }
		            }
			 };
			 threadLdaCalculate.start();
				
			}
		});
	    
	    // Cancel-Button for LDA
	    JButton cancelLda = new JButton("cancel");
	    cancelLda.addActionListener(new ActionListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void actionPerformed(ActionEvent arg0) {
				threadLdaCalculate.stop(); 
			}
		});  
	    
	    JButton buttonLdaOptions = new JButton("Options");
	    buttonLdaOptions.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				// disable unused parameters
//				ldaLearnOptions.getBayesButton().setEnabled(false);
//				ldaLearnOptions.getGibbsButton().setEnabled(false);
				ldaLearnOptions.getButtonSelectSliceColumns().setEnabled(false);
//				ldaLearnOptions.getButtonSelectDataColumn().setEnabled(false);
//				ldaLearnOptions.getSpinnerTopicSmoothing().setEnabled(false);
//				ldaLearnOptions.getSpinnerTermSmoothing().setEnabled(false);
				ldaLearnOptions.getSpinnerNumberOfTopics().setEnabled(false);
//				ldaLearnOptions.getSpinnerNumberOfIterations().setEnabled(false);
//				ldaLearnOptions.getSpinnerTrainingData().setEnabled(false);
				ldaLearnOptions.getTextFieldSliceColumns().setEnabled(false);
//				ldaLearnOptions.getTextFieldDataColumn().setEnabled(false);
								
				ldaLearnOptions.setVisible(true);
				if (textFieldDirectory.getText() != null) {
					ldaLearnOptions.setFilepath(FileUtilities.checkDirectoryEnding(chosenLdaData).concat(MyFrame.DATAFILE));
				}
				ldaLearnOptions.getButtonOk().addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {						
						ldaLearnOptions.setVisible(false);
					}});
				
			}});
	    
	    // Panel for LDA
		JPanel ldaPanel = new JPanel();		
		ldaPanel.setName("LDA - calculate");
		ldaPanel.add(labelLda);
		ldaPanel.add(textFieldNumberOfTopicsToTest);
		ldaPanel.add(buttonLdaOptions);
		ldaPanel.add(startLda);
		ldaPanel.add(cancelLda);
		
		return ldaPanel;
    	
    }

    
    
	private void appendMessage(String s) {
		textAreaBottomOutput.append("\n".concat(DateTimeUtilities.getCurrentDateTime()).concat(" - ").concat(s));
		
		if (checkBoxMenuItemAutoScroll.isSelected())
			textAreaBottomOutput.setCaretPosition(textAreaBottomOutput.getText().length());
		
	}
	
	private java.util.List<String> createStopwordList() {
		
		String filepath = FileUtilities.checkDirectoryEnding(textFieldDirectory.getText()).concat(PrepareDomain.STOPWORDS_FILE);
		
		String content = null;
		try {
			content = FileUtilities.readFile(filepath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			;
		} catch (IOException e) {
			e.printStackTrace();
			;
		}
		String[] lines = content.split(System.getProperty("line.separator"));
		
		java.util.List<String> l = new ArrayList<String>();
		for (int i = 0; i < l.size(); i++) {
			l.add(lines[i]);
		}
		
		return l;
	}
	
//		java.util.List<String> l = new ArrayList<String>();
//		
//		// stopwords for step 1
//		for (int i = 1900; i <= 2012; i++) {
//			l.add(Integer.toString(i));
//		}
//		for (double i = 0.00; i <= 1.00; i += 0.01) {
//			l.add(Double.toString(i));
//		}		
//		l.add("the");
//		l.add("and");
//		l.add("for");
//		l.add("that");
//		l.add("thi");
//		l.add("with");
//		l.add("from");
//		l.add("studi");
//		l.add("have");
//		l.add("not");
//		l.add("can");
//		l.add("their");
//		l.add("which");
//		l.add("more");
//		l.add("these");
//		l.add("other");
//		l.add("were");
//		l.add("new");
//		l.add("thei");
//		l.add("will");
//		l.add("between");
//		l.add("effect");
//		l.add("result");
//		l.add("also");
//		l.add("such");
//		l.add("confer");
//		l.add("our");
//		l.add("level");
//		l.add("group");
//		l.add("all");
//		l.add("mai");
//		l.add("been");
//		l.add("journal");
//		l.add("base");
//		l.add("proceed");
//		l.add("how");
//		l.add("two");
//		l.add("there");
//		l.add("each");
//		l.add("than");
//		l.add("but");
//		l.add("when");
//		l.add("e.g.");
//		
//		// stopwords for step 2
//		l.add("australia");
//		l.add("australian");
//		l.add("south");
//		l.add("pacif");
//		l.add("asia");
//		l.add("0");
//		l.add("0,001");
//		l.add("tabl");
//		l.add("1");
//		l.add("signific");
//		l.add("0,002");
//		l.add("0,004");
//		l.add("0,003");
//		l.add("0,005");
//		l.add("coeffici");
//		l.add("tion");
//		l.add("infonn");
//		l.add("ing");
//		l.add("arc");
//		l.add("ment");
//		l.add("tile");
//		l.add("pro");
//		l.add("e.g");
////		l.add("1991");
////		l.add("1990");
////		l.add("1992");
////		l.add("1988");
//		l.add("0");
//		l.add("tabl");
//		l.add("correl");
//		l.add("format");
//		l.add("delet");
//		l.add("sig");
//		l.add("analysi");
//		l.add("1");
//		l.add("page");
//		l.add("variabl");
//		l.add("san");
//		l.add("francisco");
//		l.add("california");
//		l.add("fifteenth");
//		l.add("6th-9th");
//		l.add("âˆš");
//		l.add("€™t");
//		l.add(":1),");
//		l.add(":2),");
//		l.add(":3),");
//		l.add(":1)");
//		l.add(":2)");
//		l.add(":3)");
//		l.add("0");
//		l.add("0,01");
//		l.add("0,05");
//		l.add("0,01");
//		l.add("0,05");
//		l.add("1");
//		l.add("0,02");
//		l.add("0,03");
//		l.add("0,04");
//		l.add("michigan");
//		l.add("seventeenth");
//		l.add("detroit");
//		l.add("4th-7th");
//		l.add("august");
//		l.add("matur");
//		l.add("mexico");
//		l.add("peru");
//		l.add("sixteenth");
//		l.add("lima");
//		l.add("america");
//		l.add("inform");
//		l.add("amci");
//		l.add("august");
//		l.add("electron");
//		l.add("paci");
//		l.add("china");
//		l.add("aisel");
//		l.add("chines");
//		l.add("asia");
//		l.add("usersâ");
//		l.add("userâ");
//		l.add("poland");
//		l.add("june");
//
//		
//		return l;
	
	
	
}
