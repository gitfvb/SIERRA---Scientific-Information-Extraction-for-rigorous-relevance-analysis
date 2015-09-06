package download;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.EventListenerList;

import preparation.PrepareDomain;
import utilities.DateTimeUtilities;
import utilities.FileUtilities;
import utilities.MD5Checksum;
import database.MetaData;
import events.CustomEvent;
import events.CustomEventInterface;
import events.CustomEventListener;

public class Normalize implements CustomEventInterface {
	
	/*
	 * Beim Normalisieren anbieten, vorher alle Daten im Ordner zu löschen
	 * PrepareDomain nutzen
	 * 
	 */
	
	private int idRangeStart = 80000000;
	private int idRangeEnd = 89999999;
	
	public void normalizeDownloadedData(String domainFolder) throws IOException {
		String source1 = FileUtilities.checkDirectoryEnding(domainFolder).concat(PrepareDomain.PDF_DOWNLOAD_FOLDER);
		String source2 = FileUtilities.checkDirectoryEnding(domainFolder).concat(PrepareDomain.PDF_IMPORT_FOLDER);
		String target = FileUtilities.checkDirectoryEnding(domainFolder).concat(PrepareDomain.PDF_CHANGE_FOLDER);
		target = FileUtilities.checkDirectoryEnding(target);
		
		
		FileUtilities fu = new FileUtilities();
		
		
		// extra logge zum import der dateien - normalisieren der dateien im import-ordner
		List<String> filesImportPdf = new ArrayList<String>();
		filesImportPdf.addAll(fu.searchForFiles(source2, ".pdf"));
		
		// check for xml-files
		List<String> filesImportXml = new ArrayList<String>();
		filesImportXml.addAll(fu.searchForFiles(source2, ".xml"));
		
		// fill the collection with all path from pdf-files
		List<String> filesImportParentPdf = new ArrayList<String>();
		List<String> filesImportParentXml = new ArrayList<String>();
		List<String> filesImportTemp = new ArrayList<String>();
		for (String file : filesImportPdf) {
			filesImportParentPdf.add(new File(file).getParent());
		}
		for (String file : filesImportXml) {
			filesImportParentXml.add(new File(file).getParent());
		}
		
		// remove those files which are already normalized (have xml-files)
		for (String file : filesImportParentPdf) {
			if (!filesImportParentXml.contains(file)) {
				filesImportTemp.add(file);
			}
		}
		List<String> filesImport = filesImportTemp;
		
		// message
		fireCustomEvent(new CustomEvent(this).setMessage(filesImport.size() + " new files found to import"));
		
		for (String file : filesImportPdf) {
			
			if (filesImport.contains(new File(file).getParent())) {
				
				// create directory
				int id = FileUtilities.getNextFreeId(source2, idRangeStart, idRangeEnd);
				String targetFolder = FileUtilities.checkDirectoryEnding(source2).concat(Integer.toString(id));
				new File(targetFolder).mkdirs();				
				
				// get the conference and year
				String conference = "";
				String year = "";
				int folderDepth = FileUtilities.getRelativeFolderDepth(new File(file), source2);
				if (folderDepth >= 1) {
					conference = FileUtilities.getTheNthFolderInRelativeHierarchy(new File(file), 1, new File(domainFolder)).getName();
				}
				if (folderDepth >= 2) {
					year = FileUtilities.getTheNthFolderInRelativeHierarchy(new File(file), 2, new File(domainFolder)).getName();
				}
				
				// create xml-file
				File xmlFile = new File(FileUtilities.checkDirectoryEnding(targetFolder).concat(MetaData.STANDARDFILENAME));
				MetaData md = new MetaData();
				md.setDataSource("manually_imported");				
				md.setFileName(new File(file).getName());
				String s = MD5Checksum.getMD5Checksum(file);
				md.setPdfhash(s);
				md.setDownloadDate(DateTimeUtilities.getCurrentDate());
				md.setConferenceName(conference);
				md.setConferenceYear(year);
				md.setFileSize(Long.toString(new File(file).length()));
				md.createNewFile(xmlFile);
			
				// move file
				FileUtilities.moveFile(new File(file), targetFolder);
				
			} 
			
		} 
		
		fireCustomEvent(new CustomEvent(this).setMessage("checking for new files"));
		
		// copy files to the change folder
		List<String> files = new ArrayList<String>();
		List<String> sources = new ArrayList<String>();
		sources.add(source1);
		sources.add(source2);
		
		for (String source : sources) {
			files.addAll(fu.searchForFiles(source, ".pdf"));
			files.addAll(fu.searchForFiles(source, ".xml"));
			for (String file : files) {
				File sourceFile = new File(file);			
				File targetFile = new File(target.concat(FileUtilities.getRelativeFilePath(sourceFile, source)));
				
				// if file not exists -> copy the file
				if (!targetFile.exists()) {
					targetFile.mkdirs();
				}
				if (!new File(FileUtilities.checkDirectoryEnding(targetFile.getAbsolutePath().toString()).concat(sourceFile.getName())).exists()) {
					FileUtilities.copy(sourceFile.getAbsolutePath(), targetFile.getAbsolutePath());
				}
							
			}
			files.clear();
		}
		
		fireCustomEvent(new CustomEvent(this).setMessage("downloaded files are normalized "));
		
		
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
