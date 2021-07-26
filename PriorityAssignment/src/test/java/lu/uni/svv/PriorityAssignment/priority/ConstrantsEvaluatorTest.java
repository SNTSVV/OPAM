package lu.uni.svv.PriorityAssignment.priority;

import junit.framework.TestCase;
import lu.uni.svv.PriorityAssignment.arrivals.ArrivalsSolution;
import lu.uni.svv.PriorityAssignment.scheduler.RTScheduler;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.uma.jmetal.util.JMetalLogger;


public class ConstrantsEvaluatorTest extends TestCase
{
    public PriorityProblem problem;

    public void init() throws Exception {
        // Environment Settings
        Settings.update(new String[] {});
        Settings.INPUT_FILE = "res/industrial/CCS.csv";
        Settings.BASE_PATH = "results/TEST/CCS";
        Settings.TIME_MAX = 0;
        Settings.TIME_QUANTA = 1;
        Settings.CYCLE_NUM = 2;
        Settings.P1_ITERATION = 10;
        Settings.P2_GROUP_FITNESS = "average";

        // load input
        TaskDescriptor[] input = TaskDescriptor.loadFromCSV(Settings.INPUT_FILE, Settings.TIME_MAX, Settings.TIME_QUANTA);

        // Set SIMULATION_TIME
        int simulationTime = 0;
        if (Settings.TIME_MAX == 0) {
            long[] periodArray = new long[input.length];
            for(int x=0; x<input.length; x++){
                periodArray[x] = input[x].Period;
            }
            simulationTime = (int) RTScheduler.lcm(periodArray);
            if (simulationTime<0) {
                System.out.println("Cannot calculate simulation time");
                return;
            }
        }
        else{
            simulationTime = (int) (Settings.TIME_MAX * (1/Settings.TIME_QUANTA));
            if (Settings.EXTEND_SIMULATION_TIME) {
                long max_deadline = TaskDescriptor.findMaximumDeadline(input);
                simulationTime += max_deadline;
            }
        }
        Settings.TIME_MAX = simulationTime;

        // update specifit options
        if (Settings.P2_MUTATION_PROB==0)
            Settings.P2_MUTATION_PROB = 1.0/input.length;

        // initialze static objects
        ArrivalsSolution.initUUID();
        PrioritySolution.initUUID();

        this.problem = new PriorityProblem(input, simulationTime, Settings.SCHEDULER);
        JMetalLogger.logger.info("Initialized program");

    }

    public void testCalculate() throws Exception {
        init();

        // test for the worst case
        PrioritySolution solution = new PrioritySolution(problem, new Integer[]{10,9,8,7,6,5,4,3,2,1,0});
        double value = ConstrantsEvaluator.calculate(solution, problem.Tasks);
        System.out.printf("priorities: %s, calculate value: %.4f\n", solution.getVariablesString(), value);

        // test for the best case
        solution = new PrioritySolution(problem, new Integer[]{0,1,2,3,4,5,6,7,8,9,10});
        value = ConstrantsEvaluator.calculate(solution, problem.Tasks);
        System.out.printf("priorities: %s, calculate value: %.4f\n", solution.getVariablesString(), value);

        // test for the near best case
        solution = new PrioritySolution(problem, new Integer[]{0,1,4,3,2,5,6,7,8,9,10});
        value = ConstrantsEvaluator.calculate(solution, problem.Tasks);
        System.out.printf("priorities: %s, calculate value: %.4f\n", solution.getVariablesString(), value);

        // test for the middle cases
        for (int i=0; i<10; i++){
            solution = problem.createSolution();
            value = ConstrantsEvaluator.calculate(solution, problem.Tasks);
            System.out.printf("priorities: %s, calculate value: %.4f\n", solution.getVariablesString(), value);
        }
    }
}