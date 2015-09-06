package database;

/**
 * 
 * This class helps to fill a combobox with a model of keys and values.
 * 
 * help: http://java-tutorial.org/comboboxmodel.html
 * 
 * @author Florian Friedrichs
 *
 */
public class KeyValue {

	private String key;
	private String value;
	
	public KeyValue(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public String getKeyValue(){
        return this.key.toString() + ": " + this.value.toString();
    }
	
	public String getKey() {
		return this.key.toString();
	}
	
	public static String getKey(String keyValue) {
		return keyValue.substring(0, keyValue.indexOf(":"));
	}
	
	public String getValue() {
		return this.value.toString();
	}
	
}
