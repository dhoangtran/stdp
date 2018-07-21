package stdp.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import stdp.util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.Math;

/**
 * This class represents a Solution of the Static Taxi Dispatching Problem
 *
 * @author Hoang Tran
 */
public class Solution {

	class Route {
		public Taxi taxi;
		public ArrayList<Request> route;
		
		public Route(Taxi _taxi) {
			taxi = _taxi;
		}
	}
	
	public final Problem problem;
	public final Route[] route;
	public double cost;
	
	/**
	 * Instantiates a new Solution.
	 *
	 * @param problem problem considered.
	 */
	
	public Solution(Problem _problem) {
		problem = _problem;
		route = new Route[problem.nTaxis];
	}

	/**
	 * Private constructor used for cloning.
	 *
	 * @param solution solution to copy from.
	 */
	private Solution(Solution solution) {
		this.problem = solution.problem;
		route = solution.route.clone();
	}

	/**
	 * Creates and returns a copy of this solution.
	 */
	public Solution clone() {
		return new Solution(this);
	}


	/**
	 * Updates (and returns) the cost of the solution.
	 *
	 * @return the updated solution cost.
	 */
	public double updateCost() {
		return cost;
	}
	
	/**
	 * Validates the solution.
	 *
	 * @param output output stream (example: System.out) to print eventual error
	 *               messages.
	 * @return true if the solution and its costs are valid and false otherwise.
	 */
	public boolean validate() {
		boolean valid = true;

		// initial supply
		int[] supply = new int[problem.nRegions];
		for (int i = 0; i < problem.nRegions; i++) {
			supply[i] = problem.initialSupply[i];
		}

		for (int k = 0; k < problem.nTimeHorizons; k++) { 
			// update supply after dispatch
			for (int i = 0; i < problem.nRegions; i++) {
				for (int j = 0; j < problem.nRegions; j++) {
					if (i < j && dispatch[MVMapping[k][i][j]] > 0) {
						supply[i] -= dispatch[MVMapping[k][i][j]];
						supply[j] += dispatch[MVMapping[k][i][j]];
					} else if (j < i && dispatch[MVMapping[k][j][i]] < 0) {
						supply[i] -= -dispatch[MVMapping[k][j][i]];
						supply[j] += -dispatch[MVMapping[k][j][i]];
					}
				}
			}
			// update supply after dispatch
			for (int i = 0; i < problem.nRegions; i++) {
				if (supply[i] < 0) {
					valid = false;
					//Util.safePrintf(output, "Supply %d at time %d is nagative\n", i, k);
				}
			}

			// update supply after mobility
			double[] temp = new double[problem.nRegions];
			for (int ii = 0; ii < problem.nRegions; ii++) {
				for (int jj = 0; jj < problem.nRegions; jj++) {
					temp[ii] += supply[jj] * problem.mobilityPattern[jj][ii];
				}
			}
			for (int ii = 0; ii < problem.nRegions; ii++) {
				supply[ii] = (int) Math.round(temp[ii]);
			}
		}

		return valid;
	}

	/**
	 * Validates the solution.
	 *
	 * @param output output stream (example: System.out) to print eventual error
	 *               messages.
	 * @return true if the solution and its costs are valid and false otherwise.
	 */
	public boolean validate(int position, int alpha) {
		boolean valid = false;
		int k = VMMapping[position][0];
		int i = VMMapping[position][1];
		int j = VMMapping[position][2];
		if (alpha > 0 && supplies[k][i] - alpha >= 0) valid = true;
		else if (alpha < 0 && supplies[k][j] + alpha >= 0) valid = true;
		return valid;
	}

	public int getRange(int position) {
		int k = VMMapping[position][0];
		int i = VMMapping[position][1];
		int j = VMMapping[position][2];
		return supplies[k][i] + supplies[k][j];
	}
	
	public List<Solution> getNeighbors() {
		int alpha = 0;
		List<Solution> neighbors = new LinkedList<Solution>();
		for (int i = 0; i < this.size; i++) {
			Solution neighbor1 = this.clone();
			Solution neighbor2 = this.clone();
			neighbor1.dispatch[i] += alpha;
			if (neighbor1.validate()) {
				neighbor1.updateTotalCost();
				neighbors.add(neighbor1);
			}
			neighbor2.dispatch[i] -= alpha;
			if (neighbor2.validate()) {
				neighbor2.updateTotalCost();
				neighbors.add(neighbor2);
			}
		}
		return neighbors;
	}

	@Override
	public boolean equals(Object o) {
		return ((Integer) this.hashCode()).equals(((Solution)o).hashCode());
	}

