package gui;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


// Set the mac look and feel
// source: http://www.devdaily.com/apple/mac/java-mac-native-look/Putting_your_application_na.shtml
public class MacOSXAdapter {

	public static void initialize(String appName) {
		
		String lcOSName = System.getProperty("os.name").toLowerCase();
		boolean IS_MAC = lcOSName.startsWith("mac os x");
		if (IS_MAC) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
		}
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		
	}
	
}
