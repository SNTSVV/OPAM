package lu.uni.svv.PriorityAssignment.scheduler;

import lu.uni.svv.PriorityAssignment.priority.PriorityProblem;
import lu.uni.svv.PriorityAssignment.priority.PrioritySolution;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.uma.jmetal.util.JMetalLogger;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.ConsoleHandler;

public class SchedulerDrawer {
	
	
	public static void main( String[] args ) throws Exception {
		// Environment Settings
//		String PATH = "results/TEST_T";
//		String PATH = "results/20200505_Minimum3/Run02";
		String PATH = "results/COEVOLEX/DistAnalysis3/";
		int SIMULATION_TIME = 80000;
		GAWriter.setBasePath(PATH);
		
		System.out.println("-----------Schedule drawer----------------");
		int solutionID = 1;
		int testNum = 0;
		System.out.print("Loading schedules....");
		Schedule[][] schedules = Schedule.loadSchedules(PATH + String.format("/_schedules/schedule_%d_num%d.json", solutionID, testNum));
		System.out.println("Done");
		
		ScheduleCalculator sc = new ScheduleCalculator(schedules, null);
		int[] maxs = sc.getMaximumExecutions();
		for (int x=0; x<maxs.length; x++)
			System.out.println(String.format("Task%d: %d",x+1, maxs[x]));
		
		System.out.println("\nMinimum:");
		int[] mins = sc.getMinimumExecutions();
		for (int x=0; x<mins.length; x++)
			System.out.println(String.format("Task%d: %d",x+1, mins[x]));
		
		System.out.println("\nCounts:");
		CountMap[] counts = sc.getExecutionDistList();
		for (int t=0; t<counts.length; t++){
			System.out.print("Task"+(t+1)+": {");
			Integer[] keys = new Integer[counts[t].size()];
			counts[t].keySet().toArray(keys);
			Arrays.sort(keys);
			for(int x=keys.length-1; x>=0; x--){
				int value = counts[t].get(keys[x]);
				System.out.print(String.format("%d: %d, ",keys[x], value));
			}
			System.out.println("}");
		}

		//TODO:: need to debug (these two lines are added by the class modification)
		TaskDescriptor[] input = TaskDescriptor.loadFromCSV(PATH + "input.csv", Settings.TIME_MAX, Settings.TIME_QUANTA);
		PriorityProblem problem = new PriorityProblem(input, SIMULATION_TIME);
		PrioritySolution solution = PrioritySolution.loadFromJSON(problem, PATH +String.format("/_priorities/priority_%d_num%d.json",solutionID, testNum));
		
		String filename = String.format(PATH + "/draws/schedule_%d_num%d.txt", solutionID, testNum);
		drawSchedule(schedules, solution.toArray(), SIMULATION_TIME, filename);
	}
	
	public static void drawSchedule(Schedule[][] schedules, Integer[] _priorities, int stime, String _filename){
		int maxTime = 0;
		for (int t=0; t<schedules.length; t++) {
			int finishedTime = schedules[t][schedules[t].length-1].finishedTime;
			if (maxTime < finishedTime){
				maxTime = finishedTime;
			}
		}
		PrintStream writer = null;
		if (_filename==null){
			writer = System.out;
		}
		else{
			File file = new File(_filename);
			if (!file.getParentFile().exists())
			{
				file.getParentFile().mkdirs();
			}
			OutputStream os = null;
			try {
				os = new FileOutputStream(_filename);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			if (os==null) return;
			writer = new PrintStream(os);
		}
		
		
		int simulationTime = Math.min(stime, maxTime);
		writer.println(String.format("Total simulation Time: %d", simulationTime));
		
		ScheduleCalculator calculator = new ScheduleCalculator(schedules, null);
		int[] dms = calculator.getDeadlineMissList();
		writer.println(String.format("Total simulation Time: %d", simulationTime));
		
		for (int t=0; t<schedules.length; t++) {
			StringBuilder sb = new StringBuilder();
			sb.append("Task");
			sb.append(String.format("%02d", t + 1));
			sb.append(" (");
			sb.append(String.format("%02d", _priorities[t]));
			sb.append(", DM-");
			sb.append(String.format("%02d", dms[t]));
			sb.append("): ");
			
			int e = 0;
			int a = -1;
			int start = -1;
			int end = -1;
			for (int time = 0; time < simulationTime; time++) {
				if (time%10==0) sb.append("|");
				if (time % 100 == 0) sb.append(String.format("%d|",time));
				if (time % 1000 == 0) sb.append("|");
				
				if (time >= end) {
					int[] vals = findNextArrival(schedules[t], e, a);
					e = vals[0];
					a = vals[1];
					start = vals[2];
					end = vals[3];
				}
				
				if (time >= start && time < end) {
					if (time>=schedules[t][e].deadline){
						sb.append("!");
					}
					else sb.append("*");
				} else {
					sb.append(".");
				}
			}
			
			writer.println(sb.toString());
		}
	}
	
	
	public static int[] findNextArrival(Schedule[] schedules, int e, int a){
		int[] vals = new int[4];
		Arrays.fill(vals, -1);
		vals[0] = e;
		vals[1] = a;
		
		vals[1]+=1;
		
		if (vals[0]>=schedules.length) return vals;
		if (vals[1]>= schedules[e].startTime.size()) {
			vals[1]=0;
			vals[0]++;
		}
		if (vals[0]>=schedules.length) return vals;
		
		List<Integer> starts =  schedules[vals[0]].startTime;
		List<Integer> ends =  schedules[vals[0]].endTime;
		vals[2] = starts.get(vals[1]);
		vals[3] = ends.get(vals[1]);
		return vals;
	}
}
