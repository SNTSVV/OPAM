
getline<- function(item, measure, dtype, first, groupSize=4, stress=NULL){
    typeName <- ifelse(dtype=="A12", "$\\hat{A}_{12}$", dtype)
    measureName <- ifelse(measure=="Spread", "$\\Delta$", measure)
    if (is.null(stress)==FALSE && !(stress %in% c("normal", "reverse"))){
        return (NULL)
    }
    
    
    if (first==TRUE){
        line <- sprintf("\t\t& \\multirow{%d}{*}{\\textbf{%s}}\t& \\textbf{%s}", groupSize, measureName, typeName )
    }else{
        line <- sprintf("\t\t&\t\t\t\t\t\t& \\textbf{%s}", typeName )
    }
    for (subject in SUBJECTS){
        value <- item[item$valueType==dtype & item$Subject==subject,]$value
        p <- item[item$valueType=="p-value" & item$Subject==subject,]$value
        a12 <- item[item$valueType=="A12" & item$Subject==subject,]$value
        
        if (is.null(stress)==FALSE && p<0.05){
            if (stress=="normal"){
                if (measure %in% c("HV", "C") && a12>0.5)
                    line <- sprintf("%s & \\cellcolor{blue!30}\\textbf{%.4f}", line, value)  #
                else if (measure %in% c("GD+", "Spread") && a12<0.5)
                    line <- sprintf("%s & \\cellcolor{blue!30}\\textbf{%.4f}", line, value) #\\cellcolor{green!15}
                else
                    line <- sprintf("%s & %.4f", line, value)
            }else{
                if (measure %in% c("HV", "C") && a12<0.5)
                    line <- sprintf("%s & \\cellcolor{gray!20}\\textbf{%.4f}", line, value) #
                else if (measure %in% c("GD+", "Spread") && a12>0.5)
                    line <- sprintf("%s & \\cellcolor{gray!20}\\textbf{%.4f}", line, value) #\\cellcolor{red!15}
                else
                    line <- sprintf("%s & %.4f", line, value)
            }
            
        }else{
            line <- sprintf("%s & %.4f", line, value)
        }
    }
    line <- sprintf("%s \\\\\n", line)
    return (line)
}

getline_st<- function(item, measure, first, groupSize=4){
    typeName <- "$p\\vert\\hat{A}_{12}$"
    measureName <- ifelse(measure=="Spread", "$\\Delta$", measure)
    
    if (first==TRUE){
        line <- sprintf("\t\t& \\multirow{%d}{*}{%s}\t& %10s", groupSize, measureName, typeName )
    }else{
        line <- sprintf("\t\t&\t\t\t\t\t\t& %10s", typeName )
    }
    for (subject in SUBJECTS){
        p <- item[item$valueType=="p-value" & item$Subject==subject,]$value
        a12 <- item[item$valueType=="A12" & item$Subject==subject,]$value
        line <- sprintf("%s & %.2f$\\vert$%.2f", line, p, a12)
    }
    line <- sprintf("%s \\\\\n", line)
    return (line)
}
