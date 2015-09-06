package database;

public class DataSource {

	private String link = "";
	private String conference = "";	
	private String year = "";
	
	/**
	 * @return the conference
	 */
	public String getConference() {
		return conference;
	}

	/**
	 * @param conference the conference to set
	 */
	public void setConference(String conference) {
		this.conference = conference;
	}

	/**
	 * @return the year
	 */
	public String getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(String year) {
		this.year = year;
	}

	
	
	public DataSource(String link) {
		this.setLink(link);
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getLink() {
		return link;
	}
}
