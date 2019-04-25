package wsc.ecj.ga;

import ec.*;
import ec.simple.*;

public class WSCProblem extends Problem implements SimpleProblemForm {
	private static final long serialVersionUID = 1L;

	public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation,
			final int threadnum) {
		if (ind.evaluated)
			return;

		if (!(ind instanceof SequenceVectorIndividual))
			state.output.fatal("Whoa!  It's not a SequenceVectorIndividual!!!", null);

		SequenceVectorIndividual ind2 = (SequenceVectorIndividual) ind;
		WSCInitializer init = (WSCInitializer) state.initializer;

		if (!(ind2.fitness instanceof SimpleFitness))
			state.output.fatal("Whoa!  It's not a SimpleFitness!!!", null);

		// an evaluation 4 f(ind2)
		double f_ind2 = ind2.calculateSequenceFitness(ind2.genome, init, state);

		// evaluations 4 robustness
		double f_ind2_robust = calculateRustnessFitness(ind2, init, state);

		// Set up fitness values
//		((SimpleFitness) fitness).setFitness(state, f, false); // XXX Move this inside the other one
//		this.evaluated = true;

	}

	private double calculateRustnessFitness(SequenceVectorIndividual ind2, WSCInitializer init, EvolutionState state) {

		double f_sum = 0.0;
		for (int i = 0; i < init.robustNum; i++) {
			// simulate an disturbance
			double f_ind2 = ind2.calculateSequenceFitness4Disturbance(ind2.genome, init, state);
			f_sum += f_ind2;
		}

		return f_sum / 31;

	}

}