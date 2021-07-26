PROJECT_PATH <- "~/projects/OPAM_pub"
CODE_PATH <- sprintf("%s/scripts/R", PROJECT_PATH)
setwd(CODE_PATH)
source("libs/lib_draw.R")
setwd(PROJECT_PATH)

###########################################################################
# settings
RESULT_PATH <-sprintf('%s/results/RQ3', PROJECT_PATH)
# Setting variables
{
    EXPNUM <- c(1,2,3,4)
    VarTitle <- c(
        TeX("Number of tasks ($n$)"),
        TeX("Ratio of aperiodic tasks ($\\gamma$)"),
        TeX("Range factor ($\\mu$)"),
        TeX("Simulation time ($T$)")
    )
    EXPPATH <- c(
        '1_nTasks',
        '2_ratioAperiodic',
        '3_maxArrivalRange',
        '4_simTime'
    )
    breaksList <- list(
        NULL,
        NULL,
        c(2,4,6,8,10),
        c(2000,4000,6000,8000,10000)
    )
}


#OUTPUT time and memory graph
ExpIdx<-2
OUTPUT_TO_FILE <- TRUE
OUTPUT_PATH <- RESULT_PATH
if(!dir.exists(OUTPUT_PATH)){ dir.create(OUTPUT_PATH, recursive = TRUE) } 

for(ExpIdx in c(1:length(EXPNUM))){
    #load data
    print(sprintf("Working for %d from %s ..", EXPNUM[[ExpIdx]], EXPPATH[[ExpIdx]]))
    data<-read.csv(sprintf("%s/%s/timeinfo.csv", RESULT_PATH, EXPPATH[[ExpIdx]]), header = TRUE)

    ### time
    p <- draw_descrete_box_plot(data$Variable, data$Total, VarTitle[[ExpIdx]], "Execution time (s)", breaksList[[ExpIdx]])
    print(p)
    if (OUTPUT_TO_FILE){
        filename <- sprintf("%s/rq3-time-Exp%d.pdf",OUTPUT_PATH, EXPNUM[[ExpIdx]])
        ggsave(filename, plot=p, width=4, height=3)
    }
    
    # Memory
    p <- draw_descrete_box_plot(data$Variable, data$UsedHeap, VarTitle[[ExpIdx]], "Memory usage (MB)", breaksList[[ExpIdx]])
    print(p)
    if (OUTPUT_TO_FILE){
        filename <- sprintf("%s/rq3-memory-Exp%d.pdf",OUTPUT_PATH, EXPNUM[[ExpIdx]])
        ggsave(filename, plot=p, width=4, height=3)
    }
}