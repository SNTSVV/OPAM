PROJECT_PATH <- "~/projects/OPAM_pub"
CODE_PATH <- sprintf("%s/scripts/R", PROJECT_PATH)
setwd(CODE_PATH)
suppressMessages(library(gridExtra))
source("libs/lib_statistics.R")
source("libs/lib_draw.R")
setwd(PROJECT_PATH)

#################################################################
# RQ1 :: comparing two approaches in last pareto front
##################################################################
# QIs       <- c("HV", "GS", "GDP", "UNFR", "CI", "SP", "CSunique") # "GD", "CS")
# QIs_NAMES <- c("HV", "GS", "GD+", "UNFR", "CI", "SP", "C")    # "GD", "CS" == C
QIs       <- c("HV", "GDP", "CSunique", "GS") # "GD", "CS")
QIs_NAMES <- c("HV", "GD+", "C", "Spread")    # "GD", "CS" == C
SUBJECTS <- c('ICS', 'CCS', 'UAV', 'GAP',  'HPSS', 'ESAIL')
SUBJOUTS <- c('ICS', 'CCS', 'UAV', 'GAP',  'HPSS', 'ESAIL')
APPRS<-c("RS", "OPAM")


RESULT_PATH <-sprintf('%s/results/RQ1', PROJECT_PATH)
EXT_TYPE <- NULL      # set this variable for another external fitness (default: null)
SELECTED_TEST_NUM<- NULL    # Set for Ext results

# When you want to generate the results for Ext (external E')
RESULT_PATH <-sprintf('%s/results/RQ1-Ext', PROJECT_PATH)
# EXT_TYPE <- "Adaptive10"      # set this variable for another external fitness (default: null)
# EXT_TYPE <- "Worst10"      # set this variable for another external fitness (default: null)
# EXT_TYPE <- "Random10"      # set this variable for another external fitness (default: null)
EXT_TYPE <- "Adaptive500"      # set this variable for another external fitness (default: null)
# EXT_TYPE <- "Random500"      # set this variable for another external fitness (default: null)
# EXT_TYPE <- "Worst500"      # set this variable for another external fitness (default: null)
# for the Adaptive10, Worst10, and Random10, they requires to select a test ID
# SELECTED_TEST_NUM<- 1       # Set for Ext results

########################################################################
########################################################################
########################################################################
{
    optionname <-"QI"
    if (is.null(EXT_TYPE)==FALSE) optionname <- sprintf("%s_%s", optionname, EXT_TYPE)
    
    # load QI data
    filename <- sprintf("%s/fitness_%s.csv",RESULT_PATH, optionname)
    data <- read.csv(filename, header=TRUE, sep = ",", row.names=NULL)
    
    # Generate output path if it needs
    dpath <- sprintf("%s/RQ1_%s", RESULT_PATH, optionname)
    if (is.null(SELECTED_TEST_NUM)==FALSE) dpath <- sprintf("%s_Set%02d", dpath, SELECTED_TEST_NUM)
    if(!dir.exists(dpath)){ dir.create(dpath, recursive = TRUE) }

    

    # RQ1: one graph! for all data
    measure<-"GD"
    for(measureID in c(1:length(QIs))){
        measure <- QIs[measureID]
        print(sprintf("Working for %s", measure))

        # create data set
        {
            gData <- data.frame()
            dt <-data.frame()
            subID<-6
            for (subID in c(1:length(SUBJECTS))){
                subjName <- SUBJECTS[[subID]]

                # convert data for each subject
                sub <- data[data$Subject==subjName,]
                if (is.null(SELECTED_TEST_NUM)==FALSE) sub <- sub[sub$TestID==SELECTED_TEST_NUM,]
                sub <- data.frame(Subject=SUBJOUTS[subID], Approach=sub$Approach, Measure=measure, Run=sub$Run, Value=sub[[measure]])
                gData <- rbind(gData, sub)

                compList<-list()
                for (appr in APPRS){  # aID in 1:length(APPRS)
                    compList[[appr]] <- sub[sub$Approach==appr & sub$Measure==measure,]$Value
                }
                # compare OPAM and the other
                st <- getStatisticalTest(compList[[APPRS[2]]], compList[[APPRS[1]]], FALSE)
                dt <- rbind(dt, data.frame(Type="p-value", Subject=SUBJOUTS[subID], Value=st[[2]], Pos=2))
                dt <- rbind(dt, data.frame(Type="A12", Subject=SUBJOUTS[subID], Value=st[[3]], Pos=1))
            }

            gData$Approach <- factor(gData$Approach, levels = APPRS)
            gData$Subject <- factor(gData$Subject, levels = SUBJOUTS)
            dt$Subject <- factor(dt$Subject, levels=SUBJOUTS)
        }
        
        # draw data graph
        g<-QI_graph_horizen(gData, legend="top", yLimit=c(0.0, 1.0), colorSet=c("#AAAAAA", "#000000"))
        

        # draw for the statistical information
        {
            font_size<-4
            g2 <- ggplot(dt) +
              geom_text(aes(x=as.factor(Subject), y=Pos, label=sprintf("%.2f",Value)), color="blue", size=font_size)+
              theme_classic()+
              theme(axis.text=element_blank(),
                    axis.title=element_blank(),
                    axis.line=element_blank(),
                    axis.ticks=element_blank(),
                    plot.margin = margin(0, 0, 0, 30, "pt"),
                    plot.background = element_rect()) +
              geom_text(x=0.6, y=2, hjust=1, size=font_size, label=TeX("p-value"), color="blue")+
              geom_text(x=0.6, y=1,  hjust=1, size=font_size, label=TeX("\\hat{A}_{12}"), color="blue")+
              coord_cartesian(xlim=c(1,length(SUBJECTS)), ylim = c(0.5, 2.5), clip = "off")
        }

        # output both graphs
        to_print<-grid.arrange(g, g2, nrow=2, heights = c(4, 1))
        filename <- sprintf("%s/rq1-%s.pdf",dpath, QIs_NAMES[measureID])
        ggsave(filename, to_print, width=4, height=2.5)
    }
}