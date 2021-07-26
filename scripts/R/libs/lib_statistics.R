suppressMessages(library(effsize))

#################################################################
# get statistical test results (p-value and A12) comparing two approaches in last pareto front
##################################################################
getStatisticalTest<-function(ga, gb, reverse=FALSE){
    w <- wilcox.test(ga, y = gb, alternative = c("two.sided"))
    if (reverse == TRUE){
        vda<- VD.A(gb,ga)
    }else{
        vda<- VD.A(ga,gb)
    }
    ret <- list()
    ret[[1]] <- sprintf("%.2f\n%.2f", w$p.value, vda$estimate)#sprintf("(%s%s)", pg, mg)
    ret[[2]] <- ifelse(is.nan(w$p.value), 1, w$p.value)
    ret[[3]] <- ifelse(is.nan(vda$estimate), 1, vda$estimate)
    return (ret)
}