# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################


#' in cases where estimated ED50 is out of range, checks whether the model fit should be shown or not
#' 
#' function to check checks whether the model fit should be shown or not
#' @param fittedModel a drc object
#' @param inputData a data frame with two columns: column one the numeric dose, and column2 the response
#' @param slope the direction of the curve: either ascending or descending
#' @param estimatedslope the slope from the fitted model
#' @param inactiveSuperpotentParams a numeric vector of length 2. In cases where the response rage is below validRespRange, 
#' the first element of this vecotr shows the  response threshold to distinguish between inactive and suerpotent compounds, 
#' the second one shows the minimum proportion of response values that should be larger than this threshold to consider a compound superpotent. 
#' The default value for this argument is (50, 0.5) which means if more than half of data are larger than 50, then we consider the compound superpotent, 
#' and otherwise inactive.
#' @return TRUE/FALSE 
#' 
#' @author Vahid Nassiri
#' @export
checkED50Range <- function(fittedModel, inputData, slope, estimatedslope, inactiveSuperpotentParams){
	## ED50 is always the last parameter.
	estED50 <- coef(fittedModel)[length(coef(fittedModel))]
	if (is.na(estED50)){
		isRangeGood <- FALSE
	}else{
		if (estED50>= min(inputData$dose) & estED50 <= max(inputData$dose)){
			isRangeGood <- TRUE
		}else{
			## Now we check for inactive/super potent
			slope2use <- determineTheSlope(inputData, slope, estimatedslope)
			compStatus <- compoundStatus(inputData, inactiveSuperpotentParams, slope2use)[1]
			if (compStatus == "superPotent"){
				if(estED50 > max(inputData$dose)){
					isRangeGood <- TRUE
				}else{
					isRangeGood <- FALSE
				}
			}else{
					if(estED50 < min(inputData$dose)){
						isRangeGood <- TRUE
					}else{
						isRangeGood <- FALSE
					}
			}
		}
	}
	return(isRangeGood)
}
