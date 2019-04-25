package wsc.dynamic.localsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ec.EvolutionState;
import wsc.InitialWSCPool;
import wsc.data.pool.Service;
import wsc.graph.ServiceGraph;
import wsc.ecj.ga.WSCInitializer;
import wsc.ecj.ga.SequenceVectorIndividual;

public class LocalSearch {

	/**
	 * local search for random one-point swap based on one individual
	 * 
	 * @param state
	 * @param init
	 * @return
	 *
	 */

	public SequenceVectorIndividual randomSwapOnefromLayers(SequenceVectorIndividual indi_start, WSCInitializer init,
			EvolutionState state) {
		int split = 0;

		split = indi_start.getSplitPosition();
		List<SequenceVectorIndividual> indi_neigbouring = new ArrayList<SequenceVectorIndividual>();
		SequenceVectorIndividual indi_best = new SequenceVectorIndividual();

		// initial best from the starting individual
		indi_best = indi_start;

		for (int nOfls = 0; nOfls < init.lsNum; nOfls++) {

			// obtain the split position of the individual

			SequenceVectorIndividual indi_temp = new SequenceVectorIndividual();
			List<Integer> serQueue_temp = new ArrayList<Integer>(); // service
																	// Index
																	// arrayList
			Set<Integer> unused_ser = new HashSet<Integer>();

			// deep clone the services into a service queue for indi_temp
			for (int index = 0; index < indi_start.serQueue.size(); index++) {
				int ser = indi_start.serQueue.get(index);
				serQueue_temp.add(ser);
				// obtain unused service set
				if (index >= split) {
					unused_ser.add(indi_start.serQueue.get(index));
				}
			}

			indi_temp.serQueue = serQueue_temp;

			int swap_a = init.random.nextInt(split);// between 0 (inclusive) and split (exclusive)
			Integer item_a = indi_temp.serQueue.get(swap_a);

			// initial swap_b for index of list, item_b for service index
			int swap_b = -1;
			Integer item_b = -1;
			int continue4Swap = 0;

			// obtain services in the same layer of the selected service

			for (List<Integer> ser_lay : WSCInitializer.layers4SerIndex.values()) {
				if (ser_lay.contains(item_a)) {
					// obtain an intersection of services in the layer and
					// unused list
					List<Integer> swap_b_list = Lists
							.newArrayList(Sets.intersection(Sets.newHashSet(ser_lay), unused_ser));
					if (swap_b_list.size() > 0) {
						continue4Swap = 1;
						item_b = swap_b_list.get(init.random.nextInt(swap_b_list.size()));
						swap_b = indi_temp.serQueue.indexOf(item_b);
					}
					break;
				}
			}

			if (continue4Swap == 1) {

				Integer item_temp = new Integer(item_a);

				indi_temp.serQueue.set(swap_a, item_b);
				indi_temp.serQueue.set(swap_b, item_temp);

				List<Service> serviceCandidates = new ArrayList<Service>();
				for (int n = 0; n < indi_temp.serQueue.size(); n++) {

					// deep clone may be not needed if no changes are applied to
					// the pointed service
					serviceCandidates.add(WSCInitializer.Index2ServiceMap.get(indi_temp.serQueue.get(n)));
				}

				// set the service candidates according to the sampling
				InitialWSCPool.getServiceCandidates().clear();
				InitialWSCPool.setServiceCandidates(serviceCandidates);

				List<Integer> usedSerQueue = new ArrayList<Integer>();

				ServiceGraph update_graph = init.graGenerator.generateGraphBySerQueue();

				// evaluate the update_graph and calculate the fitness

				// adjust the bias according to the valid solution from the
				// service queue
				List<Integer> usedQueue = init.graGenerator.usedQueueofLayers("startNode", update_graph, usedSerQueue);
				// set up the split index for the updated individual
				indi_temp.setSplitPosition(usedQueue.size());

				// add unused queue to form a complete a vector-based individual
				List<Integer> serQueue = init.graGenerator.completeSerQueueIndi(usedQueue, indi_temp.serQueue);

				// set the serQueue to the updatedIndividual
				indi_temp.serQueue = serQueue;

				// evaluate updated updated_graph
				init.eval.aggregationAttribute(indi_temp, update_graph);
				indi_temp.setFitness_value(init.eval.calculateFitness(indi_temp));

				// add
				indi_neigbouring.add(indi_temp);
			}
		}
		
		//please debug
		
		// sort by fitness values
		for (SequenceVectorIndividual indi : indi_neigbouring) {
			if (indi.getFitness_value() > indi_best.getFitness_value()) {
				indi_best = indi;
			}

		}

		return indi_best;
	}
}
