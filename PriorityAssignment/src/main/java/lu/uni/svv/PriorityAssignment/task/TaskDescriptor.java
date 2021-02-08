package lu.uni.svv.PriorityAssignment.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Arrays;
import lu.uni.svv.PriorityAssignment.utils.Settings;


public class TaskDescriptor implements Comparable<TaskDescriptor>{
	
	public static int UNIQUE_ID = 1;
	public int		ID;			// The sequence of input data (main key for ordering)
	public String	Name;		// Task Name
	public TaskType Type;		// Task type {Periodic, Aperiodic, Sporadic}
	public int      Offset;		// first execution time of a periodic task
	public int		WCET;	    // Worst case execution time
	public int		MaxWCET;	// Worst case execution time can be extended
	public int		Period;		// Time period which a task occurs, This variable is for the Periodic task
	public int 	    MinIA;		// Minimum inter-arrival time,This variable is for Aperiodic or Sporadic Task
	public int		MaxIA;		// Maximum inter-arrival time, This variable is for Aperiodic or Sporadic Task
	public int		Deadline;	// Time period which a task should be finished
	public int		Priority;	// Fixed Priority read from input data
	public TaskSeverity	Severity;	// {Hard, Soft}
	public int[]    Dependencies;	// {Hard, Soft}
	public int[]    Triggers;	    // {Hard, Soft}
	
	
	/**
	 * Create object without paramter
	 */
	public TaskDescriptor() {
		ID = TaskDescriptor.UNIQUE_ID++;
		Name 		= "";
		Type 		= TaskType.Periodic;
		Offset 	    = 0;
		WCET 	    = 0;
		MaxWCET     = 0;
		Period  	= 0;
		MinIA		= 0;
		MaxIA  		= 0;
		Deadline 	= 0;
		Priority 	= 0;
		Severity    = TaskSeverity.HARD;
		Dependencies= new int[0];
		Triggers    = new int[0];
	}
	
	/**
	 * Create object for copy
	 */
	public TaskDescriptor(TaskDescriptor _task) {
		ID          = _task.ID;
		Name 		= _task.Name;
		Type 		= _task.Type;
		Offset 	    = _task.Offset;
		WCET 	    = _task.WCET;
		MaxWCET 	= _task.MaxWCET;
		Period  	= _task.Period;
		MinIA		= _task.MinIA;
		MaxIA  		= _task.MaxIA;
		Deadline 	= _task.Deadline;
		Priority 	= _task.Priority;
		Severity    = _task.Severity;
		
		Dependencies= Arrays.copyOf(_task.Dependencies, _task.Dependencies.length);
		Triggers    = Arrays.copyOf(_task.Triggers, _task.Triggers.length);
	}

	public TaskDescriptor copy(){
		return new TaskDescriptor(this);
	}
	
	@Override
	public String toString(){
		String period = String.format("%d", Period);
		if (Type != TaskType.Periodic){
			period = String.format("[%d-%d]", MinIA, MaxIA);
		}
		return String.format("%s: {ID: %d, type:%s, priority:%d, period:%s, wcet:%d}, deadline:%d", Name, ID, Type, Priority, period, WCET, Deadline);
	}
	/* ********************************************************************
		Comparator
	 */
	@Override
	public int compareTo(TaskDescriptor _o) {
		if ((this.Period - _o.Period) > 0)
			return 1;
		else 
			return -1;
	}
	
	public static Comparator<TaskDescriptor> PriorityComparator = new Comparator<TaskDescriptor>() {

		@Override
		public int compare(TaskDescriptor _o1, TaskDescriptor _o2) {
			if ((_o1.Priority - _o2.Priority) > 0)
				return 1;
			else 
				return -1;
		}
		
	};
	
