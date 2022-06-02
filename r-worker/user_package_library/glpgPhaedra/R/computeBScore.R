
#' Compute a B Score 
#' @param input input data 
#' @param correctionType correction type; should be one of the values of the \code{WELLTYPE_CODE} column;
#'   default value \code{"NC"}
#' @export
computeBScore <- function(input, correctionType){
    
	# bscore is calculated based on same samples as plate correction. Is this always the case?

    mad_value <- mad(subset(input$correction, input$WELLTYPE_CODE %in% correctionType), na.rm = TRUE)

    input$Bscore <- input$correction / mad_value
    input$bscore_type <-  paste(correctionType, collapse = ".")

  return(input)
  
}
