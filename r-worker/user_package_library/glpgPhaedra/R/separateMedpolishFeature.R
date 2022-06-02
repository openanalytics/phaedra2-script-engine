
#' Apply the medpolish to a Feature separately for different WELLTYPE_CODEs
#' @param plateData data frame with columns  \code{WELLTYPE_CODE}, \code{ROW_NR}, \code{COL_NR} and the feature specified
#'   in the \code{featureName} argument
#' @param featureName name of the column that contains the feature data to be polished
#' @param separateWELLTYPE_CODEs list of which WELLTYPE_CODEs should be separated from other WELLTYPE_CODEs for the calculation of medpolish
#' values; should be one of the values of the \code{WELLTYPE_CODE} column; default value \code{"PC"}
#' @param pes logical; default value \code{FALSE}
#' @param maxiter maximum number of iterations for the medpolish procedure 
#' @export
separateMedpolishFeature <- function(plateData, featureName, separateWELLTYPE_CODEs, pes = FALSE, maxiter = 60){
  
  plateDataSubPlates<-lapply(separateWELLTYPE_CODEs,medpolishFeature,plateData = plateData,
         featureName = featureName,nRows = length(unique(plateData$ROW_NR)), nCols = length(unique(plateData$COL_NR)), pes = pes, maxiter = maxiter)
  
  subsetSubPlatesbyWELLTYPE_CODEs<-function(x){
    plateDataSubPlates[[x]][plateDataSubPlates[[x]]$WELLTYPE_CODE %in% separateWELLTYPE_CODEs[[x]],]
  }
  
  plateDataSubPlates<-lapply(1:length(separateWELLTYPE_CODEs),subsetSubPlatesbyWELLTYPE_CODEs)
  
  plateDataCompletePlate <- do.call("rbind", plateDataSubPlates)
  
  return(plateDataCompletePlate)
  
}
