package wsc.ecj.ga;

import ec.EvolutionState;
import ec.Individual;
import ec.simple.SimpleShortStatistics;
import ec.util.Parameter;
import wsc.data.pool.Service;

/**
 *
 * @author Alex
 */
public class GraphStatistics extends SimpleShortStatistics {

	private static final long serialVersionUID = 1L;
	public int histogramLog = 0; // 0 by default means stdout

	@Override
    public void postEvaluationStatistics(EvolutionState state){
        boolean output = (state.generation % modulus == 0);

        // gather timings
//        if (output && doTime)
//            {
//            state.output.print("" + (System.currentTimeMillis()-lastTime) + " ",  statisticslog);
//            }

        // gather timings
        if (output && doTime)
            {
        	long time = System.currentTimeMillis()-lastTime;
        	if (state.generation == 0)
        		time += WSCInitializer.setupTime;
            state.output.print("" + time + " ",  statisticslog);
            }
        
        
        
        int subpops = state.population.subpops.length;                          // number of supopulations
        totalIndsThisGen = new long[subpops];                                           // total assessed individuals
        bestOfGeneration = new Individual[subpops];                                     // per-subpop best individual this generation
        totalSizeThisGen = new long[subpops];                           // per-subpop total size of individuals this generation
        totalFitnessThisGen = new double[subpops];                      // per-subpop mean fitness this generation
        double[] meanFitnessThisGen = new double[subpops];                      // per-subpop mean fitness this generation


        prepareStatistics(state);

        // gather per-subpopulation statistics

        for(int x=0;x<subpops;x++)
            {
            for(int y=0; y<state.population.subpops[x].individuals.length; y++)
                {
                if (state.population.subpops[x].individuals[y].evaluated)               // he's got a valid fitness
                    {
                    // update sizes
                    long size = state.population.subpops[x].individuals[y].size();
                    totalSizeThisGen[x] += size;
                    totalSizeSoFar[x] += size;
                    totalIndsThisGen[x] += 1;
                    totalIndsSoFar[x] += 1;

                    // update fitness
                    if (bestOfGeneration[x]==null ||
                        state.population.subpops[x].individuals[y].fitness.betterThan(bestOfGeneration[x].fitness))
                        {
                        bestOfGeneration[x] = state.population.subpops[x].individuals[y];
                        if (bestSoFar[x]==null || bestOfGeneration[x].fitness.betterThan(bestSoFar[x].fitness))
                            bestSoFar[x] = (Individual)(bestOfGeneration[x].clone());
                        }

                    // sum up mean fitness for population
                    totalFitnessThisGen[x] += state.population.subpops[x].individuals[y].fitness.fitness();

                    // hook for KozaShortStatistics etc.
                    gatherExtraSubpopStatistics(state, x, y);
                    }
                }
            // compute mean fitness stats
            meanFitnessThisGen[x] = (totalIndsThisGen[x] > 0 ? totalFitnessThisGen[x] / totalIndsThisGen[x] : 0);

            // hook for KozaShortStatistics etc.
            if (output && doSubpops) printExtraSubpopStatisticsBefore(state, x);

            // print out optional average size information
            if (output && doSize && doSubpops)
                {
                state.output.print("" + (totalIndsThisGen[x] > 0 ? ((double)totalSizeThisGen[x])/totalIndsThisGen[x] : 0) + " ",  statisticslog);
                state.output.print("" + (totalIndsSoFar[x] > 0 ? ((double)totalSizeSoFar[x])/totalIndsSoFar[x] : 0) + " ",  statisticslog);
                state.output.print("" + (double)(bestOfGeneration[x].size()) + " ", statisticslog);
                state.output.print("" + (double)(bestSoFar[x].size()) + " ", statisticslog);
                }

            // print out fitness information
            if (output && doSubpops)
                {
                state.output.print("" + meanFitnessThisGen[x] + " ", statisticslog);
                state.output.print("" + bestOfGeneration[x].fitness.fitness() + " ", statisticslog);
                state.output.print("" + bestSoFar[x].fitness.fitness() + " ", statisticslog);
                }

            // hook for KozaShortStatistics etc.
            if (output && doSubpops) printExtraSubpopStatisticsAfter(state, x);
            }



        // Now gather per-Population statistics
        long popTotalInds = 0;
        long popTotalIndsSoFar = 0;
        long popTotalSize = 0;
        long popTotalSizeSoFar = 0;
        double popMeanFitness = 0;
        double popTotalFitness = 0;
        Individual popBestOfGeneration = null;
        Individual popBestSoFar = null;

        for(int x=0;x<subpops;x++)
            {
            popTotalInds += totalIndsThisGen[x];
            popTotalIndsSoFar += totalIndsSoFar[x];
            popTotalSize += totalSizeThisGen[x];
            popTotalSizeSoFar += totalSizeSoFar[x];
            popTotalFitness += totalFitnessThisGen[x];
            if (bestOfGeneration[x] != null && (popBestOfGeneration == null || bestOfGeneration[x].fitness.betterThan(popBestOfGeneration.fitness)))
                popBestOfGeneration = bestOfGeneration[x];
            if (bestSoFar[x] != null && (popBestSoFar == null || bestSoFar[x].fitness.betterThan(popBestSoFar.fitness))) {
                popBestSoFar = bestSoFar[x];
            }

            // hook for KozaShortStatistics etc.
            gatherExtraPopStatistics(state, x);
            }

        // build mean
        popMeanFitness = (popTotalInds > 0 ? popTotalFitness / popTotalInds : 0);               // average out

        // hook for KozaShortStatistics etc.
        if (output) printExtraPopStatisticsBefore(state);

        // optionally print out mean size info
        if (output && doSize)
            {
            state.output.print("" + (popTotalInds > 0 ? popTotalSize / popTotalInds : 0)  + " " , statisticslog);                                           // mean size of pop this gen
            state.output.print("" + (popTotalIndsSoFar > 0 ? popTotalSizeSoFar / popTotalIndsSoFar : 0) + " " , statisticslog);                             // mean size of pop so far
            state.output.print("" + (double)(popBestOfGeneration.size()) + " " , statisticslog);                                    // size of best ind of pop this gen
            state.output.print("" + (double)(popBestSoFar.size()) + " " , statisticslog);                           // size of best ind of pop so far
            }

        // print out fitness info
        if (output)
            {
            state.output.print("" + popMeanFitness + " " , statisticslog);                                                                                  // mean fitness of pop this gen
            state.output.print("" + (popBestOfGeneration.fitness.fitness()) + " " , statisticslog);                 // best fitness of pop this gen
            state.output.print("" + (popBestSoFar.fitness.fitness()) + " " , statisticslog);                // best fitness of pop so far
            state.output.print("" + ((SequenceVectorIndividual)popBestSoFar).getFitness_value() + " " , statisticslog);                // best fitness part1 of pop so far
            state.output.print("" + ((SequenceVectorIndividual)popBestSoFar).getFitness_value2() + " " , statisticslog);                // best fitness part2 of pop so far

            
            }

        // hook for KozaShortStatistics etc.
        if (output) printExtraPopStatisticsAfter(state);

        // we're done!
        if (output) state.output.println("", statisticslog);

        // Now let's write the histogram log
        if (output) {
            // Print the best candidate at the end of the run
            if (state.generation == state.parameters.getInt(new Parameter("generations"), null)-1) {
                state.output.println(popBestSoFar.toString(), statisticslog);
                state.output.println("" + " " , statisticslog);               
                for(Service s: ((SequenceVectorIndividual)popBestSoFar).genome) {
                    state.output.print("" + s.serviceID+ " " , statisticslog);                 	
                }
                state.output.println("" + " " , statisticslog);               
                for(Service s: ((SequenceVectorIndividual)popBestSoFar).genome) {
                    state.output.print("" + s.serviceIndex+ " " , statisticslog);                 	
                }


            }
        }
    }
}
