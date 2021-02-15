#!/bin/bash -l

##############
# Synthesizer.jar parameters
#    -w <string>: work name for this tool, this name will be used to store task sets, you can specify a path, no default
#    -c <string>: value of control variable, this value is just used for generating sub folder, no default
#    -n <integer>: number of task sets to be generated, default=10
#    -m <integer>: number of tasks , default=20
#    -r <float>: ratio of aperiodic tasks, default=0.4
#    -a <integer>: range factor to determine maximum inter-arrival times, default=2
#    -s <integer>: simulation time (unit: milliseconds), default=10000
#    -t <integer>: unit of time (unit: milliseconds), default=0.1
#    -u <float>: target utilization, default=0.7
#    -d <float>: delta value to allow differences between target and actual utilization, default=0.01
#    --granularity <integer>: granularity of task period, default=10
#    --priority <RM | ENGINEER>: initial priority assignment policy, default=RM (rate-monotonic)
#    --minArrival <float>: range factor to determine minimum inter-arrival times, default=1.0


N_TASKSETS=10
SIM_TIME=2000
CMD_PREFIX=""
TARGET_PATH="res/synthetics"

# Generate synthetic task sets (EXP2.1: varying the number of tasks)
for ((nTask=10; nTask<=50; nTask+=5)); do
    ${CMD_PREFIX} java -jar artifacts/Synthesizer.jar -w ${TARGET_PATH}/1_nTasks -n ${N_TASKSETS} -s ${SIM_TIME} -c ${nTask} -m ${nTask}
done

# Generate synthetic task sets (EXP2.2: varying the ratio of the aperiodic tasks)
for ((ratio=5; ratio<=50; ratio+=5)); do
    ratioValue=`echo "${ratio} 0.01"|awk '{printf "%.2f", $1*$2}'` # convert $ratio into percentage value
    ${CMD_PREFIX} java -jar artifacts/Synthesizer.jar -w ${TARGET_PATH}/2_ratioAperiodic -n ${N_TASKSETS} -s ${SIM_TIME} -c ${ratioValue} -r ${ratioValue}
done

# Generate synthetic task sets (EXP2.3: varying the maximum arrival time range factor)
for ((rangeFactor=2; rangeFactor<=10; rangeFactor+=1)); do
    SimulationTime=10000 # specific value for EXP2.3
    ${CMD_PREFIX} java -jar artifacts/Synthesizer.jar -w ${TARGET_PATH}/3_maxArrivalRange -n ${N_TASKSETS} -s ${SimulationTime} -c ${rangeFactor} -a ${rangeFactor}
done

# Generate synthetic task sets (EXP2.4: varying the simulation time)
for ((T=2000; T<=10000; T+=1000)); do
    ${CMD_PREFIX} java -jar artifacts/Synthesizer.jar -w ${TARGET_PATH}/4_simTime -n ${N_TASKSETS} -s ${T} -c ${T}
done