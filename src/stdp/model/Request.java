package stdp.model;

public class Request {
	public int requestId;
	public int time;
	public int pickup;
	public int delivery;
	public boolean isRouted;
	
	public Request(int _time, int _pickup, int _delivery) {
		requestId = 0;
		time = _time;
		pickup = _pickup;
		delivery = _delivery;
		isRouted = false;
	}
}
