package download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.event.EventListenerList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import utilities.DateTimeUtilities;
import utilities.FileUtilities;
import utilities.MD5Checksum;
import database.DataSource;
import database.MetaData;
import download.HTMLLinkExtractor.HtmlLink;
import events.CustomEvent;
import events.CustomEventInterface;
import events.CustomEventListener;



public class HttpCrawler implements CustomEventInterface {
	// custom events: http://www.exampledepot.com/egs/java.util/custevent.html


	public int lowestId = 10000000;
	public int highestId = 79999999;



	/**
	 * @return the highestId
	 */
	public int getHighestId() {
		return highestId;
	}

	/**
	 * @return the lowestId
	 */
	public int getLowestId() {
		return lowestId;
	}

	/**
	 * @param lowestId the lowestId to set
	 */
	public void setLowestId(int lowestId) {
		this.lowestId = lowestId;
	}

	/**
	 * @param highestId the highestId to set
	 */
	public void setHighestId(int highestId) {
		this.highestId = highestId;
	}

	private int maxDepth = 1;
	private ArrayList<String> linkList = new ArrayList<String>();

	public void getAllPdfLinks(DataSource dataSource, String targetFolder) {
		this.fireCustomEvent(new CustomEvent(this).setMessage("Crawler is starting..."));
		this.fireCustomEvent(new CustomEvent(this).setMessage("First, I will collect all pdf-links..."));
		this.getAllFiles(dataSource.getLink(), 0);
		this.fireCustomEvent(new CustomEvent(this).setMessage("Ok, I have ".concat(Integer.toString(linkList.size()).concat(" pdf-links."))));
		this.fireCustomEvent(new CustomEvent(this).setMessage("Starting download..."));
		this.downloadAllPdfFiles(targetFolder, dataSource);
		this.fireCustomEvent(new CustomEvent(this).setMessage("Download completed."));
	}

	private void getAllFiles(String link, int depth)  {

		if (depth <= maxDepth) {

			try {

				// Verbindung aufbauen
				HttpClient httpclient = new DefaultHttpClient();				
				httpclient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
				HttpGet httpget = new HttpGet(link);				
				HttpResponse response  = httpclient.execute(httpget);

				// Antwort auslesen
				HttpEntity entity = response.getEntity();

				// Typ auslesen
				ContentType contentType = getContentTypeFor(entity);


				switch (contentType) {

				case HTML:

					// Text auslesen
					String html = EntityUtils.toString(entity);


					// Links extrahieren
					HTMLLinkExtractor htmlLinkExtractor = new HTMLLinkExtractor();
					Vector<HtmlLink> htmlLinks = htmlLinkExtractor.grabHTMLLinks(html);

					// Rekursiv alle Links durchgehen
					for (HtmlLink htmlLink : htmlLinks) {						
						if (!(htmlLink.link.toString().contains("#"))) { // Verweise ausschließen, die auf die gleiche Seite verweisen								
							URI uri = URIUtils.resolve(httpget.getURI(), htmlLink.link.toString());
							String newLink = uri.toString();								
							getAllFiles(newLink, depth + 1);
						}
					}

					break;
				case PDF:

					if (entity != null) {
						String currentUrl = URLDecoder.decode(link, "UTF-8");
						fireCustomEvent(new CustomEvent(this).setMessage("pdf found: ".concat(currentUrl)));							
						System.out.println("pdf found: ".concat(currentUrl));							
						linkList.add(link);
					}


					break;
				default: break;

				}


				// alle Links aus dem Dokument rauskriegen (DOM wird noch nicht benötigt, da XML-Parser möglicherweise Fehler verursachen, da HTML oft nicht so konform ist)
				// Quelle für die regexp-Ausdrücke: http://www.mkyong.com/regular-expressions/how-to-extract-html-links-with-regular-expression/				

			} catch (ClientProtocolException e) {
				System.out.println("ClientProtocolException");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("IOException");
				e.printStackTrace();
			}

		}

	}

