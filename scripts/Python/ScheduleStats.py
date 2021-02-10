#!/usr/bin/env python
# -*- coding: UTF-8 -*-

import os
import math
import statistics as st
from scipy.stats import mannwhitneyu as utest
from ResultFileLoader import ResultFileLoader

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

    def check(self, basePath, outputPath, maxRun=50):
        print(" + Executing Directory: "+os.path.abspath(os.curdir))
        print(" + Loading Directory: "+os.path.abspath(basePath))
        print(" + Output Path: "+os.path.abspath(outputPath))
        self.verifying_runs(basePath, maxRun)
        print(" + Available runs: : %d" % maxRun)
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
    def get_priorities(self, tasks):
        priorities = []
        for t in range(0, len(tasks)):
            priorities.append(tasks[t]['PriorityLevel'])
        return priorities

    def calculate_fc(self, tasks, priorities):
        pMin = 0
        for t in range(0, len(priorities)):
            if tasks[t]['Type'] == 'Aperiodic': continue
            if priorities[t] > pMin:
                pMin = priorities[t]
        fc = 0
        for t in range(0, len(priorities)):
            if tasks[t]['Type'] != 'Aperiodic': continue
            fc += (priorities[t] - pMin)
        return fc

    def get_num_violation(self, tasks, priorities):
        pMin = 0
        for t in range(0, len(priorities)):
            if tasks[t]['Type'] == 'Aperiodic': continue
            if priorities[t] > pMin:
                pMin = priorities[t]
        num = 0
        for t in range(0, len(priorities)):
            if tasks[t]['Type'] != 'Aperiodic': continue
            if priorities[t] < pMin:
                num += 1
        return num

    def analysis_solution(self, _workPath, _runID, _solutionID, _FS, _FC, _arrCnt=0, UNIT=1.0, _compArray=None, option="All"):
        if _runID is None:
            retText = "%d,%d, %.1f,%.1f,%d, %d,%.1f,  %d,%d,%d, %f,%f,%f,%f,  %.4f"%(
                0,0,  0,0,0,  0,0.0,   0,0,0,  0,0,0,0  ,0)
            print("There is no specified solution.")
            return retText, None

        # create statistics for each task type
        stMargins = {'Periodic':[],'Aperiodic':[],'Sporadic':[]}
        stMissed = {'Periodic':0,'Aperiodic':0,'Sporadic':0}
        nTasks = 0
        nViolation = 0
        nExecution = 0
        print("processing for each arrivals", end="")
        for arrID in range(0, _arrCnt):
            print(".", end="")

            # load data
            inputpath = os.path.join(_workPath, "Run%02d"%_runID)
            tasks = self.load_input(os.path.join(inputpath, 'input.csv'))
            schedules = self.load_schedules(os.path.join(inputpath, self.formatSchedule%(_solutionID, arrID)))
            priorities = self.load_priorities(os.path.join(inputpath, self.formatPriority%(_solutionID)))

            # counting stats
            res = self.stats_schedules(schedules, UNIT=UNIT)
            # fC_origin = self.calculate_fc(tasks, self.get_priorities(tasks))
            # fC = self.calculate_fc(tasks, priorities)

            # get info for each task
            for t in range(0, len(tasks)):
                stMissed[tasks[t]['Type']] += res[t]['nMissed']
                stMargins[tasks[t]['Type']] += res[t]['margins']
                nExecution += len(res[t]['margins'])
            nTasks = len(tasks)
            nViolation = self.get_num_violation(tasks, priorities)

        # stats for a solution tested with all arrivals
        allMargins = self.groupingMargins(stMargins, condition=option)

        # statistical comparing
        if _compArray is not None:
            test = utest(_compArray, allMargins, alternative='two-sided')
            pv = test.pvalue
        else:
            pv = -1

        medianMargin = st.median(allMargins)
        meanMargin = sum(allMargins)/len(allMargins)
        retText = "%d,%d, %.1f,%.1f,%d, %d,%.1f,  %d,%d,%d, %f,%f,%f,%f,  %.4f"%(
                        nTasks,_solutionID, _FS, _FC,
                        nViolation,
                        nExecution, nExecution/_arrCnt,
                        stMissed['Periodic'], stMissed['Sporadic'], stMissed['Aperiodic'],
                        -max(allMargins), -min(allMargins), -meanMargin, -medianMargin, pv)
        print("Done")
        return retText, allMargins

    def groupingMargins(self, margins, condition):
        all = []
        if condition == "All":
            all = margins['Periodic'] + margins['Aperiodic'] + margins['Sporadic']
        elif condition == "NonAperiodic":
            all = margins['Periodic'] + margins['Sporadic']
        elif condition == "AperiodicAll":
            all = margins['Aperiodic'] + margins['Sporadic']
        elif condition == "Periodic":
            all = margins['Periodic']
        elif condition == "Aperiodic":
            all = margins['Aperiodic']
        elif condition == "Sporadic":
            all += margins['Sporadic']
        return all

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
            if solutions[x][0]==1 and solutions[x][1]==0: # run==1 & cycle==0
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
    # Run function
    #################################################
    def run(self, subject, targetPath, outputPath, option="All", runNum=50, timeunit=0.1, numTest=10, cycleNum=1000):
        print(" + Executing Directory: "+os.path.abspath(os.curdir))
        print(" + Loading Directory: "+os.path.abspath(targetPath))
        print(" + Output Path: "+os.path.abspath(outputPath))
        print(" + Available runs: : %d" % runNum)
        self.verifying_runs(targetPath, runNum)

        # preparing output
        parent = os.path.dirname(outputPath)
        if os.path.exists(parent) is False:
            os.makedirs(parent)
        output = open(outputPath, "w")
        title = ("Subject, PointType, nTasks,SolutionID,FS,FC,"
                 +"nViolation,"
                 +"nExecution,avgExecution,"
                 +"nMissedPeriodic," #avgMissedP,medianMissedP,"
                 +"nMissedSporadic," #avgMissedS,medianMissedS,"
                 +"nMissedAperiodic," #avgMissedA,medianMissedA,"
                 +"minMargin,minMargin, meanMargin,medianMargin,P-value\n")
        output.write(title)

        # select target solutions
        solutions = self.select_solution(targetPath, runNum, cycleNum)

        # analysis solutions and print out
        initial = None
        for key, solution in solutions.items():
            retText, comp = self.analysis_solution(targetPath,
                                                   solution['Run'],
                                                   solution['SolutionID'], solution['FS'],
                                                   solution['FC'],
                                               _arrCnt=numTest, UNIT=timeunit,
                                               _compArray=initial, option=option)

            output.write("%s,%s,%s\n"%(subject, key, retText))
            if initial is None:
                initial = comp
        output.close()
        return True