	public static Comparator<TaskDescriptor> PeriodComparator = new Comparator<TaskDescriptor>() {

		@Override
		public int compare(TaskDescriptor _o1, TaskDescriptor _o2) {
			if ((_o1.Period - _o2.Period) > 0)
				return 1;
			else 
				return -1;
		}
		
	};
	
	public static Comparator<TaskDescriptor> OrderComparator = new Comparator<TaskDescriptor>() {
		@Override
		public int compare(TaskDescriptor _o1, TaskDescriptor _o2) {
			if ((_o1.ID - _o2.ID) > 0)
				return 1;
			else 
				return -1;
		}
		
	};
	
	/**
	 * This comparator is for the assuming maximum fitness value
	 */
	public static Comparator<TaskDescriptor> deadlineComparator = new Comparator<TaskDescriptor>() {
		@Override
		public int compare(TaskDescriptor o1, TaskDescriptor o2) {
			int diff = o2.Deadline - o1.Deadline;
			if (diff==0){
				diff = ((o2.Type==TaskType.Periodic)?o2.Period:o2.MinIA);
				diff -= ((o1.Type==TaskType.Periodic)?o1.Period:o1.MinIA);
				if (diff == 0){
					diff = o2.MaxWCET - o1.MaxWCET;
				}
			}
			return diff;
		}
	};
	/* **********************************************************
	 *  Functions to deal with multiple task descriptions
	 */
	
	/**
	 * copy an array of task descriptors
	 * @param _tasks
	 * @return
	 */
	public static TaskDescriptor[] copyArray(TaskDescriptor[] _tasks){
		TaskDescriptor[] tasks = new TaskDescriptor[_tasks.length];
		for (int i=0; i< _tasks.length; i++){
			tasks[i] = _tasks[i].copy();
		}
		return tasks;
	}
	
	/**
	 * load from CSV file  (Time unit is TIME_QUANTA )
	 * @param _filepath
	 * @param _maximumTime
	 * @param _timeQuanta
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static TaskDescriptor[] loadFromCSV(String _filepath, double _maximumTime, double _timeQuanta) throws NumberFormatException, IOException {
		List<TaskDescriptor> listJobs = new ArrayList<>();
		
		File file = new File(_filepath);
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		long lineCnt=0;
		String data;
		while ((data = br.readLine()) != null) {
			lineCnt++;
			if (lineCnt==1) continue;   // Remove CSV file header
			if (data.trim().length()==0) continue;
			
			String[] cols = data.split(",");
			
			TaskDescriptor aJob = new TaskDescriptor();
			aJob.Name 		= cols[1].trim();	// Name
			aJob.Type 		= getTypeFromString(cols[2].trim());
			aJob.Priority 	= getValueFromString(cols[3].trim(), 10000);
			aJob.Offset     = getTimeFromString(cols[4].trim(), 0, _maximumTime, _timeQuanta);
			aJob.WCET 	    = getTimeFromString(cols[5].trim(), 0, _maximumTime, _timeQuanta);
			aJob.MaxWCET 	= getTimeFromString(cols[6].trim(), 0, _maximumTime,_timeQuanta);
			aJob.Period 	= getTimeFromString(cols[7].trim(), _maximumTime, _maximumTime, _timeQuanta);
			aJob.MinIA 		= getTimeFromString(cols[8].trim(), 0, _maximumTime,_timeQuanta);
			aJob.MaxIA		= getTimeFromString(cols[9].trim(), _maximumTime, _maximumTime, _timeQuanta);
			aJob.Deadline 	= getTimeFromString(cols[10].trim(), _maximumTime, _maximumTime, _timeQuanta);
			aJob.Severity 	= getSeverityFromString(cols[11].trim());	// Severity type
			if (cols.length>12) {
				aJob.Dependencies = getListFromString(cols[12].trim());
			}
			if (cols.length>13) {
				aJob.Triggers 	= getListFromString(cols[13].trim());
			}
			
			listJobs.add(aJob);
		}
		// after loop, close reader
		br.close();
		
		// Return loaded data
		TaskDescriptor[] tasks = new TaskDescriptor[listJobs.size()];
		listJobs.toArray(tasks);
		return tasks;
	}
	
	/**
	 * converting to string to store an array of task descriptors
	 * @param _tasks
	 * @param _timeQuanta
	 * @return
	 */
	public static String toString(TaskDescriptor[] _tasks, double _timeQuanta){
		StringBuilder sb = new StringBuilder();
		
		sb.append("Task ID, Task Name,Task Type,Task Priority,Offset,WCET min,WCET max,Task Period (ms),Minimum interarrival-time (ms),Maximum Interarrival time,Task Deadline, Deadline Type, Dependencies, Triggers\n");
		for(TaskDescriptor task:_tasks)
		{
			sb.append(String.format("%d,\"%s\",%s,%d,%f,%f,%f,%f",
					task.ID,
					task.Name, task.Type.toString(), task.Priority, task.Offset* _timeQuanta,
					task.WCET * _timeQuanta, task.MaxWCET* _timeQuanta,
					task.Period * _timeQuanta));
			
			if (task.Type==TaskType.Periodic) {
				sb.append(",,");
			}
			else{
				sb.append(String.format(",%f,%f", task.MinIA* _timeQuanta, task.MaxIA* _timeQuanta));
			}
			sb.append(String.format(",%f,%s,%s,%s\n",
					  task.Deadline* _timeQuanta, task.Severity,
					   listtoString(task.Dependencies), listtoString(task.Triggers)));
		}
		return sb.toString();
	}
	
