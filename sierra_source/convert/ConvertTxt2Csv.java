package convert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.swing.event.EventListenerList;
import javax.xml.parsers.ParserConfigurationException;

import me.champeau.ld.EuroparlDetector;

import org.xml.sax.SAXException;

import preparation.PrepareDomain;
import utilities.DateTimeUtilities;
import utilities.FileUtilities;
import utilities.StringUtilities;
import au.com.bytecode.opencsv.CSVWriter;
import database.MetaData;
import events.CustomEvent;
import events.CustomEventInterface;
import events.CustomEventListener;

public class ConvertTxt2Csv implements CustomEventInterface {
	
	public void convertTxt2CsvInAFolder(String domainFolder, String language) {
		fireCustomEvent(new CustomEvent(this).setMessage("Starting Conversion..."));
		FileUtilities f = new FileUtilities();
		
		String sourceDirectory = FileUtilities.checkDirectoryEnding(domainFolder).concat(PrepareDomain.TXT_CHANGE_FOLDER);
		String targetDirectory = FileUtilities.checkDirectoryEnding(domainFolder).concat(PrepareDomain.LDA_FOLDER);
		targetDirectory = FileUtilities.checkDirectoryEnding(targetDirectory).concat(DateTimeUtilities.getCurrentDate()); // add a folder with date and time
		//String language = "en";
		
		List<String> l = f.searchForFiles(sourceDirectory, ".txt");	
		writeTxt2CsvFile(l, FileUtilities.checkDirectoryEnding(sourceDirectory), FileUtilities.checkDirectoryEnding(targetDirectory), language);
	}
	
	private void writeTxt2CsvFile(List<String> files, String sourceDirectory, String targetDirectory, String inputLanguage) {

		int counter = 0;
		int success = 0;

		CSVWriter writer;
		try {
			new File(targetDirectory).mkdirs();
			writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(targetDirectory.concat("converted_file.csv")),"ISO-8859-1"), ','); // vorher: "UTF-8"



			for (String file : files) {

				try {

					// fire message
					if (counter%10 == 0) fireCustomEvent(new CustomEvent(this).setMessage("file ".concat(Integer.toString(counter+1)).concat(" of ").concat(Integer.toString(files.size()))));

					// read the file
					String content;

					content = FileUtilities.readFile(file);


					File txtFile = new File(file);

					// get language and other metadata				
					if (!(txtFile.getName().compareTo("error.txt")==0)) {
						
						FileUtilities fu = new FileUtilities();
						List<String> al = fu.searchForFiles(txtFile.getParent(), ".xml");
						File xmlFile = new File(al.get(0));
						System.out.println(xmlFile.getAbsolutePath());
						MetaData mdRead = new MetaData();
						String language = "";
						String[] metaData = new String[0];
						if (xmlFile.exists()) {

							try {
								metaData = mdRead.readFileAsArray(xmlFile);
								mdRead.readFile(xmlFile);
								language = mdRead.getLanguage();
							} catch (SAXException e) {
								System.out.println("SAXException: ".concat(file));
							} catch (ParserConfigurationException e) {
								System.out.println("ParserConfigurationException: ".concat(file));
							} catch (Exception e) {
								System.out.println("Exception: ".concat(file));
							}

						}
						
						if (language.isEmpty()) {
							EuroparlDetector detector = EuroparlDetector.getInstance();
							language = detector.detectLang(content);							
						}


						// take only english papers
						if (language.trim().compareToIgnoreCase(inputLanguage) == 0 && !content.trim().isEmpty()) {

							// remove line separators
							content = StringUtilities.removeLineSeparators(content);
							content = content.replaceAll("\"", "");
							
							// write header in the first place
							if (success == 0) {
								String[] metaDataTitles = MetaData.getTitleRow();
								String[] header = new String[metaDataTitles.length + 4];
								header[0] = "number";
								header[1] = "id";
								for (int i = 0; i < metaData.length; i++) {
									header[i+2] = StringUtilities.removeLineSeparators(metaDataTitles[i]);
								}
								header[header.length-2] = "file";
								header[header.length-1] = "content";
								
								writer.writeNext(header);
								if (success%100 == 0) writer.flush();
							}
							
							// Save all contents of one document in a row
							String[] newLine = new String[metaData.length + 4];
							newLine[0] = Integer.toString(++success);
							newLine[1] = FileUtilities.getTheNthFolderInRelativeHierarchy(txtFile, 0,new File(sourceDirectory)).getName();
							for (int i = 0; i < metaData.length; i++) {
								newLine[i+2] = StringUtilities.removeLineSeparators(metaData[i]);
							}
							newLine[newLine.length-2] = file; // vorletzte Position für die Datei
							newLine[newLine.length-1] = content; // letzte Position für den Inhalt
							 
							writer.writeNext(newLine);
							if (success%100 == 0) writer.flush(); // flush every 100 files

						}

					}


					counter++;

				} catch (FileNotFoundException e1) {
					// e1.printStackTrace();
					System.out.println("FileNotFoundException: ".concat(file));
				} catch (IOException e1) {
					// e1.printStackTrace();
					System.out.println("IOException: ".concat(file));
				} 

			}

			// finally flush and close
			writer.flush();
			writer.close();
			
		} catch (IOException e2) {
			System.out.println("IOException2: ");
		} 

		fireCustomEvent(new CustomEvent(this).setMessage("Checked " + counter + " documents."));
		fireCustomEvent(new CustomEvent(this).setMessage(success + " succeeded."));
		fireCustomEvent(new CustomEvent(this).setMessage(counter-success + " failed."));
		System.out.println("Checked " + counter + " documents.");
		System.out.println(success + " succeeded.");
		System.out.println(counter-success + " failed.");

	}
			


	
	
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
