
####################################
#
####################################
load_multi_data<-function(basepath, phaseText=NULL, cycleFilter=NULL){
    filelist <- list.dirs(basepath, full.names=FALSE, recursive=FALSE)
    if (length(filelist)==0) stop(sprintf("cannot find files in %s", basepath))
    result <-data.frame()
    for (runName in filelist){
        runID<- as.integer(substr(runName, 4,5))
        if (is.numeric(phaseText)==FALSE){
            datafile <- sprintf("%s/%s/_fitness/fitness_%s.csv", basepath, runName, phaseText)
            data <- read.csv(datafile, header=TRUE, sep = ",", row.names=NULL)
        }else{
            datafile <- sprintf("%s/%s/_fitness/fitness_phase%d.csv", basepath, runName, phaseText)
            data <- read.csv(datafile, header=TRUE, sep = ",", row.names=NULL)
            data$Cycle <- data$Iteration
            data <- data[data$Rank==0,]
        }
        # cat(sprintf("loaded %s..\n",datafile))
        if (is.null(cycleFilter)==FALSE){
            data<-data[data$Cycle==cycleFilter,]
        }
        result<-rbind(result, data.frame(Run=runID, data))
    }
    return (result)
}

####################################
#
####################################
change_factor_names<- function(listitems, from, to){
    listitems<-as.character(listitems)
    for (idx in c(1:length(from))){
        listitems<-ifelse(listitems==from[idx], to[idx], listitems)
    }
    listitems <- factor(listitems, levels = to)
    return (listitems)
}


####################################
#
####################################
meanfilter<-function(x){
    t=data.frame(x)
    x<-c(t[is.nan(t$x)==FALSE,])
    if (length(x)==0) return (NaN)
    return (mean(x))
}



############################################################
# selecting data points
############################################################
selecting_data <- function(subj, subjData, removeDM=FALSE){
    # selecting initial point
    subjData<-subData
    initial <- subjData[(subjData$Cycle==0 & subjData$Run==1),]
    
    # select last cycle
    miniSet <- subjData[subjData$Cycle==max(subjData$Cycle),]
    if (removeDM==TRUE){
        prevCnt <- nrow(miniSet)
        miniSet <- miniSet[miniSet$DM==0,]
        if (nrow(miniSet)==0){
            print(sprintf("The subject %s has no point that is not missing deadline.", subj))
            md <- data.frame(Type="Initial", initial)
            # md <- rbind(md, data.frame(Type=c("Extreme1", "Knee", "Extreme2"), Run=c(NA,NA,NA), Cycle=c(NA,NA,NA),
            #                           SolutionID=c(NA,NA,NA),FD=c(NA,NA,NA),FC=c(NA,NA,NA),DM=c(NA,NA,NA)))
            md <- rbind(md , data.frame(Type=c("Extreme1", "Knee", "Extreme2"),
                                        Subject=initial$Subject[[1]],
                                        Run=NA, Cycle=NA, SolutionID=NA,FD=NA,FC=NA,DM=NA))
            return (md)
        } else{
            nDM<-prevCnt-nrow(miniSet)
            print(sprintf("The subject %s has %d deadline missed points (%.4f%%).",subj, nDM, nDM/prevCnt*100))
        }
    }
    
    # points normalization
    minV<-min(miniSet$FD)
    maxV<-max(miniSet$FD)
    miniSet$nFD <- (miniSet$FD-minV)/(maxV-minV)
    miniSet$nFD <- ifelse(is.nan(miniSet$nFD)==TRUE, 0, miniSet$nFD)
    # need to invert FC values, So I timed -1
    minV<-min(-miniSet$FC)
    maxV<-max(-miniSet$FC)
    miniSet$nFC <- (-miniSet$FC-minV)/(maxV-minV)
    miniSet$nFC <- ifelse(is.nan(miniSet$nFC)==TRUE, 0, miniSet$nFC)
    miniSet$dist <- sqrt(miniSet$nFC**2 + miniSet$nFD**2)
    
    
    
    # select knee point
    # mv <- min(normalized$dist)
    # normalized$type <- ifelse(normalized$dist==mv, "Knee", "Norm")
    knee<- miniSet[miniSet$dist==min(miniSet$dist),]
    knee<- knee[1,]
    
    # select extreme up (extreme minimum FD and maximum FC among non-dominated points)
    # FC is supposed to be min_FC, but to eliminate dominated points I used max FC)
    a <- miniSet[miniSet$nFD==min(miniSet$nFD),]
    a <- a[a$nFC==min(a$nFC),]
    a <- a[1,]
    
    # select extreme down  (extreme maximum FD and minimum FC among non-dominated points)
    # FD is supposed to be min_FD, but to eliminate dominated points I used max FD)
    b <- miniSet[miniSet$nFC==min(miniSet$nFC),]
    b <- b[b$nFD==min(b$nFD),]
    b <- b[1,]
    
    # test display
    # ddd<-rbind(a,b,knee)
    # ggplot(data=miniSet, aes(x=nFD, y=nFC)) + geom_point() + xlim(0,1) + ylim(0,1)
    # ggplot(data=ddd, aes(x=nFD, y=nFC)) + geom_point() + xlim(0,1) + ylim(0,1)
    # ggplot(data=md, aes(x=FD, y=-FC)) + geom_point() + ylim(-max(miniSet$FC),-min(miniSet$FC))
    # ggplot(data=miniSet, aes(x=FD, y=-FC)) + geom_point()
    md <- data.frame(Type="Initial", initial)
    md <- rbind(md, data.frame(Type="Extreme1", a[1:7]))
    md <- rbind(md, data.frame(Type="Knee", knee[1:7]))
    md <- rbind(md, data.frame(Type="Extreme2", b[1:7]))
    return (md)
}
