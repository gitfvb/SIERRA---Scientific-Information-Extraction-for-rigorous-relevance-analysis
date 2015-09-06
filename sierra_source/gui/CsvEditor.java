package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;


public class CsvEditor extends JFrame {

/**
	 * 
	 */
	private static final long serialVersionUID = -1379870556642145784L;
	private JTable tableData;
	private String filepath;
	private String[] columnNames = new String[0];
	private JScrollPane scrollPaneDataTable;
	
	
	/**
	 * @return the columnNames
	 */
	public String[] getColumnNames() {
		return columnNames;
	}

	/**
	 * @param columnNames the columnNames to set
	 */
	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;		
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
	}

	/**
	 * @return the tableData
	 */
	public JTable getTableData() {
		return tableData;
	}


	private JButton buttonOk;	
	/**
	 * @return the buttonOk
	 */
	public JButton getButtonOk() {
		return buttonOk;
	}

	
	public CsvEditor(String filepath) {
		
		// title for the frame
		super("Edit links");
		
		setFilepath(filepath);
		
	}
	
	public void loadData() {
		/*
		 *  Panel for the ok and cancel button
		 */
		buttonOk = new JButton("OK");
		buttonOk.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveLines(getFilepath());
				setVisible(false);
			}
		});
		JButton buttonCancel = new JButton("Cancel");
		buttonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);				
			}});
		JButton buttonNewLine = new JButton("new line");
		buttonNewLine.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				DefaultTableModel model= (DefaultTableModel) getTableData().getModel();
				model.addRow(new String[getTableData().getModel().getColumnCount()]);			
			}
		});
		JPanel panelButtonOkAndCancel = new JPanel();
		panelButtonOkAndCancel.add(buttonNewLine);
		panelButtonOkAndCancel.add(buttonOk);
		panelButtonOkAndCancel.add(buttonCancel);
		
		/*
		 *  The top panel
		 */
		JPanel panelTop = new JPanel();
		panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.Y_AXIS));
		panelTop.add(panelButtonOkAndCancel);
		
		// panel for the table data
		tableData = createJTableOfData(filepath);
		scrollPaneDataTable = new JScrollPane(tableData);
				
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,panelTop,scrollPaneDataTable);
		add(splitPane);		
				
		// set position
		setLocation(100,100);
		
		// set size
		pack(); // set size automatically
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
		
		Object[][] data = getLines(filepath);
		
		String[] columnNames = null;
		if (getColumnNames().length == data[0].length) {					
			 columnNames = getColumnNames();
		} else {
			if (data != null) {
				columnNames = new String[data[0].length];
				for (int i = 0; i < data[0].length; i++) {
					columnNames[i] = Integer.toString(i+1);
				}				 
			}
		}
		
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		JTable table = new JTable();
		table.setModel(model);
		
		return table;
	}
	
	public String[][] getLines(String filepath) {

		String[][] tableData = null;
		
		
		try {
			CSVReader reader = new CSVReader(new FileReader(filepath));
			List<String[]> entries = reader.readAll();
			Iterator<String[]> it = entries.iterator();
			int counter = 0;
			
			String[] nextLine;
			while (it.hasNext()) {				
				nextLine = it.next();
				if (tableData == null) {
					tableData = new String[entries.size()][nextLine.length];
				}
				
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
	
	/**
	 * get the current data out of the data
	 * @return
	 */
	public List<String[]> getData() {
		List<String[]> data = new ArrayList<String[]>();
		for (int y = 0; y < getTableData().getModel().getRowCount(); y++) {
			String[] newLine = new String[getTableData().getModel().getColumnCount()];
			for (int x = 0; x < getTableData().getModel().getColumnCount(); x++) {
				Object o = getTableData().getModel().getValueAt(y, x);
				newLine[x] = (o != null) ? o.toString() : "";
//				System.out.println(getTableData().getModel().getValueAt(y, x).toString());
			}				 
			data.add(newLine);				
		}
		return data;
	}
	
	private void saveLines(String filepath) {
		try {
			CSVWriter writer = new CSVWriter(new FileWriter(filepath), ',');
			List<String[]> data = getData();
			for (String[] newLine : data) {
				writer.writeNext(newLine);
			}			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
