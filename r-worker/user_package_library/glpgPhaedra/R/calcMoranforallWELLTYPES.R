#' Calculate Moran's I for all Well Types
#'
#' @param plateData data for one plate containing rows and columns with negative controls
#' @param featureName character, the column with the data to test
#' @param ... additional arguments
#'
#' @return returns vector of p-value for Moran's I autocorrelation test 
#' for the different welltypes in WELLTYPE_CODE and the combination of NC and SAMPLE
#'
#' @author hwouters
#' @export
calcMoranforallWELLTYPES <- function(plateData, featureName, ...) {
  PlateNCSAMPLE <-
    plateData[plateData$WELLTYPE_CODE %in% c("NC", "SAMPLE"),]
  MoranresNCSAMPLE <-
    calcMoran(onePlateData = PlateNCSAMPLE, targetColumn = featureName)
  PlatePC <-
    plateData[plateData$WELLTYPE_CODE %in% c("PC"),]
  MoranresPC <-
    calcMoran(onePlateData = PlatePC, targetColumn = featureName)
  PlateNC <-
    plateData[plateData$WELLTYPE_CODE %in% c("NC"),]
  MoranresNC <-
    calcMoran(onePlateData = PlateNC, targetColumn = featureName)
  PlateSAMPLE <-
    plateData[plateData$WELLTYPE_CODE %in% c("SAMPLE"),]
  MoranresSAMPLE <-
    calcMoran(onePlateData = PlateSAMPLE, targetColumn = featureName)

  resultsMoran <-
    c(MoranresNCSAMPLE, MoranresPC, MoranresNC, MoranresSAMPLE)
  names(resultsMoran) <- c("NCSAMPLE", "PC", "NC", "SAMPLE")
  
  # End
  return(resultsMoran)
}
