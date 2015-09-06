package events;

public interface CustomEventInterface {

	public void addCustomEventListener(CustomEventListener listener);
	public void removeCustomEventListener(CustomEventListener listener);
	public void fireCustomEvent(CustomEvent evt);
	
}
