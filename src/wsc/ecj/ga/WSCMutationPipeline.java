package wsc.ecj.ga;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import wsc.data.pool.Service;

public class WSCMutationPipeline extends BreedingPipeline {

	private static final long serialVersionUID = 1L;

	@Override
	public Parameter defaultBase() {
		return new Parameter("wscmutationpipeline");
	}

	@Override
	public int numSources() {
		return 1;
	}

	@Override
	public int produce(int min, int max, int start, int subpopulation, Individual[] inds, EvolutionState state,
			int thread) {

		int n = sources[0].produce(min, max, start, subpopulation, inds, state, thread);

		if (!(sources[0] instanceof BreedingPipeline)) {
			for (int q = start; q < n + start; q++)
				inds[q] = (Individual) (inds[q].clone());
		}

		if (!(inds[start] instanceof SequenceVectorIndividual))
			// uh oh, wrong kind of individual
			state.output.fatal(
					"WSCMutationPipeline didn't get a SequenceVectorIndividual. The offending individual is in subpopulation "
							+ subpopulation + " and it's:" + inds[start]);

		WSCInitializer init = (WSCInitializer) state.initializer;

		// Perform mutation
		for (int q = start; q < n + start; q++) {
			SequenceVectorIndividual indi4ls = (SequenceVectorIndividual) inds[q];

			int indexA = init.random.nextInt(indi4ls.genome.length);
			int indexB = init.random.nextInt(indi4ls.genome.length);
			swapServices(indi4ls.genome, indexA, indexB);
			indi4ls.evaluated = false;
		}
		return n;
	}

	private void swapServices(Service[] genome, int indexA, int indexB) {
		Service temp = genome[indexA];
		genome[indexA] = genome[indexB];
		genome[indexB] = temp;
	}

}
