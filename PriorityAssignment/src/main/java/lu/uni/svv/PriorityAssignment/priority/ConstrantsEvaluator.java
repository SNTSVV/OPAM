package lu.uni.svv.PriorityAssignment.priority;

import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.task.TaskType;

import java.util.List;

public class ConstrantsEvaluator {
	/**
	 * Assuming that the greater value of priority has the higher priority level,
	 * This function calculates the distance of priority level of aperiodic task
	 *                          from the lowest priority level among periodic tasks
	 * The higher return value represents the higher extent of the constraint satisfaction
	 * @param solution
	 * @param tasks
	 * @return
	 */
	public static double calculate(PrioritySolution solution, TaskDescriptor[] tasks){
		List<Integer> priorities = solution.getVariables();
		
		// calculate lowest priority among periodic tasks
		int lowest = Integer.MAX_VALUE;
		for (int x=0; x<priorities.size(); x++){
			if(tasks[x].Type!= TaskType.Aperiodic && lowest > priorities.get(x)){
				lowest = priorities.get(x);
			}
		}
		
		// calculate distance lowest priority of priority task and each aperiodic task
		int priorityDistance = 0;
		for (int x=0; x<priorities.size(); x++){
			if(tasks[x].Type==TaskType.Aperiodic){
				int diff = lowest - priorities.get(x);
				priorityDistance += diff;
			}
		}
		return priorityDistance;
	}
}
