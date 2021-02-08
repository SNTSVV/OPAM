import re
import os
from tqdm import tqdm


class ResultColllector():
    def __init__(self):
        pass

    def expandDirs(self, _dirList, _findKey='', _ptn=None):
        rex = None
        if _ptn is not None:
            rex = re.compile(_ptn)

        data = []
        for dirItem in _dirList:
            flist = os.listdir(dirItem['path'])
            for fname in flist:
                fullpath = os.path.join(dirItem['path'], fname)
                if os.path.isfile(fullpath): continue          # pass not a directory
                if fullpath.startswith(".") is True: continue  # pass hidden dir

                if rex is not None:
                    result = rex.search(fname)
                    if result == None:
                        print("\tPattern ('%s') doesn't matach: %s"%(_ptn, fullpath))
                        continue
                    fname = result.group(0)
                newItem = dirItem.copy()
                newItem[_findKey] = fname
                newItem['path'] = fullpath
                data.append(newItem)
        return data

    ##################################################
    # Collecting functions for timeinfo
    ##################################################
    def load_time(self, _filepath):
        f = open(_filepath)
        lines = f.readlines()
        f.close()

        rex = re.compile('[\d\.]+')
        data = {}
        for line in lines:
            items = line.split(":")
            idx = items[0].find("(")
            if idx >0:
                items[0] = items[0][:idx]

            idx = items[1].find("(")
            if idx >0:
                items[1] = items[1][:idx].strip()
                items[1] = rex.search(items[1]).group(0)

            data[items[0]] = float(items[1].strip())
        return data

    def merge_time(self, _dirpath, _outputname=None, _exptions=None):
        if _outputname is None:
            _outputname = 'timeinfo.csv'

        output = open(os.path.join(_dirpath, _outputname), "w")
        output.write("Variable,Index,Run,Total,Search,P1,P2,Ex,InitHeap,UsedHeap,CommitHeap,MaxHeap,MaxNonHeap\n")

        targets = self.expandDirs([{'path':_dirpath}], 'Variable')
        for target in targets:
            if _exptions is not None and target['Variable'] in _exptions: continue

            subs = self.expandDirs([{'path':target['path']}], 'Index')
            subs = self.expandDirs(subs, 'Run', _ptn=r'\d+')

            progress = tqdm(desc='Collecting data', total=len(subs), unit=' #', postfix=None)
            for item in subs:
                timeInfo = self.load_time(os.path.join(item['path'], '_result.txt'))
                # item['time'] = timeInfo

                output.write("%s,%s,%d, %f,%f,%f,%f,%f, %f,%f,%f,%f,%f\n"% (
                    target['Variable'],item['Index'],int(item['Run']),
                    timeInfo['TotalExecutionTime'],
                    timeInfo['SearchTime'],
                    timeInfo['EvaluationTimeP1'],
                    timeInfo['EvaluationTimeP2'],
                    timeInfo['EvaluationTimeEx'],
                    timeInfo['InitHeap'],
                    timeInfo['usedHeap'],
                    timeInfo['commitHeap'],
                    timeInfo['MaxHeap'],
                    timeInfo['MaxNonHeap']
                ))
                progress.update(1)
                progress.set_postfix_str(item['path'])
            progress.close()
        output.close()

    def merge_time_empirical(self, _dirpath, _outputname=None, _exptions=None):
        if _outputname is None:
            _outputname = 'timeinfo.csv'

        # prepare target dirs
        targets = self.expandDirs([{'path':_dirpath}], 'Variable', _ptn=r'\w+')
        targets = self.expandDirs(targets, 'Run', _ptn=r'\d+')

        # prepare output file
        output = open(os.path.join(_dirpath, _outputname), "w")
        output.write("Variable,Index,Run,Total,Search,P1,P2,Ex,InitHeap,UsedHeap,CommitHeap,MaxHeap,MaxNonHeap\n")

        progress = tqdm(desc='Collecting data', total=len(targets), unit=' #', postfix=None)
        for item in targets:
            timeInfo = self.load_time(os.path.join(item['path'], '_result.txt'))
            # item['time'] = timeInfo

            output.write("%s,%s,%d, %f,%f,%f,%f,%f, %f,%f,%f,%f,%f\n"% (
                item['Variable'],0,int(item['Run']),
                timeInfo['TotalExecutionTime'],
                timeInfo['SearchTime'],
                timeInfo['EvaluationTimeP1'],
                timeInfo['EvaluationTimeP2'],
                timeInfo['EvaluationTimeEx'],
                timeInfo['InitHeap'],
                timeInfo['usedHeap'],
                timeInfo['commitHeap'],
                timeInfo['MaxHeap'],
                timeInfo['MaxNonHeap']
            ))
            progress.update(1)
            progress.set_postfix_str(item['path'])
        progress.close()
        output.close()

    ##################################################
    # Collecting functions for fitness
    ##################################################
    def load_fitness(self, _filepath):
        f = open(_filepath)
        lines = f.readlines()
        f.close()

        # titles = lines[0].split(",")
        # titles[-1] = titles[-1].strip()

        data = []
        for line in lines[1:]:
            items = line.split(",")
            cycle = int(items[0]) #cycle
            iter = int(items[1]) #cycle
            idx = int(items[3])
            sID = int(items[4])
            # fD = int(items[5])
            # fC = int(items[6])
            # dm = int(items[7])
            if cycle==0:
                if not (iter==0 and sID==1): continue
            elif not (cycle==1000 and iter==2): continue
            data.append(line)
            # data[cycle] = {titles[0]:cycle, titles[3]:idx, titles[4]:sID, titles[5]:fD, titles[6]:fC, titles[7]:dm}
        return lines[0], data

    def merge_fitness(self, _dirpath, _outputname=None, _exptions=None):
        if _outputname is None:
            _outputname = 'fitness.csv'

        # preparing target dirs
        targets = self.expandDirs([{'path':_dirpath}], 'Variable')

        # preparing output file
        output = open(os.path.join(_dirpath, _outputname), "w")
        firstWork = True

        # collecting
        for target in targets:
            if _exptions is not None and target['Variable'] in _exptions: continue

            # expand sub-dirs for each target
            subs = self.expandDirs([{'path':target['path']}], 'Index')
            subs = self.expandDirs(subs, 'Run', _ptn=r'\d+')

            progress = tqdm(desc='Collecting data', total=len(subs), unit=' #', postfix=None)
            for item in subs:
                titles, data = self.load_fitness(os.path.join(item['path'], '_fitness/fitness_external.csv'))
                if firstWork is True:
                    output.write("Variable,Index,Run,%s"%titles)
                    firstWork = False
                for line in data:
                    output.write("%s,%s,%d,%s"% (target['Variable'],item['Index'],int(item['Run']),line))
                output.flush()
                progress.update(1)
                progress.set_postfix_str(item['path'])
            progress.close()
        output.close()

    def merge_fitness_empirical(self, _dirpath, _outputname=None, _exptions=None):
        if _outputname is None:
            _outputname = 'fitness.csv'

        # preparing target dirs
        targets = self.expandDirs([{'path':_dirpath}], 'Variable', _ptn=r'\w+')  # subject level folder
        targets = self.expandDirs(targets, 'Run', _ptn=r'\d+')

        # preparing output file
        output = open(os.path.join(_dirpath, _outputname), "w")
        firstWork = True
        index = 0

        # working
        progress = tqdm(desc='Collecting data', total=len(targets), unit=' #', postfix=None)
        for target in targets:
            titles, data = self.load_fitness(os.path.join(target['path'], '_fitness/fitness_external.csv'))
            if firstWork is True:
                output.write("Variable,Index,Run,%s"%titles)
                firstWork = False
            for line in data:
                output.write("%s,%d,%d,%s"% (target['Variable'], index, int(target['Run']),line))
            output.flush()
            progress.update(1)
            progress.set_postfix_str(target['path'])
        progress.close()
        output.close()


