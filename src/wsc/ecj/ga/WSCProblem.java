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
		double f = calculateRustnessFitness(ind2, init, state);

//		double f = 0.75 * f_ind2 + 0.25 * f_ind2_robust;

		// Set up fitness values
		((SimpleFitness) ind2.fitness).setFitness(state, f, false); // XXX Move this inside the other one
		ind2.evaluated = true;

	}

	private double calculateRustnessFitness(SequenceVectorIndividual ind2, WSCInitializer init, EvolutionState state) {

		double f_sum = 0.0;
		for (int i = 0; i < init.robustNum; i++) {
			// simulate an disturbance
			double f_ind2 = ind2.calculateSequenceFitness4Disturbance(ind2.genome, init, state);
			f_sum += f_ind2;
		}

		double part2 = f_sum / init.robustNum;

//		double part2 = 1 - Math.abs(f_sum / init.robustNum - ind2.getFitness_value()) / ind2.getFitness_value();

//		double fit_robust = f_sum / init.robustNum;
//		double part2 = 0.00;
//
//		if (f_sum / init.robustNum <= ind2.getFitness_value()) { // We prefer small changes
//
//			 part2 = 1 - (ind2.getFitness_value() - f_sum / init.robustNum) / ind2.getFitness_value();
//
//		} else { // we prefer big changes
//			 part2  = (f_sum / init.robustNum - ind2.getFitness_value()) / ind2.getFitness_value();
//		}

		// set fitness value for part2
		ind2.setFitness_value2(part2);

		return part2;

	}

}