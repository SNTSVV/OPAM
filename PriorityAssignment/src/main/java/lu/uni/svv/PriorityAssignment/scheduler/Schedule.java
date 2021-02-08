package lu.uni.svv.PriorityAssignment.scheduler;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;


public class Schedule {
	int arrivalTime;
	int deadline;
	int finishedTime;
	int executedTime;
	int activatedNum;
	ArrayList<Integer> startTime;
	ArrayList<Integer> endTime;
	ArrayList<Integer> CPUs;
	
	public Schedule(int arrival, int deadline, int start, int end){
		this(arrival, deadline, start, end, 0);
	}
	
	public Schedule(int arrival, int deadline){
		this.arrivalTime = arrival;
		this.deadline = deadline;
		this.finishedTime = 0;
		this.executedTime = 0;
		this.activatedNum = 0;
		startTime = new ArrayList<>(5);
		endTime = new ArrayList<>(5);
		CPUs = new ArrayList<>(5);
	}
	
	public Schedule(int arrival, int deadline, int start, int end, int cpu){
		this.arrivalTime = arrival;
		this.deadline = deadline;
		if (cpu>=0) {
			this.finishedTime = end;
			this.executedTime = end - start;
		}
		else
			this.finishedTime = 0;
		this.activatedNum = 1;
		startTime = new ArrayList<>(5);
		endTime = new ArrayList<>(5);
		CPUs = new ArrayList<>(5);
		startTime.add(start);
		endTime.add(end);
		CPUs.add(cpu);
	}
	
	/**
	 * For loading schedule
	 * @param arrival
	 * @param deadline
	 * @param finished
	 * @param executed
	 * @param activated
	 * @param starts
	 * @param ends
	 * @param cpus
	 */
	public Schedule(int arrival, int deadline, int finished, int executed, int activated, ArrayList<Integer> starts, ArrayList<Integer> ends, ArrayList<Integer> cpus){
		this.arrivalTime = arrival;
		this.deadline = deadline;
		this.finishedTime = finished;
		this.executedTime = executed;
		this.activatedNum = activated;
		startTime = starts;
		endTime = ends;
		CPUs = cpus;
	}
	
	public void add(int start, int end){
		add(start, end, 0);
	}
	
	public void add(int start, int end, int cpu){
		this.activatedNum++;
		startTime.add(start);
		endTime.add(end);
		CPUs.add(cpu);
		if (cpu>=0) {
			this.executedTime += end - start;
			this.finishedTime = end;
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		startTime.clear();
		endTime.clear();
		startTime = null;
		endTime = null;
		super.finalize();
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(arrivalTime);
		sb.append(", ");
		sb.append(deadline);
		sb.append(", ");
		sb.append(finishedTime);
		sb.append(", ");
		sb.append(executedTime);
		sb.append(", ");
		sb.append(activatedNum);
		sb.append(", [");
		for( int x=0; x<activatedNum; x++){
			sb.append("[");
			sb.append(startTime.get(x));
			sb.append(",");
			sb.append(endTime.get(x));
			sb.append(",");
			sb.append(CPUs.get(x));
			sb.append("]");
			if (x+1!=activatedNum) sb.append(", ");
		}
		sb.append("] ]");
		
		return sb.toString();
	}
	
	////////////////////////////////////////////////////////////////
	// Static functions
	////////////////////////////////////////////////////////////////
	public static void printSchedules(String _filename, Schedule[][] _schedules){
		
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int tID=0; tID<_schedules.length; tID++){
			sb.append("\t[");
			for (int x=0; x<_schedules[tID].length; x++) {
				sb.append("\t\t");
				sb.append(_schedules[tID][x].toString());
				if (x+1==_schedules[tID].length){
					sb.append("\n");
				}
				else{
					sb.append(",\n");
				}
			}
			if (tID+1 == _schedules.length)
				sb.append("\t]");
			else
				sb.append("\t],");
		}
		sb.append("]");
		
		GAWriter writer = new GAWriter(_filename, Level.INFO, "", null);
		writer.write(sb.toString());
		writer.close();
	}
	
	public static Schedule[][] loadSchedules(String _filename){
		
		// Parse Json
		Schedule[][] schedules = null;
		FileReader reader = null;
		
		try {
			reader = new FileReader(_filename);
			JSONParser parser = new JSONParser();
			JSONArray json = (JSONArray) parser.parse(reader);
			
			schedules = new Schedule[json.size()][];
			for (int t = 0; t < json.size(); t++) {
				JSONArray execs = (JSONArray) json.get(t);
				
				schedules[t] = new Schedule[execs.size()];
				for (int x=0; x<execs.size(); x++) {
					JSONArray items = (JSONArray) execs.get(x);
					
					// prepare variables
					int activatedNum =  ((Long)items.get(4)).intValue();
					ArrayList<Integer> starts = new ArrayList<>();
					ArrayList<Integer> ends = new ArrayList<>();
					ArrayList<Integer> cpus = new ArrayList<>();
					
					// load activates
					JSONArray activates = (JSONArray) items.get(5);
					for (int a=0; a<activatedNum; a++) {
						JSONArray values = (JSONArray)activates.get(a);
						starts.add(((Long)values.get(0)).intValue());
						ends.add(((Long)values.get(1)).intValue());
						int cpu = (values.size()>2) ? ((Long)values.get(2)).intValue() : 0;
						cpus.add(cpu);
					}

					// create new schedule object
					schedules[t][x] = new Schedule(((Long)items.get(0)).intValue(),
							((Long)items.get(1)).intValue(),
							((Long)items.get(2)).intValue(),
							((Long)items.get(3)).intValue(), activatedNum, starts, ends, cpus);
				}
			}
		}
		catch (IOException | ParseException e){
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return schedules;
	}
}
