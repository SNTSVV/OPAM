package lu.uni.svv.PriorityAssignment;

import lu.uni.svv.PriorityAssignment.arrivals.Arrivals;
import lu.uni.svv.PriorityAssignment.priority.PriorityProblem;
import lu.uni.svv.PriorityAssignment.priority.PrioritySolution;
import lu.uni.svv.PriorityAssignment.priority.PrioritySolutionEvaluator;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.utils.FileManager;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.uma.jmetal.util.solutionattribute.impl.CrowdingDistance;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class FitnessEvaluator {

    /**
     * This class evaluates experiment results with a set of sequences of task arrivals (a test set)
     *     - The test set can be one used for external fitness or separately generated sequences
     *     - The path of test set can be below two ways
     *          - One test set: specify the path with Settings.TEST_PATH and Settings.TEST_CNT=0
     *          - Multiple test set: specify the path with Settings.TEST_PATH and Settings.TEST_CNT>0
     *          -  the second case, each name of sub-folder for one test set should be "Set%02d"
     * @param args
     * @throws Exception
     */

    public static void main( String[] args ) throws Exception {
        // Environment Settings
        Initializer.initLogger();
        Settings.update(args);

        //////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////
        // Output path setting
        GAWriter.init(".", false);

        String inputFile = String.format("%s/input.csv", Settings.BASE_PATH);
        TaskDescriptor[] input = TaskDescriptor.loadFromCSV(inputFile, Settings.TIME_MAX, Settings.TIME_QUANTA);
        int simulationTime = calculateSimulationTime(input);
        PriorityProblem problem = new PriorityProblem(input, simulationTime, Settings.SCHEDULER);

        run(Settings.BASE_PATH, problem, Settings.TEST_PATH, Settings.NUM_TEST,  Settings.OUTPUT_PATH);
    }

    public static int calculateSimulationTime(TaskDescriptor[] input) throws Exception{
        // Set SIMULATION_TIME
        int simulationTime = Initializer.calculateSimulationTime(input);
        if (simulationTime<0) {
            System.out.println("Cannot calculate simulation time");
            throw new Exception("Cannot calculate simulation time");
        }
        Settings.TIME_MAX = simulationTime;

        Settings.ADDITIONAL_SIMULATION_TIME = (int)(Settings.ADDITIONAL_SIMULATION_TIME * (1/Settings.TIME_QUANTA));
        Settings.MAX_OVER_DEADLINE = (int)(Settings.MAX_OVER_DEADLINE * (1/Settings.TIME_QUANTA));

        // make simulation time
        simulationTime = (int) Settings.TIME_MAX;
        if (Settings.ADDITIONAL_SIMULATION_TIME != 0) {
            simulationTime += Settings.ADDITIONAL_SIMULATION_TIME;
        }
        return simulationTime;

    }

    public static void run(String _basePath, PriorityProblem _problem, String _testPath, int _testCnt, String _outputName) throws Exception {

        String solutionFile = String.format("%s/_best_pareto.list", _basePath);
        List<PrioritySolution> solutions = loadSolutions(solutionFile, _problem);

        // create output file
        if (_outputName== null || _outputName.length()==0){
            File path = new File(_testPath);
            _outputName = path.getName();
        }

        String output = String.format("%s/_external/fitness_%s.csv", _basePath, _outputName);
        String title = "TestID,SolutionIndex,SolutionID,Schedulability,Satisfaction,DeadlineMiss,Rank,CrowdDistance"; //
        GAWriter writer = new GAWriter(output, Level.FINE, null);
        writer.info(title);

        //Re-evaluation
        System.out.print(String.format("[%s] Working with %s", _basePath, _testPath));
        if (_testCnt==0){
            System.out.print("...");
            String testFile = String.format("%s/test.list", _testPath);
            reEvaluate(1, writer, testFile, _problem, solutions);
        }
        else {
            for (int setID = 1; setID <= _testCnt; setID++) {
                System.out.print(".");
                String testFile = String.format("%s/Set%02d/test.list", _testPath, setID);
                reEvaluate(setID, writer, testFile, _problem, solutions);
            }
        }
        writer.close();
        System.out.println("..Done");
    }


    public static void reEvaluate(int _setID, GAWriter _writer, String _testPath,
                                  PriorityProblem _problem, List<PrioritySolution> _solutions) throws Exception {

        List<Arrivals[]> testArrivals = FileManager.LoadTestArrivals(_testPath, _problem.Tasks, _problem.SimulationTime, 0);

        // evaluate with a new test arrivals
        PrioritySolutionEvaluator evaluator = new PrioritySolutionEvaluator(testArrivals, null);
        evaluator.evaluate(_solutions, _problem);

        for(int idx=0; idx<_solutions.size(); idx++) {
            _writer.info(getSolutionInfoLine(_setID, idx, _solutions.get(idx)));
        }
    }

    public static List<PrioritySolution> loadSolutions(String _solutionFile, PriorityProblem _problem) throws Exception{
        BufferedReader br = new BufferedReader(new FileReader(_solutionFile));
        br.readLine(); // throw file header

        // load solutions
        List<PrioritySolution> solutions = new ArrayList<>();
        while(true){
            String line = br.readLine();
            if (line==null) break;
            String[] columns = line.split("\t");
            int solID = Integer.parseInt(columns[0]);
            PrioritySolution solution = new PrioritySolution(_problem, columns[1]);
            solution.ID = solID;
            solutions.add(solution);
        }
        br.close();
        return solutions;
    }

    public static String getSolutionInfoLine(int _testID, int _idx, PrioritySolution _solution) {
        int rank = 0;
        if (_solution.hasAttribute(DominanceRanking.class))
            rank = (Integer)_solution.getAttribute(DominanceRanking.class);

        double cDistance = 0.0;
        if (_solution.hasAttribute(CrowdingDistance.class))
            cDistance = (Double)_solution.getAttribute(CrowdingDistance.class);

        StringBuilder line = new StringBuilder();
        line.append(_testID);
        line.append(",");
        line.append(_idx);
        line.append(",");
        line.append(_solution.ID);
        line.append(",");
        line.append(-_solution.getObjective(0));
        line.append(",");
        line.append(-_solution.getObjective(1));
        line.append(",");
        line.append((int)_solution.getAttribute("DeadlineMiss"));
        line.append(",");
        line.append(rank);
        line.append(",");
        line.append(cDistance);

        return line.toString();
    }


}
