# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################

#' returns the valid estimated EDx results
#' 
#' function to return the valid estimated EDx results
#' @param resEDx a vector with estimate, standard error and confidence interval of the estimated effective dose
#' @param doseVals the dose values 
#' @param p effective dose level (between 0 and 100, e.g. p = 50 for IC50)
#' @return a vector with valid elements.
#' 
#' @author Vahid Nassiri
#' @export


validEDx <- function(resEDx, doseVals, p){
	isSaneEDxRes <- isSaneEDx(resEDx, doseVals)
	if (sum(isSaneEDxRes) == 5){
		validpICx <- resEDx
	}else if (sum(isSaneEDxRes[c(1,2,3, 4)]) == 4){
		validpICx <- resEDx
		validpICx[4] <- NA
	}else if (sum(isSaneEDxRes[c(1,2,3,5)]) == 4){
		validpICx <- resEDx
		validpICx[3] <- NA
	}else if (sum(isSaneEDxRes[c(1,2, 3)]) == 3){
		validpICx <- resEDx
		validpICx[3:4] <- NA
	}else if(sum(isSaneEDxRes[c(1,3)]) ==2 & isSaneEDxRes[2] == 0){
		validpICx <- resEDx
		validpICx[2:4] <- NA
	}else if(sum(isSaneEDxRes[c(1, 3)]) !=2){
		validpICx <- resEDx
		validpICx[1:4] <- NA
	}else{
		validpICx <- resEDx
		validpICx[1:4] <- NA
	}
	## Now, if we get pIC50 and it's smaller than lower dose, we give a message, also
	## if we get pIC50 and it's between first and second lower dose, we give a message
	EDxMessage <- NULL
#	if (resEDx[1] < min(doseVals)){
#		EDxMessage <- paste(paste("pIC", p, sep = ""), "<", round(min(doseVals),3))
#	}
#	if (resEDx[1] > min(doseVals) & resEDx[1]< sort(unique(doseVals))[2]){
#		EDxMessage <- paste(paste("pIC", p, sep = ""), "<", round(sort(unique(doseVals))[2],3))
#	}
	return(list(validpICx = validpICx, isSaneEDxRes = isSaneEDxRes, EDxMessage = EDxMessage))
}
