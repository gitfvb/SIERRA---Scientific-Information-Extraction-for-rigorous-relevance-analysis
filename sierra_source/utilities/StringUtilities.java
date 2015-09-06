package utilities;


public class StringUtilities {

	
//	public static String stemming(String content) {
//		// Perform stemming.
//		SnowballStemmer stemmer = new englishStemmer();
//		String[] strArr = content.split(" ");
//		String o = "";
//		for (String s: strArr) {
//			stemmer.setCurrent(s);
//			stemmer.stem();
//			o = stemmer.getCurrent();
//			System.out.println(o);
//		}
//		return o;
//	}


	/**
	 * removes line separators like "\r\n", "\r" and "\n" from a string
	 * @param input the string you want to be cleaned
	 * @return a string without line separators
	 */
	public static String removeLineSeparators(String input) {
		return input.replaceAll("\\r\\n|\\r|\\n", " ");			
	}
	
}
