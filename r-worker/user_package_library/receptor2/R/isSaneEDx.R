# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################


#' checks sanity conditions for an estimated ED50
#' 
#' function to check sanity of an estimated EDx
#' @param resEDx a vector with estimate, standard error and confidence interval of the estimated effective dose
#' @param doseVals the dose values 
#' @return a vector of length 5 with the results of 5 sanity check.
#' 
#' @author Vahid Nassiri
#' @export
isSaneEDx <- function(resEDx, doseVals){
#	## Obtain ED50 results: we knwo always trhe last row is ED50
#	resEDx <- coef2return[nrow(coef2return),]
	##  Find the dose range of the data
	minDose <- min(doseVals, na.rm = TRUE)
	maxDose <- max(doseVals, na.rm = TRUE)
	checkEst <- checkRangeEst <- checkStdErr <- checkRangeLower <- checkRangeUpper <- 0
	## Condiftion:
	## 1- if ED50 could be estimated at all
	invisible(ifelse(!is.na(resEDx[1]),checkEst <- 1, checkEst <- 0))
	if (checkEst ==1){
		## check for range
		invisible(ifelse(resEDx[1]>= minDose & resEDx[1]<=maxDose,checkRangeEst <- 1, checkRangeEst <- 0))
		##2- check for StdErr
		invisible(ifelse(!is.na(resEDx[2]) &is.finite(resEDx[2]),checkStdErr <- 1, checkStdErr <- 0))
		## if we have the standard error, so we check if its limits are within the range
		if (checkStdErr == 1){
			invisible(ifelse(resEDx[3]>= minDose & resEDx[3]<=maxDose,checkRangeLower <- 1, checkRangeLower <- 0))
			invisible(ifelse(resEDx[4]>= minDose & resEDx[4]<=maxDose,checkRangeUpper <- 1, checkRangeUpper <- 0))
		}
	}
	res2return <- c(checkEst, checkStdErr, checkRangeEst, checkRangeLower, checkRangeUpper)
	names(res2return) <-  c("checkEst", "checkStdErr", "checkRangeEst", "checkRangeLower", "checkRangeUpper")
	return(res2return)
}
