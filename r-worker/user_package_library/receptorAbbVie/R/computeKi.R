# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################


#' function to compute Ki, when EC50 is already available, and insertedHotRadioligandConcentration and Kd are provided.
#' @param EC50 EC50 to be transformed to Ki
#' @param insertedHotRadioligandConcentration  inserted hot radioligand concentration
#' @param Kd Kd
#' @return Ki 
#' 
#' @author Vahid Nassiri
#' @export
computeKi <- function(EC50, insertedHotRadioligandConcentration, Kd){
	if (!is.na(EC50) & !is.na(Kd) & !is.na(insertedHotRadioligandConcentration)){
		Ki <- EC50/(1+insertedHotRadioligandConcentration/Kd)
	}else{
		Ki <- NA
	}
	return(Ki)
}
