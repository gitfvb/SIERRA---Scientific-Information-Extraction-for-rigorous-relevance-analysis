package utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeUtilities {
	
	public static String getEnglishDateTime(Calendar calendar) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		if (calendar != null) {
			Date date = calendar.getTime();
	        return dateFormat.format(date);
		}
		return "";
	}
	
	public static String getCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
	
	public static String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date = new Date();
        return dateFormat.format(date);
    }
	
	public static String getYear(Date d) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy");
        return dateFormat.format(d);
	}
	
}
