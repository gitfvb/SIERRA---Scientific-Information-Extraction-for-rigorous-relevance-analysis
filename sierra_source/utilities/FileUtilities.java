package utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;



public class FileUtilities {

	public static String ILLEGAL_FILE_NAME_CHARACTERS = "[:\\\\/*?|<>\t]"; // : \ / * ? | < > <tab>
	public static int MAX_FILE_NAME_LENGTH = 250;
	
	private List<String> fileList;
	private String fileTypeIntern;
	

	// Get the files into a list
	// http://stackoverflow.com/questions/1384947/java-find-txt-files-in-specified-folder
	public static List<String> getAllFilesInFolder(String directory, String fileSuffix) {
		if (!fileSuffix.startsWith(".")) fileSuffix = ".".concat(fileSuffix);
		List<String> textFiles = new ArrayList<String>();
		File dir = new File(directory);
		for (File file : dir.listFiles()) {
			if (file.getName().toLowerCase().endsWith((fileSuffix))) {
				textFiles.add(file.getPath().toString());
			}
		}
		return textFiles;
	}
	
	// Get the Subfolders into a list
	public static List<String> getAllFoldersInFolder(String directory) {
		List<String> textFiles = new ArrayList<String>();
		if (!directory.isEmpty()) {
			File dir = new File(directory);
			for (File file : dir.listFiles()) {
				if (file.isDirectory()) {
					textFiles.add(file.getPath().toString());
				}
			}
		}
		return textFiles;
	}
	
	public static String checkDirectoryEnding(String directory) {
		return (!directory.endsWith(File.separator) ? directory.concat(File.separator) : directory);
	}

	/**
	 * replace illegal characters in a filename with ""
	 * illegal characters :
	 *           : \ / * ? | < >
	 * @param name
	 * @return
	 */
	public static String sanitizeFilename(String name) {
		// source: http://www.rgagnon.com/javadetails/java-0662.html
		if (name.length() > MAX_FILE_NAME_LENGTH) name = name.substring(0, MAX_FILE_NAME_LENGTH);
		return name.replaceAll(ILLEGAL_FILE_NAME_CHARACTERS, "");
	}

	
	
	/**
	 * 
	 * @param folder
	 * @param fileType
	 * @return the boolean value is not for function yet... it is only used to break the recursion
	 */
	private boolean searchRecursivly(File folder, FileFilter fileType) {
		
		// check, if the given string is really a folder		 
		if (!folder.isDirectory()) {
			return false;
		}
		
		// get all files (matching the fileType) and sub-directories in this folder
		File[] fList = folder.listFiles(fileType);
		
		// recursion anchor
		if (fList.length == 0) {
			return true;
		}
		
		// recursion step
		for (File f : fList) {
			if (f.isDirectory()) {
//				System.out.println(f.getName());
				searchRecursivly(f, fileType);				
			} else {
				// must a a file of the given filetype because of the filter
				fileList.add(f.getAbsolutePath());
//				System.out.println(f.getName());
			}
		}		
		return true;		
	} 
	
	
	
