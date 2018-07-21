package stdp.model;

import java.util.ArrayList;

public class Taxi {
	
	public int taxiId;
	public int time0;
	public int location0;
	
    
	public Taxi(int _time0, int _location0) {
		taxiId = 0;
		time0 = _time0;
		location0 = _location0;
		route = new ArrayList<Request>();
	}
}