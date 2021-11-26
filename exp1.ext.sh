#!/bin/bash -l

## define variables
CMD_PREFIX="echo"    # Please make "echo" to "" for actual running
CODE="RQ1"

NUM_RUNS=50
SUBJECT_NAME="ESAIL"  # Subject name
NCPU=1                # Number of CPUs, Use a different number depends on the subject
SIM_TIME=6000         # Simulation time, only ESAIL uses 60000ms, otherwise 0
TIME_QUANTA=0.1       # Unit of time, only ESAIL uses 0.1ms, otherwise 1


# Path settings
OPAMPATH="results/${CODE}/OPAM/${SUBJECT_NAME}"
RSPATH="results/${CODE}/RS/${SUBJECT_NAME}"
TESTPATH="results/TestPool/${CODE}/${SUBJECT_NAME}"
RESOURCE="res/industrial/${SUBJECT_NAME}.csv"

# Output path for ext results
OUTPUT="results/${CODE}-Ext/"

#############################################
# Generate another Test Sets
#############################################
${CMD_PREFIX} java -jar artifacts/OPAM.jar --genTest Adaptive --numTest 10  -b ${TESTPATH}/Adaptive10  --data ${RESOURCE} --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}
${CMD_PREFIX} java -jar artifacts/OPAM.jar --genTest Random   --numTest 10  -b ${TESTPATH}/Random10    --data ${RESOURCE} --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}
${CMD_PREFIX} java -jar artifacts/OPAM.jar --genTest Worst    --numTest 10  -b ${TESTPATH}/Worst10     --data ${RESOURCE} --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}
${CMD_PREFIX} java -jar artifacts/OPAM.jar --genTest Adaptive --numTest 500 -b ${TESTPATH}/Adaptive500 --data ${RESOURCE} --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}
${CMD_PREFIX} java -jar artifacts/OPAM.jar --genTest Random   --numTest 500 -b ${TESTPATH}/Random500   --data ${RESOURCE} --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}
${CMD_PREFIX} java -jar artifacts/OPAM.jar --genTest Worst    --numTest 500 -b ${TESTPATH}/Worst500    --data ${RESOURCE} --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}


#############################################
# Run OPAM Evaluation with another Test Sets
#############################################
for (( runID=1; runID<=${NUM_RUNS}; runID++)); do
	runName=$(printf 'Run%02d' "${runID}")
	${CMD_PREFIX} java -jar artifacts/OPAM-Ext.jar -b ${OPAMPATH}/${runName} --testPath ${TESTPATH}/Adaptive10  --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}
	${CMD_PREFIX} java -jar artifacts/OPAM-Ext.jar -b ${OPAMPATH}/${runName} --testPath ${TESTPATH}/Adaptive10  --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}
	${CMD_PREFIX} java -jar artifacts/OPAM-Ext.jar -b ${OPAMPATH}/${runName} --testPath ${TESTPATH}/Adaptive10  --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}
	${CMD_PREFIX} java -jar artifacts/OPAM-Ext.jar -b ${OPAMPATH}/${runName} --testPath ${TESTPATH}/Adaptive500 --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}
	${CMD_PREFIX} java -jar artifacts/OPAM-Ext.jar -b ${OPAMPATH}/${runName} --testPath ${TESTPATH}/Random500   --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}
	${CMD_PREFIX} java -jar artifacts/OPAM-Ext.jar -b ${OPAMPATH}/${runName} --testPath ${TESTPATH}/Worst500    --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}
done

#############################################
# Run RS Evaluation with another Test Sets
#############################################
for (( runID=1; runID<=${NUM_RUNS}; runID++)); do
	runName=$(printf 'Run%02d' "${runID}")
	${CMD_PREFIX} java -jar artifacts/OPAM-Ext.jar -b ${RSPATH}/${runName} --testPath ${TESTPATH}/Adaptive10  --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}
	${CMD_PREFIX} java -jar artifacts/OPAM-Ext.jar -b ${RSPATH}/${runName} --testPath ${TESTPATH}/Adaptive10  --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}
	${CMD_PREFIX} java -jar artifacts/OPAM-Ext.jar -b ${RSPATH}/${runName} --testPath ${TESTPATH}/Adaptive10  --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}
	${CMD_PREFIX} java -jar artifacts/OPAM-Ext.jar -b ${RSPATH}/${runName} --testPath ${TESTPATH}/Adaptive500 --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}
	${CMD_PREFIX} java -jar artifacts/OPAM-Ext.jar -b ${RSPATH}/${runName} --testPath ${TESTPATH}/Random500   --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}
	${CMD_PREFIX} java -jar artifacts/OPAM-Ext.jar -b ${RSPATH}/${runName} --testPath ${TESTPATH}/Worst500    --cpus ${NCPU} --max ${SIM_TIME} --quanta ${TIME_QUANTA}
done


