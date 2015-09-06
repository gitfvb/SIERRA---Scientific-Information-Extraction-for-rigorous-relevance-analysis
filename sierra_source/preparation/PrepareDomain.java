package preparation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.event.EventListenerList;

import events.CustomEvent;
import events.CustomEventInterface;
import events.CustomEventListener;

import utilities.FileUtilities;
import au.com.bytecode.opencsv.CSVWriter;

public class PrepareDomain implements CustomEventInterface {

	public static String STOPWORDS_FILE = "stopwords.txt";
	public static String DOWNLOAD_LINKS_FILE = "download_links.csv";
	public static String PDF_DOWNLOAD_FOLDER = "1_pdf_download";
	public static String PDF_IMPORT_FOLDER = "2_pdf_import";
	public static String PDF_CHANGE_FOLDER = "3_pdf_change";
	public static String PDF_ERROR_FOLDER = "3b_pdf_error";
	public static String TXT_CONVERT_FOLDER = "4_txt_convert";
	public static String TXT_IMPORT_FOLDER = "5_txt_import";
	public static String TXT_CHANGE_FOLDER = "6_txt_change";
	public static String TXT_ERROR_FOLDER = "6b_txt_error";
	public static String LDA_FOLDER = "7_lda_algorithm";
	
	
	private File domainDirectory;
	
	public PrepareDomain(File domainDirectory) {
		this.domainDirectory = domainDirectory;
	}
	
	public void createFilesAndFolders() {
		String dir = FileUtilities.checkDirectoryEnding(domainDirectory.getAbsolutePath());
		
		// create folders		
		new File(dir.concat(PrepareDomain.PDF_DOWNLOAD_FOLDER)).mkdirs();
		new File(dir.concat(PrepareDomain.PDF_IMPORT_FOLDER)).mkdirs();
		new File(dir.concat(PrepareDomain.PDF_CHANGE_FOLDER)).mkdirs();
		new File(dir.concat(PrepareDomain.PDF_ERROR_FOLDER)).mkdirs();
		new File(dir.concat(PrepareDomain.TXT_CONVERT_FOLDER)).mkdirs();
		new File(dir.concat(PrepareDomain.TXT_IMPORT_FOLDER)).mkdirs();
		new File(dir.concat(PrepareDomain.TXT_CHANGE_FOLDER)).mkdirs();
		new File(dir.concat(PrepareDomain.TXT_ERROR_FOLDER)).mkdirs();
		new File(dir.concat(PrepareDomain.LDA_FOLDER)).mkdirs();
		
		// create files
		try {
			File stopWords = new File(dir.concat(PrepareDomain.STOPWORDS_FILE));
			if (!stopWords.exists()) stopWords.createNewFile();
			
			File downloadLinks = new File(dir.concat(PrepareDomain.DOWNLOAD_LINKS_FILE));
			if (!downloadLinks.exists()) {
				CSVWriter writer = new CSVWriter(new FileWriter(downloadLinks.getAbsolutePath()), ',');
				String[] newLine = {"link - change me", "conference - change me", "year - change me"}; 
				writer.writeNext(newLine);
				writer.flush();
				writer.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		fireCustomEvent(new CustomEvent(this).setMessage("domain is created"));
		
	}
	
	// Functions for the events

	protected EventListenerList listenerList = new EventListenerList();

	@Override
	public void addCustomEventListener(CustomEventListener listener) {
		listenerList.add(CustomEventListener.class, listener);		
	}

	@Override
	public void removeCustomEventListener(CustomEventListener listener) {
		listenerList.remove(CustomEventListener.class, listener);		
	}

	@Override
	public void fireCustomEvent(CustomEvent evt) {
		for (CustomEventListener listener : listenerList.getListeners(CustomEventListener.class)) {
			listener.customEventOccurred(evt);
		}
	}
	
	
}
