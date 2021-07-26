#!/bin/bash -l

## define variables
CMD_PREFIX="echo"    # Please make "echo" to "" for actual running
CODE="OPAM"

NUM_RUNS=50
NUM_TEST=10
SUBJECT_NAME="ESAIL"  # Subject name
NCPU=1                # Number of CPUs, Use a different number depends on the subject
SIM_TIME=6000         # Simulation time, only ESAIL has specified simulation time as 60000
TIME_QUANTA=0.1         # Unit of time, only ESAIL uses 0.1ms otherwise, 1
OPTION=''            # Add additional option as you need

# Path settings
WORKPATH="results/${CODE}/${SUBJECT_NAME}"
TESTPOOL="results/TestPool/${CODE}/${SUBJECT_NAME}"
RESOURCE="res/industrial/${SUBJECT_NAME}.csv"

# Generate test arrivals (Adaptive random search)
${CMD_PREFIX} java -jar artifacts/OPAM.jar --getTest Adaptive --numTest ${NUM_TEST} -b ${TESTPOOL} --data ${RESOURCE} --max ${SIM_TIME} --quanta ${TIME_QUANTA}


# Execute OPAM (${NUM_RUNS} runs will be executed)
${CMD_PREFIX} java -jar artifacts/OPAM.jar --runCnt ${NUM_RUNS} -b ${WORKPATH} --data ${RESOURCE} --testPath ${TESTPOOL}/test.list --numTest ${NUM_TEST} --max ${SIM_TIME} --quanta ${TIME_QUANTA} --cpus ${NCPU} --maxMissed 1000 ${OPTION}

# Collecting results
${CMD_PREFIX} python3 scripts/Python/ResultCollector.py -f merge_fitness_industrial -t results/${CODE} -o results/${CODE}/fitness_${CODE}.csv


