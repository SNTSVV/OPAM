suppressMessages(library(ggplot2))
suppressMessages(library(latex2exp))
suppressMessages(library(scales))    # hue_pal

####################################
# common Settings
####################################
# cbPalette <- c("#000000", "#AAAAAA","#F8766D", "#00BE67", "#C77CFF", "#00A9FF", "#ABA300")  #c("#000000", "#AAAAAA", "#009E73", "#D55E00", "#0072B2", "#999999", "#E69F00", "#56B4E9", "#009E73", "#F0E442",   "#CC79A7")#00BFC4
cbPalette <- c("#000000", "#AAAAAA","#F8766D", "#00BE67", "#C77CFF", "#00A9FF")  #c("#000000", "#AAAAAA", "#009E73", "#D55E00", "#0072B2", "#999999", "#E69F00", "#56B4E9", "#009E73", "#F0E442",   "#CC79A7")#00BFC4


####################################
#
####################################
generate_box_plot <- function(sample_points, x_col, y_col, type_col, x.title="", y.title="", nBox=20,
    title="", ylimit=NULL, colorList=NULL, legend="rb", limY=NULL,
    legend_direct="vertical", legend_font=15,  trans=NULL, avg=FALSE
){
    
    # Draw them for each
    avg_results<- aggregate(sample_points[[y_col]], list(a=sample_points[[x_col]], b=sample_points[[type_col]]), mean)
    colnames(avg_results) <- c(x_col, type_col, y_col)
    
    # change for drawing
    maxX <- max(sample_points[[x_col]])
    interval <- as.integer(maxX/nBox)
    samples <- sample_points[(sample_points[[x_col]]%%interval==0),]
    avgs <- avg_results[(avg_results[[x_col]]%%interval==0),]
    
    if(is.null(colorList)==TRUE){
        colorList <- cbPalette
    }
    fmt_dcimals <- function(digits=0){
        # return a function responpsible for formatting the
        # axis labels with a given number of decimals
        function(x) {
            if (x>10000){
                a <- sprintf("%e", round(x, digits))
            }
            else{
                a <- sprintf(sprintf("%%.%df", digits), round(x,digits))
            }
            return (a)
        }
    }
    g <- ggplot(data=samples, aes(x=as.factor(samples[[x_col]]), y=samples[[y_col]], color=as.factor(samples[[type_col]]))) +  #, linetype=as.factor(Type)
        stat_boxplot(geom = "errorbar", width = 0.7, alpha=1, size=0.7) +
        stat_boxplot(geom = "boxplot", width = 0.7, alpha=1, size=0.7, outlier.shape=1, outlier.size=1) +
        # geom_line(data=avgs, aes(x=as.factor(avgs[[x_col]]), y=avgs[[y_col]], color=as.factor(samples[[type_col]])), size=1, alpha=1)+ #, group=as.factor(samples[[type_col]])
        theme_bw() +
        scale_colour_manual(values=colorList)+
        xlab(x.title) +
        ylab(y.title) +
        # scale_y_continuous()
        # scale_y_continuous(labels = fmt_dcimals(digits=2)) +
        theme(axis.text=element_text(size=legend_font), axis.title=element_text(size=15))#,face="bold"
    
    if (!is.null(trans)){
        g<- g+ scale_y_continuous(trans=trans)
    }
    
    if (is.null(limY)==FALSE){
        g<- g + ylim(limY[1], limY[2])
    }
    
    if (legend=="rb"){
        g<- g+ theme(legend.justification=c(1,0), legend.position=c(0.999, 0.001), legend.direction = legend_direct, legend.title=element_blank(), legend.text = element_text(size=legend_font), legend.background = element_rect(colour = "black", size=0.2))
    }else if (legend=="rt"){
        g<- g+ theme(legend.justification=c(1,1), legend.position=c(0.999, 0.999), legend.direction = legend_direct, legend.title=element_blank(), legend.text = element_text(size=legend_font), legend.background = element_rect(colour = "black", size=0.2))
    } else if (legend=="lt"){
        g<- g+ theme(legend.justification=c(0,1), legend.position=c(0.001, 0.999), legend.direction = legend_direct, legend.title=element_blank(), legend.text = element_text(size=legend_font), legend.background = element_rect(colour = "black", size=0.2))
    } else if (legend=="lb"){
        g<- g+ theme(legend.justification=c(0,0), legend.position=c(0.001, 0.001), legend.direction = legend_direct, legend.title=element_blank(), legend.text = element_text(size=legend_font), legend.background = element_rect(colour = "black", size=0.2))
    } else{
        g<- g+ theme(legend.position = "none")
    }
    
    if (!is.null(ylimit)){
        g <- g + ylim(ylimit[[1]], ylimit[[2]])
    }
    if (title!=""){
        g <- g + ggtitle(title)
    }
    return (g)
}

