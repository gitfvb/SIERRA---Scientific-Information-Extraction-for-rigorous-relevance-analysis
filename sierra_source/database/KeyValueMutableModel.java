package database;

import java.util.Vector;

import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListDataListener;

/**
 * 
 * help: http://java-tutorial.org/comboboxmodel.html
 * 
 * @author Florian Friedrichs
 *
 */
public class KeyValueMutableModel implements MutableComboBoxModel {

	public static String NOTHING_SELECTED_KEY = "--";
	public static String NOTHING_SELECTED_VALUE = "nothing";
	
	// list for all KeyValue-Objects
    Vector<KeyValue> keysAndValues = new Vector<KeyValue>();
    // Index für selektierten Eintrag
    int index=-1;
	
    public KeyValueMutableModel() {
    	keysAndValues.add(new KeyValue(NOTHING_SELECTED_KEY, NOTHING_SELECTED_VALUE));
    }
    
	@Override
	public Object getSelectedItem() {
		if(index >= 0) {
            return keysAndValues.elementAt(index).getKeyValue();
        } else {
            return "";
        }
	}
	
	public KeyValue getSelectedKeyValue() {
		if(index >= 0) {
            return keysAndValues.elementAt(index);
        } else {
            return null;
        }
	}

	@Override
	public void setSelectedItem(Object keyValue) {
		for(int i = 0; i< keysAndValues.size(); i++) {
            if(keysAndValues.elementAt(i).getKeyValue().toString().equals(keyValue)) {
                index = i;
                break;
            }
        }
	}

	@Override
	public void addListDataListener(ListDataListener arg0) {
		// not necessary here
	}

	@Override
	public Object getElementAt(int index) {
		return keysAndValues.get(index).getKeyValue();
	}

	@Override
	public int getSize() {
		return keysAndValues.size();
	}

	@Override
	public void removeListDataListener(ListDataListener arg0) {
		// not necessary here
	}

	@Override
	public void addElement(Object keyValue) {
		keysAndValues.add((KeyValue) keyValue);
	}

	@Override
	public void insertElementAt(Object keyValue, int index) {
		keysAndValues.add(index, (KeyValue)keyValue);
	}

	@Override
	public void removeElement(Object keyValue) {
		keysAndValues.remove(keyValue);		
	}

	@Override
	public void removeElementAt(int index) {
		keysAndValues.remove(index);
	}

}