def parse_arg():
    import argparse
    import sys
    parser = argparse.ArgumentParser(description='Result Collector')
    parser.add_argument('-o', dest='outputName', type=str, default=None, help='')
    parser.add_argument('-t', dest='targetPath', type=str, default="", help='target path')
    parser.add_argument('-p', dest='pattern', type=str, default=None, help='sub dir pattern')
    parser.add_argument('-e', dest='exceptions', type=str, default="", help='except variables')
    parser.add_argument('-f', dest='function', type=str, default="", help='the name of working function')

    # parameter parsing
    args = parser.parse_args(args=sys.argv[1:])
    if args.targetPath is None or len(args.targetPath)==0:
        parser.print_help()
        exit(1)

    if args.function is None or len(args.function)==0:
        parser.print_help()
        exit(1)

    if args.exceptions is None or len(args.exceptions)==0:
        args.exceptions = None
    else:
        args.exceptions = args.exceptions.split(",")
    return args

################################################
################################################
################################################

if __name__ == "__main__":
    args = parse_arg()
    print("###### %s ########" % args.function)
    print("Work with %s" % args.targetPath)
    obj = ResultColllector()
    if args.pattern is None:
        getattr(obj, args.function)(args.targetPath, args.outputName, args.exceptions)
    else:
        getattr(obj, args.function)(args.targetPath, args.pattern, args.outputName, args.exceptions)
