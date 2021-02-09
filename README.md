# OPAM

OPAM (Optimal Priority Assignment Method for real-time tasks) is a tool for optimally assigning priorities to real-time tasks, aiming at maximizing the magnitude of safety margins from deadlines and constraint satisfaction. 


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
* *PriorityAssignment* : Containing Java source code of OPAM
* *UPPAAL* : Containing the result of our preliminary experiment using UPPAAL
* *artifacts*: Containing Java executable files
* *res*: Containing the input task descriptions
* *settings.json*: Parameters for the Java executable files
* **.sh*: Shell scripts for conducting experiments 


### How to run OPAM?
* Step 0: Set parameters on *settings.json*
* Step 1: Run *run.sh*
* Step 2: See output files in *results/OPAM*


### How to run experiments?

##### =EXP1=
* Step 0: Set common parameters through *settings.json*
* Step 1: Run *exp1.sh* for each subject
* Step 2: Collect result data 
* Step 3: Evaluate result data 
* Step 4: See output files in *results/RQ1_OPAM* and *results/RQ1_RS*


##### =EXP2=
* Step 0: Set common parameters through *settings.json*
* Step 1: Run *exp2.gen.sh*
* Step 2: Run *exp2.1.sh* ~ *exp2.4.sh* 
* Step 3: See output files in *results/RQ2*


##### =EXP3=
* Step 0: Set common parameters through *settings.json*
* Step 1: Running *exp3.sh*
* Step 2: See output files in *results/RQ3*