####################################
# For comparing the execution time and memory
####################################
draw_descrete_box_plot <- function(xdata, ydata, xTitle, yTitle, breaksList=NULL, drawLine=FALSE, font_size=14) {
    D <- data.frame(X=xdata, Y=ydata)
    g<-ggplot(D, aes(x=X, y=Y, group=X)) +
        geom_boxplot()+
        theme_bw()+
        xlab(xTitle)+ylab(yTitle)+
        theme(axis.text=element_text(size=font_size),#, face="bold"),
              axis.title=element_text(size=font_size),#, face="bold"),
              legend.position="none",
              plot.margin=margin(5, 5, 5, 5))
    if (is.null(breaksList)==FALSE){
        g <- g+ scale_x_continuous(breaks=breaksList)
    }
    
    if(drawLine == TRUE){
        P <- lm(Y~X, D)
        fx <- function(x){
            return (P$coefficients[2]*x + P$coefficients[1])
        }
        g <- g + stat_function(fun = fx, color="#AA0000")
    }
    return (g)
}

####################################
#
####################################
draw_scatter_simple<-function(
    dataset, xlimit=NULL, ylimit=NULL, x_scale=NULL, x_labels=NULL, x_breaks=NULL,
    colorSet=NULL, sizeSet=NULL, alphaLevel=NULL,shapeList=NULL,
    legend="rt", font_size=15, legend_direct="vertical"
){
    # get range
    if (is.null(xlimit)){
        xlimit <-  c(min(dataset$Schedulability), max(dataset$Schedulability))
    }
    if (is.null(ylimit)){
        ylimit <- c(min(dataset$Satisfaction), max(dataset$Satisfaction))
    }
    colorPalette <- c("black", "red", "blue")
    if (!is.null(colorSet)) colorPalette <- colorSet
    sizeLevel <- c(1,2,3,4)
    if (!is.null(sizeSet)) sizeLevel <- sizeSet
    shapeLevel <- c(4,16,2,5)
    if (!is.null(shapeList)) shapeLevel <- shapeList
    
    print(ylimit)
    g <- ggplot()+
        xlab(TeX('Fitness: safety margins ($fs$)')) +
        ylab(TeX('Fitness: constraints ($fc$)')) +
        ylim(ylimit[[2]], ylimit[[1]])+
        coord_cartesian(xlim = c(xlimit[[2]], xlimit[[1]]))+
        # xlim(xlimit[[2]], xlimit[[1]])+
        # ggtitle("Fitness Behavior of Phase 1")+
        theme_bw() +
        theme(axis.text=element_text(size=font_size),#, face="bold"),
              axis.title=element_text(size=font_size),#, face="bold"),
              legend.position="none",
              plot.margin=margin(5, 5, 5, 5)) +
        scale_color_manual(values = colorPalette) +  # set deadline miss color palette
        scale_size_manual(values=sizeLevel)+
        scale_shape_manual(values=shapeLevel)
    
    if (is.null(x_scale)==FALSE) {
        g<- g + scale_x_continuous(trans=x_scale, labels=trans_format(x_scale, math_format(10^.x)))
    }
    else{
        if (is.null(x_labels)==FALSE) {
            if (is.null(x_breaks)==FALSE) {
                x_breaks <- x_labels
            }
            g<- g + scale_x_continuous(breaks=x_breaks,labels=x_labels)#, guide=guide_prism_minor())
        }
    }
    
    if (!is.null(alphaLevel)){
        g <- g+ geom_point(dataset, mapping=aes(x=Schedulability, y=Satisfaction, color=Approach, size=Approach, alpha=Approach, shape=Approach))+
            scale_alpha_manual(values=alphaLevel, guide=F)
    }else{
        g <- g+ geom_point(dataset, mapping=aes(x=Schedulability, y=Satisfaction, color=Approach, size=Approach, shape=Approach))
    }
    
    
    g <- g + theme(
        legend.key.size = unit(0.5, "cm"),
        legend.key.width = unit(0.5,"cm"),
        legend.direction = legend_direct,
        legend.title=element_blank(),
        legend.text = element_text(size=font_size), #, face="bold"),
        legend.background = element_rect(colour = "black", size=0.2)
    )
    
    if (legend=="rb"){
        g<- g+ theme(legend.margin = margin(0,5,3,3), legend.justification=c(1,0), legend.position=c(0.999, 0.001))
    }else if (legend=="rt"){
        g<- g+ theme(legend.margin = margin(0,5,5,3), legend.justification=c(1,1), legend.position=c(0.999, 0.999))
    } else if (legend=="lt"){
        g<- g+ theme(legend.margin = margin(0,0,0,0), legend.justification=c(0,1), legend.position=c(0.001, 0.999))
    } else if (legend=="lb"){
        g<- g+ theme(legend.margin = margin(0,0,0,0), legend.justification=c(0,0), legend.position=c(0.001, 0.001))
    } else{
        g<- g+ theme(legend.position = "none")
    }
    
    return(g)
}

