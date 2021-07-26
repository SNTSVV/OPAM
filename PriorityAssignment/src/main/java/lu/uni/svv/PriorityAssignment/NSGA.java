package lu.uni.svv.PriorityAssignment;

import lu.uni.svv.PriorityAssignment.arrivals.Arrivals;
import lu.uni.svv.PriorityAssignment.arrivals.ArrivalsSolution;
import lu.uni.svv.PriorityAssignment.priority.*;
import lu.uni.svv.PriorityAssignment.priority.search.PriorityNSGAII;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.utils.FileManager;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Monitor;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class NSGA {
    /**
     * Main code to start co-evolution
     *      - initialize parameters
     *      - initialize global objects
     * @param args
     * @throws Exception
     */
    public static void main( String[] args ) throws Exception {
        // Environment Settings
        Initializer.initLogger();
        Settings.update(args);

        // load input
        TaskDescriptor[] input = TaskDescriptor.loadFromCSV(Settings.INPUT_FILE, Settings.TIME_MAX, Settings.TIME_QUANTA);

        // update dynamic settings
        Initializer.updateSettings(input);

        // convert settings' time unit
        Initializer.convertTimeUnit(Settings.TIME_QUANTA);

        // make simulation time
        int simulationTime = (int) Settings.TIME_MAX;
        if (Settings.ADDITIONAL_SIMULATION_TIME != 0) {
            simulationTime += Settings.ADDITIONAL_SIMULATION_TIME;
        }

        // create engineer's priority assignment
        Integer[] prioritiy = null;
        if (!Settings.RANDOM_PRIORITY) {
            prioritiy = getPrioritiesFromInput(input);
        }

        List<Arrivals[]> testArrivals = FileManager.LoadTestArrivals(Settings.TEST_PATH, input, simulationTime, Settings.NUM_TEST);

        // save input and settings
        GAWriter.init(null, true);
        printInput(TaskDescriptor.toString(input, Settings.TIME_QUANTA));

        // initialze static objects
        ArrivalsSolution.initUUID();
        PrioritySolution.initUUID();
        AbstractPrioritySearch.init();

        // Run Co-Evolution
        NSGA system = new NSGA();
        List<PrioritySolution> best = system.run(input, simulationTime, prioritiy, testArrivals);
    }

    /**
     * printing input and setting to the result folder to distinguish real
     * @param inputs
     */
    private static void printInput(String inputs){
        String settingStr = Settings.getString();
        System.out.print(settingStr);

        // print current settings
        GAWriter writer = new GAWriter("settings.txt", Level.FINE, null);
        writer.info(settingStr);
        writer.close();

        // print original input
        writer = new GAWriter("input.csv", Level.INFO, null);
        writer.info(inputs);
        writer.close();
    }

    public List<PrioritySolution> run(TaskDescriptor[] input, int simulationTime, Integer[] priorityEngineer, List<Arrivals[]> arrivals) throws Exception, JMetalException, FileNotFoundException{
        Monitor.init();
        JMetalLogger.logger.info("Start only NAGA-II appraoch");

        // generate initial objects
        PriorityProblem problem  = new PriorityProblem(input, simulationTime, Settings.SCHEDULER);
        List<PrioritySolution> P = this.createInitialPriorities(Settings.P2_POPULATION, priorityEngineer, problem);
        PrioritySolutionEvaluator evaluator = new PrioritySolutionEvaluator(arrivals, null);

        String runMsg = (Settings.RUN_NUM==0)?"": "[Run"+ Settings.RUN_NUM + "] ";
        JMetalLogger.logger.info(runMsg+ "Search Optimal Priority");

        // define operators
        CrossoverOperator<PrioritySolution> crossover;
        MutationOperator<PrioritySolution> mutation;
        SelectionOperator<List<PrioritySolution>, PrioritySolution> selection;

        crossover = new PMXCrossover(Settings.P2_CROSSOVER_PROB);
        mutation = new SwapMutation(Settings.P2_MUTATION_PROB);
        selection = new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());

        // Define algorithm
        PriorityNSGAII algorithm =	new PriorityNSGAII(
                0, // Priority NSGA-II does not evaluate with external fitness
                P, null, problem,
                Settings.P2_ITERATIONS, Settings.P2_POPULATION, Settings.P2_POPULATION, Settings.P2_POPULATION,
                crossover, mutation, selection, evaluator);

        // execute algorithm in thread
        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

        Monitor.finish();
        System.gc();

        // return with result
        List<PrioritySolution> pop = algorithm.getPopulation();
        List<PrioritySolution> best = algorithm.getResult();
        saveResults(pop, best);
        JMetalLogger.logger.info("Finished NSGA search process.");
        return null;
    }

    /**
     * Generate initial priorities from input
     * This function converts engineer's priority values from task input into priority level (0 to number of tasks - 1),
     * it assumes that the higher value of priority is the higher priority level
     * @param input
     * @return
     */
    public static Integer[] getPrioritiesFromInput(TaskDescriptor[] input) {
        Integer[] priorities = new Integer[input.length];
        int[] assigned = new int[input.length];
        Arrays.fill(assigned, 0);

        // assign priority level (from highest to lowest)
        for(int priority=input.length-1; priority>=0;priority--) {
            int maxTaskIdx = 0;
            int maxPriority = -1;
            for (int x = 0; x < input.length; x++) {
                if (assigned[x]==1) continue;

                if (input[x].Priority > maxPriority) {
                    maxTaskIdx = x;
                    maxPriority = input[x].Priority;
                }
            }

            priorities[maxTaskIdx] = priority;
            assigned[maxTaskIdx]=1;
        }

        return priorities;
    }

    /**
     * Create initial priority assignments
     * @param maxPopulation
     * @param priorityEngineer
     * @param problem
     * @return
     */
    public List<PrioritySolution> createInitialPriorities(int maxPopulation, Integer[] priorityEngineer, PriorityProblem problem) {

        List<PrioritySolution> population = new ArrayList<>(maxPopulation);
        PrioritySolution individual;
        for (int i = 0; i < maxPopulation; i++) {
            if (i==0 && priorityEngineer!=null){
                individual = new PrioritySolution(problem, priorityEngineer);
            }
            else {
                individual = new PrioritySolution(problem);
            }
            population.add(individual);
        }
        return population;
    }


    /**
     * Print out all results (best pareto, last population, time information, memory information..)
     */
    private void saveResults(List<PrioritySolution> population, List<PrioritySolution> pareto)
    {
        JMetalLogger.logger.info("Saving best pareto priority assignments...");
        // print pareto results
        if (pareto.size()!=0) {
            GAWriter writer = new GAWriter("_best_pareto.list", Level.FINE, null);
            writer.info("Index\tPriorityJSON");
            for (int idx = 0; idx < pareto.size(); idx++) {
                writer.write(String.format("%d\t", idx));
                writer.write(pareto.get(idx).getVariablesString());
                writer.write("\n");
            }
            writer.close();
        }

        // print population results
        if (population.size()!=0) {
            JMetalLogger.logger.info("Saving population of priority assignments...");
            GAWriter writer = new GAWriter("_last_population.list", Level.FINE, null);
            writer.info("Index\tPriorityJSON");
            for (int idx = 0; idx < population.size(); idx++) {
                writer.write(String.format("%d\t", idx));
                writer.write(population.get(idx).getVariablesString());
                writer.write("\n");
            }
            writer.close();
        }

        GAWriter writer = new GAWriter(String.format("_result.txt"), Level.FINE, null);
        long all = Monitor.getTime();
        long evP1 = Monitor.getTime("evaluateP1");
        long evP2 = Monitor.getTime("evaluateP2");
        long evP2Ex = Monitor.getTime("external");
        long searchTime = all-evP1-evP2-evP2Ex;
        writer.info(String.format("TotalExecutionTime( s): %.3f",all/1000.0));
        writer.info(String.format("SearchTime(s): %.3f",(searchTime)/1000.0));
        writer.info(String.format("EvaluationTimeP1(s): %.3f",evP1/1000.0));
        writer.info(String.format("EvaluationTimeP2(s): %.3f",evP2/1000.0));
        writer.info(String.format("EvaluationTimeEx(s): %.3f",evP2Ex/1000.0));
        writer.info(String.format("InitHeap: %.1fM (%.1fG)", Monitor.heapInit/Monitor.MB, Monitor.heapInit/Monitor.GB));
        writer.info(String.format("usedHeap: %.1fM (%.1fG)", Monitor.heapUsed/Monitor.MB, Monitor.heapUsed/Monitor.GB));
        writer.info(String.format("commitHeap: %.1fM (%.1fG)", Monitor.heapCommit/Monitor.MB, Monitor.heapCommit/Monitor.GB));
        writer.info(String.format("MaxHeap: %.1fM (%.1fG)", Monitor.heapMax/Monitor.MB, Monitor.heapMax/Monitor.GB));
        writer.info(String.format("MaxNonHeap: %.1fM (%.1fG)", Monitor.nonheapUsed/Monitor.MB, Monitor.nonheapUsed/Monitor.GB));
        writer.close();

        JMetalLogger.logger.info("Saving population...Done");
    }



}
