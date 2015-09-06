package download;
 
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML link extractor
 * @author mkyong
 * Source: http://www.mkyong.com/regular-expressions/how-to-extract-html-links-with-regular-expression/
 */
public class HTMLLinkExtractor{
 
	  private Pattern patternTag, patternLink;
	  private Matcher matcherTag, matcherLink;
 
	  private static final String HTML_A_TAG_PATTERN = 
                      "(?i)<a([^>]+)>(.+?)</a>";
 
	  private static final String HTML_A_HREF_TAG_PATTERN = 
		  "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
	  
	  private static final String HTML_BETWEEN_QUOTATION_MARKS = "[\"']";
	  
	  public HTMLLinkExtractor(){
		  patternTag = Pattern.compile(HTML_A_TAG_PATTERN);
		  patternLink = Pattern.compile(HTML_A_HREF_TAG_PATTERN);
	  }
 
	  /**
	   * Validate html with regular expression
	   * @param html html content for validation
	   * @return Vector links and link text
	   */
	  public Vector<HtmlLink> grabHTMLLinks(final String html){
 
		  Vector<HtmlLink> result = new Vector<HtmlLink>();
 
		  matcherTag = patternTag.matcher(html);
 
		  while(matcherTag.find()){
 
			  String href = matcherTag.group(1).replaceAll(HTML_BETWEEN_QUOTATION_MARKS, ""); //href
			  String linkText = matcherTag.group(2); //link text
 
			  matcherLink = patternLink.matcher(href);
 
			  while(matcherLink.find()){
 
				  String link = matcherLink.group(1); //link
				  result.add(new HtmlLink(link, linkText));
 
			  }
 
		  }
 
		  return result;
 
	  }
 
	class HtmlLink {
 
		String link;
		String linkText;
 
		HtmlLink(String link, String linkText){
			this.link = link;
			this.linkText = linkText;
		}
 
		@Override
		public String toString() {
			return new StringBuffer("Link : ")
			.append(this.link)
			.append(" Link Text : ")
			.append(this.linkText).toString();
		}	    
	}
}