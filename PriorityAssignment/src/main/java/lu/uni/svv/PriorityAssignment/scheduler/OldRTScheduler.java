package lu.uni.svv.PriorityAssignment.scheduler;

import java.util.*;
import lu.uni.svv.PriorityAssignment.task.Task;
import lu.uni.svv.PriorityAssignment.arrivals.Arrivals;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
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
public class OldRTScheduler extends RTScheduler {
	
	/* For Scheduling */
	private Schedule[][]        schedules2=null;     // for saving schedule results
	
	// For Single Scheduler single core
	private Task                    curTask;
	private Task                    prevTask;
	private int                     subStartTime = 0;   // for checking preemption start time
	
	
	public OldRTScheduler(TaskDescriptor[] _tasks, int _simulationTime) {
		super(_tasks, _simulationTime);
	}
	
	/**
	 * properties to get result of scheduling
	 * @return
	 */
	public Schedule[][] getResult(){
		return schedules2;
	}
	
	/////////////////////////////////////////////////////////////////
	// Scheduling
	/////////////////////////////////////////////////////////////////
	/**
	 * Execute the RateMonotonic Algorithm
	 */
	public void run(Arrivals[] _arrivals, Integer[] priorities) {
		initilizeEvaluationTools();
		
		initialize(_arrivals);
		
		try {
			// Running Scheduler
			timeLapsed = 0;
			while(timeLapsed <= this.SIMULATION_TIME){
				// append a new task which is matched with this time
				appendNewTask(_arrivals, priorities);
				
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
			if (t1.Priority > t2.Priority)
				return 1;
			else if (t1.Priority < t2.Priority)
				return -1;
			else{
				if (t1.ExecutionID < t2.ExecutionID)
					return -1;
				else if (t1.ExecutionID > t2.ExecutionID)
					return 1;
				return 0;
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
		schedules2 = new Schedule[_arrivals.length][];
		for (int x=0; x<_arrivals.length; x++) {
			schedules2[x] = new Schedule[_arrivals[x].size()];
		}
	}
	
	/**
	 * append a new task execution into readyQueue
	 * @param _variables
	 */
	private void appendNewTask(Arrivals[] _variables, Integer[] _priorities){
		
		//compare arrivalTime and add a new execution into ReadyQueue for each task
		for (int tIDX=0; tIDX<_variables.length; tIDX++)
		{
			// Check whether there is more executions
			if (_variables[tIDX].size() <= executionIndex[tIDX]) continue;
			if (timeLapsed != _variables[tIDX].get(executionIndex[tIDX])) continue;
			
			// Add Tasks
			TaskDescriptor task = this.Tasks[tIDX];
			
			int priority = task.Priority;
			if (_priorities != null){
				priority = _priorities[tIDX];
			}
			
			Task t = new Task(task.ID, executionIndex[tIDX],
					task.WCET,
					timeLapsed,                             // arrival time
					timeLapsed + task.Deadline,    // deadline
					priority,
					task.Severity);
			readyQueue.add(t);
			executionIndex[tIDX]++;
		}
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
					logExecutions(prevTask, subStartTime, timeLapsed);
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
				logExecutions(curTask, subStartTime, (timeLapsed+1));
			}
		}
		if (RTScheduler.DETAIL) { printState(); }
		// End of this function
		return 0;
		
	}
	
	/**
	 * logging execution result into "schedules"
	 * @param _task
	 * @param _start
	 * @param _end
	 */
	private void logExecutions(Task _task, int _start, int _end){
		int tID = _task.ID-1;
		int exID = _task.ExecutionID;
		if (schedules2[tID][exID] == null) {
			schedules2[tID][exID] =
					new Schedule(_task.ArrivedTime,
							_task.Deadline,
							_start,
							_end);
		}
		else{
			schedules2[tID][exID].add(_start, _end);
		}
	}
	
	/**
	 * Utility to show the scheduler execution status
	 */
	private void printState(){
		// check state
		boolean preemption = false;
		long missedTime = 0;
		
		// process preemption
		if ((prevTask != null) && (prevTask != curTask)){
			if (prevTask.RemainTime != 0) {
				preemption = true;
			}
		}
		
		// calculate deadline miss
		if (curTask!=null) missedTime = (timeLapsed+1) - curTask.Deadline;
		
		
		// Represent working status ========================
		String prefix = " ";
		String postfix = " ";
		
		// Notification for a Task execution started.
		if ((curTask != null) &&
				((curTask.RemainTime + 1 == curTask.ExecutionTime) || (curTask != prevTask))) {
			if (preemption)	prefix = "*";
			else			prefix = "+";
		}
		
		//showing deadline miss time
		if (missedTime>0) postfix ="x";
		
		//After running if remaining time becomes zero, i.e. process is completely executed
		if ((curTask != null) && (curTask.RemainTime == 0)) {
			
			if (missedTime<0)
				postfix = "/";	// Finished Normal.
			else
				postfix = "!";	//finished after Deadline.
		}
		
		if ( (timeLapsed%LINEFEED) == 0 )
			printer.format("\nCPU[%010d]: ", timeLapsed);
		
		printer.format("%s%02d%s ", prefix, (curTask == null ? 0 : curTask.ID), postfix);
		
		if (curTask!=null && RTScheduler.PROOF) {
			int type = (postfix.compareTo(" ")!=0 && postfix.compareTo("x")!=0)? 4: (prefix.compareTo(" ")!=0)?2:3;
			timelines.get(curTask.ID -1)[timeLapsed] = type;
		}
	}
	
	
	@Override
	protected void finalize() throws Throwable{
		for (int x = 0; x < schedules2.length; x++) {
			for (int y = 0; y < schedules2[x].length; y++) {
				schedules2[x][y] = null;
			}
		}
		schedules2 = null;
	}
	
}