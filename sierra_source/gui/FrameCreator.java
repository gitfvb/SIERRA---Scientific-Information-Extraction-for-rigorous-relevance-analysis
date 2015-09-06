package gui;


public class FrameCreator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		MacOSXAdapter.initialize("SIERRA");
		new MyFrame();
		
		
	}

}
