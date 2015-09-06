package utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * helper class to handle collections
 * @author Florian Friedrichs
 *
 */
public class CollectionsUtilities {


	/**
	 * 
	 * @param m
	 * @return
	 */
	public static List<String> sortByValueDesc(final Map<String, Integer> m) {		
		List<String> keys = sortByValueAsc(m);
		Collections.reverse(keys);
		return keys;
	}
	
	
	/**
	 * 
	 * source: http://www.xinotes.org/notes/note/306/
	 * @param m
	 * @return
	 */
	public static List<String> sortByValueAsc(final Map<String, Integer> m) {
        List<String> keys = new ArrayList<String>();
        keys.addAll(m.keySet());
        Collections.sort(keys, new Comparator<String>() {
            public int compare(String o1, String o2) {
                Integer v1 = m.get(o1);
                Integer v2 = m.get(o2);
                if (v1 == null) {
                    return (v2 == null) ? 0 : 1;
                }
                else if (v1 instanceof Comparable) {
                    Comparable<Integer> comparable = v1;
					return comparable.compareTo(v2);
                }
                else {
                    return 0;
                }
            }
        });
        
        return keys;
    }
	
}
