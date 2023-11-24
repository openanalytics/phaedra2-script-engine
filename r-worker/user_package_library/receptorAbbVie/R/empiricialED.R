# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################



#' function to find the empirical effective dose
#' @param data a dataset with dose and response values
#' @param respLevel the level of response for which we want the concentration
#' @return empirical ED
#' 
#' @author Vahid Nassiri
#' @export
empiricialED <- function(data, respLevel){
	idx2use <- identify_range(data, respLevel)
	if (any(is.na(idx2use))){
		ed2return <- NA
	}else{
		ed2return <- 10^(interpolate_concentration(data[idx2use,], respLevel))
	}
	return(ed2return)
}
