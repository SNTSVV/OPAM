package lu.uni.svv.PriorityAssignment.generator;

import junit.framework.TestCase;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.task.TaskType;
import lu.uni.svv.PriorityAssignment.utils.ArgumentParser;

import java.io.*;
import java.util.Random;

public class TaskSetSynthesizerTest extends TestCase {
	
	
	/**
	 * test generating tasks // old version
	 *
	 * @throws FileNotFoundException
	 */
	public void testGenerateTaskSet() throws FileNotFoundException {
		int MAX_SIM_TIME = 10000;
		double TIMEUNIT = 0.1;
		double targetU = 0.675;
		double delta = 0.05;
		int nTasks = 10;
		String NAME = "utilization";
		
		String fname = String.format("../R/priorities/utilizationDist/%s.csv", NAME);
		PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(fname, false)), true);
		ps.println("nTasks,LCM");
		
		TaskSetSynthesizer syn = new TaskSetSynthesizer((int)(MAX_SIM_TIME/TIMEUNIT), true, 10);
		for (nTasks = 10; nTasks <= 50; nTasks += 5) {
			System.out.print(String.format("Working with nTask=%d", nTasks));
			for (int x = 0; x < 10000; x++) {
				while (true) {
					OPAMTaskSet taskset = syn.generatePeriodicTaskSet(nTasks, targetU, (int) (10 / TIMEUNIT), (int) (1000 / TIMEUNIT), (int) (10 / TIMEUNIT), true);
					if (!taskset.isValidWCET()) taskset = null;
					else if (!taskset.isValidUtilization(targetU, delta)) taskset = null;
					else if (!taskset.isValidSimulationTime((int)(MAX_SIM_TIME/TIMEUNIT))) taskset = null;
					if (taskset == null) continue;
					
					double lcm = taskset.calculateLCM() * TIMEUNIT;
					ps.println(String.format("%d, %d", nTasks, (int) lcm));
//					ps.println(taskset.getString(TIMEUNIT));
					break;
				}
				
				if (x % 20 == 0) {
					System.out.print(".");
				}
			} // for number of tast set
			System.out.println("Done");
		}
		ps.close();
	}
	
	
	/**
	 * test generating task set with double data type
	 *
	 * @throws FileNotFoundException
	 */
	public void testGenerateTaskSetDouble() throws FileNotFoundException {
		int MAX_SIM_TIME = 10000;
		double TIMEUNIT = 1;
		double targetU = 0.7;
		double delta = 0.3;
		int nTasks = 10;
		String NAME = "utilization_double";
		
		String fname = String.format("../R/priorities/utilizationDist/%s.csv", NAME);
		PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(fname, false)), true);
		ps.println("nTasks,Utilization");
		
		TaskSetSynthesizer syn = new TaskSetSynthesizer((int)(MAX_SIM_TIME/TIMEUNIT), true, 10);
		for (nTasks = 10; nTasks <= 50; nTasks += 5) {
			System.out.print(String.format("Working with nTask=%d", nTasks));
			for (int x = 0; x < 10000; x++) {
				while (true) {
					double[] utilizations = syn.UUniFast(nTasks, targetU);
					int[] periods = syn.generatePeriods(nTasks, (int) (10 / TIMEUNIT), (int) (1000 / TIMEUNIT), (int) (10 / TIMEUNIT), true);
					double[] WCETs = syn.generateWCETsDouble(periods, utilizations);
					double actualU = syn.getUtilization(periods, WCETs);
					if (actualU < targetU - delta || actualU > targetU + delta) continue;
					ps.println(String.format("%d, %.4f", nTasks, actualU));
					break;
				}
				
				if (x % 200 == 0) {
					System.out.print(".");
				}
			} // for number of tast set
			System.out.println("Done");
		}
		ps.close();
	}
	
	/**
	 * test generating task set with double data type
	 *
	 * @throws FileNotFoundException
	 */
	public void testGenerateRandom() throws FileNotFoundException {
		int MAX_SIM_TIME = 10000;
		double TIMEUNIT = 0.01;
		double targetU = 0.7;
		double delta = 0.05;
		int nTasks = 10;
		String NAME = "utilization_random3";
		
		String fname = String.format("../R/priorities/utilizationDist/%s.csv", NAME);
		PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(fname, false)), true);
		ps.println("Type,Values");
		
		Random rand = new Random();
		for (int x = 0; x < 100000; x++) {
			double value = rand.nextDouble() * (delta * 2) + (targetU - delta);
			ps.println(String.format("Rand, %.5f", value));
			ps.println(String.format("Prec1, %.5f", Math.round(value / 0.1) * 0.1));
			ps.println(String.format("Prec2, %.5f", Math.round(value / 0.01) * 0.01));
			ps.println(String.format("Prec3, %.5f", Math.round(value / 0.001) * 0.001));
			ps.println(String.format("Prec4, %.5f", Math.round(value / 0.0001) * 0.0001));
			ps.println(String.format("Prec5, %.5f", Math.round(value / 0.00001) * 0.00001));
			if (x % 200 == 0) {
				System.out.print(".");
			}
		} // for number of tast set
		ps.close();
	}
	
	/**
	 * test generating tasks // by discarding when WCET condition is not acceptable
	 *
	 * @throws FileNotFoundException
	 */
	public void testGenerateTaskSet1() throws FileNotFoundException {
		// parsing args
		int MAX_SIM_TIME = 10000;
		double TIMEUNIT = 0.1;
		double targetU = 0.7;
		String NAME = "utilization";
		
		String fname = String.format("../R/priorities/utilizationDist/%s.csv", NAME);
		PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(fname, false)), true);
		ps.println("nTasks,Utilization");
		
		TaskSetSynthesizer syn = new TaskSetSynthesizer((int)(MAX_SIM_TIME/TIMEUNIT), true, 10);
		for (int nTasks = 10; nTasks <= 50; nTasks += 5) {
			System.out.print(String.format("Working with nTask=%d", nTasks));
			
			for (int x = 0; x < 10000; x++) {
				double[] utilizations;
				int[] periods;
				int[] WCETs;
				while (true) {
					utilizations = syn.UUniFast(nTasks, targetU);
					periods = syn.generatePeriods(nTasks, (int) (10 / TIMEUNIT), (int) (1000 / TIMEUNIT), (int) (10 / TIMEUNIT), true);
					WCETs = syn.generateWCETs(periods, utilizations);
					boolean flag = true;
					for (int k = 0; k < WCETs.length; k++) {
						if (WCETs[k] == 0) {
							flag = false;
							break;
						}
					}
					if (flag) break;
				}
				double actualU = syn.getUtilization(periods, WCETs);
				ps.println(String.format("%d, %.4f", nTasks, actualU));
				if (x % 200 == 0) {
					System.out.print(".");
				}
			} // for number of tast set
			System.out.println("Done");
		}
		
	}
	
	/**
	 * test generating tasks // by discarding taskset when the conditions are not acceptable ( with class functions)
	 *
	 * @throws FileNotFoundException
	 */
	public void testGenerateTaskSet2() throws FileNotFoundException {
		// parsing args
		int MAX_SIM_TIME = 10000;
		double TIMEUNIT = 0.1;
		double targetU = 0.7;
		double delta = 0.02;
		String NAME = String.format("utilization_period_T1_vWCET_vUtil%.2f", delta);
		
		
		String fname = String.format("../R/priorities/utilizationDist/%s.csv", NAME);
		PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(fname, false)), true);
		ps.println("nTasks,Utilization");
		
		TaskSetSynthesizer syn = new TaskSetSynthesizer((int)(MAX_SIM_TIME/TIMEUNIT), true, 10);
		for (int nTasks = 10; nTasks <= 50; nTasks += 5) {
			System.out.print(String.format("Working with nTask=%d", nTasks));
			
			for (int x = 0; x < 10000; x++) {
				OPAMTaskSet taskset = null;
				
				while (taskset == null) {
					taskset = syn.generatePeriodicTaskSet(nTasks, targetU,
							(int) (10 / TIMEUNIT), (int) (1000 / TIMEUNIT), (int) (10 / TIMEUNIT), true);
//					if(!taskset.isValidWCET()) taskset = null;
					if (!taskset.isValidWCET() || !taskset.isValidUtilization(targetU, delta)) taskset = null;
				}
				
				double actualU = taskset.getUtilization();
				ps.println(String.format("%d, %.4f", nTasks, actualU));
//				ps.println(String.format("%d, %.4f, %s", nTasks, actualU, taskset.getString(TIMEUNIT)));
				
				if (x % 200 == 0) {
					System.out.print(".");
				}
			} // for number of tast set
			System.out.println("Done");
		}
	}
	
	/**
	 * test generating tasks // by discarding taskset when the conditions are not acceptable ( with class functions)
	 *
	 * @throws FileNotFoundException
	 */
	public void testGenerateTaskSet3() throws FileNotFoundException {
		// parsing args
		int MAX_SIM_TIME = 10000;
		double TIMEUNIT = 0.1;
		double delta = 0.01;
		double targetU = 0.7;
		
		
		String NAME = String.format("utilization_mix_vWCET_vUtil%.3f", delta);
		String fname = String.format("../R/priorities/utilizationDist/%s.csv", NAME);
		PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(fname, false)), true);
