package reports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.event.EventListenerList;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
import org.xml.sax.SAXException;

import scala.collection.Set;
import scalanlp.text.tokenize.StopWordFilter;
import utilities.CollectionsUtilities;
import utilities.DateTimeUtilities;
import utilities.FileUtilities;
import au.com.bytecode.opencsv.CSVWriter;
import database.MetaData;
import events.CustomEvent;
import events.CustomEventInterface;
import events.CustomEventListener;


public class ReportUtilities implements CustomEventInterface {

	/*
	 * REPORT FOR FILE STATISTICS
	 */
	public void printFileStatistics(String rootFolder, String filetype) {

		try {

			FileUtilities fu = new FileUtilities();
			
			String[] titleRow = new String[2];
			titleRow[0] = "folder";
			titleRow[1] = "filename";
			
			List<String[]> fileList = new ArrayList<String[]>();
			for (String file : fu.searchForFiles(rootFolder, filetype)) {
				String[] line = new String[2];				
				line[0] = FileUtilities.getTheNthFolderInRelativeHierarchy(new File(file), 0, new File(rootFolder)).getName();
				line[1] = new File(file).getName();
				fileList.add(line);
			} 

			CSVWriter writer = new CSVWriter(new FileWriter(rootFolder.concat(DateTimeUtilities.getCurrentDate() + "_file_summary.csv")), ',');
			for (String[] newLine : fileList) {
				writer.writeNext(newLine);
			}

			writer.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	
	/*
	 * REPORT FOR EXTRACTING THE XML TO AN CSV-FILE
	 */	
	public String printXmlFilesToCsv(String rootFolder) {
		
		
		
		FileUtilities fu = new FileUtilities();
		
		CSVWriter writer;
		String targetFile = FileUtilities.checkDirectoryEnding(rootFolder).concat(DateTimeUtilities.getCurrentDate() + "_xml_summary.csv");
		try {
			writer = new CSVWriter(new FileWriter(targetFile), ',');
			String[] idHeader = {"id"};
			String[] header = (String[]) ArrayUtils.addAll(idHeader, MetaData.getTitleRow());
			writer.writeNext(header); // title row
			int counter = 0;
			for (String file : fu.searchForFiles(rootFolder, ".xml")) {
				File xmlFile = new File(file);
				
				String[] id = { FileUtilities.getTheNthFolderInRelativeHierarchy(new File(file), 0, new File(rootFolder)).getName() };				
				MetaData mdRead = new MetaData();				
				String[] metaData = mdRead.readFileAsArray(xmlFile);
				
				String[] newLine = (String[]) ArrayUtils.addAll(id, metaData);
				
				writer.writeNext(newLine);
				if (counter++%100 == 0) writer.flush(); // flush every 100 files				
			}

			// finally flush and close
			writer.flush();
			writer.close();
			
		} catch(SAXException e) {
			;
		} catch(ParserConfigurationException e) {
			;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return targetFile;
	}
	
	public void printTopNWords(String folderpath, int topNWords) {
		// search for zipped *.gz-Files and unpack the first found file
		String filepath = findGZipFile(folderpath);
		
		// output the numbers and facts
		if (filepath != "") {
			// get the counts of the words
			Map<String, Integer> m = getCountsOfWords(filepath);

			// get all stop words
			ArrayList<String> stopWordList = getAllEnglishStopWords();
			
			// show the top n words without stopwords	       
			fireCustomEvent(new CustomEvent(this).setMessage("----------------------"));
			fireCustomEvent(new CustomEvent(this).setMessage("MOST WORDS WITHOUT STOP WORDS:"));			
			printTopNWordsWithoutStopwords(m, stopWordList, topNWords);
		}
		
		// now delete the unzipped file
		new File(filepath).delete();
	}
	
	public void printWordsByCumulation(String folderpath, double percentageToShow) {
		// search for zipped *.gz-Files and unpack the first found file
		String filepath = findGZipFile(folderpath);
		
		// output the numbers and facts
		if (filepath != "") {
			// get the counts of the words
			Map<String, Integer> m = getCountsOfWords(filepath);

			// get all stop words
			ArrayList<String> stopWordList = getAllEnglishStopWords();

			// get the n percent words of the total sum of counts
			fireCustomEvent(new CustomEvent(this).setMessage("----------------------"));
			fireCustomEvent(new CustomEvent(this).setMessage("FIRST " + Double.toString(percentageToShow*100) + " PERCENT OF THE TOTAL COUNT:"));
			printNPercentWordsOfTotalCount(m, stopWordList, percentageToShow);
			
		}
		
		// now delete the unzipped file
		new File(filepath).delete();
	}
	
	public void printLogValues(String folderpath) {
		// load files
		TreeMap<String, Double> tm = loadLogValues(folderpath);
				
		// print every key and entry in the TreeMap 
		fireCustomEvent(new CustomEvent(this).setMessage("----------------------"));
		fireCustomEvent(new CustomEvent(this).setMessage("LOG VALUES:"));
		printEntryValues(tm);
		
	}
	
	private String findGZipFile(String folderpath) {		
		List<String> gzFiles = FileUtilities.getAllFilesInFolder(folderpath, ".gz");
		if (gzFiles.size() > 0) {
			return FileUtilities.unpackGzip(gzFiles.get(0));
		} else {
			return "";
		}
	}
	
	private TreeMap<String, Double> loadLogValues(String folderpath) {
		
		// load files
		FileUtilities fu = new FileUtilities();
		List<String> logFiles = fu.searchForFiles(folderpath, "log-probability-estimate.txt");		
		
		// read every file into a sorted TreeMap
		TreeMap<String, Double> tm = new TreeMap<String, Double>();
		for (String file: logFiles) {
			try {
				tm.put(FileUtilities.getParentFolder(new File(file), 1).getName(), Double.valueOf(FileUtilities.readFile(file)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return tm;
	} 
	
	private void printEntryValues(TreeMap<String, Double> tm) {
		Iterator<String> i = tm.keySet().iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
//			System.out.printf("key: %s, value: %s\n", key, tm.get(key));
			fireCustomEvent(new CustomEvent(this).setMessage("key: " + key + ", value: " + tm.get(key)));
		}
	}
	

	private void printNPercentWordsOfTotalCount(Map<String, Integer> countedWords, ArrayList<String> stopWordList, double percentageToRemove) {
		// sum all occurences
		int totalSumOfOccurences = 0;
		for (Iterator<String> i = countedWords.keySet().iterator(); i.hasNext(); ) {
			totalSumOfOccurences += (Integer)countedWords.get(i.next());
		}
//		System.out.println("Total sum of counts: " + totalSumOfOccurences);
		fireCustomEvent(new CustomEvent(this).setMessage("Total sum of counts: " + totalSumOfOccurences));

		// now get all words in sum that make up to the definied percentage of occurences
//		double percentageToRemove = 0.1;
		double sumOfOccurences = 0;
		int counter = 0;
		double maxSumOfOccurences = new Double(totalSumOfOccurences) * percentageToRemove;
		Iterator<String> i = CollectionsUtilities.sortByValueDesc(countedWords).iterator();
		while (i.hasNext() && sumOfOccurences <= maxSumOfOccurences) {
			String key = (String) i.next();
			if (!stopWordList.contains(countedWords.get(key))) {
//				System.out.printf(counter + " - key: %s, value: %s\n", key, countedWords.get(key));
				fireCustomEvent(new CustomEvent(this).setMessage(counter + " - key: " + key + ", value: " + countedWords.get(key)));
				sumOfOccurences += (Integer)countedWords.get(key);
				counter++;
			}
		}	
	}
	
	private void printTopNWordsWithoutStopwords(Map<String, Integer> countedWords, ArrayList<String> stopWordList, int n) {
		int counter = 0;
		Iterator<String> i = CollectionsUtilities.sortByValueDesc(countedWords).iterator();
		while (i.hasNext() && counter <= n) {
			String key = (String) i.next();
			if (!stopWordList.contains(countedWords.get(key))) {
//				System.out.printf(counter + " - key: %s, value: %s\n", key, countedWords.get(key));
				fireCustomEvent(new CustomEvent(this).setMessage(counter + " - key: " + key + ", value: " + countedWords.get(key)));
				counter++;
			}
		}	
	}
	
	
	
	
	private Map<String, Integer> getCountsOfWords(String filepath) {

		
		Map<String, Integer> m = new HashMap<String,Integer>();

		try {
			// read the content
			String content = FileUtilities.readFile(filepath);


			//		content = "(10,Index(\"a\",\"b\",\"c\",\"d\",\"e\",\"f\",\"g\",\"h\",\"i\",\"j\"),Array(1,2,3,4,5,6,10,7,8,9))";

			// extract the positions of the markers
			int firstComma = content.indexOf(",");
			int firstIndex = content.indexOf(",Index(") + 7;
			int lastIndex = content.indexOf("),Array(");

			// extract the important parts with the calculated positions
			int numberOfWords = Integer.valueOf(content.substring(1, firstComma));
			String words = content.substring(firstIndex+1, lastIndex-1);
			String counts = content.substring(lastIndex+8,content.length()-2);

			// split the lists
			String[] wordsList = words.split("\",\"");
			String[] countsList = counts.split(",");

			// create a map with the word as key and the count as entry				
			for (int i = 0; i < numberOfWords; i++) {
				m.put(wordsList[i], Integer.valueOf(countsList[i]));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return m;
	}
	
	public void printTMTStopWords() {
		ArrayList<String> wordList = getAllEnglishStopWords();
		for (String word: wordList) {
			fireCustomEvent(new CustomEvent(this).setMessage(word));
		}
	}
	
	private ArrayList<String> getAllEnglishStopWords() {
//		System.out.println("STOP WORDS:");
		StopWordFilter stopWordFilter = new scalanlp.text.tokenize.StopWordFilter("en");
        Set<String> stopWords = stopWordFilter.words();
        scala.collection.Iterator<String> i = stopWords.iterator();
        ArrayList<String> stopWordList = new ArrayList<String>();
        while (i.hasNext()) {
        	String stopWord = i.next(); 
        	stopWordList.add(stopWord);
//        	System.out.println(stopWord);
        }
        return stopWordList;
	}
	
	public void printMapSortedByValueAsc(Map<String, Integer> m) {
		for (Iterator<String> i = CollectionsUtilities.sortByValueAsc(m).iterator(); i.hasNext(); ) {
			String key = (String) i.next();
//			System.out.printf("key: %s, value: %s\n", key, m.get(key));
			fireCustomEvent(new CustomEvent(this).setMessage("key: " + key + ", value: " + m.get(key)));
		}
	}
	
	public void printMapSortedByValueDesc(Map<String, Integer> m) {
		for (Iterator<String> i = CollectionsUtilities.sortByValueDesc(m).iterator(); i.hasNext(); ) {
			String key = (String) i.next();
//			System.out.printf("key: %s, value: %s\n", key, m.get(key));
			fireCustomEvent(new CustomEvent(this).setMessage("key: " + key + ", value: " + m.get(key)));
		}
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