	public List<String> searchForFiles(String startFolder, String fileType) {
		
		// create a FileFilter for files with the ending fileType and Folders
		fileTypeIntern = fileType;
		FileFilter ff = new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().toLowerCase().endsWith(fileTypeIntern) || pathname.isDirectory() ;
			}
		};
		
		fileList = new ArrayList<String>();
		searchRecursivly(new File(startFolder), ff);
		return fileList;
		
	}
	
	// http://www.java-forum.org/hausaufgaben/30158-textdatei-einlesen.html
	public static String readFile(String fileName) throws FileNotFoundException, IOException{ 
		String s = null; 
		StringBuffer datei = new StringBuffer();
		BufferedReader in = new BufferedReader(new FileReader(fileName)); 
		while((s = in.readLine()) != null){ 
			datei.append(s + System.getProperty("line.separator"));
		} 
		in.close();
		return datei.toString();
	}
	
	// Write the file (source: http://www.javapractices.com/topic/TopicAction.do?Id=42)
	public static void saveStringToFile(File aFile, String aContents)
	throws FileNotFoundException, IOException {
		if (aFile == null) {
			throw new IllegalArgumentException("File should not be null.");
		}
		if (!aFile.exists()) {
			aFile.createNewFile();
			//	throw new FileNotFoundException("File does not exist: " + aFile);
		}
		if (!aFile.isFile()) {
			throw new IllegalArgumentException("Should not be a directory: "
					+ aFile);
		}
		if (!aFile.canWrite()) {
			throw new IllegalArgumentException("File cannot be written: "
					+ aFile);
		}

		// use buffering
		Writer output = new BufferedWriter(new FileWriter(aFile));
		try {
			// FileWriter always assumes default encoding is OK!
			output.write(aContents);
		} finally {
			output.close();
		}
	}
	
	// source: http://www.java2s.com/Code/Java/File-Input-Output/CopyfilesusingJavaIOAPI.htm
	public static void copy(String fromFileName, String toFileName)
	throws IOException {
		File fromFile = new File(fromFileName);
		File toFile = new File(toFileName);

		if (!fromFile.exists())
			throw new IOException("FileCopy: " + "no such source file: "
					+ fromFileName);
		if (!fromFile.isFile())
			throw new IOException("FileCopy: " + "can't copy directory: "
					+ fromFileName);
		if (!fromFile.canRead())
			throw new IOException("FileCopy: " + "source file is unreadable: "
					+ fromFileName);

		if (toFile.isDirectory())
			toFile = new File(toFile, fromFile.getName());

		if (toFile.exists()) {
			if (!toFile.canWrite())
				throw new IOException("FileCopy: "
						+ "destination file is unwriteable: " + toFileName);
			System.out.print("Overwrite existing file " + toFile.getName()
					+ "? (Y/N): ");
			System.out.flush();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			String response = in.readLine();
			if (!response.equals("Y") && !response.equals("y"))
				throw new IOException("FileCopy: "
						+ "existing file was not overwritten.");
		} else {
			String parent = toFile.getParent();
			if (parent == null)
				parent = System.getProperty("user.dir");
			File dir = new File(parent);
			if (!dir.exists())
				throw new IOException("FileCopy: "
						+ "destination directory doesn't exist: " + parent);
			if (dir.isFile())
				throw new IOException("FileCopy: "
						+ "destination is not a directory: " + parent);
			if (!dir.canWrite())
				throw new IOException("FileCopy: "
						+ "destination directory is unwriteable: " + parent);
		}

		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = from.read(buffer)) != -1)
				to.write(buffer, 0, bytesRead); // write
		} finally {
			if (from != null)
				try {
					from.close();
				} catch (IOException e) {
					;
				}
				if (to != null)
					try {
						to.close();
					} catch (IOException e) {
						;
					}
		}
	}
	
	
	
	/**
	 * changes the file extension of a file, like "foo.txt" to "foo.csv"
	 * source: http://www.roseindia.net/java/string-examples/java-change-file.shtml
	 * @param fileName the given file name you want to be changed
	 * @param newfileExtension the ne extension you want to replace the old extension with
	 * @return the new file name with the new extension
	 */
	public static String changeFileExtension(String fileName, String newfileExtension) {
		int dotPos = fileName.lastIndexOf(".");
		String strFilename = fileName.substring(0, dotPos);
		return strFilename + "." + newfileExtension; 
	}
	
	/**
	 * Gets the parent folder of a file or folder specified by the depth to go backwards in the file hierarchy.
	 * An example is: If you have a file like "C:\Users\foo\bar\text.txt" and you define the depth with 2, you
	 * get the folder "foo" in return. 
	 * @param file the source file or folder
	 * @param depth the depth to go backwards
	 * @return the parental folder you wanted to have
	 */
	public static File getParentFolder(File file, int depth) {
		if (depth==0) {
			return file; // recursive anchor
		} else {
			return getParentFolder(new File(file.getParent()), depth-1);
		}
	}
	
	public static String getRelativeFilePath(File file, String domainFolder) {
		String relativeFilepath = "";
		if (FileUtilities.checkDirectoryEnding(domainFolder).length() < FileUtilities.checkDirectoryEnding(file.getParent()).length()) {
			relativeFilepath = FileUtilities.checkDirectoryEnding(file.getParent().substring(domainFolder.length()));
		} else {
			relativeFilepath = "/";
		}
		return relativeFilepath;
	}
	
	/**
	 * Gets the specified folder in a folder hierarchy. The first folder has n = 0.
	 * Example: If you have a a file like "C:\Users\foo\bar\text.txt" and you
	 * define your root folder as "C:\Users", you get the folder "foo", if your
	 * n = 0.
	 * @param file given file you want get the folders from
	 * @param n stage in hierarchy. The first folder has n = 0
	 * @param rootFolder the part of the path you do not want to be counted
	 * @return the folder you wanted in the file hierarchy
	 */
	public static File getTheNthFolderInRelativeHierarchy(File file, int n, File rootFolder) {
		int folders = getRelativeFolderDepth(file, rootFolder.getAbsolutePath());
		return getParentFolder(file ,folders - n);		
	}
	
	/**
	 * Gives you the amount of parental folders without the given root folder.
	 * Example: If you have a a file like "C:\Users\foo\bar\text.txt" and you
	 * define your root folder as "C:\Users", you get the depth of 2 folders.
	 * @param file given file you want to know the depth from.
	 * @param rootFolder the part of the path you do not want to be counted
	 * @return the amount of parental folders
	 */
	public static int getRelativeFolderDepth(File file, String rootFolder) {
		if (checkDirectoryEnding(file.getParent()).compareTo(checkDirectoryEnding(rootFolder)) == 0) {
			return 0;
		} else {
			return getRelativeFolderDepth(new File(file.getParent()), rootFolder) + 1;
		}
		
	}
	
	/**
	 * Moves a file to a specified folder
	 * @param file file to move
	 * @param folder target folder
	 * @return boolean, if the operation was successful
	 */
	public static boolean moveFile(File file, String folder) {
		file.renameTo(new File(checkDirectoryEnding(folder).concat(file.getName())));
		return true;
	}
	
	/**
	 * Deletes all files and subdirectories under dir.
	 * Returns true if all deletions were successful.
	 * If a deletion fails, the method stops attempting to delete and returns false.
	 * Source: http://www.theserverside.com/discussions/thread.tss?thread_id=32492
	 * @param dir
	 * @return
	 */
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	} 
	
	public static String unpackGzip(String source) {
		String outFilename = source.substring(0, source.lastIndexOf(".gz"));

		try {

			//	     System.out.println("File for Extracting : "+source);
			FileInputStream instream= new FileInputStream(source);
			GZIPInputStream ginstream =new GZIPInputStream(instream);
			FileOutputStream outstream = new FileOutputStream(outFilename);
			byte[] buf = new byte[1024]; 
			int len;
			while ((len = ginstream.read(buf)) > 0) 
			{
				outstream.write(buf, 0, len);
			}
			//	   System.out.println("File Successfylly Extract");
			//	     System.out.println("Extract file : "+outFilename);
			ginstream.close();
			outstream.close();
			return outFilename;
		} catch(IOException ioe) {
			System.out.println("Exception has been thrown" + ioe);
			return "";
		}
	}
	
	// source: http://bittyjava.wordpress.com/2007/04/30/converting-files-to-utf-8/
	public static void convertFileToUTF8(String inputFile, String outputFile) {
		try {
			FileInputStream fis = new FileInputStream(inputFile);
			byte[] contents = new byte[fis.available()];
			fis.read(contents, 0, contents.length);
			String asString = new String(contents, "ISO8859_1");
			byte[] newBytes = asString.getBytes("UTF8");
			FileOutputStream fos = new FileOutputStream(outputFile);
			fos.write(newBytes);
			fos.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// source for random int: http://blog.root-of-all-evil.com/2010/03/math-random-zufallszahlen-in-java/
	public static int getNextFreeId(String targetDirectory, int low, int high) {	      
		high++; // include the highest number to the calculation
		String directory;
		int id;
		do {
			id = (int) (Math.random() * (high - low) + low);
			directory = FileUtilities.checkDirectoryEnding(targetDirectory).concat(Integer.toString(id));
		} while (new File(directory).exists());
		return id; 		
	}
	
	
}