#######################################################
# Collecting results
#######################################################
# Collecting the results (E'_Adaptive 10)
${CMD_PREFIX} ~/venv/bin/python3 scripts/Python/ResultCollector.py -t results/${CODE}/OPAM -f merge_test_fitness_industrial -o ${OUTPUT}/fitness_OPAM_Adaptive10.csv -tN Adaptive10
${CMD_PREFIX} ~/venv/bin/python3 scripts/Python/ResultCollector.py -t results/${CODE}/RS   -f merge_test_fitness_industrial -o ${OUTPUT}/fitness_RS_Adaptive10.csv -tN Adaptive10
# Collecting the results (E'_Random 10)
${CMD_PREFIX} ~/venv/bin/python3 scripts/Python/ResultCollector.py -t results/${CODE}/OPAM -f merge_test_fitness_industrial -o ${OUTPUT}/fitness_OPAM_Random10.csv -tN Random10
${CMD_PREFIX} ~/venv/bin/python3 scripts/Python/ResultCollector.py -t results/${CODE}/RS   -f merge_test_fitness_industrial -o ${OUTPUT}/fitness_RS_Random10.csv -tN Random10
# Collecting the results (E'_Worst 10)
${CMD_PREFIX} ~/venv/bin/python3 scripts/Python/ResultCollector.py -t results/${CODE}/OPAM -f merge_test_fitness_industrial -o ${OUTPUT}/fitness_OPAM_Worst10.csv -tN Worst10
${CMD_PREFIX} ~/venv/bin/python3 scripts/Python/ResultCollector.py -t results/${CODE}/RS   -f merge_test_fitness_industrial -o ${OUTPUT}/fitness_RS_Worst10.csv -tN Worst10

# Collecting the results (E'_Adaptive 500)
${CMD_PREFIX} ~/venv/bin/python3 scripts/Python/ResultCollector.py -t results/${CODE}/OPAM -f merge_test_fitness_industrial -o ${OUTPUT}/fitness_OPAM_Adaptive500.csv -tN Adaptive500
${CMD_PREFIX} ~/venv/bin/python3 scripts/Python/ResultCollector.py -t results/${CODE}/RS   -f merge_test_fitness_industrial -o ${OUTPUT}/fitness_RS_Adaptive500.csv -tN Adaptive500
# Collecting the results (E'_Random 500)
${CMD_PREFIX} ~/venv/bin/python3 scripts/Python/ResultCollector.py -t results/${CODE}/OPAM -f merge_test_fitness_industrial -o ${OUTPUT}/fitness_OPAM_Random500.csv -tN Random500
${CMD_PREFIX} ~/venv/bin/python3 scripts/Python/ResultCollector.py -t results/${CODE}/RS   -f merge_test_fitness_industrial -o ${OUTPUT}/fitness_RS_Random500.csv -tN Random500
# Collecting the results (E'_Worst 500)
${CMD_PREFIX} ~/venv/bin/python3 scripts/Python/ResultCollector.py -t results/${CODE}/OPAM -f merge_test_fitness_industrial -o ${OUTPUT}/fitness_OPAM_Worst500.csv -tN Worst500
${CMD_PREFIX} ~/venv/bin/python3 scripts/Python/ResultCollector.py -t results/${CODE}/RS   -f merge_test_fitness_industrial -o ${OUTPUT}/fitness_RS_Worst500.csv -tN Worst500



#######################################################
# Generate QI data
#######################################################
${CMD_PREFIX} java -jar artifacts/QI-Ext.jar --apprs OPAM,RS --compare1 ${OUTPUT}/fitness_OPAM_Adaptive10.csv --compare2 ${OUTPUT}/fitness_RS_Adaptive10.csv --output ${OUTPUT}/fitness_QI_Adaptive10.csv --runCnt 50 --numTest 1
${CMD_PREFIX} java -jar artifacts/QI-Ext.jar --apprs OPAM,RS --compare1 ${OUTPUT}/fitness_OPAM_Random10.csv --compare2 ${OUTPUT}/fitness_RS_Random10.csv --output ${OUTPUT}/fitness_QI_Random10.csv --runCnt 50 --numTest 1
${CMD_PREFIX} java -jar artifacts/QI-Ext.jar --apprs OPAM,RS --compare1 ${OUTPUT}/fitness_OPAM_Worst10.csv --compare2 ${OUTPUT}/fitness_RS_Worst10.csv --output ${OUTPUT}/fitness_QI_Worst10.csv --runCnt 50 --numTest 1

${CMD_PREFIX} java -jar artifacts/QI-Ext.jar --apprs OPAM,RS --compare1 ${OUTPUT}/fitness_OPAM_Adaptive500.csv --compare2 ${OUTPUT}/fitness_RS_Adaptive500.csv --output ${OUTPUT}/fitness_QI_Adaptive500.csv --runCnt 50 --numTest 1
${CMD_PREFIX} java -jar artifacts/QI-Ext.jar --apprs OPAM,RS --compare1 ${OUTPUT}/fitness_OPAM_Random500.csv --compare2 ${OUTPUT}/fitness_RS_Random500.csv --output ${OUTPUT}/fitness_QI_Random500.csv --runCnt 50 --numTest 1
${CMD_PREFIX} java -jar artifacts/QI-Ext.jar --apprs OPAM,RS --compare1 ${OUTPUT}/fitness_OPAM_Worst500.csv --compare2 ${OUTPUT}/fitness_RS_Worst500.csv --output ${OUTPUT}/fitness_QI_Worst500.csv --runCnt 50 --numTest 1