#!/usr/bin/env python
# -*- coding: UTF-8 -*-

import os
import json

class ResultFileLoader():
    def __init__(self):
        pass

    def load_schedules(self, _filepath):
        if not os.path.exists(_filepath): raise FileNotFoundError("No file \"%s\""%_filepath)
        f = open(_filepath)
        schedules = json.load(f)
        f.close()
        return schedules

    def load_priorities(self, _filepath):
        if not os.path.exists(_filepath): raise FileNotFoundError("No file \"%s\""%_filepath)

        f = open(_filepath)
        priorities = json.load(f)
        f.close()

        # priority starts 1
        for p in range(0, len(priorities)):
            priorities[p] = priorities[p]+1

        return priorities

    def load_arrivals(self, _filename):
        if not os.path.exists(_filename): raise FileNotFoundError("No file \"%s\""%_filename)
        f = open(_filename)
        arrivals = json.load(f)
        f.close()
        return arrivals

    def load_input(self, _filepath):
        if not os.path.exists(_filepath): raise FileNotFoundError("No file \"index.csv\"")
        f = open(_filepath)
        lines = f.readlines()
        f.close()

        tasks = []
        for idx in range(0, len(lines)):
            if idx==0: continue
            line = lines[idx]
            if line.strip() =="": continue
            cols = line.split(',')
            tasks.append({'Name':cols[1], 'Type':cols[2], 'Priority':int(cols[3])})

        for item in tasks:
            p = item['Priority']

        # find priority level
        levels = [-1] * len(tasks)
        for level in range(0, len(tasks)):
            minTaskIdx = 0
            minPriority = 1000000
            for tID in range(0, len(tasks)):
                if levels[tID]!=-1: continue

                p = tasks[tID]['Priority']
                if minPriority > p:
                    minTaskIdx = tID
                    minPriority = p

            levels[minTaskIdx] = level

        # assign priority level
        for tID in range(0, len(tasks)):
            tasks[tID]['PriorityLevel'] = levels[tID] + 1

        return tasks

    def load_fitness(self, targetPath, runNum, worktype='external', selectedCycle=None, removeDM=False):
        data = []
        for runID in range(0, runNum):
            inputpath = "%s/Run%02d/_fitness/fitness_%s.csv" % (targetPath, runID+1, worktype)
            f = open(inputpath, 'r')
            line = f.readline()    # remove title
            while True:
                line = f.readline()
                if line is None or line=="":break
                cols = line.split(",")

                # filter of cycle
                if worktype=='external':
                    cycle = int(cols[0]) # Cycle
                    solID = int(cols[4]) # SolutionID
                    FS = float(cols[5]) # Safety margin
                    FC = float(cols[6]) # Constraint
                    DM = int(cols[7])    # deadline miss
                else:
                    raise Exception("Not acceptable worktype: %s"%worktype)
                if selectedCycle != None and cycle not in selectedCycle:continue
                if removeDM is True and DM != 0: continue

                data.append([runID+1, cycle, solID, FS, FC, DM])
        return data

    def load_solutions_from_fitness(self, _filename, _worktype='external', _selectedCycle=1000, _removeDM=False):
        if not os.path.exists(_filename): raise FileNotFoundError("No file \"%s\""%_filename)

        f = open(_filename, 'r')
        line = f.readline()    # filter out title
        solutions = []
        while True:
            line = f.readline()
            if line is None or line=="":break
            cols = line.split(",")

            # filter of cycle
            if _worktype=='external':
                cycle = int(cols[0]) # Cycle
                solID = int(cols[4]) # SolutionID
                DM = int(cols[7])    # deadline miss
            else:
                cycle = int(cols[0]) # Cycle
                solID = int(cols[3]) # SolutionID
                DM = int(cols[6])    # deadline miss
            if cycle!=_selectedCycle:continue
            if _removeDM is True and DM != 0: continue
            solutions.append(solID)
        return solutions

    def stats_schedules(self, schedules, UNIT=1.0):
        #for each task, we gather the margin data
        results = []
        for t in range(0, len(schedules)):
            nMissed = 0
            margins = []
            for e in range(0, len(schedules[t])):
                schedule = schedules[t][e]  # [arrival, deadline, finish, execution, activatedNum, [start, end, CPU] // CPU ==-1 대기
                SafetyMargin = (schedule[2] - schedule[1])*UNIT   # finish - deadline
                if SafetyMargin > 0.0:
                    nMissed += 1
                margins.append(SafetyMargin)
            results.append({"nMissed":nMissed, "margins":margins})
        return results