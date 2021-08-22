#!/usr/bin/env python
# -*- coding: UTF-8 -*-

import os
import math
import statistics as st
from data.ResultFileLoader import ResultFileLoader


class ScheduleStats(ResultFileLoader):
    formatSchedule = '_schedulesEx/sol%d_arr%d.json'
    formatPriority = '_prioritiesEx/sol%d.json'
    basePath = ''
    outputPath = ''
    MaxRun = 0

    def __init__(self, scheduleFormat=None, priorityFormat=None):
        super(ResultFileLoader, self).__init__()

        if scheduleFormat is not None:
            self.formatSchedule = scheduleFormat

        if priorityFormat is not None:
            self.formatPriority = priorityFormat
        pass

    # Check all required runs are exists (based on the settings)
    def verifying_runs(self, basePath, numRuns):
        listID = [0] * numRuns

        runs = os.listdir(basePath)
        for item in runs:
            if item.startswith(".") is True: continue
            if os.path.isdir(os.path.join(basePath, item)) is False:
                continue

            try:
                idnum = int(item[3:])
            except ValueError as e:
                raise e
            if idnum>numRuns: continue
            listID[idnum-1] = idnum

        zeroList = []
        for idx in range(0, len(listID)):
            if listID[idx] == 0:
                zeroList.append(idx+1)

        if len(zeroList)!=0:
            raise Exception("Not executed following runs: "+ str(zeroList))
        pass

    #################################################
    # Get informations from data
    #################################################
    def get_stats_solution(self, _inputpath, _tasks, _solutionID, _arrCnt=0, _UNIT=1.0):
        nExecution = [0] * len(_tasks)
        stMissed = [0] * len(_tasks)
        stMargins = []
        for x in range(0, len(_tasks)):
            stMargins.append([])

        for arrID in range(0, _arrCnt):
            print(".", end="")

            # counting stats
            schedules = self.load_schedules(os.path.join(_inputpath, self.formatSchedule%(_solutionID, arrID)))
            res = self.stats_schedules(schedules, UNIT=_UNIT)

            # get info for each task
            for t in range(0, len(_tasks)):
                stMissed[t] += res[t]['nMissed']
                stMargins[t] += res[t]['margins']
                nExecution[t] += len(res[t]['margins'])
        return {"nExecution":nExecution, "nMissed":stMissed, "margins":stMargins}

    def print_stats(self, _tasks, _priorities, _stats, _outputFile, _arrCnt):
        margins = _stats["margins"]
        nMissed = _stats["nMissed"]
        nExecution = _stats["nExecution"]

        output = open(_outputFile, "w")
        title = ("TaskID,TaskName,TaskType,Priority,nExecution,avgExecution,nMissed,Avg.nMissed,"
                 +"minMargin,maxMargin,meanMargin,medianMargin\n")
        output.write(title)

        for t in range(0, len(_tasks)):
            medianMargin = st.median(margins[t])
            meanMargin = sum(margins[t])/len(margins[t])
            retText = "%d,%s,%s,%d, %d,%f,%d,%f, %f,%f,%f,%f\n"%(
                t+1, _tasks[t]['Name'], _tasks[t]['Type'], _priorities[t],
                nExecution[t], nExecution[t]/_arrCnt,
                nMissed[t], nMissed[t]/_arrCnt,
                -max(margins[t]), -min(margins[t]), -meanMargin, -medianMargin)
            output.write(retText)
        output.close()

    def analysis_solution(self, _tasks, _workPath, _outputPath, _solutionType, _solution, _arrCnt=0, _UNIT=1.0):
        if _solution['Run'] is None:
            return None
        inputPath = os.path.join(_workPath, "Run%02d"%_solution['Run'])

        priorities = self.load_priorities(os.path.join(inputPath, self.formatPriority%(_solution['SolutionID'])))

        # create statistics for each task type
        print("processing %s point (solutionID: %d)" % (_solutionType, _solution['SolutionID']), end="")
        stats = self.get_stats_solution(inputPath, _tasks, _solution['SolutionID'], _arrCnt, _UNIT)
        print("Done")

        # Writing result for each task type
        self.print_stats(_tasks, priorities, stats, _outputPath, _arrCnt)
        return stats

    #################################################
    # Selecting solutions (Initial, Knee, two Extremes)
    #################################################
    def select_solution(self, targetPath, runNum, cycleNum):
        # load fitness data
        solutions = self.load_fitness(targetPath, runNum, selectedCycle=[0,cycleNum], removeDM=False)
        # solutions = list of [Run, Cycle, SolutionID, FS, FC, DM]

        # selecting initial item
        initial = None
        for x in range(0, len(solutions)):
            if solutions[x][0]==1 and solutions[x][1]==0 and solutions[x][2]==1: # cycle==0 & run==1 & solutionID==1
                initial = solutions[x]
                break
        if initial is None:
            raise Exception("Not found initial solution")

        # selecting candidates point
        candidates = []
        for x in range(0, len(solutions)):
            if solutions[x][1]!=cycleNum: continue  # cycle!=cycleNum
            candidates.append(solutions[x])

        # normalize FS and FC (inverted normalized values to calculate distance from (0,0))
        candidates = self.normalize(candidates, 3)  # normalize FS  ==> save to index 6 of each item
        candidates = self.normalize(candidates, 4)  # normalize FC  ==> save to index 7 of each item

        # calculate distance from (0,0)
        minDist = 2**1024
        minIdx = 0
        for x in range(0, len(candidates)):
            dist = math.sqrt(candidates[x][6]**2 + candidates[x][7]**2)
            candidates[x].append(dist)
            if dist < minDist:
                minDist = dist
                minIdx= x
        knee = candidates[minIdx]

        # min FS and max FC
        minFSIdx = self.selectMinIndex(candidates, 6)
        items = self.selectMinItems(candidates, 6, candidates[minFSIdx][6])
        ext1 = items[self.selectMinIndex(items, 7)]

        # min FC and max FS
        minFSIdx = self.selectMinIndex(candidates, 7)
        items = self.selectMinItems(candidates, 7, candidates[minFSIdx][7])
        ext2 = items[self.selectMinIndex(items, 6)]

        # create dictionary
        initial = {'Run':initial[0], 'SolutionID':initial[2], 'FS':initial[3], 'FC':initial[4]}
        knee = {'Run':knee[0], 'SolutionID':knee[2], 'FS':knee[3], 'FC':knee[4]}
        ext1 = {'Run':ext1[0], 'SolutionID':ext1[2], 'FS':ext1[3], 'FC':ext1[4]}
        ext2 = {'Run':ext2[0], 'SolutionID':ext2[2], 'FS':ext2[3], 'FC':ext2[4]}
        return {"Initial": initial, "Knee":knee, "Extreme1":ext1, "Extreme2":ext2}

    def normalize(self, solutions, idx):
        # make list of FD
        itemlist = []
        for x in range(0, len(solutions)):
            itemlist.append(solutions[x][idx])

        # normalize FD
        minFD = min(itemlist)
        maxFD = max(itemlist)
        if (maxFD==minFD):
            for x in range(0, len(solutions)):
                solutions[x].append(0)   # inverted normalized values to calculate distance from (0,0)
        else:
            for x in range(0, len(solutions)):
                normV = (solutions[x][idx]-minFD)/(maxFD-minFD)
                normV = normV if math.isnan(normV) is False and math.isinf(normV) is False else 1
                solutions[x].append(1-normV)   # inverted normalized values to calculate distance from (0,0)
        return solutions

    def selectMinIndex(self, data, index):
        minV = 1
        minIdx = 0
        for x in range(0, len(data)):
            if data[x][index] < minV:
                minV = data[x][index]
                minIdx= x
        return minIdx

    def selectMinItems(self, data, index, selectedValue):
        items = []
        for x in range(0, len(data)):
            if data[x][index] == selectedValue:
                items.append(data[x])
        return items


    #################################################
    # latex function
    #################################################
    def compare_stats(self, _outputPath, _tasks, _initial, _key, _stats):
        output = open(_outputPath, "w")
        output.write("\\begin{table}[t]\n")
        output.write("\\caption{Comparing safety margins from the task executions of ESAIL when using our optimized priority assignment and the one defined by engineers.}\n")
        output.write("\t\\begin{center}\n")
        output.write("\t\\begin{tabular}{ccrrr}\n")
        output.write("\t\t\\toprule\n")
        header = "\t\t\\multicolumn{1}{l}{} & \\multicolumn{1}{l}{} & \\multicolumn{1}{c}{Periodic tasks}" + \
                 " & \\multicolumn{1}{c}{Aperiodic tasks} & \\multicolumn{1}{c}{All tasks} \\\\\n"
        output.write(header)
        output.write("\t\t\\midrule\n")

        # Engineer
        typeStats1 = self.stats_by_type(_tasks, _initial)
        text = self.make_latex_result("Engineer", typeStats1)
        output.write(text)
        output.write("\t\t\\midrule\n")

        # OPAM
        typeStats2 = self.stats_by_type(_tasks, _stats)
        text = self.make_latex_result("OPAM", typeStats2)
        output.write(text)
        output.write("\t\t\\midrule\n")

        # Comparing
        text = self.make_latex_result_comp("\\% Difference", typeStats1, typeStats2)
        output.write(text)

        output.write("\t\t\\bottomrule\n")
        output.write("\t\t\\multicolumn{5}{l}{\\footnotesize $\\ast$ Unit of time: ms}\n")
        output.write("\t\\end{tabular}\n")
        output.write("\t\\end{center}\n")
        output.write("\\end{table}\n")
        output.close()

        pass

    def stats_by_type(self, _tasks, _stats):
        # gather initial information
        all = []
        periodics = []
        aperiodics = []
        for t in range(0, len(_tasks)):
            if _tasks[t]['Type']=="Periodic":
                periodics += _stats["margins"][t]
            else:
                aperiodics += _stats["margins"][t]
            all += _stats["margins"][t]
        return {"all":all, "periodics":periodics, "aperiodics":aperiodics}

    def make_latex_result(self, _name, _typeStats):
        lineText = '\\multirow{4}{*}{%s} & Min' % (_name)
        for typeName in ["periodics","aperiodics","all"]:
            margins = _typeStats[typeName]
            lineText +="& %.1f"%(-max(margins))
        lineText += ' \\\\\n'

        lineText += ' & Max'
        for typeName in ["periodics","aperiodics","all"]:
            margins = _typeStats[typeName]
            lineText +="& %.1f"%(-min(margins))
        lineText += ' \\\\\n'

        lineText += ' & Avg.'
        for typeName in ["periodics","aperiodics","all"]:
            margins = _typeStats[typeName]
            lineText +="& %.1f"%(-sum(margins)/len(margins))
        lineText += ' \\\\\n'

        lineText += ' & Median'
        for typeName in ["periodics","aperiodics","all"]:
            margins = _typeStats[typeName]
            lineText +="& %.1f"%(-st.median(margins))
        lineText += ' \\\\\n'
        return lineText

    def make_latex_result_comp(self, _name, _typeStats1, _typeStats2):
        lineText = '\\multirow{4}{*}{%s} & Min' % (_name)
        for typeName in ["periodics", "aperiodics", "all"]:
            value1 = -max(_typeStats1[typeName])
            value2 = -max(_typeStats2[typeName])
            lineText +="& %.2f\\%%"%( (value2-value1)/abs(value1)*100.0 )
        lineText += ' \\\\\n'

        lineText += ' & Max'
        for typeName in ["periodics", "aperiodics", "all"]:
            value1 = -min(_typeStats1[typeName])
            value2 = -min(_typeStats2[typeName])
            lineText +="& %.2f\\%%"%( (value2-value1)/abs(value1)*100.0 )
        lineText += ' \\\\\n'

        lineText += ' & Avg.'
        for typeName in ["periodics", "aperiodics", "all"]:
            value1 = -sum(_typeStats1[typeName]) / len(_typeStats1[typeName])
            value2 = -sum(_typeStats2[typeName]) / len(_typeStats2[typeName])
            lineText +="& %.2f\\%%"%( (value2-value1)/abs(value1)*100.0 )
        lineText += ' \\\\\n'

        lineText += ' & Median'
        for typeName in ["periodics", "aperiodics", "all"]:
            value1 = -st.median(_typeStats1[typeName])
            value2 = -st.median(_typeStats2[typeName])
            lineText +="& %.2f\\%%"%( (value2-value1)/abs(value1)*100.0 )
        lineText += ' \\\\\n'
        return lineText

    #################################################
    # Run function
    #################################################
    def run(self, targetPath, outputPath, runNum=50, timeunit=0.1, numTest=10, cycleNum=1000):
        self.verifying_runs(targetPath, runNum)

        # preparing task output
        taskOutput = "%s_tasks"%(outputPath)
        if os.path.exists(taskOutput) is False:
            os.makedirs(taskOutput)

        # select target solutions
        solutions = self.select_solution(targetPath, runNum, cycleNum)

        # load common data and set initial
        initialSolution = solutions["Initial"]
        inputPath = os.path.join(targetPath, "Run%02d"%initialSolution['Run'])
        tasks = self.load_input(os.path.join(inputPath, 'input.csv'))
        filename = "%s/%s_Run%d_Sol%d.csv" % (taskOutput, "Initial", initialSolution['Run'], initialSolution['SolutionID'])
        initial = self.analysis_solution(tasks, targetPath, filename, "Initial", initialSolution, _arrCnt=numTest, _UNIT=timeunit)

        # analysis solutions and print out
        for key, solution in solutions.items():
            if key == "Initial": continue
            filename = "%s/%s_Run%d_Sol%d.csv" % (taskOutput, key, solution['Run'], solution['SolutionID'])
            stats = self.analysis_solution(tasks, targetPath, filename, key, solution, _arrCnt=numTest, _UNIT=timeunit)
            filename = "%s_%s.tex" % (outputPath, key)
            self.compare_stats(filename, tasks, initial, key, stats)
        return True