	/**
	 * Reads the solution from a file.
	 *
	 * @param filePath the output file path.
	 * @throws IOException in case any IO error occurs.
	 */
	public void read(String filePath) throws IOException {
		BufferedReader reader = Files.newBufferedReader(Paths.get(filePath));
		SimpleTokenizer token;
		for (int k = 0; k < problem.nTimeHorizons; k++) { 
			for (int i = 0; i < problem.nRegions; i++) {
				token = new SimpleTokenizer(reader.readLine());
				for (int j = 0; j < problem.nRegions; j++) {
					int h = token.nextInteger();
					if (i < j && h > 0) {
						dispatch[MVMapping[k][i][j]] = h;
					} else if (j < i && h > 0) {
						dispatch[MVMapping[k][j][i]] = -h;
					}
				}
			}
			reader.readLine();
		}
		updateTotalCost();
		reader.close();
	}

	/**
	 * Writes the solution to a file.
	 *
	 * @param filePath the output file path.
	 * @throws IOException in case any IO error occurs.
	 */
	public void write(String filePath) throws IOException {
		PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(filePath)));

		// print dispatch solution
		writer.printf("Dispatch solution\n");
		for (int i = 0; i < problem.nTimeHorizons * problem.nRegions * (problem.nRegions - 1) / 2; i++) { 
			writer.printf("%d ", dispatch[i]);
		}
		writer.printf("\n\n");

		// print supply
		int[] supply = new int[problem.nRegions];
		for (int i = 0; i < problem.nRegions; i++) {
			supply[i] = problem.initialSupply[i];
		}
		for (int k = 0; k < problem.nTimeHorizons; k++) { 
			writer.printf("k = %d\n", k+1);
			// print dispatch solution
			/*
        	writer.printf("Dispatch solution\n");
			for (int i = 0; i < problem.nRegions; i++) {
				for (int j = 0; j < problem.nRegions; j++) {
					if (j < i) {
						if (dispatchSolution[solutionMapping[k][j][i]] < 0) {
							writer.printf("%d ", -dispatchSolution[solutionMapping[k][j][i]]);
						} else {
							writer.printf("0 ");
						}
					} else if (j == i) {
						writer.printf("0 ");
					} else if (j > i) {
						if (dispatchSolution[solutionMapping[k][i][j]] > 0) {
							writer.printf("%d ", dispatchSolution[solutionMapping[k][i][j]]);
						} else {
							writer.printf("0 ");
						}
					}
				}
				writer.printf("\n");
			}
			 */
			// print demand
			writer.printf("Demand   ");
			for (int i = 0; i < problem.nRegions; i++) {
				writer.printf("%d ", problem.predictedDemand[k][i]);
			}
			// print supply after dispatch
			for (int i = 0; i < problem.nRegions; i++) {
				for (int j = 0; j < problem.nRegions; j++) {
					if (i < j && dispatch[MVMapping[k][i][j]] > 0) {
						supply[i] -= dispatch[MVMapping[k][i][j]];
						supply[j] += dispatch[MVMapping[k][i][j]];
					} else if (j < i && dispatch[MVMapping[k][j][i]] < 0) {
						supply[i] -= -dispatch[MVMapping[k][j][i]];
						supply[j] += -dispatch[MVMapping[k][j][i]];
					}
				}
			}
			writer.printf("\nSupply   ");
			for (int i = 0; i < problem.nRegions; i++) {
				writer.printf("%d ", supply[i]);
			}
			// print supply after mobility
			writer.printf("\nMobility ");
			double[] temp = new double[problem.nRegions];
			for (int ii = 0; ii < problem.nRegions; ii++) {
				for (int jj = 0; jj < problem.nRegions; jj++) {
					temp[ii] += supply[jj] * problem.mobilityPattern[jj][ii];
				}
			}
			for (int ii = 0; ii < problem.nRegions; ii++) {
				supply[ii] = (int) Math.round(temp[ii]);
				writer.printf("%d ", supply[ii]);
			}
			writer.printf("\n\n");
		}

		updateTotalCost();
		writer.printf("Driving cost=%f\n", drivingCost);
		writer.printf("Balancing cost=%f\n", balancingCost);
		writer.printf("Total cost=%f\n", totalCost);
		writer.close();
	}
}

// backup

