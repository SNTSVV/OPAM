PROJECT_PATH <- "~/projects/OPAM_pub"
CODE_PATH <- sprintf("%s/scripts/R", PROJECT_PATH)
setwd(CODE_PATH)
suppressMessages(library(gridExtra))
source("libs/lib_statistics.R")
source("libs/lib_latex.R")
setwd(PROJECT_PATH)


#################################################################
# RQ1 :: comparing two approaches in last pareto front
##################################################################
# RQ1 experiments ( How many cycle do we need)  only last Graph drawing
RESULT_PATH <-sprintf('%s/results/RQ1-Ext', PROJECT_PATH)
QIs       <- c("HV", "GDP", "CSunique", "GS") # "GD", "CS")
QIs_NAMES <- c("HV", "GD+", "C", "Spread")    # "GD", "CS" == C

SUBJECTS <- c('ICS', 'CCS', 'UAV', 'GAP',  'HPSS', 'ESAIL')
SUBJ_NAMES <- c('ICS', 'CCS', 'UAV', 'GAP',  'HPSS', 'ESAIL')
APPRS<-c("RS", "OPAM")
SELECTED_TEST_NUM<- NULL

# Set for another external fitness (default: null, all points will be considered)
EXT_TYPES <- c("Adaptive10", "Worst10", "Random10", "Adaptive500", "Worst500", "Random500")
SELECTED_TEST_NUM<- 1      # Set for another external fitness

outputFile <- sprintf("%s/QI-table.tex", RESULT_PATH)

########################################################################
########################################################################
########################################################################
# load data
data <- data.frame()
for(extType in EXT_TYPES){
    optionname <-"QI"
    if (is.null(extType)==FALSE || extType!="") optionname <- sprintf("%s_%s", optionname, extType)
    
    # load QI data
    filename <- sprintf("%s/fitness_%s.csv",RESULT_PATH, optionname)
    item <- read.csv(filename, header=TRUE, sep = ",", row.names=NULL)
    if("TestID" %in% colnames(item)){
        if (is.null(SELECTED_TEST_NUM)==FALSE) item <- item[item$TestID==SELECTED_TEST_NUM,]
    }else{
        item <- data.frame(TestID=1, item)
    }
    data <- rbind(data, data.frame(Exp=extType, item))
}

# calculate statistical data
st <- data.frame()
for(extType in EXT_TYPES){
    for(measure in QIs){
        for (subject in SUBJECTS){
            sub <- data[data$Exp==extType & data$Subject==subject,]
            compList<-list()
            for (appr in APPRS){  # aID in 1:length(APPRS)
                compList[[appr]] <- sub[sub$Approach==appr,][[measure]]
            }
            # compare OPAM and the other
            ret <- getStatisticalTest(compList[[APPRS[2]]], compList[[APPRS[1]]], FALSE)
            st <- rbind(st, data.frame(Exp=extType, Measure=measure, Subject=subject, valueType="p-value", value=ret[[2]]))
            st <- rbind(st, data.frame(Exp=extType, Measure=measure, Subject=subject, valueType="A12", value=ret[[3]]))
        }
    }
}

# calculate mean data
# st <- data.frame()
for(extType in EXT_TYPES){
    for(measure in QIs){
        for (subject in SUBJECTS){
            sub <- data[data$Exp==extType & data$Subject==subject,]
            for (appr in APPRS){
                compdata <- sub[sub$Approach==appr,][[measure]]
                st <- rbind(st, data.frame(Exp=extType, Measure=measure, Subject=subject, valueType=appr, value=mean(compdata)))
            }
        }
    }
}

# Table type 1
# sink(outputFile)
for(extType in EXT_TYPES){
    cat("\\midrule \n")
    cat(sprintf("\\multirow{15}{*}{\\rotatebox{90}{$\\mathbf{T_{%s}}$}}\n",extType))
    for(measureID in c(1:length(QIs))){
        cat(getline(st[st$Exp==extType & st$Measure == QIs[measureID],], QIs_NAMES[measureID], APPRS[[2]], first=TRUE, groupSize=3, stress="normal"))
        cat(getline(st[st$Exp==extType & st$Measure == QIs[measureID],], QIs_NAMES[measureID], APPRS[[1]], first=FALSE, groupSize=3, stress="reverse"))
        cat(getline_st(st[st$Exp==extType & st$Measure == QIs[measureID],], QIs_NAMES[measureID], first=FALSE, groupSize=3))
        # cat(getline(st[st$Exp==extType & st$Measure == QIs[measureID],], QIs_NAMES[measureID], "A12", first=FALSE))
        if (measureID!=length(QIs))
            cat("\t\t\\addlinespace \n")
    }
}
# sink()