def parse_arg():
    import argparse
    import sys
    parser = argparse.ArgumentParser(description='Result Collector')
    parser.add_argument('-o', dest='outputPath', type=str, default=None, help='')
    parser.add_argument('-t', dest='targetPath', type=str, default=None, help='target path')
    parser.add_argument('-r', dest='numRuns', type=int, default=50, help='number of runs')
    parser.add_argument('-c', dest='numCycle', type=int, default=1000, help='number of cycles')
    parser.add_argument('-a', dest='numTest', type=int, default=10, help='number of test cases of worst-case arrival times')
    parser.add_argument('-u', dest='timeUnit', type=float, default=0.1, help='unit of time')

    # parameter parsing
    args = parser.parse_args(args=sys.argv[1:])
    if args.targetPath is None or len(args.targetPath)==0:
        parser.print_help()
        exit(1)

    if args.outputPath is None or len(args.outputPath)==0:
        parser.print_help()
        exit(1)
    return args


################################################
################################################
################################################
# For All Points
if __name__ == "__main__":
    args = parse_arg()
    print(" + Executing path  : "+os.path.abspath(os.curdir))
    print(" + Data path       : "+os.path.abspath(args.targetPath))
    print(" + Output path     : "+os.path.abspath(args.outputPath))
    print(" + Number of runs: : %d" % args.numRuns)
    print(" + Number of test  : %d" % args.numTest)
    print(" + Number of cycle : %d" % args.numCycle)
    print(" + Unit of time:   : %.2f" % args.timeUnit)

    obj = ScheduleStats()
    obj.run(args.targetPath, args.outputPath, args.numRuns, args.timeUnit, args.numTest, args.numCycle)


# ~/venv/bin/python3 scripts/Python/ScheduleStatsComp.py -t results/RQ1/OPAM/ESAIL -u 0.1 -o results/RQ4/ESAIL_stats
