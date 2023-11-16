# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################


#' computes the estimated range
#' 
#' function to compute the estimated rage
#' @param fittedModel a drc object
#' @param fixedBottom fixed bottom
#' @param fixedTop fixed top
#' 
#' @author Vahid Nassiri
#' @export
estimateRange <- function(fittedModel, fixedBottom, fixedTop){
	estRange0 <- coef(fittedModel)[which(names(coef(fittedModel)) == "Bottom:(Intercept)" | names(coef(fittedModel)) == "Top:(Intercept)")]
	if (is.na(fixedBottom & is.na(fixedTop))){
		estRange2return <- estRange0
	}
	if (is.na(fixedBottom) & !is.na(fixedTop)){
		estRange2return <- c(estRange0, fixedTop)
	}
	if (!is.na(fixedBottom) & is.na(fixedTop)){
		estRange2return <- c(fixedBottom, estRange0)
	}
	if (!is.na(fixedBottom) & !is.na(fixedTop)){
		estRange2return <- c(fixedBottom, fixedTop)
	}
	return(estRange2return)
}
