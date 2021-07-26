PROJECT_PATH <- "~/projects/OPAM_pub"
CODE_PATH <- sprintf("%s/scripts/R", PROJECT_PATH)
setwd(CODE_PATH)
source("libs/lib_statistics.R")
source("libs/lib_draw.R")
setwd(PROJECT_PATH)

###########################################################################
{
    RESULT_PATH <-sprintf('%s/results/RQ2-Ext', PROJECT_PATH)
    SUBJECTS <- c('ICS', 'CCS', 'UAV', 'GAP',  'HPSS', 'ESAIL')
    APPRS <- c("SEQ", "OPAM")
    SELECTED_EXT_NUM<- NULL   # Set for another external fitness (default: null, all points will be drawn)

    # for the experiment "ext"
    cycleNum <- NULL
    # EXT_TYPE <- "Adaptive10"      # set this variable for another external fitness (default: null)
    # EXT_TYPE <- "Worst10"      # set this variable for another external fitness (default: null)
    # EXT_TYPE <- "Random10"      # set this variable for another external fitness (default: null)
    EXT_TYPE <- "Adaptive500"      # set this variable for another external fitness (default: null)
    # EXT_TYPE <- "Worst500"      # set this variable for another external fitness (default: null)
    # EXT_TYPE <- "Random500"      # set this variable for another external fitness (default: null)
    # EXT_TYPE <- "InAdaptive10"      # set this variable for another external fitness (default: null)
    # EXT_TYPE <- "InWorst10"      # set this variable for another external fitness (default: null)


    # Graph range for paper (- safety margin)
    # set the graph range for x-axis to generate graphs for paper (for each subject)
    xlim <- list(NULL, NULL, NULL, NULL, NULL, NULL)
    xlabs <- list(NULL, NULL, NULL, NULL, NULL, NULL)
    xlabs <- list( c(-9.7575, -9.7580, -9.7585),
                  c(-21.265, -21.275, -21.285),
                  NULL,
                  # c(-1727.5, -1730.0, -1732.5),
                  c(-1646.0, -1646.5, -1647.0, -1647.5),
                  c(-67.3, -67.4, -67.5, -67.6 ),
                  c(-3180.47, -3180.49))
    xbreaks <- xlabs

    ylim<- list(c(-12, 6), c(-27, 6),  c(-54, 10),  c(-148, 36), c(-243, 45), c(-110, 15))

    cat("\n==Settings==============================\n")
    cat(sprintf("RESULT_PATH: %s\n", RESULT_PATH))
    cat(sprintf("EXT_TYPE   : %s\n", EXT_TYPE))
    cat(sprintf("SELECTED_EXT_NUM: %d\n", SELECTED_EXT_NUM))
    cat(sprintf("xlim: %s\n",  paste( unlist(xlim), collapse='')))
    cat("=====================================\n")
    ################################################################################
    ################################################################################
    ################################################################################
    ##### Load data points from fitness file (only contains initial and last)
    data<-data.frame()
    for(appr in APPRS){
        filename <- sprintf("%s/fitness_%s%s.csv",RESULT_PATH, appr, ifelse(is.null(EXT_TYPE), "", sprintf("_%s",EXT_TYPE)))
        items <- read.csv(filename, header=TRUE, sep = ",", row.names=NULL)
        items <- data.frame(Approach=appr, items)
        data <- rbind(data, items)
        cat(sprintf("loaded data from %s\n", filename))
    }

    ##### Generate target output path
    ##### the path is decieded by the variable EXT_TYPE and SELECTED_EXT_NUM
    dpath <- sprintf("%s/RQ2_scatter%s",RESULT_PATH, ifelse(is.null(EXT_TYPE), "", sprintf("_%s",EXT_TYPE)))
    if (is.null(SELECTED_EXT_NUM)==FALSE) dpath <- sprintf("%s_Set%02d", dpath, SELECTED_EXT_NUM)
    if(!dir.exists(dpath)){ dir.create(dpath, recursive = TRUE) }
    cat(sprintf("output path: %s\n", dpath))

    ##### drawing graphs
    subID<-2
    for (subID in c(1:length(SUBJECTS))){
        subj <- SUBJECTS[[subID]]
        cat(sprintf("[%s] Drawring ...", subj))


        points <- data[data$Variable==subj,]
        if (is.null(cycleNum)==FALSE) points <- points[points$Cycle==cycleNum,]
        if (is.null(SELECTED_EXT_NUM)==FALSE) points <- points[points$TestID==SELECTED_EXT_NUM,]
        points <- data.frame(Approach=points$Approach, Schedulability=points$Schedulability, Satisfaction=points$Satisfaction)
        points <- unique(points)
        points$Approach <- factor(points$Approach, levels=APPRS)
        g<-draw_scatter_simple(points, xlimit=xlim[[subID]], ylimit=ylim[[subID]], x_scale=NULL,
                               x_labels=xlabs[[subID]],x_breaks=xbreaks[[subID]],
                               colorSet = c("darkgray", "blue"),
                               sizeSet=c(2,2),
                               alphaLevel = c(1, 0.5),
                               shapeList=c(4,17),
                               legend="rt", font_size=23)
        filename <- sprintf("%s/rq2-scatter-%s.pdf",dpath, SUBJECTS[[subID]])
        ggsave(filename, g, width = 6, height = 4)
        cat("Done.\n")
    }
}
