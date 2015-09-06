package gui;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;

public class FileDialogForDirectory {
	
	public static String chooseDirectoryDialog(Frame parentFrame, String title) {
		
		String folderName = "";

		// source: http://lists.apple.com/archives/java-dev/2010/May/msg00149.html
		String osName = System.getProperty("os.name");
		if( osName.equalsIgnoreCase("mac os x")) {
			FileDialog chooser = new FileDialog(parentFrame, title);
			System.setProperty( "apple.awt.fileDialogForDirectories", "true" );
			chooser.setVisible( true );

			System.setProperty( "apple.awt.fileDialogForDirectories", "false" );
			if( chooser.getDirectory() != null )
			{
				folderName += chooser.getDirectory();
				folderName += chooser.getFile();
			}
		} else {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Select Target Folder");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			int returnVal = chooser.showDialog(parentFrame, title);
			if( returnVal == JFileChooser.APPROVE_OPTION )
			{
				File userSelectedFolder = chooser.getSelectedFile();
				folderName += userSelectedFolder.getAbsolutePath();
			}
		}
		return folderName;
	}

}
