package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class About extends JFrame {

	
/**
	 * 
	 */
	private static final long serialVersionUID = -6417054729553818690L;

public About() {
		
		// title for the frame
		super("About SIERRA");
		

	    /*
	     * SETTINGS
	     */
	    
		
	    // settings panel
	    JLabel labelDataColumn = new JLabel("2012 - written by Florian Friedrichs (friedrichsflorian@googlemail.com)");
	    JPanel panelSettings = new JPanel();
	    panelSettings.add(labelDataColumn);
	    
	    
		/*
		 *  Panel for the ok and cancel button
		 */
		JButton buttonCancel = new JButton("close");
		buttonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);				
			}});
		JPanel panelButtonOkAndCancel = new JPanel();
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
		setVisible(false);
	    
	}
	
	
}
