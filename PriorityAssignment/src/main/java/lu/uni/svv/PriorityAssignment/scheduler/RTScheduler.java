package lu.uni.svv.PriorityAssignment.scheduler;

import java.io.PrintStream;
import java.util.*;
import lu.uni.svv.PriorityAssignment.task.Task;
import lu.uni.svv.PriorityAssignment.arrivals.Arrivals;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.task.TaskState;
import lu.uni.svv.PriorityAssignment.task.TaskType;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;




/**
 * Scheduling Policy
 * 	- We use Fixed Priority to schedule tasks.
 * 	- The two task has same priority, first arrived execution in the ready queue has higher priority to use CPU.
 *  -   In the other words, the execution that is arrived lately couldn't preempt the other executions already arrived in the ready queue.
 *
 * Deadline misses detection
 * 	- We count the number of deadline misses while the scheduling(by SIMULATION_TIME)
 * 	- And, if the ready queue has tasks after being done scheduling(after SIMULATION_TIME),
 * 			we will execute this scheduler by finishQuing all tasks in the ready queue.
 * @author jaekwon.lee
 *
 */
public class RTScheduler {
	
	/* Common Scheduling Variables */
	protected int                   SIMULATION_TIME;
	protected TaskDescriptor[]      Tasks;
	protected PriorityQueue<Task>   readyQueue;
	protected int[]                 executionIndex = null;
	protected ArrayList<Schedule>[] schedules=null;     // for saving schedule results
	protected int                   timeLapsed;		    // current CPU time quanta
	protected Integer[]             priorities;         // priorities
	
	// For Single Scheduler single core
	private Task                    curTask;
	private Task                    prevTask;
	private int                     subStartTime = 0;   // for checking preemption start time
	
	/* For Debugging */
	public static boolean   DETAIL          = false;    // Show detail information (for debugging)
	public static boolean   PROOF           = false;    // Show proof of execution (for debugging)
	protected long          LINEFEED        = 50;		// This value uses for showing CPU execution detail, This value gives line feed every LINEFEED time quanta
	protected PrintStream   printer         = null;	    // print object to show progress during the scheduling.
	protected List<int[]>   timelines       = null;     // Timelines for each task
	protected List<Task>    executedTasks   = null;     // Save all executions' details
	
	
	public RTScheduler(TaskDescriptor[] _tasks, int _simulationTime) {
		this.Tasks = _tasks;
		this.SIMULATION_TIME = _simulationTime;
		printer = System.out;
	}
	
	/*
	 * setter
	 */
	public void setPrinter(PrintStream _stream) {
		printer = _stream;
	}
	
