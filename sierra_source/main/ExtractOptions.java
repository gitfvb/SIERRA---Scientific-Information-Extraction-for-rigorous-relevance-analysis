package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import utilities.FileUtilities;

public class ExtractOptions {

	private String path = "";
	
	public ExtractOptions(String path) {
		this.path = path;
	}
	
	public String getLdaAlgorithm() {
		FileUtilities fu = new FileUtilities();
		List<String> files = fu.searchForFiles(path, "description.txt");
		if (files.size() > 0 ) {
			File f = new File(files.get(0));
			String algo = "";
			try {
				
				String content = FileUtilities.readFile(f.getAbsolutePath());
				String[] lines = content.split(System.getProperty("line.separator"));
				String line0 = lines[0];
				algo = line0.split(" ")[0];
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return algo;
		}
		
		return "";
		
	}

}
