#!/bin/bash -l

## define variables
CMD_PREFIX="echo"    # Please make "echo" to "" for actual running
CODE="RQ3"

NUM_RUNS=50
NUM_TEST=10
EXP_NAME="4_simTime"  # Experiment name
NCPU=1               # We test EXPs on a single processor
SIM_TIME=2000        # Simulation time, it is different depends on a EXP
TIME_QUANTA=0.1      # Unit of time, all EXPs use 0.1ms

# EXP variables
TASKSET_NUM=10      # number of task sets

##################################################################
# conducting experiments
# it is better to run below experiments on multiple nodes
##################################################################
for ((control=2000; control<=10000; control+=1000)); do
    SIM_TIME=${control}
    for ((tsIdx=0; tsIdx<${TASKSET_NUM}; tsIdx++)); do
        # Path settings
        MIDPATH="${CODE}/${EXP_NAME}/${control}/${tsIdx}"
        WORKPATH="results/${MIDPATH}"
        TESTPATH="results/TestPool/${MIDPATH}"
        formatIDX=$(printf '%03d' "${tsIdx}")
        RESOURCE="res/synthetics/${EXP_NAME}/${control}/taskset_${formatIDX}.csv"

        # Generate test arrivals (Adaptive random search)
        ${CMD_PREFIX} java -jar artifacts/OPAM.jar --genTest Adaptive --numTest ${NUM_TEST} -b ${TESTPATH} --data ${RESOURCE} --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}

        # Execute OPAM (${NUM_RUNS} runs will be executed)
        ${CMD_PREFIX} java -jar artifacts/OPAM.jar --runCnt ${NUM_RUNS} -b ${WORKPATH} --data ${RESOURCE} --testPath ${TESTPATH}/test.list --numTest ${NUM_TEST} --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA} --maxMissed 1000
    done
done


# Collecting results
${CMD_PREFIX} ~/venv/bin/python3  scripts/Python/ResultCollector.py -f merge_fitness -t results/${CODE}/${EXP_NAME}
${CMD_PREFIX} ~/venv/bin/python3  scripts/Python/ResultCollector.py -f merge_time -t results/${CODE}/${EXP_NAME}