	/**
	 * Generates masks for each task that is not arrived by its period due to the triggering tasks
	 * @param _tasks
	 * @return
	 */
	public static boolean[] getArrivalExceptionTasks(TaskDescriptor[] _tasks){
		boolean[] list = new boolean[_tasks.length+1];
		for(int x=0; x<_tasks.length; x++){
			for(int i=0; i<_tasks[x].Triggers.length; i++){
				list[_tasks[x].Triggers[i]] = true;
			}
		}
		return list;
	}
	
	/**
	 * Finds the maximum number of resources from task descriptions
	 * @param _tasks
	 * @return
	 */
	public static int getMaxNumResources(TaskDescriptor[] _tasks){
		// find max number of resources;
		int maxResource = 0;
		for (int tIDX=0; tIDX<_tasks.length; tIDX++) {
			for (int r=0; r<_tasks[tIDX].Dependencies.length; r++){
				if (_tasks[tIDX].Dependencies[r] > maxResource)
					maxResource = _tasks[tIDX].Dependencies[r];
			}
		}
		return maxResource;
	}
	
	
	/**
	 * Get aperiodic tasks which inter-arrival times are different
	 * @return
	 */
	public static List<Integer> getVaryingTasks(TaskDescriptor[] _tasks){
		List<Integer> list = new ArrayList<Integer>();
		
		for(TaskDescriptor task : _tasks){
			if (task.Type != TaskType.Periodic && task.MinIA != task.MaxIA)
				list.add(task.ID);
		}
		return list;
	}
	
	/**
	 * Get a number of aperiodic tasks
	 * @return
	 */
	public static int getNumberOfAperiodics(TaskDescriptor[] _tasks){
		int count=0;
		for (TaskDescriptor task:_tasks){
			if (task.Type == TaskType.Aperiodic)
				count+=1;
		}
		return count;
	}
	
	/**
	 * Find maximum deadline among an array of task descriptors
	 * @param _tasks
	 * @return
	 */
	public static int findMaximumDeadline(TaskDescriptor[] _tasks){
		int max_dealine=0;
		for (TaskDescriptor task:_tasks){
			if (task.Deadline> max_dealine)
				max_dealine = task.Deadline;
		}
		return max_dealine;
	}
	
