# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################


#' transforms the estimated ICx's in natrual log to log10.
#' @param estVec vector of length 4 with estimate, standard error and confidence interval.
#' @param fittedModel drc opbject of the fitted model to obtain degrees of freedom of the residual.
#' @return a vector of length 4 with estimate, standard error and confidence interval for the transformed ICx.
#' 
#' @author Vahid Nassiri
#' @export
transformEstimates <- function(estVec, fittedModel){
	transEst <- estVec
	## For estimate
	if (!is.na(estVec[1])){
		transEst[1] <- log10(exp(estVec[1]))
	}
	## For stadard error
	if (!is.na(estVec[2])){
		transEst[2] <- estVec[2]/log(10)
	}
	if (sum(!is.na(estVec[1:2])) == 2){
		# ci
		ciTol <- transEst[2] * qt(0.975, fittedModel$df.residual)
		transEst[3] <- transEst[1]-ciTol
		transEst[4] <- transEst[1]+ciTol
	}
	return(transEst)
}
