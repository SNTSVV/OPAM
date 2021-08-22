#!/bin/bash -l

## define variables
CMD_PREFIX="echo"    # Please make "echo" to "" for actual running
CODE="RQ4"

NUM_RUNS=50
NUM_TEST=10
SUBJECT_NAME="ESAIL"  # Subject name
NCPU=1                # Number of CPUs, Use a different number depends on the subject
SIM_TIME=6000         # Simulation time, only ESAIL uses 60000ms, otherwise 0
TIME_QUANTA=0.1       # Unit of time, only ESAIL uses 0.1ms, otherwise 1
OPTION='--printFinal' # To produce the detailed schedule results

# Path settings
WORKPATH="results/${CODE}/${SUBJECT_NAME}"
TESTPATH="results/TestPool/${CODE}/${SUBJECT_NAME}"
RESOURCE="res/industrial/${SUBJECT_NAME}.csv"

# Generate test arrivals (Adaptive random search)
${CMD_PREFIX} java -jar artifacts/OPAM.jar --genTest Adaptive --numTest ${NUM_TEST} -b ${TESTPATH} --data ${RESOURCE} --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA} ${OPTION}


# Execute OPAM (${NUM_RUNS} runs will be executed)
${CMD_PREFIX} java -jar artifacts/OPAM.jar --runCnt ${NUM_RUNS} -b ${WORKPATH} --data ${RESOURCE} --testPath ${TESTPATH}/test.list --numTest ${NUM_TEST} --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA} ${OPTION}


# Collecting results
${CMD_PREFIX} ~/venv/bin/python3 scripts/Python/ScheduleStatsComp.py -t results/RQ4/OPAM/ESAIL -u 0.1 -o results/RQ4/ESAIL_stats