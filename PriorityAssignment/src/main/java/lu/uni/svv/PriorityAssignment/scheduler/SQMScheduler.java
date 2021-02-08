package lu.uni.svv.PriorityAssignment.scheduler;

import lu.uni.svv.PriorityAssignment.arrivals.Arrivals;
import lu.uni.svv.PriorityAssignment.task.Task;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.task.TaskState;
import lu.uni.svv.PriorityAssignment.utils.RandomGenerator;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import java.util.ArrayList;


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
public class SQMScheduler extends RTScheduler{
	
	/* For Scheduling */
	protected Task[]                CPU;
	protected int[]                 resources;          // shared resources
	protected ArrayList<Task>[]     blockQueue;         // shared resources
	protected boolean[]             exceptions;         // for excepting periodic arrival to triggered task
	protected ArrayList<Integer>    triggerList;        // tasks list to make trigger event
	
	protected int[]                 CIDs;
	RandomGenerator                 randomGen;
	
	public SQMScheduler(TaskDescriptor[] _tasks, int _simulationTime) {
		super(_tasks, _simulationTime);
	}


	/////////////////////////////////////////////////////////////////
	// Scheduling
	/////////////////////////////////////////////////////////////////
	/**
	 * initialize scheduler
	 */
	protected void initialize(Arrivals[] _arrivals){
		super.initialize(_arrivals);
		
		this.CPU = new Task[Settings.N_CPUS];
		for (int i=0; i<Settings.N_CPUS; i++) { this.CPU[i] = null; }
		
		// for randomizing selecting a CPU
		randomGen = new RandomGenerator();
		CIDs = new int[Settings.N_CPUS];
		for (int i = 0; i <CIDs.length; i++) { CIDs[i] = i; }
		
		int maxResources = TaskDescriptor.getMaxNumResources(Tasks);
		this.resources = new int[maxResources+1]; // used from index 1;
		this.blockQueue = new ArrayList[this.resources.length];
		for(int x=0; x<blockQueue.length; x++)
			this.blockQueue[x] = new ArrayList<>();
		
		this.triggerList = new ArrayList<>();
		this.exceptions = TaskDescriptor.getArrivalExceptionTasks(Tasks);
	}
	

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
			while (timeLapsed <= this.SIMULATION_TIME) {
				// append a new task which is matched with this time
				appendNewTask(_arrivals);
			
				//Execute Once!
				executeMultiCore(Settings.N_CPUS);
				timeLapsed++;
			}
			
