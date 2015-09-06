package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LdaSelect extends JFrame {


	/**
	 * 
	 */
	private static final long serialVersionUID = 8251118356755459536L;
	private JComboBox comboBoxDataSelection;

	/**
	 * @return the comboBoxDataSelection
	 */
	public JComboBox getComboBoxDataSelection() {
		return comboBoxDataSelection;
	}

	/**
	 * @param comboBoxDataSelection the comboBoxDataSelection to set
	 */
	public void setComboBoxDataSelection(JComboBox comboBoxDataSelection) {
		this.comboBoxDataSelection = comboBoxDataSelection;
	}

	private JButton buttonOk;
	
	/**
	 * @return the buttonOk
	 */
	public JButton getButtonOk() {
		return buttonOk;
	}
	
	private String[] folders;	
	private List<String> foldersList;
	
	private void setFolders(List<String> folders) {
		String[] data = new String[folders.size()];
		for (int i = 0; i < folders.size(); i++) {
			data[i] = new File(folders.get(i)).getName();
		}
		this.folders = data;
	}
	
	public String getSelectedFolder() {
		int i = comboBoxDataSelection.getSelectedIndex();
		return foldersList.get(i);
	}
	
	public LdaSelect(List<String> foldersList) {
		
		// title for the frame
		super("Choose LDA data");
		
		// close the thread, if the JFrame closes
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		

	    
	    /*
	     * SETTINGS
	     */
	    
	    // datacolumn panel
		this.foldersList = foldersList;
		setFolders(this.foldersList);
		this.comboBoxDataSelection = new JComboBox( this.folders );
		comboBoxDataSelection.setEditable( false );

		
	    // settings panel
	    JLabel labelDataColumn = new JLabel("Data");
	    JPanel panelSettings = new JPanel();
	    panelSettings.add(labelDataColumn);
	    panelSettings.add(comboBoxDataSelection);
	    
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
		
	    // main panel
	    JPanel panelMain = new JPanel();
	    panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.Y_AXIS));

	    panelMain.add(panelSettings);
	    panelMain.add(panelButtonOkAndCancel);
//	    panelMain.add();
	    
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
