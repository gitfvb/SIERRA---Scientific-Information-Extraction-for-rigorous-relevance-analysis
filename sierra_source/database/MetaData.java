package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import utilities.StringUtilities;


public class MetaData {

	public static String STANDARDFILENAME = "metadata.xml";
	
	private String fileName = "";
	private String fileSize = "";
	private String dataSource = "";
	private String numberOfPages = "";
	private String title = "";
	private String author = "";
	private String subject = "";
	private String keywords = "";
	private String creator = "";
	private String producer = "";
	private String creationDate = "";
	private String modificationDate = "";
	private String trapped = "";
	private String language = "";
	private String conferenceName = "";
	private String conferenceYear = "";
	private String downloadDate = "";
	private String comment = "";
	private String pdfhash = "";
	private String txthash = "";
	
	public MetaData readFile(File fXmlFile) throws ParserConfigurationException {
		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()),"ISO-8859-1");
//			InputStreamReader isr = new InputStreamReader(new FileInputStream(fXmlFile), "ISO-8859-1");
			InputStream is = new FileInputStream(fXmlFile);
			
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("file");			
			Node nNode = nList.item(0);
			Element eElement = (Element) nNode;
			
			this.fileName = (eElement.hasAttribute("filename")) ? eElement.getAttribute("filename") : "";			
			this.fileSize = getTagValue("filesize", eElement);
			this.dataSource = getTagValue("source", eElement);
			this.numberOfPages = getTagValue("numberofpages", eElement);
			this.title = getTagValue("title", eElement);
			this.author = getTagValue("author", eElement);
			this.subject = getTagValue("subject", eElement);
			this.keywords = getTagValue("keywords", eElement);
			this.creator = getTagValue("creator", eElement);
			this.producer = getTagValue("producer", eElement);
			this.creationDate = getTagValue("creationdate", eElement);
			this.modificationDate = getTagValue("modificationdate", eElement);
			this.trapped = getTagValue("trapped", eElement);					
			this.language = getTagValue("language", eElement);
			this.conferenceName = getTagValue("conferencename", eElement);
			this.conferenceYear = getTagValue("conferenceyear", eElement);
			this.downloadDate = getTagValue("downloaddate", eElement);
			this.comment = getTagValue("comment", eElement);
			this.pdfhash = getTagValue("pdfhash", eElement);
			this.txthash = getTagValue("txthash", eElement);

		} catch (SAXException e) {
			;
		} catch (IOException e) {
			;
		}
			
		
		
		
		return this;
	}
	
	public static String[] getTitleRow() {
		List<String> l = new ArrayList<String>();
		l.add("filename");
		l.add("filesize");
		l.add("source");
		l.add("numberofpages");
		l.add("title");
		l.add("author");
		l.add("subject");
		l.add("keywords");
		l.add("creator");
		l.add("producer");
		l.add("creationdate");
		l.add("modificationdate");
		l.add("trapped");
		l.add("language");
		l.add("conferencename");
		l.add("conferenceyear");
		l.add("downloaddate");
		l.add("comment");
		l.add("pdfhash");
		l.add("txthash");
		
		
		String[] result = new String[l.size()];
		for (int i = 0; i < l.size(); i++) {
			result[i] = l.get(i);
		}
		return result;
	}
	
	public String[] readFileAsArray(File fXmlFile) throws SAXException, ParserConfigurationException {
		readFile(fXmlFile);
		List<String> l = new ArrayList<String>();
		l.add(fileName);
		l.add(fileSize);
		l.add(dataSource);
		l.add(numberOfPages);
		l.add(title);
		l.add(author);
		l.add(subject);
		l.add(keywords);
		l.add(creator);
		l.add(producer);
		l.add(creationDate);
		l.add(modificationDate);
		l.add(trapped);
		l.add(language);
		l.add(conferenceName);
		l.add(conferenceYear);
		l.add(downloadDate);
		l.add(comment);
		l.add(pdfhash);
		l.add(txthash);
		
		String[] result = new String[l.size()];
		for (int i = 0; i < l.size(); i++) {
			result[i] = StringUtilities.removeLineSeparators(l.get(i));
		}
		return result;
	}
	
	public boolean createNewFile(File file) {
		
		try {
		
			// initiate
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			
			// root element
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("file");
			doc.appendChild(rootElement);

						
			// write data
			
			// fileName
			rootElement.setAttribute("filename", fileName);
			
			// create elements
			rootElement.appendChild(createElement(doc, "filesize", normalizeString(this.fileSize)));
			rootElement.appendChild(createElement(doc, "source", normalizeString(this.dataSource)));
			rootElement.appendChild(createElement(doc, "numberofpages", normalizeString(this.numberOfPages)));
			rootElement.appendChild(createElement(doc, "title", normalizeString(this.title)));
			rootElement.appendChild(createElement(doc, "author", normalizeString(this.author)));
			rootElement.appendChild(createElement(doc, "subject", normalizeString(this.subject)));
			rootElement.appendChild(createElement(doc, "keywords", normalizeString(this.keywords)));
			rootElement.appendChild(createElement(doc, "creator", normalizeString(this.creator)));
			rootElement.appendChild(createElement(doc, "producer", normalizeString(this.producer)));
			rootElement.appendChild(createElement(doc, "creationdate", normalizeString(this.creationDate)));
			rootElement.appendChild(createElement(doc, "modificationdate", normalizeString(this.modificationDate)));
			rootElement.appendChild(createElement(doc, "trapped", normalizeString(this.trapped)));
			rootElement.appendChild(createElement(doc, "language", normalizeString(this.language)));
			rootElement.appendChild(createElement(doc, "conferencename", normalizeString(this.conferenceName)));
			rootElement.appendChild(createElement(doc, "conferenceyear", normalizeString(this.conferenceYear)));
			rootElement.appendChild(createElement(doc, "downloaddate", normalizeString(this.downloadDate)));
			rootElement.appendChild(createElement(doc, "comment", normalizeString(this.comment)));
			rootElement.appendChild(createElement(doc, "pdfhash", normalizeString(this.pdfhash)));
			rootElement.appendChild(createElement(doc, "txthash", normalizeString(this.txthash)));
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();			
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.VERSION, "1.1");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);
			
			transformer.transform(source, result);
			
			//System.out.println("File saved!");
			
		
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	
	
	   
	
	
	private String normalizeString(String input) {		
		String pattern = "[^" 
            + "\u0001-\uD7FF" 
            + "\uE000-\uFFFD"
            + "\ud800\udc00-\udbff\udfff" 
            + "]+";
		input = input.replaceAll(pattern, "");
		
		String invalidXmlPattern = "[^"
			+ "\\u0009\\u000A\\u000D"
			+ "\\u0020-\\uD7FF"
			+ "\\uE000-\\uFFFD"
			+ "\\u10000-\\u10FFFF"
			+ "]+";
		input = input.replaceAll(invalidXmlPattern, "");
		
		return StringEscapeUtils.escapeXml(input);
	}
	
	private Element createElement(Document doc, String key, String value) {
		Element e = doc.createElement(key);
		e.appendChild(doc.createTextNode(value));
		return e;
	}
	
	/**
	 * @return the fileSize
	 */
	public String getFileSize() {
		return fileSize;
	}

	/**
	 * @param fileSize the fileSize to set
	 */
	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	// beachten, dass sich diese Funktion immer nur das erste Element rausholt
	// bei mehreren möglichen Elementen sollte hier vielleicht ein Array zurück-
	// geliefert werden
	private String getTagValue(String sTag, Element eElement) {		
		if (eElement != null) {
			NodeList nlList = eElement.getElementsByTagName(sTag);		
			if (nlList.getLength() > 0) {
				NodeList nList = nlList.item(0).getChildNodes();
				Node nValue = (Node) nList.item(0);
				if (nValue != null) {
					return StringEscapeUtils.unescapeXml(nValue.getNodeValue());
				}				
			}
		}
		return "";
	}
	
	
	
	/*
	 * ------------------------------
	 * GETTER AND SETTER
	 * ------------------------------
	 */
	
	public void setFileName(String fileName) {
		this.fileName = (fileName != null) ? fileName : "";
	}

	public String getFileName() {
		return fileName;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = (dataSource != null) ? dataSource : "";
	}

	public String getDataSource() {
		return dataSource;
	}
	
	/**
	 * @return the numberOfPages
	 */
	public String getNumberOfPages() {
		return numberOfPages;
	}

	/**
	 * @param numberOfPages the numberOfPages to set
	 */
	public void setNumberOfPages(String numberOfPages) {
		this.numberOfPages = (numberOfPages != null) ? numberOfPages : "";
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = (title != null) ? title : "";
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {		
		this.author = (author != null) ? author : "";
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = (subject != null) ? subject : "";
	}

	/**
	 * @return the keywords
	 */
	public String getKeywords() {
		return keywords;
	}

	/**
	 * @param keywords the keywords to set
	 */
	public void setKeywords(String keywords) {		
		this.keywords = (keywords != null) ? keywords : "";
	}

	/**
	 * @return the creator
	 */
	public String getCreator() {
		return creator;
	}

	/**
	 * @param creator the creator to set
	 */
	public void setCreator(String creator) {
		this.creator = (creator != null) ? creator : "";
	}

	/**
	 * @return the producer
	 */
	public String getProducer() {
		return producer;
	}

	/**
	 * @param producer the producer to set
	 */
	public void setProducer(String producer) {
		this.producer = (producer != null) ? producer : "";
	}

	/**
	 * @return the creationDate
	 */
	public String getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(String creationDate) {
		this.creationDate = (creationDate != null) ? creationDate : "";
	}

	/**
	 * @return the modificationDate
	 */
	public String getModificationDate() {
		return modificationDate;
	}

	/**
	 * @param modificationDate the modificationDate to set
	 */
	public void setModificationDate(String modificationDate) {
		this.modificationDate = (modificationDate != null) ? modificationDate : "";
	}

	/**
	 * @return the trapped
	 */
	public String getTrapped() {
		return trapped;
	}

	/**
	 * @param trapped the trapped to set
	 */
	public void setTrapped(String trapped) {
		this.trapped = (trapped != null) ? trapped : "";
	}
	
	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = (language != null) ? language : "";
	}
	

	/**
	 * @return the downloadDate
	 */
	public String getDownloadDate() {
		return downloadDate;
	}

	/**
	 * @param downloadDate the downloadDate to set
	 */
	public void setDownloadDate(String downloadDate) {
		this.downloadDate = downloadDate;
	}

	/**
	 * @return the conferenceName
	 */
	public String getConferenceName() {
		return conferenceName;
	}

	/**
	 * @param conferenceName the conferenceName to set
	 */
	public void setConferenceName(String conferenceName) {
		this.conferenceName = conferenceName;
	}

	/**
	 * @return the conferenceYear
	 */
	public String getConferenceYear() {
		return conferenceYear;
	}

	/**
	 * @param conferenceYear the conferenceYear to set
	 */
	public void setConferenceYear(String conferenceYear) {
		this.conferenceYear = conferenceYear;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the pdfhash
	 */
	public String getPdfhash() {
		return pdfhash;
	}

	/**
	 * @param pdfhash the pdfhash to set
	 */
	public void setPdfhash(String pdfhash) {
		this.pdfhash = pdfhash;
	}

	/**
	 * @return the txthash
	 */
	public String getTxthash() {
		return txthash;
	}

	/**
	 * @param txthash the txthash to set
	 */
	public void setTxthash(String txthash) {
		this.txthash = txthash;
	}
	
}
