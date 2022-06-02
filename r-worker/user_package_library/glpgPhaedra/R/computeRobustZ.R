#' Compute the Robust Z Score
#' @param input input data 
#' @param correctionType correction type; should be one of the values of the \code{WELLTYPE_CODE} column;
#'   default value \code{"NC"}
#' @export
computeRobustZ <- function(input, correctionType){
	
	# background of plate based on correctionType
	
	mad_neg <- mad(subset(input$correction, input$WELLTYPE_CODE %in% correctionType), na.rm = TRUE)
	med_neg <- median(subset(input$correction, input$WELLTYPE_CODE %in% correctionType), na.rm = TRUE)
	
	
	input$robustz = ( input$correction - med_neg ) / mad_neg
	input$robustz_type <- paste(correctionType, collapse = ".")
	
	
	if (nrow(input) < 6 ) {
		stop("Insufficient negative controls for Bscore calculation")
	}
	
	return(input)
}
