package stdp.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


/**
 * This class represents an Static Taxi Dispatching Problem.
 *
 * @author Hoang Tran
 */
public class Problem {

	public class Taxi {
		public int time0;
		public int location0;
		
		public Taxi(int _time0, int _location0) {
			time0 = _time0;
			location0 = _location0;
		}
	}
	
	public class Request {
		public int time;
		public int pickup;
		public int delivery;
		
		public Request(int _time, int _pickup, int _delivery) {
			time = _time;
			pickup = _pickup;
			delivery = _delivery;
		}
	}
	
    /***
     * Number of regions
     */
    public final int nRegions;

    /***
     * Matrix with the distance of a region to region
     * distances[region][region]
     */
    public final double distances[][];

    /***
     * Matrix with average travel time from region to region
     * travelTimes[region][region]
     */
    //public final double travelTimes[][];

    /***
     * Array with the list of Taxis 
     * taxis[region]
     */
    public final Taxi taxis[];

    /***
     * Array with the sequence of Taxis 
     * taxis[region]
     */
    public final Request requests[];

    /**
     * Instantiates a new Problem from a file.
     *
     * @param instancePath the instance file path
     */
    public Problem(int _nRegions, String distances_file, String taxis_file, String requests_file) throws IOException {
    
    	BufferedReader reader = null;
    	String line = null;
    	
    	reader = Files.newBufferedReader(Paths.get(distances_file));
    	line = reader.readLine();
    	nRegions = Integer.parseInt(line);
    	distances = new double[nRegions][nRegions];
    	for (int i = 0; i < nRegions; i++)
    	{
    		line = reader.readLine();
    		String[] arrString = line.split(",");
    		for (int j = 0; j < nRegions; j++) {
    			distances[i][j] = Double.parseDouble(arrString[j]);
    		}
    	}	
    	reader.close();
    	
    	ArrayList<Taxi> taxisList = new ArrayList<Taxi>();
    	reader = Files.newBufferedReader(Paths.get(taxis_file));
    	while((line = reader.readLine()) != null) {
        	String[] arrString = line.split(",");
        	if (arrString.length == 2) {
        		int time0 = Integer.parseInt(arrString[0]);
        		int location0 = Integer.parseInt(arrString[1]);
        		taxisList.add(new Taxi(time0, location0));
        	}
        }
        reader.close();
        taxis = taxisList.toArray(new Taxi[taxisList.size()]);
        
    	ArrayList<Request> requestsList = new ArrayList<Request>();
    	reader = Files.newBufferedReader(Paths.get(requests_file));
    	while((line = reader.readLine()) != null) {
        	String[] arrString = line.split(",");
        	if (arrString.length == 3) {
        		int time = Integer.parseInt(arrString[0]);
        		int pickup = Integer.parseInt(arrString[1]);
        		int delivery = Integer.parseInt(arrString[1]);
        		requestsList.add(new Request(time, pickup, delivery));
        	}
        }
        reader.close();
        requests = requestsList.toArray(new Request[requestsList.size()]);
    }
}
