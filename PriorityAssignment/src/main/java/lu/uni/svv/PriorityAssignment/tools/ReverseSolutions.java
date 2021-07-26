package lu.uni.svv.PriorityAssignment.tools;

import lu.uni.svv.PriorityAssignment.Initializer;
import lu.uni.svv.PriorityAssignment.priority.PriorityProblem;
import lu.uni.svv.PriorityAssignment.priority.PrioritySolution;
import lu.uni.svv.PriorityAssignment.utils.FileManager;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;


public class ReverseSolutions {

    public static void main(String[] args) throws Exception {
        // Environment Settings
        Initializer.initLogger();
        Settings.update(args);
        GAWriter.init("", false);

        // load test file names
        for(int runID=1; runID<= Settings.RUN_CNT; runID++) {
            String path = String.format("%s/Run%02d", Settings.BASE_PATH, runID);
            reverseSolutions(String.format("%s/_best_pareto.list", path));
            reverseSolutions(String.format("%s/_last_population.list", path));
        }
    }

    public static void reverseSolutions(String _filename) throws Exception {
        System.out.print("Reverse solutions in " + _filename);
        List<Integer[]> solutions = loadSolutions(_filename);

        // make reverse
        List<Integer[]> reversed = new ArrayList<>();
        for(int i=0; i<solutions.size(); i++){
            Integer[] temp = reverse(solutions.get(i));
            reversed.add(temp);
        }

        // load arrivals from  file and save it
        GAWriter writer = new GAWriter(_filename, Level.FINE, null);
        writer.write("Index\tPriorityJSON\n");

        for(int idx=0; idx<reversed.size(); idx++) {
            String json = convertToJSON(reversed.get(idx));
            writer.write(String.format("%d\t%s\n", idx, json));
        }
        writer.close();
        System.out.println("..Done");
    }

    public static List<Integer[]> loadSolutions(String _solutionFile) throws Exception{
        BufferedReader br = new BufferedReader(new FileReader(_solutionFile));
        br.readLine(); // throw file header

        // load solutions
        List<Integer[]> solutions = new ArrayList<>();
        while(true){
            String line = br.readLine();
            if (line==null) break;
            String[] columns = line.split("\t");
            int solID = Integer.parseInt(columns[0]);
            String listStr = columns[1].trim();
            listStr = listStr.substring(1,listStr.length()-2);
            String[] list = listStr.split(",");

            Integer[] priorities = new Integer[list.length];
            for(int i=0; i<list.length; i++){
                priorities[i] = Integer.parseInt(list[i].trim());
            }
            solutions.add(priorities);
        }
        br.close();
        return solutions;
    }

    public static Integer[] reverse(Integer[] priorities){
        // find max value
        int maxLv = 0;
        for(int i=0; i<priorities.length; i++){
            if (priorities[i]>maxLv) maxLv = priorities[i];
        }

        // make reverse
        Integer[] reverse = new Integer[priorities.length];
        for(int i=0; i<priorities.length; i++){
            reverse[i] = maxLv - priorities[i];
        }
        return reverse;
    }

    public static String convertToJSON(Integer[] priorities){
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        sb.append(priorities[0]);
        for(int i=1; i<priorities.length; i++){
            sb.append(", ");
            sb.append(priorities[i]);
        }
        sb.append(" ]");
        return sb.toString();
    }

}
