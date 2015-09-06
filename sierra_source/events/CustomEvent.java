package events;

import java.util.EventObject;

public class CustomEvent extends EventObject {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -1449237565092414961L;

	private String message = "";
	
	public CustomEvent(Object source) {
		super(source);
    }
	
	public CustomEvent setMessage(String message) {
		this.message = message;
		return this;
	}

	public String getMessage() {
		return message;
	}
	
	

}