///**
// * This class represents a Solution of the Unrelated Parallel Machine Scheduling
// * Problem..
// *
// * @author Hoang Tran
// */
//public class Solution {
//
//	public final Problem problem;
//    public final int[] dispatchSolution;
//    public final int solutionSize;
//    protected Double JDCost;
//    protected Double JECost;
//    protected Double dispatchCost;
//    private int[][][] solutionMapping;
//	
//    /**
//     * Instantiates a new Solution.
//     *
//     * @param problem problem considered.
//     */
//    public Solution(Problem problem) {
//        this.problem = problem;
//
//        solutionSize = problem.nTimeHorizons * problem.nRegions * (problem.nRegions - 1) / 2;
//        dispatchSolution = new int[solutionSize];
//		dispatchCost = 0.0;
//		solutionMapping = new int[problem.nTimeHorizons][problem.nRegions][problem.nRegions];
//		for (int k = 0; k < problem.nTimeHorizons; k++) { 
//			for (int i = 0; i < problem.nRegions - 1; i++) {
//				for (int j = i + 1; j < problem.nRegions; j++) {
//					solutionMapping[k][i][j] = k * problem.nRegions * (problem.nRegions - 1) / 2 + i * problem.nRegions - i * (i + 1) /2 + j - i - 1;
//				}
//			}
//		}
//    }
//
//    /**
//     * Private constructor used for cloning.
//     *
//     * @param solution solution to copy from.
//     */
//    private Solution(Solution solution) {
//        this.problem = solution.problem;
//
//        solutionSize = problem.nTimeHorizons * problem.nRegions * (problem.nRegions - 1) / 2;
//        dispatchSolution = new int[solutionSize];
//		for (int i = 0; i < solutionSize; i++) {
//            dispatchSolution[i] = solution.dispatchSolution[i];
//        }
//		solutionMapping = new int[problem.nTimeHorizons][problem.nRegions][problem.nRegions];
//		for (int k = 0; k < problem.nTimeHorizons; k++) { 
//			for (int i = 0; i < problem.nRegions - 1; i++) {
//				for (int j = i + 1; j < problem.nRegions; j++) {
//					solutionMapping[k][i][j] = k * problem.nRegions * (problem.nRegions - 1) / 2 + i * problem.nRegions - i * (i + 1) /2 + j - i - 1;
//				}
//			}
//		}
//		this.dispatchCost = solution.dispatchCost;
//    }
//
//    /**
//     * Creates and returns a copy of this solution.
//     */
//    public Solution clone() {
//        return new Solution(this);
//    }
//
//    public void readFromFile(String instancePath) throws IOException {
//        BufferedReader reader = Files.newBufferedReader(Paths.get(instancePath));
//
//        SimpleTokenizer token;
//        
//        for (int k = 0; k < problem.nTimeHorizons; k++) { 
//			for (int i = 0; i < problem.nRegions; i++) {
//				token = new SimpleTokenizer(reader.readLine());
//				for (int j = 0; j < problem.nRegions; j++) {
//					int h = token.nextInteger();
//					if (i < j && h > 0) {
//						dispatchSolution[solutionMapping[k][i][j]] = h;
//					} else if (j < i && h > 0) {
//						dispatchSolution[solutionMapping[k][j][i]] = -h;
//					}
//				}
//			}
//	        reader.readLine();
//		}
//        
//        updateCost();
//        
//        reader.close();
//    }
//    
//    /**
//     * Gets the solution makespan. Note the the makespan may be outdated if the
//     * solution was modified. To ensure that it is updated, call {@link
//     * #updateCost()}.
//     *
//     * @return the solution cost.
//     */
//    public Double getCost() {
//        return dispatchCost;
//    }
//    
//    public Double getJD() {
//        return JDCost;
//    }
//
//    public Double getJE() {
//        return JECost;
//    }
//    
//    /**
//     * Updates (and returns) the makespan of the solution.
//     *
//     * @return the updated solution cost.
//     */
//    public double updateCost() {
//    	double drivingCost = 0;
//    	double balancingCost = 0;
//        
//    	// compute driving cost
//        for (int k = 0; k < problem.nTimeHorizons; k++) { 
//			for (int i = 0; i < problem.nRegions; i++) {
//				for (int j = 0; j < problem.nRegions; j++) {
//					if (i < j && dispatchSolution[solutionMapping[k][i][j]] > 0) {
//						drivingCost += dispatchSolution[solutionMapping[k][i][j]] * problem.distances[i][j];
//					} else if (j < i && dispatchSolution[solutionMapping[k][j][i]] < 0) {
//						drivingCost += -dispatchSolution[solutionMapping[k][j][i]] * problem.distances[i][j];
//					}			
//				}
//			}
//		}
//        
//        // compute balancing cost
//        int[] supply = new int[problem.nRegions];
//        for (int i = 0; i < problem.nRegions; i++) {
//        	supply[i] = problem.initialSupply[i];
//        }
//        for (int k = 0; k < problem.nTimeHorizons; k++) {
//        	// update supply after dispatch
//        	for (int i = 0; i < problem.nRegions; i++) {
//				for (int j = 0; j < problem.nRegions; j++) {
//					if (i < j && dispatchSolution[solutionMapping[k][i][j]] > 0) {
//						supply[i] -= dispatchSolution[solutionMapping[k][i][j]];
//						supply[j] += dispatchSolution[solutionMapping[k][i][j]];
//					} else if (j < i && dispatchSolution[solutionMapping[k][j][i]] < 0) {
//						supply[i] -= -dispatchSolution[solutionMapping[k][j][i]];
//						supply[j] += -dispatchSolution[solutionMapping[k][j][i]];
//					}
//				}
//			}
//        	// compute cost
//			for (int i = 0; i < problem.nRegions; i++) {
//				if (supply[i] > 0) 
//					balancingCost += Math.abs(problem.predictedDemand[k][i] * 1.0 / supply[i] - problem.getTotalDemand(k) * 1.0 / problem.getTotalSupply());
//				else
//					balancingCost += Math.abs(problem.predictedDemand[k][i] * 1.0 / 1 - problem.getTotalDemand(k) * 1.0 / problem.getTotalSupply());
//			}
//			// update supply after mobility
//			double[] temp = new double[problem.nRegions];
//			for (int ii = 0; ii < problem.nRegions; ii++) {
//				for (int jj = 0; jj < problem.nRegions; jj++) {
//					temp[ii] += supply[jj] * problem.mobilityPattern[jj][ii];
//				}
//			}
//			for (int ii = 0; ii < problem.nRegions; ii++) {
//				supply[ii] = (int) Math.round(temp[ii]);
//			}
//		}
//		
//        //JDCost = Math.round(drivingCost);
//        //JECost = Math.round(balancingCost);
//        //dispatchCost = Math.round(drivingCost + problem.weightFactor * balancingCost); 
//        JDCost = drivingCost;
//        JECost = balancingCost;
//        dispatchCost = drivingCost + problem.weightFactor * balancingCost; 
//        return dispatchCost;
//    }
//
//    /**
//     * Validates the solution.
//     *
//     * @param output output stream (example: System.out) to print eventual error
//     *               messages.
//     * @return true if the solution and its costs are valid and false otherwise.
//     */
//    public boolean validate() {
//        boolean valid = true;
//        
//        // initial supply
//		int[] supply = new int[problem.nRegions];
//		for (int i = 0; i < problem.nRegions; i++) {
//			supply[i] = problem.initialSupply[i];
//		}
//		
//		for (int k = 0; k < problem.nTimeHorizons; k++) { 
//			// update supply after dispatch
//        	for (int i = 0; i < problem.nRegions; i++) {
//				for (int j = 0; j < problem.nRegions; j++) {
//					if (i < j && dispatchSolution[solutionMapping[k][i][j]] > 0) {
//						supply[i] -= dispatchSolution[solutionMapping[k][i][j]];
//						supply[j] += dispatchSolution[solutionMapping[k][i][j]];
//					} else if (j < i && dispatchSolution[solutionMapping[k][j][i]] < 0) {
//						supply[i] -= -dispatchSolution[solutionMapping[k][j][i]];
//						supply[j] += -dispatchSolution[solutionMapping[k][j][i]];
//					}
//				}
//			}
//			// update supply after dispatch
//			for (int i = 0; i < problem.nRegions; i++) {
//				if (supply[i] < 0) {
//					valid = false;
//	                //Util.safePrintf(output, "Supply %d at time %d is nagative\n", i, k);
//				}
//			}
//			
//			// update supply after mobility
//			double[] temp = new double[problem.nRegions];
//			for (int ii = 0; ii < problem.nRegions; ii++) {
//				for (int jj = 0; jj < problem.nRegions; jj++) {
//					temp[ii] += supply[jj] * problem.mobilityPattern[jj][ii];
//				}
//			}
//			for (int ii = 0; ii < problem.nRegions; ii++) {
//				supply[ii] = (int) Math.round(temp[ii]);
//			}
//		}
//
//        return valid;
//    }
//
//    public List<Solution> getNeighbors() {
//    	int alpha = 0;
//    	List<Solution> neighbors = new LinkedList<Solution>();
//    	for (int i = 0; i < this.solutionSize; i++) {
//    		Solution neighbor1 = this.clone();
//    		Solution neighbor2 = this.clone();
//    		neighbor1.dispatchSolution[i] += alpha;
//    		if (neighbor1.validate()) {
//    			neighbor1.updateCost();
//    			neighbors.add(neighbor1);
//    		}
//    		neighbor2.dispatchSolution[i] -= alpha;
//    		if (neighbor2.validate()) {
//    			neighbor2.updateCost();
//    			neighbors.add(neighbor2);
//    		}
//    	}
//    	return neighbors;
//    }
//    
//    @Override
//    public boolean equals(Object o) {
//    	return Arrays.equals(this.dispatchSolution, ((Solution)o).dispatchSolution);
//    }
//    
//    @Override
//    public int hashCode() {
//    	return this.dispatchSolution.hashCode();
//    }
//    
//    /**
//     * Writes the solution to a file.
//     *
//     * @param filePath the output file path.
//     * @throws IOException in case any IO error occurs.
//     */
//    public void write(String filePath) throws IOException {
//        PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(filePath)));
//        
//        // print dispatch solution
//        writer.printf("Dispatch solution\n");
//        for (int i = 0; i < problem.nTimeHorizons * problem.nRegions * (problem.nRegions - 1) / 2; i++) { 
//			writer.printf("%d ", dispatchSolution[i]);
//		}
//        writer.printf("\n\n");
//        
//        // print supply
//		int[] supply = new int[problem.nRegions];
//        for (int i = 0; i < problem.nRegions; i++) {
//        	supply[i] = problem.initialSupply[i];
//        }
//        for (int k = 0; k < problem.nTimeHorizons; k++) { 
//        	writer.printf("k = %d\n", k+1);
//        	// print dispatch solution
//        	/*
//        	writer.printf("Dispatch solution\n");
//			for (int i = 0; i < problem.nRegions; i++) {
//				for (int j = 0; j < problem.nRegions; j++) {
//					if (j < i) {
//						if (dispatchSolution[solutionMapping[k][j][i]] < 0) {
//							writer.printf("%d ", -dispatchSolution[solutionMapping[k][j][i]]);
//						} else {
//							writer.printf("0 ");
//						}
//					} else if (j == i) {
//						writer.printf("0 ");
//					} else if (j > i) {
//						if (dispatchSolution[solutionMapping[k][i][j]] > 0) {
//							writer.printf("%d ", dispatchSolution[solutionMapping[k][i][j]]);
//						} else {
//							writer.printf("0 ");
//						}
//					}
//				}
//				writer.printf("\n");
//			}
//			*/
//        	// print demand
//        	writer.printf("Demand   ");
//        	for (int i = 0; i < problem.nRegions; i++) {
//        		writer.printf("%d ", problem.predictedDemand[k][i]);
//        	}
//			// print supply after dispatch
//			for (int i = 0; i < problem.nRegions; i++) {
//				for (int j = 0; j < problem.nRegions; j++) {
//					if (i < j && dispatchSolution[solutionMapping[k][i][j]] > 0) {
//						supply[i] -= dispatchSolution[solutionMapping[k][i][j]];
//						supply[j] += dispatchSolution[solutionMapping[k][i][j]];
//					} else if (j < i && dispatchSolution[solutionMapping[k][j][i]] < 0) {
//						supply[i] -= -dispatchSolution[solutionMapping[k][j][i]];
//						supply[j] += -dispatchSolution[solutionMapping[k][j][i]];
//					}
//				}
//			}
//			writer.printf("\nSupply   ");
//        	for (int i = 0; i < problem.nRegions; i++) {
//        		writer.printf("%d ", supply[i]);
//        	}
//			// print supply after mobility
//			writer.printf("\nMobility ");
//			double[] temp = new double[problem.nRegions];
//			for (int ii = 0; ii < problem.nRegions; ii++) {
//				for (int jj = 0; jj < problem.nRegions; jj++) {
//					temp[ii] += supply[jj] * problem.mobilityPattern[jj][ii];
//				}
//			}
//			for (int ii = 0; ii < problem.nRegions; ii++) {
//				supply[ii] = (int) Math.round(temp[ii]);
//				writer.printf("%d ", supply[ii]);
//			}
//			writer.printf("\n\n");
//		}
//        
//        updateCost();
//        writer.printf("JD=%f\n", JDCost);
//        writer.printf("JE=%f\n", JECost);
//        writer.printf("Cost=%f\n", dispatchCost);
//        writer.close();
//    }
//}