//		PrintStream ps = System.out;
		ps.println("nTasks,Utilization");
		
		TaskSetSynthesizer syn = new TaskSetSynthesizer((int)(MAX_SIM_TIME/TIMEUNIT), true, 10);
		for (int nTasks = 10; nTasks <= 50; nTasks += 5) {
			System.out.print(String.format("Working with nTask=%d", nTasks));
			
			for (int x = 0; x < 100; x++) {
				OPAMTaskSet taskset = syn.generateMultiTaskset(nTasks, targetU, delta,
						(int) (10 / TIMEUNIT), (int) (1000 / TIMEUNIT), (int) (10 / TIMEUNIT), true,
						0.4, 2);
				
				double actualU = taskset.getUtilization();
				ps.println(String.format("%d, %.4f", nTasks, actualU));
//				System.out.println(String.format("%d, %.4f, %s", nTasks, actualU, taskset.getString(TIMEUNIT)));
//				taskset.print(null, TIMEUNIT);
				if (x % 2 == 0) {
					System.out.print(".");
				}
			} // for number of tast set
			System.out.println("Done");
		}
		ps.close();
	}
	
	
	public void testUUniFast() throws IOException {
		// parsing args
		int MAX_SIM_TIME = 10000;
		double TIMEUNIT = 1;
		double targetU = 0.7;
		String NAME = "utilization_uunifast";
		
		String fname = String.format("../R/priorities/utilizationDist/%s.csv", NAME);
		PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(fname, false)), true);
		ps.println("nTasks,Utilization,UtilizationP");
		
		TaskSetSynthesizer syn = new TaskSetSynthesizer((int)(MAX_SIM_TIME/TIMEUNIT), true, 10);
		for (int nTasks = 10; nTasks <= 50; nTasks += 5) {
			System.out.print(String.format("Working with nTask=%d", nTasks));
			
			for (int x = 0; x < 100000; x++) {
				double[] utilizations = syn.UUniFast(nTasks, targetU);
				double util = syn.getUtilization(utilizations);
				
				for (int t = 0; t < utilizations.length; t++) {
					utilizations[t] = Math.round(utilizations[t] * 100) / 100.0;
				}
				double utilp = syn.getUtilization(utilizations);
				
				ps.println(String.format("%d, %.4f, %.4f", nTasks, util, utilp));
				if (x % 200 == 0) {
					System.out.print(".");
				}
			} // for number of tast set
			System.out.println("Done");
		}
		
	}
	
	/**
	 * Test code for a generateDivisors function
	 */
	public void testGenerateDivisors() {
		int inc = 1000;
		double TIMEUNIT = 1;
		for (int sim = 1 * inc; sim <= 10 * inc; sim += inc) {
			TaskSetSynthesizer syn = new TaskSetSynthesizer((int)(sim/TIMEUNIT), true, 10);
			int[] divisors = syn.generateDivisors((int)(sim/TIMEUNIT), (int)(sim/TIMEUNIT), (int)(sim/TIMEUNIT));
			
			StringBuilder sb = new StringBuilder();
			sb.append(sim);
			sb.append(": [");
			for (int t = 0; t < divisors.length; t++) {
				sb.append(divisors[t]);
				sb.append(", ");
			}
			sb.append("]");
			System.out.println(sb.toString());
		}
	}
	
	/***
	 * Test for distribution of periods
	 * @throws FileNotFoundException
	 */
	public void testPeriodSelector1() throws FileNotFoundException {
		String NAME = "period_dist_discarding";
		String fname = String.format("../R/priorities/utilizationDist/divisor/%s.csv", NAME);
		PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(fname, false)), true);
		ps.println("Period");
		
		int tMin = 10;
		int tMax = 1000;
		long sim = 10000;
		int granulrity = 10;
		int nTasks = 10;
		double targetU=0.7;
		double TIMEUNIT = 0.1;
		
		TaskSetSynthesizer syn = new TaskSetSynthesizer((int)(sim/TIMEUNIT), true, 10);
		for (int q = 0; q < 1000000; q+=nTasks) {
			OPAMTaskSet taskset = null;
			while(taskset==null) {
				taskset = syn.generateMultiTaskset(nTasks, targetU, 0.01, (int)(tMin/TIMEUNIT),
												(int)(tMax/TIMEUNIT), (int)(granulrity/TIMEUNIT), true,
												0.37, 2);
//				taskset = syn.generatePeriodicTaskSet(nTasks, targetU, (int)(tMin/TIMEUNIT),
//						(int)(tMax/TIMEUNIT), (int)(granulrity/TIMEUNIT), true);
//				if (!taskset.isValidWCET() || !taskset.isValidUtilization(targetU, 0.01)) {
//					taskset = null;
//					continue;
//				}
			}
			for(int t=0; t<taskset.tasks.length; t++){
				ps.println(taskset.tasks[t].Period);
			}
//
//			int[] periods = syn.generatePeriods(nTasks, tMin, tMax, granulrity, true);
//			for(int t=0; t<periods.length; t++){
//				ps.println(periods[t]);
//			}
			if (q%50000==0) {
				System.out.println(".");
			}
		}
		ps.close();
	}
	
	public void testPeriodSelector2() throws FileNotFoundException {
		String NAME = "period_dist_index";
		String fname = String.format("../R/priorities/utilizationDist/divisor/%s.csv", NAME);
		PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(fname, false)), true);
		ps.println("Period");
		
		int tMin = 10;
		int tMax = 1000;
		long sim = 10000;
		int granularity=10;
		int nTasks = 10;
		double TIMEUNIT = 0.1;
		
		TaskSetSynthesizer syn = new TaskSetSynthesizer((int)(sim/TIMEUNIT),true, granularity);
		for (int q = 0; q < 1000000; q+=nTasks) {
			int[] periods = syn.generatePeriodsDivisors(nTasks, (int)(tMin/TIMEUNIT), (int)(tMax/TIMEUNIT), (int)(granularity/TIMEUNIT), true);
			for(int t=0; t<periods.length; t++){
				ps.println(periods[t]);
			}
			if (q%50000==0) {
				System.out.println(".");
			}
		}
		
	}
	
	public void testPeriodSelector2_raw() throws FileNotFoundException {
		String NAME = "period_dist_index";
		String fname = String.format("../R/priorities/utilizationDist/divisor/%s.csv", NAME);
		PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(fname, false)), true);
		ps.println("Period");
		
		int tMin = 10;
		int tMax = 1000;
		long sim = 10000;
		int granularity=10;
		int nTasks = 10;
		double TIMEUNIT = 0.1;
		
		Random rand = new Random();
		TaskSetSynthesizer syn = new TaskSetSynthesizer((int)(sim/TIMEUNIT), true, granularity);
		int[] divisors = syn.generateDivisors(sim, tMin, tMax);
		
		for (int q = 0; q < 1000000; q+=nTasks) {
			// get random value of log-uniform in range of indexes of divisors
			double min = Math.log(1);
			double max = Math.log(divisors.length + 1); //Math.log(tMax);
			
			// log distribution in range [log(min), log(max))  <- log(max) exclusive
			double randVal = rand.nextDouble() * (max - min) + min; // generate random value in [log(tMin), log(tMax+granularity))
			int val = (int) Math.floor(Math.exp(randVal));
			ps.println(divisors[val-1]); // divisors[val-1]
			
			if (q%50000==0) {
				System.out.println(".");
			}
		}
		
	}
	
	public void testPeriodSelector3() throws FileNotFoundException {
		String NAME = "period_dist_cdf";
		String fname = String.format("../R/priorities/utilizationDist/divisor/%s.csv", NAME);
		PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(fname, false)), true);
		ps.println("Period");
		
		int tMin = 10;
		int tMax = 1000;
		long sim = 10000;
		int granularity = 10;
		double TIMEUNIT = 0.1;
		
		int nTasks = 10;
		TaskSetSynthesizer syn = new TaskSetSynthesizer((int)(sim/TIMEUNIT), true, 10);
		for (int q = 0; q < 1000000; q++) {
			int[] periods = syn.generatePeriodsDivisorsCDF(nTasks,(int)(tMin/TIMEUNIT), (int)(tMax/TIMEUNIT), (int)(granularity/TIMEUNIT), true);
			for(int t=0; t<periods.length; t++){
				ps.println(periods[t]);
			}
			if (q%50000==0) {
				System.out.println(".");
			}
		}
	}
	
	/**
	 * Test aperiodic tasks
	 * @throws FileNotFoundException
	 */
	public void testGenerateAperiodicTask() throws FileNotFoundException {
		int MAX_SIM_TIME = 0;
		double TIMEUNIT = 0.1;
		double targetU = 0.7;
		double delta = 0.01;
		int nTasks = 15;
		String NAME = "aperiodic";
//
//		String fname = String.format("../R/priorities/utilizationDist/%s.csv", NAME);
//		PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(fname, false)), true);
//		ps.println("nTasks,LCM");
		
		int cntAll =0;
		int cntMin = 0;
		int cntMax = 0;
		int cntErr = 0;
		int granularity = 10;
		TaskSetSynthesizer syn = new TaskSetSynthesizer((int)(MAX_SIM_TIME/TIMEUNIT), true, granularity);
		for (nTasks = 10; nTasks <= 30; nTasks += 5) {
			System.out.print(String.format("Working with nTask=%d", nTasks));
			for (int x = 0; x < 10000; x++) {
				while (true) {
					OPAMTaskSet taskset = syn.generatePeriodicTaskSet(nTasks, targetU, (int) (10 / TIMEUNIT), (int) (100 / TIMEUNIT), (int) (granularity / TIMEUNIT), true);
					if (!taskset.isValidWCET()) taskset = null;
					else if (!taskset.isValidUtilization(targetU, delta)) taskset = null;
//					else if (!taskset.isValidSimulationTime(MAX_SIM_TIME)) taskset = null;
					if (taskset == null) continue;
					taskset = syn.selectAperiodicTasks(taskset, 0.4, 3, 0, 0, (int) (granularity / TIMEUNIT));
					if (taskset == null) continue;
					for(int k=0; k<nTasks; k++){
						if (taskset.tasks[k].Type == TaskType.Periodic) continue;
						if (taskset.tasks[k].MinIA > taskset.tasks[k].MaxIA) {
							cntErr ++;
//							System.out.println(String.format("inter: [%d, %d]", taskset.tasks[k].MinIA, taskset.tasks[k].MaxIA));
						}
						if (taskset.tasks[k].MinIA*3 == taskset.tasks[k].MaxIA) {
							cntMax ++;
//							System.out.println(String.format("inter2: [%d, %d]", taskset.tasks[k].MinIA, taskset.tasks[k].MaxIA));
						}
						if (taskset.tasks[k].MinIA == taskset.tasks[k].MaxIA) {
							cntMin ++;
//							System.out.println(String.format("inter3: [%d, %d]", taskset.tasks[k].MinIA, taskset.tasks[k].MaxIA));
						}
//						System.out.println(String.format("inter: [%d, %d]", taskset.tasks[k].MinIA, taskset.tasks[k].MaxIA));
						cntAll++;
					}
					System.out.print(".");
//					double lcm = taskset.calculateLCM() * TIMEUNIT;
//					ps.println(String.format("%d, %d", nTasks, (int) lcm));
//					ps.println(taskset.getString(TIMEUNIT));
					break;
				}
				
				if (x % 20 == 0) {
					System.out.print(".");
				}
			} // for number of tast set
			System.out.println("Done");
			System.out.println(String.format("cntAll: %d", cntAll));
			System.out.println(String.format("cntError: %d", cntErr));
			System.out.println(String.format("cntMin: %d", cntMin));
			System.out.println(String.format("cntMax: %d", cntMax));
			
		}
//		ps.close();
	}
	
	public void testGenerateMultiTask() throws FileNotFoundException {
		int MAX_SIM_TIME = 0;
		double TIMEUNIT = 0.1;
		double targetU = 0.7;
		double delta = 0.01;
		int nTasks = 15;
		String NAME = "aperiodic";
		
		int cntAll =0;
		int cntMin = 0;
		int cntMax = 0;
		int cntErr = 0;
		int granularity = 10;
		TaskSetSynthesizer syn = new TaskSetSynthesizer((int)(MAX_SIM_TIME/TIMEUNIT), false, granularity);
		for (nTasks = 10; nTasks <= 30; nTasks += 5) {
			System.out.print(String.format("Working with nTask=%d", nTasks));
			for (int x = 0; x < 100; x++) {
				OPAMTaskSet taskset = syn.generateMultiTaskset(nTasks, targetU, delta,
															   (int) (10 / TIMEUNIT), (int) (100 / TIMEUNIT),
						                                       (int) (granularity / TIMEUNIT), true,
																0.4, 3);
				for(int k=0; k<nTasks; k++){
					if (taskset.tasks[k].Type == TaskType.Periodic) continue;
					if (taskset.tasks[k].MinIA > taskset.tasks[k].MaxIA) {
						cntErr ++;
					}
					if (taskset.tasks[k].MinIA*3 == taskset.tasks[k].MaxIA) {
						cntMax ++;
					}
					if (taskset.tasks[k].MinIA == taskset.tasks[k].MaxIA) {
						cntMin ++;
					}
					cntAll++;
				}
				System.out.print(".");
			
				if (x % 20 == 0) {
					System.out.print(".");
				}
			} // for number of tast set
			System.out.println("Done");
			System.out.println(String.format("cntAll: %d", cntAll));
			System.out.println(String.format("cntError: %d", cntErr));
			System.out.println(String.format("cntMin: %d", cntMin));
			System.out.println(String.format("cntMax: %d", cntMax));
			
		}
//		ps.close();
	}
}