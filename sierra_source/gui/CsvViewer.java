package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;

import au.com.bytecode.opencsv.CSVReader;


public class CsvViewer extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5009610312723635272L;
	
	
	private int firstEntries = 10;
	private JTable tableData;
	private String filepath;
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
	}



	/**
	 * @return the tableData
	 */
	public JTable getTableData() {
		return tableData;
	}



//	private JTextField textFieldDataColumn;
	private JButton buttonOk;	
	/**
	 * @return the buttonOk
	 */
	public JButton getButtonOk() {
		return buttonOk;
	}
	
	public CsvViewer(String filepath) {
		
		// title for the frame
		super("Select Column");
		
		/*
		 *  Panel for the ok and cancel button
		 */
		buttonOk = new JButton("OK");
		JButton buttonCancel = new JButton("Cancel");
		buttonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);				
			}});
		JPanel panelButtonOkAndCancel = new JPanel();
		panelButtonOkAndCancel.add(buttonOk);
		panelButtonOkAndCancel.add(buttonCancel);
		
		/*
		 *  The top panel
		 */
		JPanel panelTop = new JPanel();
		panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.Y_AXIS));
//		panelTop.add(panelDataColumn);
//		panelTop.add(panelSliceColumns);
		panelTop.add(panelButtonOkAndCancel);
		
		// panel for the table data
		tableData = createJTableOfData(filepath);
		JScrollPane scrollPaneDataTable = new JScrollPane(tableData);
		
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,panelTop,scrollPaneDataTable);
		add(splitPane);		
				
		// set position
		setLocation(100,100);
		
		// set size
//		setSize(800,600);
		pack(); // set size automatically
		
		//Das JFrame anzeigen
//		setVisible(true);
	}
	
	
	public String getSelectedColumnsAsString() {
		int[] selectedColumns = tableData.getSelectedColumns();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < selectedColumns.length; i++) {
			sb.append(Integer.toString(selectedColumns[i]+1));
			if (i+1 != selectedColumns.length) {
				sb.append(",");
			}
		}
		return sb.toString();
	}
	
	public JTable createJTableOfData(String filepath) {

		Object[][] data = getFirstLines(filepath);
		
		String[] columnNames = null;		
		if (data != null) {
			columnNames = new String[data[0].length];
			for (int i = 0; i < data[0].length; i++) {
				columnNames[i] = Integer.toString(i+1);
			}
			 
		}
		
		JTable table = new JTable(data, columnNames);
		
		
		table.setColumnSelectionAllowed(true);
		table.setRowSelectionAllowed( false );
		
		return table;
	}
	
	public String[][] getFirstLines(String filepath) {

		String[][] tableData = null;
		
		
		try {
			CSVReader reader = new CSVReader(new FileReader(filepath));

			int counter = 0;
			
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null && counter < firstEntries) {				
				if (tableData == null) {
					tableData = new String[firstEntries][nextLine.length];
				}
				
				// nextLine[] is an array of values from the line
				for (int i = 0; i < nextLine.length; i++) {
					String s = nextLine[i];

					try {
						tableData[counter][i] = s;
					} catch (java.lang.ArrayIndexOutOfBoundsException e){
						;
					}

				}
				counter++;
			}

			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return tableData;		
	}
	
}
