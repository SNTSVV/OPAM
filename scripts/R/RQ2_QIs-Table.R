PROJECT_PATH <- "~/projects/OPAM_pub"
CODE_PATH <- sprintf("%s/scripts/R", PROJECT_PATH)
setwd(CODE_PATH)
suppressMessages(library(gridExtra))
source("libs/lib_statistics.R")
source("libs/lib_latex.R")
setwd(PROJECT_PATH)


#################################################################
# RQ2 :: comparing OPAM and SEQ approaches
##################################################################
{
    # RQ1 experiments ( How many cycle do we need)  only last Graph drawing
    RESULT_PATH <-sprintf('%s/results/RQ2-Ext', PROJECT_PATH)
    QIs       <- c("HV", "GDP", "CSunique", "GS") # "GD", "CS")
    QIs_NAMES <- c("HV", "GD+", "C", "Spread")    # "GD", "CS" == C
    
    SUBJECTS <- c('ICS', 'CCS', 'UAV', 'GAP',  'HPSS', 'ESAIL')
    SUBJ_NAMES <- c('ICS', 'CCS', 'UAV', 'GAP',  'HPSS', 'ESAIL')
    APPRS<-c("SEQ", "OPAM")
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
        print(filename)
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
}

# Table type 1
{
    sink(outputFile)
    cat("\\begin{table}[p]\n")
    cat("\\color{rev}\n")
    cat("\t\\caption{\\color{rev}Comparing OPAM and SEQ using the four quality indicators: HV, GD+, C, and $\\Delta$. Average quality values computed based on 50 runs of OPAM and SEQ using the different sets of task-arrival sequences (see Section~\\ref{subsec:design}).}\n")
    cat("\t%\\footnotesize\n")
    cat("\t\\vspace{-1.2em}\n")
    cat("\t\\fontsize{8}{8}\\selectfont\n")
    cat("\t\\def\\arraystretch{0.2}%  1 is the default, change whatever you need\n")
    cat("\\begin{center}\n")
    cat("\\begin{tabularx}{\\columnwidth}{m{1.1em}@{}c@{\\hspace{0.3em}}r@{\\hspace{1em}} RRRRRR}\n")
    cat("\t\t\\toprule\n")
    cat("\t\t\\addlinespace[0.2em]\n")
    cat("\t\t\\multicolumn{3}{c}{} & \\multicolumn{1}{c}{\\textbf{ICS}} & \\multicolumn{1}{c}{\\textbf{CCS}}\n")
    cat("\t\t & \\multicolumn{1}{c}{\\textbf{UAV}} & \\multicolumn{1}{c}{\\textbf{GAP}} & \\multicolumn{1}{c}{\\textbf{HPSS}} & \\multicolumn{1}{c}{\\textbf{ESAIL}} \\\\\n")
    cat("\t\t\\addlinespace[0.2em]\n")
    for(extType in EXT_TYPES){
        cat("\\midrule \n")
        ret <- regexpr("[0-9]+", extType)
        num<-regmatches(extType, ret)
        ret <- regexpr("[a-zA-Z]+", extType)
        name<-tolower(regmatches(extType, ret))
        code <- substr(name, 1, 1)
        cat(sprintf("\\multirow{15}{*}{\\rotatebox{90}{$\\mathbf{T}^{%s}_{%s}$ (%s, size %s)}}\n",num, code, name, num))
        for(measureID in c(1:length(QIs))){
            cat(getline(st[st$Exp==extType & st$Measure == QIs[measureID],], QIs_NAMES[measureID], APPRS[[2]], first=TRUE, groupSize=3, stress="normal"))
            cat(getline(st[st$Exp==extType & st$Measure == QIs[measureID],], QIs_NAMES[measureID], APPRS[[1]], first=FALSE, groupSize=3, stress="reverse"))
            cat(getline_st(st[st$Exp==extType & st$Measure == QIs[measureID],], QIs_NAMES[measureID], first=FALSE, groupSize=3))
            # cat(getline(st[st$Exp==extType & st$Measure == QIs[measureID],], QIs_NAMES[measureID], "A12", first=FALSE))
            if (measureID!=length(QIs))
                cat("\t\t\\addlinespace[0.2em]\n")
        }
    }
    cat("\t\t\\bottomrule\n")
    cat("\t\t\\addlinespace[0.2em]\n")
    cat("\t\t\\multicolumn{9}{l}{\\parbox[t]{0.95\\linewidth}{\n")
    cat("\t\t\\colorbox{blue!30}{\\textbf{n.nnnn}}: OPAM outperforms SEQ \\quad\\quad \\colorbox{gray!20}{\\textbf{n.nnnn}}: SEQ outperforms OPAM}} \\\\\n")
    cat("\\end{tabularx}\n")
    cat("\\end{center}\n")
    cat("\\label{tbl:rq2QI-SEQ}\n")
    cat("\\end{table}\n")
    sink()
}