	private void downloadAllPdfFiles(String targetFolder, DataSource dataSource) {

		targetFolder = FileUtilities.checkDirectoryEnding(targetFolder);
			
		HttpClient httpclient = new DefaultHttpClient();
		for (int i = 0; i < linkList.size(); i++) {

			if (i%10==0) fireCustomEvent(new CustomEvent(this).setMessage("download file ".concat(Integer.toString(i)).concat(" of ").concat(Integer.toString(linkList.size()))));

			String filename = "";
			long filesize = 0;
			String folderId = Integer.toString(FileUtilities.getNextFreeId(targetFolder, getLowestId(), getHighestId()));
			String filepath = FileUtilities.checkDirectoryEnding(targetFolder.concat(folderId));			

			// create the folder
			File folder = new File(filepath);
			folder.mkdirs();
			
			String pdfFile = "";
			try {
				HttpGet httpget = new HttpGet(linkList.get(i));				
				HttpResponse response = httpclient.execute(httpget);				

				HttpEntity entity = response.getEntity();
				byte[] buf = EntityUtils.toByteArray(entity);
				
				filename = getFilename(response, linkList.get(i), folderId);								
				
				// create the file in this folder				
				try {					
					pdfFile = filepath.concat(filename);				
					FileOutputStream fos = new FileOutputStream(pdfFile);
					fos.write(buf);
					fos.close(); // close the stream
					filesize = new File(pdfFile).length();
				} catch (FileNotFoundException e) {
					; // just go on, write the data to the xml and do the next file
				} 
				
				// release the connection
				httpget.abort(); 
				
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// create an xml-file with the link to the file
			MetaData md = new MetaData();
			md.setDataSource(linkList.get(i));
			md.setFileName(filename);
			md.setPdfhash(MD5Checksum.getMD5Checksum(pdfFile));
			md.setConferenceName(dataSource.getConference());
			md.setConferenceYear(dataSource.getYear());
			md.setDownloadDate(DateTimeUtilities.getCurrentDate());
			md.setFileSize(Long.toString(filesize));
			md.createNewFile(new File(filepath.concat(folderId).concat(".xml")));

		}
		linkList.clear(); // after the download clear the linklist

	}



	private String getFilename(HttpResponse response, String link, String filenameAtFailure) {

		// example for header: [Content-disposition: inline; filename="AN ASSESSMENT OF M-HEALTH IN DEVELOPING COUNTRIES USING TASK TECH.pdf"]
		Header[] h = (Header[]) response.getHeaders("Content-disposition");

		String fileName = "";
		if (h.length > 0) {			
			Pattern regex = Pattern.compile("(?<=filename=\").*?(?=\")");
			Matcher regexMatcher = regex.matcher(h[0].getValue());
			if (regexMatcher.find()) {
				fileName = regexMatcher.group();
			}		
		} else if (link.toLowerCase().endsWith(".pdf")) {


			try {
				File f = new File(new URI(link).getPath());				
				fileName  = f.getName();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

		}
		
		// last chance to get a filename
		if (fileName.isEmpty() || fileName == "" || fileName.length() == 0) {
			// last chance: give a failure filename
			fileName = filenameAtFailure.concat(".pdf");
		}
		
		// last chance to get a file-ending
		if (!fileName.toLowerCase().endsWith(".pdf")) {
			// last chance: give a failure filename
			fileName = fileName.concat(".pdf");
		} 
		

		return FileUtilities.sanitizeFilename(fileName);
		
	}

	private ContentType getContentTypeFor(HttpEntity entity) {
		String s = entity.getContentType().getValue().toString();
		if (s.startsWith("application/pdf")) return ContentType.PDF;
		else if (s.startsWith("text/html")) return ContentType.HTML;
		else return null;
		// Notiz: kann man wohl auch mit EntityUtils.getContentMimeType(entity); machen 

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