	/**
	 * properties to get result of scheduling
	 * @return
	 */
	public Schedule[][] getResult(){
		Schedule[][] result = new Schedule[schedules.length][];
		for (int x=0; x<schedules.length; x++){
			result[x] = new Schedule[schedules[x].size()];
			schedules[x].toArray(result[x]);
		}
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// Scheduling
	/////////////////////////////////////////////////////////////////
	/**
	 * Execute the RateMonotonic Algorithm
	 */
	public void run(Arrivals[] _arrivals, Integer[] _priorities) {
		initilizeEvaluationTools();
		
		initialize(_arrivals);
		priorities = _priorities;
		
		try {
			// Running Scheduler
			timeLapsed = 0;
			while(timeLapsed <= this.SIMULATION_TIME){
				// append a new task which is matched with this time
				appendNewTask(_arrivals);
				
				//Execute Once!
				executeOneUnit();
				timeLapsed++;
			}
			
			//Check cycle complete or not  (It was before ExecuteOneUnit() originally)
			if (Settings.EXTEND_SCHEDULER && readyQueue.size() > 0) {
				if (RTScheduler.DETAIL)
				{
					printer.println("\nEnd of expected time quanta");
					printer.println("Here are extra execution because of ramaining tasks in queue");
					printer.println("------------------------------------------");
				}
//				System.out.println(String.format("Exetended, still we have %d executions", readyQueue.size()));
				while(readyQueue.size() > 0) {
					executeOneUnit();
					timeLapsed++;
				}
//				System.out.println(String.format("Ended %.1fms", time*0.1));
			}
		} catch (Exception e) {
			printer.println("Some error occurred. Program will now terminate: " + e);
			e.printStackTrace();
		}
		
		// schedules value updated inside function among functions used this function
		return ;
	}
	
	/**
	 * Ready queue for the Scheduler
	 *   This uses fixed priority for adding new task
	 *
	 */
	public Comparator<Task> queueComparator = new Comparator<Task>(){
		@Override
		public int compare(Task t1, Task t2) {
			if (t1.Priority != t2.Priority)
				return t2.Priority - t1.Priority;   // the higher number is the higher priority
			else {
				if (t1.ID != t2.ID)
					return t1.ID - t2.ID;  // the lower number of task order is the the higher priority
				else {
					if (t1.ExecutionID != t2.ExecutionID)
						return t1.ExecutionID - t2.ExecutionID;  // the lower number of execution ID is the the higher priority
					else
						return 0;
				}
			}
		}
	};
	
	/**
	 * initialize scheduler
	 */
	protected void initialize(Arrivals[] _arrivals){
		timeLapsed = 0;
		curTask = null;
		prevTask = null;
		readyQueue = new PriorityQueue<Task>(60000, queueComparator);
		
		// initialize index tables for accessing execution index at each time (all index starts from 0)
		this.executionIndex= new int[_arrivals.length];
		Arrays.fill(executionIndex, 0);
		
		// Initialize result schedules
		schedules = new ArrayList[_arrivals.length]; // (ArrayList<Schedule>[])
		for (int x=0; x<_arrivals.length; x++) {
			schedules[x] = new ArrayList<>();
		}
	}
	
	/**
	 * append a new task execution into readyQueue
	 * @param _variables
	 */
	protected void appendNewTask(Arrivals[] _variables){
		
		//compare arrivalTime and add a new execution into ReadyQueue for each task
		for (int tIDX=0; tIDX<_variables.length; tIDX++)
		{
			// Check whether there is more executions
			if (_variables[tIDX].size() <= executionIndex[tIDX]) continue;
			if (timeLapsed != _variables[tIDX].get(executionIndex[tIDX])) continue;
			if (Tasks[tIDX].WCET==0 && Tasks[tIDX].MaxWCET==0) continue;
			
			// Add Tasks
			addReadyQueue(tIDX, timeLapsed);
		}
	}
	
	
	protected boolean addReadyQueue(int taskIdx, int _currentTime){
		TaskDescriptor taskDesc = this.Tasks[taskIdx];
		
		int priority = taskDesc.Priority;
		if (priorities != null){
			priority = priorities[taskIdx];
		}
		
		Task t = new Task(taskDesc.ID, executionIndex[taskIdx],
				taskDesc.WCET,
				_currentTime,                             // arrival time
				_currentTime + taskDesc.Deadline,    // deadline
				priority,
				taskDesc.Severity);
		
		t.updateTaskState(TaskState.Ready, _currentTime);
		newSchedule(t);
		readyQueue.add(t);
		executionIndex[taskIdx]++;
		return true;
	}
	
	/**
	 *
	 * @return 0: Nothing to do
	 *		 1: Everything went normally
	 *		 2: Deadline Missed!
	 *		 3: Process completely executed without missing the deadline
	 * @throws Exception
	 */
	protected int executeOneUnit() throws Exception {
		// init current task
		prevTask = curTask;
		curTask = null;
		
		// If it has tasks to be executed
		if (!readyQueue.isEmpty()) {
			// Get one task from Queue
			curTask = readyQueue.peek();
			if (curTask.RemainTime <= 0)
				throw new Exception(); //Somehow remaining time became negative, or is 0 from first
			
			// process preemption
			if ((prevTask != null) && (prevTask != curTask)){
				if (prevTask.RemainTime != 0){
					addSchedule(prevTask, subStartTime, timeLapsed, 0);
				}
				subStartTime = timeLapsed;
			}
			
			// Set StartedTime
			if (curTask.RemainTime == curTask.ExecutionTime) {
				curTask.StartedTime = timeLapsed;
				subStartTime = timeLapsed;
			}
			
			// Execute
			curTask.RemainTime--;
		}
		
		// CPU time increased!
		// timeLapsed++;  // Process out of thie function, we assume that timeLapsed is increased after this line
		
		if (curTask != null) {
			// Check task finished and deadline misses
			if (curTask.RemainTime == 0) {
				readyQueue.poll();    // Task finished, poll the Task out.
				
				// Set finished time of the task ended
				curTask.FinishedTime = (timeLapsed+1);
				addSchedule(curTask, subStartTime, (timeLapsed+1),0);
			}
		}

		return 0;
	}
	
	/**
	 * add execution result into "schedules"
	 * @param _task
	 * @param _start
	 * @param _end
	 */
	protected void addSchedule(Task _task, int _start, int _end, int _cid){
		int tID = _task.ID-1;
		int exID = _task.ExecutionID;
		if (schedules[tID].size()-1 < exID) {
			Schedule s = new Schedule(_task.ArrivedTime, _task.Deadline, _start, _end, _cid);
			schedules[tID].add(s);
		}
		else{
			schedules[tID].get(exID).add(_start, _end, _cid);
		}
	}
	
	protected void newSchedule(Task _task){
		int tID = _task.ID-1;
		int exID = _task.ExecutionID;
		schedules[tID].add(new Schedule(_task.ArrivedTime, _task.Deadline));
	}
	
	
	/////////////////////////////////////////////////////////////////
	// Utilities
	/////////////////////////////////////////////////////////////////
	/**
	 * calculating Task Utilization
	 * @return
	 */
	public double calculateUtilization() {
		
		double result = 0;
		
		for (TaskDescriptor task:this.Tasks)
		{
			if (task.Type == TaskType.Periodic)
				result += (task.WCET / (double)task.Period);
			else
				result += (task.WCET / (double)task.MinIA);
		}
		return result;
	}
	
	/**
	 * check input data's feasibility
	 * @return
	 */
	public boolean checkFeasibility() {
		double feasible = muSigma(this.Tasks.length);
		
		if (feasible >= calculateUtilization())
			return true;
		return false;
	}
	
	/**
	 * Greatest Common Divisor for two int values
	 * @param _result
	 * @param _periodArray
	 * @return
	 */
	public static long gcd(long _result, long _periodArray) {
		while (_periodArray > 0) {
			long temp = _periodArray;
			_periodArray = _result % _periodArray; // % is remainder
			_result = temp;
		}
		return _result;
	}
	
	/**
	 * Least Common Multiple for two int numbers
	 * @param _result
	 * @param _periodArray
	 * @return
	 */
	public static long lcm(long _result, long _periodArray) {
		return _result * (_periodArray / RTScheduler.gcd(_result, _periodArray));
	}
	
	/**
	 * Least Common Multiple for int arrays
	 *
	 * @param _periodArray
	 * @return
	 */
	public static long lcm(long[] _periodArray) {
		long result = _periodArray[0];
		for (int i = 1; i < _periodArray.length; i++) {
			result = RTScheduler.lcm(result, _periodArray[i]);
			if (result<0){
				result = -1;
				break;
			}
			
		}
		return result;
	}
	
	/**
	 * calculate feasible values
	 * @param _n
	 * @return
	 */
	public double muSigma(int _n) {
		return ((double) _n) * ((Math.pow((double) 2, ((1 / ((double) _n)))) - (double) 1));
	}
	
	
	/////////////////////////////////////////////////////////////////
	//  Evaluation functions
	/////////////////////////////////////////////////////////////////
	public void initilizeEvaluationTools()
	{
		if (RTScheduler.DETAIL == false) return;
		
		if (RTScheduler.PROOF == true) {
			timelines = new ArrayList<int[]>();
			for (TaskDescriptor task : this.Tasks)
				timelines.add(new int[(int) this.SIMULATION_TIME*2]);
		}
		
		executedTasks = new ArrayList<Task>();
	}
	
	class SortbyPriority implements Comparator<TaskDescriptor>
	{
		@Override
		public int compare(TaskDescriptor o1, TaskDescriptor o2) {
			return (int)(o1.Priority - o2.Priority);
		}
	}
	
	protected boolean isHigherTasksActive(Task t) {
		// find higher priority tasks
		List<Integer> IDXs = this.getHigherPriorityTasks(t);
		
		// find active states for all IDXs
		for(int idx:IDXs) {
			if (timelines.get(idx)[(int)(t.ArrivedTime)] != 0)
				return true;
		}
		return false;
	}
	
	protected boolean isLowerTasksActive(Task t) {
		// find lower priority tasks
		List<Integer> IDXs = this.getLowerPriorityTasks(t);
		
		//find lower tasks active
		for(int idx:IDXs) {
			int[] timeline = timelines.get(idx);
			for(int x=(int)t.StartedTime; x<t.FinishedTime; x++) {
				if (timeline[x]!=0) return true;
			}
		}
		return false;
	}
	
	protected List<Integer> getHigherPriorityTasks(Task t)
	{
		ArrayList<Integer> IDXs = new ArrayList<Integer>();
		for(TaskDescriptor item:this.Tasks) {
			if (item.Priority < t.Priority)
				IDXs.add(item.ID-1);
			if (item.Priority == t.Priority && item.ID<t.ID)
				IDXs.add(item.ID-1);
		}
		return IDXs;
	}
	
	protected List<Integer> getLowerPriorityTasks(Task t)
	{
		// find lower priority tasks
		ArrayList<Integer> IDXs = new ArrayList<Integer>();
		for(TaskDescriptor item:this.Tasks) {
			if (item.Priority > t.Priority)
				IDXs.add(item.ID-1);
			if (item.Priority == t.Priority && item.ID<t.ID)
				IDXs.add(item.ID-1);
		}
		return IDXs;
	}
	
	protected int getMinimumInactiveTimeDelta(Task t) {
		List<Integer> IDXs = this.getHigherPriorityTasks(t);
		
		int tq = (int)t.ArrivedTime;
		for (; tq<this.SIMULATION_TIME; tq++)
		{
			boolean isActive = false;
			for(int idx:IDXs) {
				if(timelines.get(idx)[tq] > 1) {
					isActive = true;
					break;
				}
			}
			if (!isActive)
				break;
		}
		
		return (int)(tq - t.ArrivedTime);
	}
	
	public int getMaximumDelayTime(Task t) {
		// find higher priority tasks
		List<Integer> IDXs = this.getHigherPriorityTasks(t);
		
		//calculate sum of delay for all higher priority tasks
		int delay =0;
		for(int tID:IDXs) {
			delay += this.Tasks[tID].WCET;
		}
		
		// calculate WCET of tasks which the tasks' periods are longer than 'delay'.
		int[] multi = new int[this.Tasks.length];
		Arrays.fill(multi, 1);
		while(true) {
			boolean flag = false;
			for(int tID:IDXs) {
				TaskDescriptor item = this.Tasks[tID];
				int period = (int)( (item.Type==TaskType.Periodic) ? item.Period : item.MinIA);
				if (delay < period*multi[tID]) break;
				delay += item.WCET;
				multi[tID]+=1;
				flag = true;
			}
			if (flag == false) break;
		}
		return delay;
	}
	
	public boolean assertScheduler(GAWriter writer) {
		if (!RTScheduler.DETAIL || !RTScheduler.PROOF) return true;
		
		for (Task task:executedTasks)
		{
			String str = String.format("{ID:%d,Arrived:%03d, Started:%03d, Finished:%03d, Deadline:%03d}: ", task.ID, task.ArrivedTime, task.StartedTime, task.FinishedTime, task.Deadline+task.ArrivedTime);
			writer.print(str);
			try {
				//first assert
				if (!isHigherTasksActive(task))	{
					assert task.StartedTime == task.ArrivedTime: "Failed to assert higher_tasks_non_active";
				}
				else {
					assert task.StartedTime <= (task.ArrivedTime+getMinimumInactiveTimeDelta(task)): "Failed to assert non exceed WCET";
				}
				//second assert
				assert !isLowerTasksActive(task): "Failed to assert lower_tasks_active";
				
				writer.print("Success\n");
			}
			catch(AssertionError e) {
				writer.print(e.getMessage()+"\n");
			}//			
		}// for
		
		return true;
	}
	
	public  String getTimelinesStr()
	{
		if (!RTScheduler.DETAIL || !RTScheduler.PROOF) return "";
		
		StringBuilder sb = new StringBuilder();
		for(int tID=0; tID<this.Tasks.length; tID++)
		{
			sb.append(String.format("Task %02d: ", tID+1));
			for (int x=0; x<this.SIMULATION_TIME; x++) {
				switch(timelines.get(tID)[x]) {
					case 0:	sb.append("0 "); break;     // Not working
					case 1:	sb.append("A "); break;     // Arrived
					case 2:	sb.append("S "); break;     // Started
					case 3:	sb.append("W "); break;     // Working
					case 4:	sb.append("E "); break;     // Ended
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	@Override
	protected void finalize() throws Throwable{
		for (int x = 0; x < schedules.length; x++) {
			schedules[x].clear();
			schedules[x] = null;
		}
		schedules = null;
	}
	
	/////////////////////////////////////////////////////////////////
	//  Information functions
	/////////////////////////////////////////////////////////////////
//
//	public String getMissedDeadlineString() {
//		StringBuilder sb = new StringBuilder();
//		sb.append("TaskID,ExecutionID,Arrival,Started,Finished,Deadline,Misses(finish-deadline)\n");
//
//		for (int tid=1; tid<=this.Tasks.length; tid++) {
//			for (Task item:missedDeadlines) {
//				if (tid!=item.ID) continue;
//				long deadline_tq = (item.ArrivedTime+item.Deadline);
//				sb.append(String.format("%d,%d,%d,%d,%d,%d,%d\n",
//						item.ID, item.ExecutionID, item.ArrivedTime, item.StartedTime, item.FinishedTime, deadline_tq, item.FinishedTime - deadline_tq));
//			}
//		}
//		return sb.toString();
//	}
//
//	public String getByproduct() {
//		return "";
//	}
//
//	public Task getMissedDeadlineTask(int idx){
//		if (this.missedDeadlines.size()>idx)
//			return this.missedDeadlines.get(idx);
//		return null;
//	}
//
//	/**
//	 * get Deadline Missed items
//	 * @return
//	 */
//	public String getExecutedTasksString() {
//		StringBuilder sb = new StringBuilder();
//		sb.append("TaskID,ExecutionID,Arrival,Started,Finished,Deadline,Misses(finish-deadline),Pow\n");
//
//		for (int tid=1; tid<=this.Tasks.length; tid++) {
//			for (Task item:executedTasks) {
//				if (tid!=item.ID) continue;
//
//				long deadline_tq = (item.ArrivedTime + item.Deadline);
//				int missed = (int)(item.FinishedTime - deadline_tq);
//				double fitness_item = evaluateDeadlineMiss(item, missed);
//
//				sb.append(String.format("%d,%d,%d,%d,%d,%d,%d,%.32e\n",
//						item.ID, item.ExecutionID,item.ArrivedTime, item.StartedTime, item.FinishedTime, deadline_tq, missed, fitness_item));
//			}
//		}
//		return sb.toString();
//	}
}