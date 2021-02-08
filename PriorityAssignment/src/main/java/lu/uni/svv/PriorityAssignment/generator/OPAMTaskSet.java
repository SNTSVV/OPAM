package lu.uni.svv.PriorityAssignment.generator;

import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.task.TaskSeverity;
import lu.uni.svv.PriorityAssignment.task.TaskType;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OPAMTaskSet {
	
	TaskDescriptor[] tasks;
	
	public OPAMTaskSet(){
	
	}
	public OPAMTaskSet(OPAMTaskSet taskset){
		super();
		tasks = new TaskDescriptor[taskset.tasks.length];
		for (int t=0; t<taskset.tasks.length; t++)
			this.tasks[t] = taskset.tasks[t].copy();
	}
	
	public OPAMTaskSet(TaskDescriptor[] tasks){
		super();
		this.tasks = tasks;
	}
	
	public OPAMTaskSet(List<TaskDescriptor> tasks){
		super();
		this.tasks = new TaskDescriptor[tasks.size()];
		for (int t=0; t<tasks.size(); t++)
			this.tasks[t] = tasks.get(t).copy();
	}
	
	public OPAMTaskSet copy(){
		return new OPAMTaskSet(this);
	}
	
	//////////////////////////////////////////////////////////////////
	// Print out the results
	//////////////////////////////////////////////////////////////////
	/**
	 *
	 * @param _filename
	 * @param _timeQuanta
	 */
	public void print(String _filename, double _timeQuanta){
		PrintStream ps;
		if (_filename==null)
			ps = System.out;
		else{
			try {
				ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(_filename, false)), true);
			}
			catch (FileNotFoundException e){
				System.out.println("Cannot find the file: "+ _filename);
				ps = System.out;
			}
		}
		
		String text = TaskDescriptor.toString(tasks, _timeQuanta);
		ps.println(text);
	}
	
	
	//////////////////////////////////////////////////////////////////
	// get information
	//////////////////////////////////////////////////////////////////
	public TaskDescriptor[] getTasks() {
		return tasks;
	}
	
	public int getNTasks(TaskType type){
		if (type == null) return tasks.length;
		
		int cnt = 0;
		for(int t=0; t<this.tasks.length; t++){
			if (tasks[t].Type == type) cnt ++;
		}
		return cnt;
	}
	
	public double getUtilization() {
		double util = 0;
		for(TaskDescriptor task : tasks){
			int period = (task.Type==TaskType.Periodic)?task.Period:task.MinIA;
			util += 1.0 * task.WCET / period;
		}
		return util;
	}
	
	public double getUtilizationLow() {
		double util = 0;
		for(TaskDescriptor task : tasks){
			int period = (task.Type==TaskType.Periodic)?task.Period:task.MaxIA;
			util += 1.0 * task.WCET / period;
		}
		return util;
	}

	public boolean isValidWCET(){
		for(TaskDescriptor task : tasks){
			if(task.WCET==0) return false;
		}
		return true;
	}
	
	public boolean isValidUtilization(double target, double delta){
		double actual = getUtilization();
		if (actual < target-delta || actual > target + delta) return false;
		return true;
	}
	
	public boolean isValidSimulationTime(int maxSimulationTime){
		double lcm = this.calculateLCM(false);
		// the reason why filtering - value of lcm is that LCM is over the maximum integer value
		if (lcm<0) return false;
		
		long maxIA = this.getMaxInterArrivalTime();
		double simTime = Math.max(lcm, maxIA);
		if (simTime>maxSimulationTime || lcm<0) return false;
		return true;
	}
	
	//////////////////////////////////////////////////////////////////
	// String Utilities
	//////////////////////////////////////////////////////////////////
	@Override
	public String toString(){
		
		String txt = String.format("NumTasks: %d [P:%d, S:%d, A:%d], Utilization: [%.3f, %.3f], Period: [%s], WCETs: [%s]",
				this.tasks.length, getNTasks(TaskType.Periodic), getNTasks(TaskType.Sporadic), getNTasks(TaskType.Aperiodic),
				getUtilization(), getUtilizationLow(), periodsToString(1), wcetsToString(1)
		);
		return txt;
	}
	
	public String getString(double timeUnit){
		String txt = String.format("NumTasks: %d [P:%d, S:%d, A:%d], Utilization: [%.3f, %.3f], Period: [%s], WCETs: [%s]",
				this.tasks.length, getNTasks(TaskType.Periodic), getNTasks(TaskType.Sporadic), getNTasks(TaskType.Aperiodic),
				getUtilization(), getUtilizationLow(), periodsToString(timeUnit), wcetsToString(timeUnit)
		);
		return txt;
	}
	
	public String periodsToString(double timeUnit){
		int precision = this.getUnitPrecision(timeUnit);
		String pf = String.format("%%.%df, ", precision);
		StringBuilder sb = new StringBuilder();
		for (int x=0; x<tasks.length; x++){
			sb.append(String.format(pf, tasks[x].Period*timeUnit));
		}
		return sb.toString();
	}
	
	public int getUnitPrecision(double timeUnit){
		int unit = 0;
		double t=1.0;
		while(timeUnit<t){
			unit += 1;
			t= t/10;
		}
		return unit;
	}
	
	public String wcetsToString(double timeUnit){
		int precision = this.getUnitPrecision(timeUnit);
		String pf = String.format("%%.%df, ", precision);
		
		StringBuilder sb = new StringBuilder();
		for (int x=0; x<tasks.length; x++){
			sb.append(String.format(pf, tasks[x].WCET*timeUnit));
		}
		return sb.toString();
	}
	
	public String ListToString(int[] list, String header){
		StringBuilder sb = new StringBuilder();
		sb.append(header);
		sb.append(": [");
		for(int i=0; i<list.length; i++){
			sb.append(list[i]);
			sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	public String ListToString(double[] list, String header){
		StringBuilder sb = new StringBuilder();
		sb.append(header);
		sb.append(": [");
		for(int i=0; i<list.length; i++){
			sb.append(String.format("%.4f, ", list[i]));
		}
		sb.append("]");
		return sb.toString();
	}
	
	//////////////////////////////////////////////////////////////////
	// calculate lcm
	//////////////////////////////////////////////////////////////////
	public long getMaxInterArrivalTime() {
		int max=0;
		for (TaskDescriptor task: tasks) {
			if (task.Type != TaskType.Aperiodic) continue;
			max = Math.max(max, task.MaxIA);
		}
		return max;
	}
	
	public long calculateLCM() {
		return calculateLCM(true, false);
	}
	
	public long calculateLCM(boolean includeAperiodic) {
		return calculateLCM(includeAperiodic, false);
	}

	public long calculateLCM(boolean includeAperiodic, boolean withMaximum) {
		long[] periods = new long[tasks.length];
		int x=0;
		for (TaskDescriptor task: tasks) {
			if (!includeAperiodic) {
				if (task.Type == TaskType.Aperiodic) continue;
			}
			long period = (task.Type==TaskType.Periodic)?task.Period:(!withMaximum?task.MinIA:task.MaxIA);
			periods[x++] = period;
		}
		
		// convert to array and get LCM
		return lcm(periods, x);
	}
	
	/**
	 * Greatest Common Divisor for two int values
	 * @param _result
	 * @param _periodArray
	 * @return
	 */
	private long gcd(long _result, long _periodArray) {
		while (_periodArray > 0) {
			long temp = _periodArray;
			_periodArray = _result % _periodArray; // % is remainder
			_result = temp;
		}
		return _result;
	}


	/**
	 * Least Common Multiple for int arrays
	 *
	 * @param _periodArray
	 * @param _length
	 * @return
	 */
	private long lcm(long[] _periodArray, long _length) {
		long result = _periodArray[0];
		for (int i = 1; i < _length; i++) {
			result = result * (_periodArray[i] / gcd(result, _periodArray[i]));
			if (result <0) return -1;
		}
		return result;
	}
}
