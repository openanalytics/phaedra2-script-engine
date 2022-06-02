#' Compute the PES
#' 
#' @param input input data frame; it is assumed the columns 
#' \code{WELLTYPE_CODE} and \code{correction} are present
#' 
#' @importFrom stats sd
#' 
#' @return data frame with input data and additional columns \code{zFactor} and \code{pes}
#' @export
computePes <- function(input) {
  
  ## TODO incorporate situation when multiple types of controls (cannot pool sd's and mean)
  ## this version is only based on NC and PC WELLTYPE_CODE entries
  
  positiveControlData <- subset(input, input$WELLTYPE_CODE == "PC")
  negativeControlData <- subset(input, input$WELLTYPE_CODE == "NC")
  
  if(nrow(positiveControlData) < 2 | nrow(negativeControlData) < 2){
    stop("Not enough neg or pos controls for pes calculation")
  }
  
  zFactor <- 1 - 3*(sd(positiveControlData$correction) + sd(negativeControlData$correction)) /
      abs(mean(positiveControlData$correction) - mean(negativeControlData$correction))
  
  input$zFactor <- zFactor
  
  input$pes <- 100 * (input$correction - mean(negativeControlData$correction, na.rm = TRUE)) /
      (mean(positiveControlData$correction, na.rm = TRUE) - mean(negativeControlData$correction, na.rm = TRUE))
  
  # End
  return(input)
}
