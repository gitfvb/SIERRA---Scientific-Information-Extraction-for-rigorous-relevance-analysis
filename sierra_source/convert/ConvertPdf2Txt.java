package convert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.swing.event.EventListenerList;
import javax.xml.parsers.ParserConfigurationException;

import me.champeau.ld.EuroparlDetector;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;

import preparation.PrepareDomain;
import utilities.DateTimeUtilities;
import utilities.FileUtilities;
import utilities.MD5Checksum;
import database.MetaData;
import events.CustomEvent;
import events.CustomEventInterface;
import events.CustomEventListener;



public class ConvertPdf2Txt implements CustomEventInterface {

	
	public void convertPdf2TxtInAFolder(String domainFolder) {
		fireCustomEvent(new CustomEvent(this).setMessage("Starting Conversion..."));
		FileUtilities f = new FileUtilities();
		
		String sourceDirectory = FileUtilities.checkDirectoryEnding(domainFolder).concat(PrepareDomain.PDF_CHANGE_FOLDER);
		String targetDirectory = FileUtilities.checkDirectoryEnding(domainFolder).concat(PrepareDomain.TXT_CONVERT_FOLDER);
		String errorDirecory = FileUtilities.checkDirectoryEnding(domainFolder).concat(PrepareDomain.PDF_ERROR_FOLDER);
		List<String> l = f.searchForFiles(sourceDirectory, "pdf");
		writePdf2TxtFile(l, FileUtilities.checkDirectoryEnding(sourceDirectory), FileUtilities.checkDirectoryEnding(targetDirectory), FileUtilities.checkDirectoryEnding(errorDirecory));
	}
	
private void writePdf2TxtFile(List<String> files, String sourceDirectory, String targetDirectory, String errorDirectory) {
		
			int counter = 0;
			int success = 0;
			for (String file : files) {
				
				boolean errorHappened = false;
				
				// create the file-object
				File pdfFile = new File(file);
				
				// get filepath starting relative from the source directory
				String relativeFilepath = FileUtilities.getRelativeFilePath(new File(pdfFile.getAbsolutePath()), sourceDirectory);
								
				// fire status messages
				if (counter%10 == 0) fireCustomEvent(new CustomEvent(this).setMessage("file ".concat(Integer.toString(counter+1)).concat(" of ").concat(Integer.toString(files.size()))));
				System.out.println("file ".concat(Integer.toString(counter+1)).concat(" of ").concat(Integer.toString(files.size())).concat(": ").concat(file));				
				
				// read the xml-file, if exists
				MetaData mdRead = new MetaData();
				String dataSource = "";
				String conferenceName = "";
				String conferenceYear = "";
				String downloadDate = "";
				String fileSize = "";
				String pdfHash = "";
				String comment = "";
				FileUtilities fu = new FileUtilities();
				List<String> al = fu.searchForFiles(pdfFile.getParent(), ".xml");
				File xmlFile = new File(al.get(0));
				if (xmlFile.exists()) {
					try {
						mdRead.readFile(xmlFile);
					} catch (ParserConfigurationException e) {
						;
					}
					dataSource = mdRead.getDataSource();
					conferenceName = mdRead.getConferenceName();
					conferenceYear = mdRead.getConferenceYear();
					downloadDate = mdRead.getDownloadDate();
					pdfHash = mdRead.getPdfhash();
					fileSize = mdRead.getFileSize();
					comment = mdRead.getComment();
				} else {
					// if the xml file does not exists, append the filename to the filepath
					relativeFilepath = FileUtilities.checkDirectoryEnding(relativeFilepath.concat(pdfFile.getName()));
				}
				
				// new filepath and filename
				String filepath = FileUtilities.checkDirectoryEnding(targetDirectory.concat(relativeFilepath));
				new File(filepath).mkdirs();
				String txtFilename = FileUtilities.changeFileExtension(pdfFile.getName(), "txt");
				String txtPath = filepath.concat(txtFilename);
				
				// create the file, if it does not exist yet
				if (!new File(txtPath).exists()) {
					// get string of pdf-file
					String s = "";
					try {
						s = ConvertPdf2Txt.getStringFromPdf(file);
					} catch (IOException e) {
						errorHappened = true;
						e.printStackTrace();
						;
					} catch (ClassCastException e) {
						errorHappened = true;
						e.printStackTrace();
						;
					} catch (RuntimeException e) {
						errorHappened = true;
						e.printStackTrace();
						;
					}


					// Extract Meta-Data of a PDF to a xml-file						
					PDDocument pdf = null;
					try {
						pdf = PDDocument.load(file);
					} catch (IOException e) {
						e.printStackTrace();
						;
					}


					// get language
					EuroparlDetector detector = EuroparlDetector.getInstance();
					String language = detector.detectLang(s);

					// metadata
					MetaData md = new MetaData();
					if (pdf != null) {
						PDDocumentInformation info = pdf.getDocumentInformation();

						// create an xml-file with the link to the file

						md.setAuthor(info.getAuthor());
						try {
							md.setCreationDate(DateTimeUtilities.getEnglishDateTime(info.getCreationDate()));
						} catch (IOException e2) {
							md.setCreationDate("");
						}
						md.setCreator(info.getCreator());					
						md.setKeywords(info.getKeywords());
						try {
							md.setModificationDate(DateTimeUtilities.getEnglishDateTime(info.getModificationDate()));
						} catch (IOException e1) {
							md.setModificationDate("");
						}
						md.setNumberOfPages(Integer.toString(pdf.getNumberOfPages()));
						md.setProducer(info.getProducer());
						md.setSubject(info.getSubject());
						md.setTitle(info.getTitle());
						md.setTrapped(info.getTrapped());
						
						
						try {
							pdf.close(); // try to close the pdf (avoid messages from pdf-box)
						} catch (IOException e2) {
							;
						}

					}
										
					md.setConferenceName(conferenceName);
					md.setConferenceYear(conferenceYear);
					md.setDownloadDate(downloadDate);
					md.setDataSource(dataSource);
					md.setPdfhash(pdfHash);
					md.setFileSize(fileSize);
					md.setComment(comment);
					md.setFileName(pdfFile.getName());
					if (s.length() > 0) md.setLanguage(language);
					
					if (s.trim().isEmpty()) errorHappened = true; // if the pdf-file is totally empty (it is not, but the converter thinks it is)... handle it like an reading error
					if (errorHappened == true) {
						FileUtilities.deleteDir(new File(filepath));
						filepath = FileUtilities.checkDirectoryEnding(errorDirectory.concat(relativeFilepath));
						new File(filepath).mkdirs();
					}
						

					if (!s.trim().isEmpty()) {					

						++success; 

						try {
							
							FileUtilities.saveStringToFile(new File(txtPath), s);
							md.setTxthash(MD5Checksum.getMD5Checksum(txtPath));
							
						} catch (FileNotFoundException e) {     
							e.printStackTrace();						
							;
						} catch (IOException e) {     
							e.printStackTrace();
							;
						}


					} else {

						try {
							if (!new File(filepath.concat(pdfFile.getName())).exists())								
								FileUtilities.copy(file, filepath.concat(pdfFile.getName()));
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					}
					
					
					md.createNewFile(new File(filepath.concat("metadata.xml"))); 
					
				}
				
				
				counter++;
			}

			
			fireCustomEvent(new CustomEvent(this).setMessage("Checked " + counter + " documents."));
			fireCustomEvent(new CustomEvent(this).setMessage(success + " succeeded."));
			fireCustomEvent(new CustomEvent(this).setMessage(counter-success + " failed."));
			System.out.println("Checked " + counter + " documents.");
			System.out.println(success + " succeeded.");
			System.out.println(counter-success + " failed.");
			

	}
	



	public static String getStringFromPdf(String pdfPath) throws IOException, ClassCastException {
		
			PDDocument pdf = PDDocument.load(pdfPath);
			PDFTextStripper stripper = new PDFTextStripper();
			String text = stripper.getText(pdf);
			pdf.close();
			return text;			
				
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
