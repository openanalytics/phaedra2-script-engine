#' @title Calculate new variant of PMP plate correction
#' 
#' @description Calculate new variant of PMP plate correction
#' It requires the presence of the columns 'COL_NR' and 'ROW_NR', and the
#' column 'WELLTYPE_CODE', as well as the column with name RAW_VALUE 
#' which contains the measured data.
#'
#' @param plateData data frame with columns WELLTYPE_CODE, ROW_NR, COL_NR and the feature specified in the featureName argument
#' @param featureName Name of the measurement column (single feature)
#' @param correctionType correction type; should be one of the values of the WELLTYPE_CODE column or, alternatively, "fullPlateRestoreMedians"; default value "NC"
#' The specified WELLTYPE_CODE type should be distributed all over the plate.
#' This is needed for e.g. rescreens with biased samples.
#' 
#' @importFrom stats median
#' @importFrom reshape2 melt
#' 
#' @return the input data frame with additional columns: the corrected values and 
#' an identifier to denote the type of correction ('PMP')
#' @author Heidi Wouters
#' 
#' @export
run_PMP <- function(plateData, featureName,  correctionType 
                    ) {

  #calculate medians per well type
  mediansByWellType <- aggregate(
    get(featureName) ~ WELLTYPE_CODE,
    data = plateData,
    FUN = function(x)
      median(x, na.rm = TRUE)
  )
  
  colnames(mediansByWellType)[colnames(mediansByWellType) == 'get(featureName)'] <-
    "median"
  
  mergedDataFrame <-
    merge(plateData,
          mediansByWellType,
          by = "WELLTYPE_CODE",
          all.x = TRUE)
  
  #subtract the well type medians
  featureName_minusMEDIAN <- paste0(featureName,"_minusMEDIAN")
  mergedDataFrame[,featureName_minusMEDIAN] = mergedDataFrame[,featureName] - mergedDataFrame$median
  
  negControlData <-
    if ("fullPlateRestoreMedians" %in% correctionType) {
      subset(mergedDataFrame,
             mergedDataFrame$WELLTYPE_CODE %in% c("NC", "PC", "SAMPLE"))
      
    } else {
      subset(mergedDataFrame, mergedDataFrame$WELLTYPE_CODE %in% correctionType)
    }
  
  
  # Create the plate
  plateArray <-
    createPlate(
      plateData = negControlData,
      rowName = "RowID",
      columnName = "ColumnID",
      plateName = "Sci_PlateID",
      valueName = featureName_minusMEDIAN
    )
  
  ctrlArray<-is.na(plateArray)
  plateArray[is.na(plateArray)]<-0
  
  # Create the assay
  myAssay <- create_assay(m = plateArray, ctrl = ctrlArray)
  
  # Detect plate-specific (P) bias
  detectedtypePtestAD <-
    detect_bias(myAssay,
                type = "P",
                alpha = 0.01,
                test = "AD")
  
  
  # Correct the bias assuming 
  correctPMP <-
    correct_bias(detectedtypePtestAD, type = "P", alpha = 0.01)
  
  
  dfcorrPMP <-
    reshape2::melt(correctPMP$mCorrected, value.name = "PMPcorrected")
  names(dfcorrPMP) <-
    c("RowID", "ColumnID", "Sci_PlateID", "PMPcorrectedtypeP")
  
  outputDataFrame <-
    merge(dfcorrPMP,
          mergedDataFrame,
          by = c("Sci_PlateID", "RowID", "ColumnID"))
  
  outputDataFrame$PMPcorrected[is.na(outputDataFrame$PMPcorrected)]<-outputDataFrame[is.na(outputDataFrame$PMPcorrected),featureName_minusMEDIAN]
  outputDataFrame$correction = outputDataFrame$PMPcorrected + outputDataFrame$median
  
  
  outputDataFrame$correction_type = "PMP"
  
  # End
  return(outputDataFrame)
}

