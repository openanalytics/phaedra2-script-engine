# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################


#' determines the direction of the curve based on various factors
#' 
#' function to determine the direction of the curve
#' @param inputData a data frame with two columns: column one the numeric dose, and column2 the response.
#' @param slope takes values from "ascending","descending", and "free", specifying the expected pattern.
#' @param estimatedSlope in case a model could be fitted, the estimated slope parameter.
#' @return 
#' 
#' @author Vahid Nassiri
#' @export
determineTheSlope <- function(inputData, slope = NULL, estimatedSlope = NULL){
	if (!is.null(slope)){
		theSlope <- slope
	}else if(!is.null(estimatedSlope)){
		invisible(ifelse(estimatedSlope >0), theSlope = "ascending", theSlope = "descending")
	}else{
		doseMin <- inputData$dose[which.min(inputData$response)]
		doseMax <- inputData$dose[which.max(inputData$response)]
		invisible(ifelse(doseMin < doseMax), theSlope = "ascending", theSlope = "descending")
	}
	return(theSlope)
}
