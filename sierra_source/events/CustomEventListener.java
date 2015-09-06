package events;

import java.util.EventListener;

public interface CustomEventListener extends EventListener {
	
	    public void customEventOccurred(CustomEvent evt);
	
}
