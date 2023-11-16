# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################



#' determines whether a compound is inactive or super potent
#' 
#' function to determine whether a compound if inactive or super potent
#' @param inputData  a data frame with two columns: column one the numeric dose, and column2 the response
#' @param inactiveSuperpotentParams a numeric vector of length 2. In cases where the response rage is below validRespRange, 
#' the first element of this vecotr shows the  response threshold to distinguish between inactive and suerpotent compounds, 
#' the second one shows the minimum proportion of response values that should be larger than this threshold to consider a compound superpotent. 
#' The default value for this argument is (50, 0.5) which means if more than half of data are larger than 50, then we consider the compound superpotent, 
#' and otherwise inactive.
#' @param slope the direction of the curve: either ascending or descending
#' @return status of the compound
#' 
#' @author Vahid Nassiri
#' @export
compoundStatus <- function(inputData, inactiveSuperpotentParams, slope = c("ascending", "descending")){
	propRespBelowThreshold <- sum(inputData$response < inactiveSuperpotentParams[1])/nrow(inputData)
	if (propRespBelowThreshold > 0.5){
		invisible(ifelse(slope == "ascending", stauts2return <- "inactive", stauts2return <- "superPotent"))
	}else{
		invisible(ifelse(slope == "ascending", stauts2return <- "superPotent", stauts2return <- "inactive"))
	}
	invisible(ifelse(stauts2return == "inactive", IC50message <- paste("<", round(min(inputData$dose),3)),
					IC50message <- paste(">", round(max(inputData$dose),3))))
	return(c(stauts2return, IC50message))
}