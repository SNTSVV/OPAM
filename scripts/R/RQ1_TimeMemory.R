PROJECT_PATH <- "~/projects/OPAM_pub"
CODE_PATH <- sprintf("%s/scripts/R", PROJECT_PATH)
setwd(CODE_PATH)
source("libs/lib_draw.R")
setwd(PROJECT_PATH)

###########################################################################
RESULT_PATH <-sprintf('%s/results/RQ1', PROJECT_PATH)
SUBJECTS <- c("ICS", "CCS", "UAV", "GAP", "HPSS", "ESAIL")
APPRS <- c("RS", "OPAM")
# APPRS <- c("NSGA", "OPAM")

OUTPUT_PATH<-sprintf("%s/RQ1_timeinfo", RESULT_PATH)
if(!dir.exists(OUTPUT_PATH)){ dir.create(OUTPUT_PATH, recursive = TRUE) }

# Setting variables (OPAM)
appr<-APPRS[2]
for (appr in APPRS){
    data<-read.csv(sprintf("%s/timeinfo_%s.csv", RESULT_PATH, appr), header = TRUE)
    # data<-data[data$Variable!="ESAIL",]
    data$Variable<-factor(data$Variable, levels=SUBJECTS)
    VarTitle <- TeX(sprintf("%s", appr))
    
    
    #collect time and memory information
    all <- data.frame()
    {
        # calculate average values
        agTime <- aggregate(Total ~ Variable, data = data, mean)
        all <- agTime
        agHeap <- aggregate(UsedHeap ~ Variable, data = data, mean)
        all <- cbind(all, Memory=agHeap$UsedHeap)
    }
        
    # Drawing graphs
    {
        # showring execution time with box-plots
        font_size <- 25
        g<- draw_descrete_box_plot(data$Variable, as.double(data$Total/3600.0), VarTitle, "Execution time (h)", font_size=font_size)
        filename <- sprintf("%s/rq1-boxplot-time-%s.pdf",OUTPUT_PATH, appr)
        ggsave(filename, g, width = 8, height = 4)
        
        # Showing memory usages with box-plots
        g<- draw_descrete_box_plot(data$Variable, data$UsedHeap, VarTitle, "Memory usage (MB)", font_size = font_size)
        filename <- sprintf("%s/rq1-boxplot-memory-%s.pdf",OUTPUT_PATH, appr)
        ggsave(filename, g, width = 8, height = 4)
    
        # Showing execution time on average
        g<- draw_bar_plot(agTime$Variable, as.double(agTime$Total/3600.0), appr,"Total ExecTime (avg) (h)", fontSize = font_size)
        filename <- sprintf("%s/rq1-boxplot-avgTime-%s.pdf",OUTPUT_PATH, appr)
        ggsave(filename, g, width = 8, height = 4)
        
        # Showing execution time on average
        g<- draw_bar_plot(agHeap$Variable, agHeap$UsedHeap, appr, "Memory usage on average (MB)", fontSize = font_size)
        filename <- sprintf("%s/rq1-boxplot-avgMemory-%s.pdf",OUTPUT_PATH, appr)
        ggsave(filename, g, width = 8, height = 6)
    }
    
    ##########################################
    ### create latex table
    {
        cat(sprintf("Approach: %s\n", appr))
        cat("Subject & Time (s) & Memory (MB) \\ \n")
        for(name in all$Variable){
            cat(sprintf("%s",name))
            item <- all[all$Variable==name,]
            cat(sprintf(" & %.2f",as.double(item$Total)))
            cat(sprintf(" & %.2f",item$Memory))
            cat("\\\\ \n")
        }
    }
    
}
