# OPAM

Optimal Priority Assignment Method for real-time tasks (OPAM) is a tool for finding optimal priority assignments, aiming at maximizing the magnitude of safety margins from deadlines and constraint satisfaction, which is applied multi-objective, two-population competitive coevolution. 


### Overview
In real-time systems, priorities assigned to real-time tasks determine the order of task executions, by relying on an underlying task scheduling policy. Assigning optimal priority values to tasks is critical to allow the tasks to complete their executions while maximizing safety margins from their specified deadlines. This enables real-time systems to tolerate unexpected overheads in task executions and still meet their deadlines. In practice, priority assignments result from an interactive process between the development and testing teams. In this article, we propose an automated method that aims to identify the best possible priority assignments in real-time systems, accounting for multiple objectives regarding safety margins and engineering constraints. Our approach is based on a multi-objective, competitive coevolutionary algorithm mimicking the interactive priority assignment process between the development and testing teams. We evaluate our approach by applying it to six industrial systems from different domains and several synthetic systems. The results indicate that our approach significantly outperforms both random search and solutions defined by practitioners. Our approach scales to complex industrial systems as an offline analysis method that attempts to find (near-)optimal solutions within acceptable time, i.e., less than two days.


### Prerequisite
OPAM runs on the following operating systems:
- Centos Linux 7 (Core, Linux 3.10.0-957.1.3.el7.x86_64)
- MacOS 10.15.7


### OPAM requires the following tools:
- Java 1.8.0.162  (Dependencies: jMetal 5.9, gson 2.8.6, commons-math3 3.6.1)
- Python 3.7+     (Dependencies: tqdm)


### Repository description
* **PriorityAssignment** : Containing Java source code for OPAM tools
* **artifacts**: Containing Java executable files
* **res**: Containing the input task description (empirical and synthetic data)
* **settings.json**: Parameters for the Java executable files
* ***.sh**: Shell scripts for conducting each experiments that used in the paper 


### How to run OPAM?
* Step 0: Setting parameters on ${REPOSITORY}/settings.json
* Step 1: Run ${REPOSITORY}/run.sh
* Step 2: See output files in ${REPOSITORY}/results/OPAM

The settings.json contains default parameter values that used in the paper. The parameters also can be set from command line arguments of OPAM. The run.sh file shows the order of execution and how to set parameters. It produces test data first and conducts OPAM. Note that due to the randomness of OPAM, we repeat our experiments 50 times as a default. To collect all experiments results, you can use python script as the last line of the script.


### How to run experiments?

##### =EXP1=
* Step 0: Setting parameters through settings.json
* Step 1: Run ${REPOSITORY}/exp1.sh
* Step 3: Repeating Step 1 for each subject
* Step 4: Collecting result data 
* Step 5: Evaluating result data 

EXP1 compares OPAM with naive random search (RS) for given six industrial subjects. Before you start, you can set common parameter values for all experiments through settings.json. The script exp1.sh shows an example commands with ESAIL subject. It contains three commands that are generating test data and conducting OPAM and RS. For the other subjects, you can conduct them by changing SUBJECT_NAME, NCPU, SIM_TIME, and TIME_QUANTA in the script according to each subject's properties. After finishing running the experiments, you can collect and evaluate results data. An example of commands for those steps can be found in the commented lines below the script exp1.sh. 


##### =EXP2=
* Step 0: Setting parameters through settings.json
* Step 1: Run ${REPOSITORY}/exp2.gen.sh
* Step 2: Run ${REPOSITORY}/exp2.1.sh
* Step 3: Run ${REPOSITORY}/exp2.2.sh
* Step 4: Run ${REPOSITORY}/exp2.3.sh
* Step 5: Run ${REPOSITORY}/exp2.4.sh
* Step 6: See output files in ${REPOSITORY}/results/RQ2

EXP2 investigates the scalability of OPAM by conducting some experiments with systems of various sizes, including six industrial and several synthetic subjects. Following EXP1, you already have the result of the industrial subjects, thus, you will conduct additional experiments with synthetic subjects in EXP2. For the common parameter values, use the settings.json. In Step 1, the exp2.gen.sh creates synthetic task sets following our experiment design which contains four sub-experiments. In the following four steps, exp2.1.sh and exp2.4 are mapped with each sub-experiment. Each script shows the order of experiments and parameters like exp1.sh. As we repeat our experiments 50 times, we recommend you do conduct those experiments on multiple nodes. 


##### =EXP3=
* Step 1: Run ${REPOSITORY}/exp3.sh
* Step 2: See output files in ${REPOSITORY}/results/RQ3

EXP3 compares the quality of priority assignments generated by OPAM with those defined by engineers. The exp3.sh shows example commands for the EXP3. It contains three commands. The first two commands which generate test data and execute OPAM are similar to exp1.sh. If you run exp1.sh with the option "--printFinal", you do not need to run those steps. The last command is the python script to generate statistical data of safety margins from the result of OPAM. 