	/**
	 * Get minimim fitness value (not exact)
	 * @param _tasks
	 * @param simulationTime
	 * @return
	 */
	public static double getMinFitness(TaskDescriptor[]  _tasks, long simulationTime){
		double fitness = 0.0;
		for (TaskDescriptor task: _tasks){
			int period = (task.Type== TaskType.Periodic)?task.Period:task.MaxIA;
			int nArrivals = (int)Math.ceil(simulationTime / (double)period);
			int diff = task.WCET - task.Deadline;
			fitness += Math.pow(Settings.FD_BASE, diff/Settings.FD_EXPONENT) * nArrivals;
		}
		return fitness;
	}
	
	/**
	 * Get maximum fitness value order by deadline and calculate cumulated WCET
	 * This assumes the maximum fitness value (not exact)
	 * @param _tasks
	 * @param simulationTime
	 * @return
	 */
	public static double getMaxFitness(TaskDescriptor[] _tasks, long simulationTime){
		TaskDescriptor[] tasks = TaskDescriptor.copyArray(_tasks);
		Arrays.sort(tasks,TaskDescriptor.deadlineComparator);
		
		double fitness = 0.0;
		int WCET = 0;
		for (TaskDescriptor task: tasks){
			int period = (task.Type == TaskType.Periodic)?task.Period:task.MinIA;
			int nArrivals = (int)Math.ceil(simulationTime / (double)period);
			WCET = WCET + task.MaxWCET;
			int diff = WCET - task.Deadline;
			double fitItem = Math.pow(Settings.FD_BASE, diff/Settings.FD_EXPONENT);
			if (fitItem == Double.NEGATIVE_INFINITY) fitItem = 0;
			if (fitItem == Double.POSITIVE_INFINITY) fitItem = Double.MAX_VALUE;
			fitness += fitItem * nArrivals;
		}
		return fitness;
	}
	
	/**
	 * Convert list to string with ';' delimiter
	 * This function is for the dependency and triggering list
	 * @param _items
	 * @return
	 */
	public static String listtoString(int[] _items){
		StringBuilder sb = new StringBuilder();
		for (int x=0; x<_items.length; x++){
			if (x!=0) sb.append(';');
			sb.append(_items[x]);
		}
		return sb.toString();
	}
	
	/////////
	// sub functions for Loading from CSV
	/////////
	public static TaskType getTypeFromString(String _text) {
		
		if (_text.toLowerCase().compareTo("sporadic")==0)
			return TaskType.Sporadic;
		else if (_text.toLowerCase().compareTo("aperiodic")==0)
			return TaskType.Aperiodic;
		else
			return TaskType.Periodic;
	}
	
	public static TaskSeverity getSeverityFromString(String _text) {
		
		if (_text.toLowerCase().compareTo("soft")==0)
			return TaskSeverity.SOFT;
		else
			return TaskSeverity.HARD;
	}
	
	public static int getValueFromString(String _text, int _default) {
		
		if (_text.compareTo("")==0 || _text.compareTo("N/A")==0)
			return _default;
		else
			return (int)(Double.parseDouble(_text));
	}
	
	public static int getTimeFromString(String _text, double _default, double _max, double _timeQuanta) {
		double value = 0.0;
		if (_text.compareTo("")==0 || _text.compareTo("N/A")==0)
			value = _default;
		else {
			value = Double.parseDouble(_text);
			if (_max !=0 && value > _max) value =  _max;
		}
		return (int)(value * (1 / _timeQuanta));
	}
	
	public static int[] getListFromString(String _text) {
		String[] texts = _text.split(";");
		int[] items = new int[texts.length];
		int cnt=0;
		for ( int i=0; i< texts.length; i++){
			texts[i] = texts[i].trim();
			if(texts[i].length()==0) continue;
			items[i] =  Integer.parseInt(texts[i]);
			cnt++;
		}
		
		if (cnt==0){
			return new int[0];
		}
		return items;
	}
}