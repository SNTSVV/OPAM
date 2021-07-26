PROJECT_PATH <- "~/projects/OPAM_pub"
CODE_PATH <- sprintf("%s/scripts/R", PROJECT_PATH)
setwd(CODE_PATH)
suppressMessages(library(gridExtra))
source("libs/lib_data.R")
source("libs/lib_draw.R")
setwd(PROJECT_PATH)


################################################
# Common settings
RESULT_PATH <-sprintf('%s/results/RQ1', PROJECT_PATH) 
SUBJECTS <- c('ICS', 'CCS', 'UAV', 'GAP',  'HPSS', 'ESAIL')
cycleNum <- 1000
APPRS<-c("OPAM")


dpath <- sprintf("%s/results/RQ4/RQ4_scatter",PROJECT_PATH)
if(!dir.exists(dpath)){ dir.create(dpath, recursive = TRUE) } 

##### Load data points from fitness file (only contains initial and last)
data <- data.frame()
data <- read.csv(sprintf("%s/fitness_%s.csv",RESULT_PATH, APPRS[[1]]), header=TRUE, sep = ",", row.names=NULL)
data <- data.frame(Subject=data$Variable, 
                    Run=data$Run, 
                    Cycle=data$Cycle, 
                    SolutionID=data$SolutionID,
                    FD=data$Schedulability, 
                    FC=data$Satisfaction, 
                    DM=data$DeadlineMiss)

###########################################################################
# RQ4 scatter plot OPAM and Initial point (all)
###########################################################################
# xlim<- list(c(-10.794, -10.812), c(-23.9, -23.97),  c(-835, -838), c(-1665, -1673), c(-67, -78), c(-3180, -3330)) # for paper (reverse)
# xlim<- list(c(-10.794, -10.812), c(-23.9, -23.97),  c(-835, -838), c(-1665, -1673), c(-67, -78), c(-3180, -3330)) # for paper (reverse)
xlim <- list(NULL, NULL, NULL, NULL, NULL, NULL)
ylim<- list(c(-12, 6), c(-27, 6),  c(-54, 10),  c(-148, 36), c(-243, 45), c(-110, 15))   #

xlabs <- list(NULL, NULL, NULL, NULL, NULL, NULL)
xlabs <- list(NULL, #c(-11.27, -11.275, -11.28),
              NULL, #c(-25.09, -25.10, -25.11, -25.12),
              NULL,
              c(-1726, -1727, -1728, -1729, -1730),
              c(-70, -72, -74, -76),
              NULL)
xbreaks <- xlabs #list(NULL, NULL, NULL, NULL, NULL, NULL)


subID<-2
for (subID in c(1:length(SUBJECTS))){
    subj <- SUBJECTS[subID]
    cat(sprintf("[%s] Drawring ...", subj))
    
    subData <- data[data$Subject==SUBJECTS[[subID]],]
    points <- subData[subData$Cycle==max(subData$Cycle),]
    if (nrow(points)!=0){
        points <- data.frame(Approach=APPRS[[1]], Schedulability=points$FD, Satisfaction=points$FC)
        points <- unique(points)
    }
    
    ips <- subData[subData$Cycle==0,]
    ips <- unique(data.frame(Approach="Initial", Schedulability=ips$FD, Satisfaction=ips$FC))
    points <- rbind(points, ips)
    points$Approach <- as.factor(points$Approach)

    filename <- sprintf("%s/rq4-scatter-%s.pdf",dpath, subj)
    legend_location <- ifelse(subID==6, "rt", "rb")
    g<-draw_scatter_simple(points, xlimit=xlim[[subID]], ylimit=ylim[[subID]], x_scale=NULL, # xPos=-0.1,
                           x_labels=xlabs[[subID]], x_breaks=xbreaks[[subID]],
                           colorSet = c("red", "blue"),   
                           sizeSet  = c(2, 2),
                           alphaLevel = c(1, 0.5),
                           shapeList  = c(16, 17),
                           legend=legend_location, font_size=18)

    ggsave(filename, g, width = 6, height = 4)
    cat("Done.\n")
}
# }


###########################################################################
# RQ4 scatter plot OPAM and Initial point (Separated DM and non-DM points)
###########################################################################
dpath <- sprintf("%s/results/RQ4/RQ4_scatter_DM",PROJECT_PATH)
if(!dir.exists(dpath)){ dir.create(dpath, recursive = TRUE) } 

for (subID in c(1:length(SUBJECTS))){
    subj <- SUBJECTS[subID]
    cat(sprintf("[%s] Drawring...", subj))
    
    points <- data[data$Subject==SUBJECTS[[subID]],]
    ndm <- points[points$DM==0 & points$Cycle==max(points$Cycle),]
    if (nrow(ndm)!=0){
        ndm <- unique(data.frame(Approach=sprintf(APPRS[[1]]), Schedulability=ndm$FD, Satisfaction=ndm$FC))
    }
    dm <- points[points$DM!=0 & points$Cycle==max(points$Cycle),]
    if (nrow(dm)!=0){
        dm <- unique(data.frame(Approach="Missed", Schedulability=dm$FD, Satisfaction=dm$FC))
    }
    
    ips <- data[data$Subject==SUBJECTS[[subID]],]
    ips <- ips[ips$SolutionID==1 & ips$Cycle==0 & ips$Run==1,]
    initColor<-ifelse(ips$DM==0,"black","#B30000")
    ips <- data.frame(Approach=ifelse(ips$DM==0,"Init","Init-Missed"), Schedulability=ips$FD, Satisfaction=ips$FC)
    points <- rbind(dm, ndm, ips)
    
    
    points$Approach <- as.factor(points$Approach)
    
    if (nrow(dm)>0 & nrow(ndm)>0){
        colors<-c(initColor, "red","green")
    }else if (nrow(dm)==0){
        colors<-c(initColor, "green")
    }else if (nrow(ndm)==0){
        colors<-c(initColor, "red")
    }
    
    # OUTPUT THE RESULT GRAPH
    g<-draw_scatter_simple(points, xlimit=xlim[[subID]], ylimit=ylim[[subID]], x_scale=NULL,
                                x_labels=xlabs[[subID]],x_breaks=xbreaks[[subID]],
                                colorSet = colors, 
                                sizeSet=c(4, 2, 2), 
                                alphaLevel = c(1,1,1), legend="rt", font_size=18)

    filename <- sprintf("%s/rq4-scatter-%s.pdf",dpath, subj)
    ggsave(filename, g, width=6, height=4)
    cat("Done.\n")
}
# }