def parse_arg():
    import argparse
    import sys
    parser = argparse.ArgumentParser(description='Result Collector')
    parser.add_argument('-s', dest='subjectName', type=str, default=None, help='subject Name')
    parser.add_argument('-o', dest='outputPath', type=str, default=None, help='')
    parser.add_argument('-t', dest='targetPath', type=str, default=None, help='target path')
    parser.add_argument('-k', dest='taskType', type=str, default="All", help='type of task (All, Periodic, Aperiodic)')
    parser.add_argument('-r', dest='numRuns', type=int, default=50, help='number of runs')
    parser.add_argument('-c', dest='numCycle', type=int, default=1000, help='number of cycles')
    parser.add_argument('-a', dest='numTest', type=int, default=10, help='number of test cases of worst-case arrival times')
    parser.add_argument('-u', dest='timeUnit', type=float, default=0.1, help='unit of time')

    # parameter parsing
    args = parser.parse_args(args=sys.argv[1:])
    if args.subjectName is None or len(args.subjectName)==0:
        parser.print_help()
        exit(1)

    if args.targetPath is None or len(args.targetPath)==0:
        parser.print_help()
        exit(1)

    if args.taskType not in ['All', 'Periodic', 'Aperiodic', 'AperiodicAll', 'Sporadic', 'NonAperiodic']:
        parser.print_help()
        exit(1)

    return args

################################################
################################################
################################################
# For All Points

if __name__ == "__main__":
    args = parse_arg()
    print("Output File: %s" % args.outputPath)
    print("Target Path: %s" % args.targetPath)
    obj = ScheduleStats()
    obj.run(args.subjectName, args.targetPath, args.outputPath, args.taskType, args.numRuns, args.timeUnit, args.numTest, args.numCycle)


# python ScheduleStats.py -s ESAIL -t results/RQ1/OPAM/ESAIL -o results/RQ3/stats_All.csv -k All
# python ScheduleStats.py -s ESAIL -t results/RQ1/OPAM/ESAIL -o results/RQ3/stats_Periodic.csv -k Periodic
# python ScheduleStats.py -s ESAIL -t results/RQ1/OPAM/ESAIL -o results/RQ3/stats_Aperiodic.csv -k AperiodicAll
