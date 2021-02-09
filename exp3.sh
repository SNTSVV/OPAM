#!/bin/bash -l

## define variables
CMD_PREFIX="echo"    # Please make "echo" to "" for actual running
CODE="RQ3"

NUM_RUNS=50
NUM_TEST=10
SUBJECT_NAME="ESAIL"  # Subject name
NCPU=1                # Number of CPUs, Use a different number depends on the subject
SIM_TIME=6000         # Simulation time, only ESAIL has specified simulation time as 60000
TIME_UNIT=0.1         # Unit of time, only ESAIL uses 0.1ms otherwise, 1
OPTION='--printFinal' # To produce the detailed schedule results

# Path settings
TESTPOOL="TestPool/${CODE}/${SUBJECT_NAME}"
RESOURCE="res/industrial/${SUBJECT_NAME}.csv"

# Generate test arrivals (Adaptive random search)
${CMD_PREFIX} java -jar artifacts/OPAM.jar -w ${TESTPOOL} --data ${RESOURCE} --max ${SIM_TIME} --quanta ${TIME_UNIT} --numTest ${NUM_TEST} --cycle 1 --workType Adaptive ${OPTION}


# Execute OPAM (${NUM_RUNS} runs will be executed)
${CMD_PREFIX} java -jar artifacts/OPAM.jar --runCnt ${NUM_RUNS} -w ${CODE}/${SUBJECT_NAME} --data ${RESOURCE} --testPath ${TESTPOOL}/test.list --max ${SIM_TIME} --quanta ${TIME_UNIT} --numTest ${NUM_TEST} --cpus ${NCPU} --maxMissed 1000 ${OPTION}


# Collecting results
#${CMD_PREFIX} ~/venv/bin/python3  scripts/Python/ScheduleAnalyzer.py -t results/${CODE} -o schedule_${CODE}.csv



