# OPAM

OPAM: Optimal Priority Assignment Method for real-time tasks


### Overview
In real-time systems, priorities assigned to real-time tasks determine the order of task executions, by relying on an underlying task scheduling policy. Assigning optimal priority values to tasks is critical to allow the tasks to complete their executions while maximizing safety margins from their specified deadlines. This enables real-time systems to tolerate unexpected overheads in task executions and still meet their deadlines. In practice, priority assignments result from an interactive process between the development and testing teams. In this article, we propose an automated method that aims to identify the best possible priority assignments in real-time systems, accounting for multiple objectives regarding safety margins and engineering constraints. Our approach is based on a multi-objective, competitive coevolutionary algorithm mimicking the interactive priority assignment process between the development and testing teams. We evaluate our approach by applying it to six industrial systems from different domains and several synthetic systems. The results indicate that our approach significantly outperforms both random search and solutions defined by practitioners. Our approach scales to complex industrial systems as an offline analysis method that attempts to find (near-)optimal solutions within acceptable time, i.e., less than two days.


### Prerequisite
OPAM runs on the following operating systems:
- Centos Linux 7 (Core, Linux 3.10.0-957.1.3.el7.x86_64)
- MacOS 10.15.7


### OPAM requires the following tools:
- Java 1.8.0.162  (Dependencies: see the file PriorityAssignment/pom.xml)
- Python 3.7+     (Dependencies: see the file scripts/Python/requirements.txt)
- R 4.1.0+        (Dependencies: see the file scripts/R/requirements.R)


### Repository description
* *PriorityAssignment* : Containing Java source code of OPAM
* *UPPAAL* : Containing the result of our preliminary experiment using UPPAAL (uppaal64-4.1.24)
* *res*: Containing input files - task descriptions
* *scripts*: Containing script files to collect data and to generate graphs
* *results*: Containing results files during all experiments
* *settings.json*: Parameters for the Java executable files
* **.sh*: Shell scripts for conducting experiments 

### How to create OPAM executable jar files?
Given the pre-configured POM files for Maven in the *PriorityAssignment* folder, you can create executable jar files that are used in the shell script files contained in this repository. Please execute the below commands in the *PriorityAssignment* folder.
* *OPAM.jar*: mvn -f opam.pom -DoutputJar=../artifacts package
* *OPAM-Ext.jar*: mvn -f opam.ext.pom -DoutputJar=../artifacts package
* *QI.jar*: mvn -f qi.pom -DoutputJar=../artifacts package
* *QI-Ext.jar*: mvn -f qi.ext.pom -DoutputJar=../artifacts package
* *NSGA.jar*: mvn -f nsga.pom -DoutputJar=../artifacts package
* *Synthesizer.jar*: mvn -f synthesizer.pom -DoutputJar=../artifacts package


### How to run OPAM?
* Step 1: Run *run.sh*
* Step 2: See output files in *results/OPAM*


### How to run experiments?

##### =EXP1=
* Step 1: Run *exp1.sh* for each subject
* Step 2: See output files in *results/RQ1/OPAM* and *results/RQ1/RS*

##### =EXP2=
* Step 1: Run *exp2.sh* for each subject
* Step 2: See output files in *results/RQ2/OPAM* and *results/RQ2/SEQ*


##### =EXP3=
* Step 1: Run *exp3.gen.sh*
* Step 2: Run *exp3.1.sh* ~ *exp3.4.sh* 
* Step 3: See output files in *results/RQ3*


##### =EXP4=
* Step 1: Running *exp4.sh*
* Step 2: See output files in *results/RQ4*