####################################
# make a QI graph using box-plots (horizental, used for the paper)
####################################
QI_graph_horizen<- function(md, legend, yLimit=NULL, colorSet=NULL){
    if (is.null(colorSet)==TRUE){
        colorSet <- cbPalette
    }
    
    #reorder(Subject, desc(Subject))
    g <- ggplot(data=md, aes(x=Subject, y=Value, color=as.factor(Approach))) +
        stat_boxplot(geom = "errorbar", width = 0.7, alpha=1, size=0.7) +
        stat_boxplot(geom = "boxplot", width = 0.7, alpha=1, size=0.7, outlier.shape=1, outlier.size=1) +
        theme_bw() +
        # theme_classic() +
        scale_colour_manual(values=colorSet)+
        xlab("") +
        ylab("") +
        theme(axis.text=element_text(size=12,face="bold"),
              axis.title=element_text(size=0),
              plot.margin = margin(5, 5, 0, 5, "pt"),
              plot.background = element_rect())
    
    if (is.null(yLimit)==FALSE){
        g<- g+ ylim(yLimit[1], yLimit[2])
    }
    
    #Add legend
    if (legend=="top"){
        g <- g + theme(legend.direction = "horizontal",
                       legend.position = "top",
                       legend.title=element_blank(),
                       legend.text = element_text(size=11),  #,face="bold"
                       legend.background = element_blank(),
                       legend.margin = margin(0,0,0,0),
                       legend.key.size = unit(0.5, "cm"),
                       legend.key.width = unit(0.5,"cm") )
    }
    else if(legend!=""){
        g <- g + theme(legend.direction = "vertical",
                       legend.title=element_blank(),
                       legend.text = element_text(size=11),  #,face="bold"
                       legend.background = element_rect(colour = "black", size=0.2),
                       legend.margin = margin(0,0,0,0))
        if (legend=="rb"){          g<- g+ theme(legend.justification=c(1,0), legend.position=c(0.999, 0.001))
        }else if (legend=="rt"){    g<- g+ theme(legend.justification=c(1,1), legend.position=c(0.999, 0.999))
        } else if (legend=="lt"){   g<- g+ theme(legend.justification=c(0,1), legend.position=c(0.001, 0.999))
        } else if (legend=="lb"){   g<- g+ theme(legend.justification=c(0,0), legend.position=c(0.001, 0.001))
        }
    }
    else{
        g<- g+ theme(legend.position = "none")
    }
    
    return (g)
}

####################################
# make a QI graph using box-plots (vertical, deprecated)
####################################
QI_graph<- function(md, legend, xLimit=NULL, colorSet=NULL){
    if (is.null(colorSet)==TRUE){
        colorSet <- cbPalette
    }
    
    #reorder(Subject, desc(Subject))
    g <- ggplot(data=md, aes(x=Subject, y=Value, color=as.factor(Approach))) +
        stat_boxplot(geom = "errorbar", width = 0.7, alpha=1, size=0.7) +
        stat_boxplot(geom = "boxplot", width = 0.7, alpha=1, size=0.7, outlier.shape=1, outlier.size=1) +
        theme_bw() +
        scale_colour_manual(values=colorSet)+
        xlab("") +
        ylab("") +
        theme(axis.text=element_text(size=12,face="bold"),
              axis.title=element_text(size=0),
              plot.margin = margin(5, 5, 10, 0, "pt"))
    
    if (is.null(xLimit)==FALSE){
        g<- g+ xlim(xLimit[1], xLimit[2])
    }
    
    g <- g + theme(legend.direction = "vertical", legend.title=element_blank(), legend.text = element_text(size=12,face="bold"),
                   legend.background = element_rect(colour = "black", size=0.2),  legend.margin = margin(3, 3, 3, 3))
    if (legend=="rb"){          g<- g+ theme(legend.justification=c(1,0), legend.position=c(0.999, 0.001))
    }else if (legend=="rt"){    g<- g+ theme(legend.justification=c(1,1), legend.position=c(0.999, 0.999))
    } else if (legend=="lt"){   g<- g+ theme(legend.justification=c(0,1), legend.position=c(0.001, 0.999))
    } else if (legend=="lb"){   g<- g+ theme(legend.justification=c(0,0), legend.position=c(0.001, 0.001))
    } else{                     g<- g+ theme(legend.position = "none")
    }
    return (g)
}


####################################
#
####################################
draw_bar_plot<-function(xData, yData, xtitle, ytitle, fontSize=25){
    data <- data.frame(x=xData, y=yData)
    g <-ggplot(data, aes(x=x, y=y)) +
        geom_bar(stat="identity")+
        theme_bw() + ylab(ytitle) + xlab(xtitle)+
        theme(axis.text=element_text(size=fontSize),
              axis.title=element_text(size=fontSize)
        )
    return (g)
}
