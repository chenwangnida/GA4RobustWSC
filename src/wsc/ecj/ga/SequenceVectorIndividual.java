package wsc.ecj.ga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ec.EvolutionState;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import ec.vector.VectorIndividual;
import wsc.InitialWSCPool;
import wsc.data.pool.Service;
import wsc.dynamic.localsearch.LocalSearch;
import wsc.graph.ServiceGraph;

public class SequenceVectorIndividual extends VectorIndividual{

	private static final long serialVersionUID = 1L;

	private double availability;
	private double reliability;
	private double time;
	private double cost;
	private double matchingType;
	private double semanticDistance;

	private String strRepresentation; // a string of graph-based representation
	private double fitness_value = 0.0; // The higher, the fitterSSSS

	public Service[] genome;
	public List<Service> relevantList;
	private int splitPosition;
	public List<Integer> serQueue = new ArrayList<Integer>(); // service Index arraylist
	

	public double getAvailability() {
		return availability;
	}

	public void setAvailability(double availability) {
		this.availability = availability;
	}

	public double getReliability() {
		return reliability;
	}

	public void setReliability(double reliability) {
		this.reliability = reliability;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getMatchingType() {
		return matchingType;
	}

	public void setMatchingType(double matchingType) {
		this.matchingType = matchingType;
	}

	public double getSemanticDistance() {
		return semanticDistance;
	}

	public void setSemanticDistance(double semanticDistance) {
		this.semanticDistance = semanticDistance;
	}

	public String getStrRepresentation() {
		return strRepresentation;
	}

	public void setStrRepresentation(String strRepresentation) {
		this.strRepresentation = strRepresentation;
	}

	public int getSplitPosition() {
		return splitPosition;
	}

	public void setSplitPosition(int splitPosition) {
		this.splitPosition = splitPosition;
	}
	
	

	public double getFitness_value() {
		return fitness_value;
	}

	public void setFitness_value(double fitness_value) {
		this.fitness_value = fitness_value;
	}

	@Override
	public Parameter defaultBase() {
		return new Parameter("sequencevectorindividual");
	}

	@Override
	/**
	 * Initializes the individual.
	 */
	public void reset(EvolutionState state, int thread) {
		WSCInitializer init = (WSCInitializer) state.initializer;
		List<Service> relevantList = init.initialWSCPool.getServiceSequence();
		Collections.shuffle(relevantList, init.random);

		genome = new Service[relevantList.size()];
		relevantList.toArray(genome);
		this.evaluated = false;
	}

	@Override
	public boolean equals(Object ind) {
		boolean result = false;

		if (ind != null && ind instanceof SequenceVectorIndividual) {
			result = true;
			SequenceVectorIndividual other = (SequenceVectorIndividual) ind;

			for (int i = 0; i < genome.length; i++) {
				if (!genome[i].equals(other.genome[i])) {
					result = false;
					break;
				}

			}
		}
		return result;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(genome);
	}

	@Override
	public String toString() {
		return strRepresentation;
	}

	// public String toGraphString(EvolutionState state) {
	// WSCInitializer init = (WSCInitializer) state.initializer;
	//
	// // set the service candidates according to the sampling
	// InitialWSCPool.getServiceCandidates().clear();
	//
	// InitialWSCPool.setServiceCandidates(relevantList);
	//
	// ServiceGraph graph = init.graGenerator.generateGraphBySerQueue();
	// return graph.toString();
	// }

	public double calculateSequenceFitness(Service[] sequence, WSCInitializer init, EvolutionState state) {
		// generate DAG corresponding to vector

		InitialWSCPool.getServiceCandidates().clear();

		relevantList = new ArrayList<Service>();

		for (Service s : sequence) {
			relevantList.add(s);
		}

		InitialWSCPool.setServiceCandidates(relevantList);
		ServiceGraph graph = init.graGenerator.generateGraphBySerQueue();

		// set DAG string to individual
		this.setStrRepresentation(graph.toString());

		// evaluation
		init.eval.aggregationAttribute(this, graph);
		this.fitness_value = init.eval.calculateFitness(this);

		return this.fitness_value;

		// set fitness to individual
//		((SimpleFitness) fitness).setFitness(state, f, false); // XXX Move this inside the other one
//		this.evaluated = true;

	}

	public double calculateSequenceFitness4Disturbance(Service[] sequence, WSCInitializer init, EvolutionState state) {
		// generate DAG corresponding to vector

		InitialWSCPool.getServiceCandidates().clear();

		relevantList = new ArrayList<Service>();
		List<Integer> fullSerQueue = new ArrayList<Integer>();

		for (Service s : sequence) {
			// check the status based on the failure probability
			double dice = init.random.nextDouble();
			if (dice >= s.getFailure_probability()) {
				relevantList.add(s);
				fullSerQueue.add(s.serviceIndex);
			}
		}

		// starting individual based on the given sequence of services
		SequenceVectorIndividual indi_start = new SequenceVectorIndividual();

		// set the service candidates according to the status
		InitialWSCPool.setServiceCandidates(relevantList);

		List<Integer> usedSerQueue = new ArrayList<Integer>();

		ServiceGraph update_graph = init.graGenerator.generateGraphBySerQueue();
		// adjust the bias according to the valid solution from the service queue

		// create a queue of services according to breathfirstsearch algorithm
		List<Integer> usedQueue = init.graGenerator.usedQueueofLayers("startNode", update_graph, usedSerQueue);
		// set up the split index for the updated individual
		indi_start.setSplitPosition(usedQueue.size());

		// add unused queue to form a complete a vector-based individual
		List<Integer> serQueue = init.graGenerator.completeSerQueueIndi(usedQueue, fullSerQueue);

		// set the serQueue to the updatedIndividual
		indi_start.serQueue = serQueue;

		// evaluate updated updated_graph
		// eval.aggregationAttribute(indi_updated, updated_graph);
		indi_start.setStrRepresentation(update_graph.toString());
		init.eval.aggregationAttribute(indi_start, update_graph);
		indi_start.fitness_value = init.eval.calculateFitness(indi_start);

		LocalSearch ls = new LocalSearch();
	    SequenceVectorIndividual indi_fixed = ls.randomSwapOnefromLayers(indi_start, init, state);

		return indi_fixed.getFitness_value();
	}

}
