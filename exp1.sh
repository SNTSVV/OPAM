#!/bin/bash -l

## define variables
CMD_PREFIX="echo"    # Please make "echo" to "" for actual running
CODE="RQ1"

NUM_RUNS=50
NUM_TEST=10
SUBJECT_NAME="ESAIL"  # Subject name
NCPU=1                # Number of CPUs, Use a different number depends on the subject
SIM_TIME=6000         # Simulation time, only ESAIL uses 60000ms, otherwise 0
TIME_QUANTA=0.1       # Unit of time, only ESAIL uses 0.1ms, otherwise 1
OPTION=''             # This variable is used for RQ4

# Path settings
OPAMPATH="results/${CODE}/OPAM/${SUBJECT_NAME}"
RSPATH="results/${CODE}/RS/${SUBJECT_NAME}"
TESTPATH="results/TestPool/${CODE}/${SUBJECT_NAME}"
RESOURCE="res/industrial/${SUBJECT_NAME}.csv"

# Generate test arrivals (Adaptive random search)
${CMD_PREFIX} java -jar artifacts/OPAM.jar --genTest Adaptive --numTest ${NUM_TEST} -b ${TESTPATH} --data ${RESOURCE} --cpus ${NCPU}  --max ${SIM_TIME} --quanta ${TIME_QUANTA} ${OPTION}


# Execute OPAM (${NUM_RUNS} runs will be executed)
${CMD_PREFIX} java -jar artifacts/OPAM.jar --runCnt ${NUM_RUNS} -b ${OPAMPATH} --data ${RESOURCE} --testPath ${TESTPATH}/test.list --numTest ${NUM_TEST} --max ${SIM_TIME} --quanta ${TIME_QUANTA} --cpus ${NCPU} TIME_QUANTA${OPTION}

# Execute RandomSearch (${NUM_RUNS} runs will be executed)
${CMD_PREFIX} java -jar artifacts/OPAM.jar --runCnt ${NUM_RUNS} -b ${RSPATH} --data ${RESOURCE} --testPath ${TESTPATH}/test.list --numTest ${NUM_TEST} --max ${SIM_TIME} --quanta ${TIME_QUANTA} --cpus ${NCPU} TIME_QUANTA--algo1 RandomSearch --simpleP2 ${OPTION}

#############################################
# Collecting results
#############################################
# Collecting results
${CMD_PREFIX} ~/venv/bin/python3  scripts/Python/ResultCollector.py -f merge_fitness_industrial -t results/${CODE}/OPAM -o results/${CODE}/fitness_OPAM.csv
${CMD_PREFIX} ~/venv/bin/python3  scripts/Python/ResultCollector.py -f merge_fitness_industrial -t results/${CODE}/RS -o results/${CODE}/fitness_RS.csv
${CMD_PREFIX} ~/venv/bin/python3  scripts/Python/ResultCollector.py -f merge_time_industrial -t results/${CODE}/OPAM -o results/${CODE}/timeinfo_OPAM.csv
${CMD_PREFIX} ~/venv/bin/python3  scripts/Python/ResultCollector.py -f merge_time_industrial -t results/${CODE}/RS -o results/${CODE}/timeinfo_RS.csv

# Evaluate the values
${CMD_PREFIX} java -jar artifacts/QI.jar --apprs OPAM,RS --compare1 results/${CODE}/fitness_OPAM.csv --compare2 results/${CODE}/fitness_RS.csv --output results/${CODE}/fitness_QI.csv --runCnt ${NUM_RUNS} --cycle 1000



