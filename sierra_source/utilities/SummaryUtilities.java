package utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import au.com.bytecode.opencsv.CSVWriter;

public class SummaryUtilities {

	public static String saveTopicTermsByCumulation(String folderpath) {
		
		SummaryUtilities su = new SummaryUtilities();
		
		TreeMap<String, File> tm = su.loadSummaryFiles(folderpath);		
		String filepath = tm.get(tm.lastKey()).getAbsolutePath();
		
		CSVWriter writer;
		
		int minTerms = 5;
		int maxTerms = 10;
		double maxCumulation = 0.1;
		
		try {
			
			String content = FileUtilities.readFile(filepath);
			
			String[] lines = content.split(System.getProperty("line.separator"));
			
			List<String[]> topicTerms = new ArrayList<String[]>();
			
			String currentTopic = "";
			double currentTopicSum = 0.0;
			
			double valueCumulation = 0.0;
			int termCounter = 0;
			
			String[] temp = new String[maxTerms+1];
			
			for (String line : lines) {
				
				// get the topic
				if (line.split("\t\t").length > 1) {
										
					if (!currentTopic.equals("")) topicTerms.add(temp);
					temp = new String[maxTerms+2];
					
					currentTopic = line.substring(0, line.indexOf("\t"));					
					currentTopicSum = Double.valueOf(line.substring(line.lastIndexOf("\t")+1,line.length()));					
					
					valueCumulation = 0.0;
					termCounter = 0;					
					temp[0] = currentTopic;
					temp[1] = Double.toString(currentTopicSum);
					
//					System.out.println(currentTopic + ": " + currentTopicSum);
				}
				
				// get the words of the topic
				if (line.startsWith("\t")) {
					String term = line.substring(line.indexOf("\t")+1, line.lastIndexOf("\t"));;
					double value = Double.valueOf(line.substring(line.lastIndexOf("\t")+1,line.length()));
					
					valueCumulation += value;
					termCounter++;
					
					if (termCounter <= minTerms || (valueCumulation/currentTopicSum < maxCumulation && termCounter <= maxTerms)) {
//						System.out.println(term + ": " + value);
						temp[termCounter+1] = term;
					} else if (termCounter <= maxTerms) {
						temp[termCounter+1] = "";
					}
										
				}
								
			}
			topicTerms.add(temp); // add the last element
			
			// save the results
			String savePath = FileUtilities.checkDirectoryEnding(new File(filepath).getParent()).concat("summary.csv");			
			writer = new CSVWriter(new FileWriter(savePath), ',');
			for (String[] newLine : topicTerms) {
				writer.writeNext(newLine);
			}
			
			writer.close();
			
			return savePath;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	private TreeMap<String, File> loadSummaryFiles(String folderpath) {
		
		// load files
		FileUtilities fu = new FileUtilities();
		List<String> logFiles = fu.searchForFiles(folderpath, "summary.txt");		
		
		// read every file into a sorted TreeMap
		TreeMap<String, File> tm = new TreeMap<String, File>();
		for (String file: logFiles) {
			tm.put(FileUtilities.getParentFolder(new File(file), 1).getName(), new File(file));
		}
		return tm;
	}
	
}