			//Check cycle complete or not  (It was before ExecuteOneUnit() originally)
			if (Settings.EXTEND_SCHEDULER && checkExecution()) {
				if (SQMScheduler.DETAIL) {
					printer.println("\nEnd of expected time quanta");
					printer.println("Here are extra execution because of ramaining tasks in queue");
					printer.println("------------------------------------------");
				}
//				System.out.println(String.format("Exetended, still we have %d executions", readyQueue.size()));
				while (checkExecution()) {
					executeMultiCore(Settings.N_CPUS);
					timeLapsed++;
				}
			}
		} catch (Exception e) {
			printer.println("Some error occurred. Program will now terminate: " + e);
			e.printStackTrace();
		}
		
		if (Settings.VERIFY_SCHEDULE && this.SIMULATION_TIME<100000)
			new ScheduleVerify(getResult(), Tasks, priorities, _arrivals).verify();
		
		return;
	}
	
	protected boolean checkExecution(){
		if (readyQueue.size() > 0) return true;
		
		for (int i=0; i< CPU.length; i++){
			if (CPU[i] != null) return true;
		}
		return false;
	}
	
	/**
	 * append a new task execution into readyQueue
	 * @param _variables
	 */
	protected void appendNewTask(Arrivals[] _variables){
		
		//compare arrivalTime and add a new execution into ReadyQueue for each task
		for (int tIDX=0; tIDX<_variables.length; tIDX++)
		{
			if (exceptions[this.Tasks[tIDX].ID]) continue;
			
			// Check whether there is more executions
			if (_variables[tIDX].size() <= executionIndex[tIDX]) continue;
			if (timeLapsed != _variables[tIDX].get(executionIndex[tIDX])) continue;
			if (Tasks[tIDX].MaxWCET==0 && Tasks[tIDX].MaxWCET==0) continue;
			
			// Add Tasks
			addReadyQueue(tIDX, timeLapsed);
		}
	}
	
	/**
	 * work with multi core
	 * We randomly choose the execution order of CPUs
	 * @param nCPUs
	 * @return
	 * @throws Exception
	 */
	public int executeMultiCore(int nCPUs) throws Exception{
		
		// Mix the order of CPU idx
		for (int i = 0; i <nCPUs-1; i++) {
//			int j = randomGen.nextInt(0, nCPUs-1);
			int j = (int)(Math.random()*nCPUs);
			int temp = CIDs[i];
			CIDs[i] = CIDs[j];
			CIDs[j] = temp;
		}
		
		// Execute each core
		for (int i = 0; i <nCPUs; i++) {
			int ret = executeOneUnit(CIDs[i]);
			if (ret!=0){
				return ret;
			}
		}
		
		// Process for the finished task
		for (int x = 0; x <nCPUs; x++) {
			if (CPU[x]!=null && CPU[x].RemainTime == 0) {
				int startTime = CPU[x].updateTaskState(TaskState.Idle, timeLapsed + 1);
				addSchedule(CPU[x], startTime, (timeLapsed + 1), x);
				releaseResources(CPU[x].ID);
				if (Tasks[CPU[x].ID - 1].Triggers.length != 0)
					triggerList.add(CPU[x].ID);
				CPU[x] = null;
			}
		}
		
		triggerTasks();
		return 0;
	}
	
	/**
	 *
	 * @return 0: Nothing to do
	 *		 1: Everything went normally
	 *		 2: Deadline Missed!
	 *		 3: Process completely executed without missing the deadline
	 * @throws Exception
	 */
	int executeOneUnit(int cid) throws Exception {
		
		// Get one task from Queue
		if (CPU[cid] == null) {
			// get top priority task and if a resource that is required by the task, block the task
			Task top = readyQueue.poll();
			while(top!=null) {
				int rID = checkResources(top.ID);
				if (rID==0) break;
			
				top.updateTaskState(TaskState.Blocked, timeLapsed);
				blockQueue[rID].add(top);
				top = readyQueue.poll();
			}
			
			// Assign CPU to the task
			if (top != null){
				top.updateTaskState(TaskState.Running, timeLapsed);
				CPU[cid] = top;
			}
		}
		else{
			// if a CPU is executing a task, we check whether the task will be preempted.
			Task top = readyQueue.peek();
			
			// managing preemption (check all cpus the task should be preempted)
			while ((top != null) && (needPreempted(cid))) {
				// check dependency, if true, move to the blockQueue
				int rID = checkResources(top.ID);
				if (rID>0){
					top.updateTaskState(TaskState.Blocked, timeLapsed);
					blockQueue[rID].add(readyQueue.poll());
					top = readyQueue.peek();
					continue;
				}
				
				// change current task state
				int startTime = CPU[cid].updateTaskState(TaskState.Preempted, timeLapsed);
				addSchedule(CPU[cid], startTime, timeLapsed, cid);
				
				// exchange task (to keep the order of ready queue we poll first)
				Task prev = CPU[cid];
				CPU[cid] = readyQueue.poll();
				readyQueue.add(prev);
				
				// change the new task state
				CPU[cid].updateTaskState(TaskState.Running, timeLapsed);
			}
		}
		
		if (CPU[cid] != null) {
			lockResources(CPU[cid].ID);
			CPU[cid].RemainTime--;
		}
		return 0;
	}
	
	/**
	 * look ahead level 2를 유지하고 확인
	 * @param cid
	 * @return
	 */
	protected boolean needPreempted(int cid){
		Task[] peeks = new Task[CPU.length];
		int pSize = 0;
		boolean needPreemption = false; // flag to check preemption requirements
		
		// we need to check for N times which is the same number of CPU
		for (int peekCnt=0; peekCnt<CPU.length; peekCnt++){
			// The task in the CPU[cid] should be preempted by the task in the ready queue?
			Task t = readyQueue.poll();
			if (t==null) break;
			peeks[pSize++] = t;
			
			// if current task running in this CPU need to be preempted
			if (t.Priority < CPU[cid].Priority) {
				needPreemption = true;
				
				// Count how many tasks can be executed by other CPUs
				int cntAvailable = 0;
				for (int y = 0; y < CPU.length; y++) {
					if (cid == y) continue;
					// find cpus that are not working or lower priority task running than current CPU
					if (CPU[y] == null || CPU[y].Priority > CPU[cid].Priority) {
						cntAvailable++;
					}
				}
				// Check whether other CPUs can deal with the peeked task
				if (pSize<=cntAvailable) {
					needPreemption = false;
					// to check the next task in the ready queue, we do not break here
					continue;
				}
			}
			break;
		}
		for (int i=0; i<pSize; i++){
			readyQueue.add(peeks[i]);
		}
		
		return needPreemption;
	}
	
	protected int checkResources(int taskID){
		int[] deps = Tasks[taskID-1].Dependencies;
		if (deps.length>0){
			for (int resID:deps ){
				int lockedID = resources[resID];
				if (lockedID!=0 && lockedID!=taskID) return resID;
			}
		}
		return 0;
	}
	
	protected boolean lockResources(int taskID){
		int[] deps = Tasks[taskID-1].Dependencies;
		if (deps.length>0){
			for (int resID : deps){
				resources[resID] = taskID;
			}
			return true;
		}
		return false;
	}
	
	protected boolean releaseResources(int taskID){
		int[] dependencies = Tasks[taskID-1].Dependencies;
		if (dependencies.length>0){
			for (int resID:dependencies){
				// release resource
				awakeBlockedTasks(resID);
			}
			return true;
		}
		return false;
	}
	
	protected boolean awakeBlockedTasks(int _resID){
		for (int x=0; x<blockQueue[_resID].size(); x++){
			Task task = blockQueue[_resID].get(x);
			
			int startTime = task.updateTaskState(TaskState.Ready, timeLapsed+1);
			addSchedule(task, startTime, timeLapsed+1, -1);
			readyQueue.add(task);
		}
		blockQueue[_resID].clear();
		resources[_resID] = 0;
		return true;
	}
	
	protected boolean triggerTasks(){
		for (int taskID: triggerList) {
			int[] triggeredList = Tasks[taskID - 1].Triggers;
			
			for (int x = 0; x < triggeredList.length; x++) {
				addReadyQueue(triggeredList[x] - 1, timeLapsed + 1);
			}
		}
		triggerList.clear();
		return true;
	}
}