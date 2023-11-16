#' Check Intra Plate effects
#' 
#' Check Intra Plate effect by test that compares each of the rows with all other 
#' rows and each of the columns with all other columns.
#'
#' @param plateData plate data with columns \code{WELLTYPE_CODE}, \code{ROW_NR}, 
#' \code{COL_NR} and the feature specified in the \code{featureName} argument.
#' @param featureName variable to be tested for intra-plate effect 
#' @param adjustmentMethod adjustment for multiplicity; default value \code{"fdr"}
#' @param isPrimary MW testing separately for PC from NCSAMPLE when \code{TRUE} (for primary screens) 
#' or MW testing based on NC only when \code{FALSE} (for rescreens); default value \code{TRUE}
#' 
#' @importFrom plyr rbind.fill
#' @importFrom stats p.adjust
#' 
#' @return list of results: significant rows/columns and number of significant 
#' rows and colums for each WELLTYPE_CODE S,NC,P andSNC
#' @export
checkIntraPlateEffect <- function(plateData, featureName = "RAW_DATA", adjustmentMethod = "fdr",isPrimary=TRUE){
  
  plateDataPC <- plateData[plateData$WELLTYPE_CODE == "PC",]
  plateDataNCSAMPLE <- plateData[plateData$WELLTYPE_CODE %in% c("NC", "SAMPLE"), ]
  plateDataNC <- plateData[plateData$WELLTYPE_CODE =="NC", ]
  
  uniqueRowsPC <- unique(plateDataPC$ROW_NR)
  uniqueColsPC <- unique(plateDataPC$COL_NR)
  uniqueRowsNCSAMPLE <- unique(plateDataNCSAMPLE$ROW_NR)
  uniqueColsNCSAMPLE <- unique(plateDataNCSAMPLE$COL_NR)
  
  uniqueRowsNC <- unique(plateDataNC$ROW_NR)
  uniqueColsNC <- unique(plateDataNC$COL_NR)
  #uniqueRowsSAMPLE <- unique(plateData[plateData$WELLTYPE_CODE == "SAMPLE", "ROW_NR"])
  #uniqueColsSAMPLE <- unique(plateData[plateData$WELLTYPE_CODE == "SAMPLE", "COL_NR"])
  
  if (length(uniqueRowsNCSAMPLE) > 1 & length(uniqueColsNCSAMPLE) > 1){
    pValuesRowsNCSAMPLE <- sapply(uniqueRowsNCSAMPLE, rowMWtest, plateData = plateDataNCSAMPLE, featureName = featureName)
  } else {
    pValuesRowsNCSAMPLE <- rep(NA,length(uniqueRowsNCSAMPLE))
  }
  
  if (length(uniqueRowsNCSAMPLE) > 1 & length(uniqueColsNCSAMPLE) > 1){
    pValuesColsNCSAMPLE <- sapply(uniqueColsNCSAMPLE, colMWtest, plateData = plateDataNCSAMPLE, featureName = featureName)
  } else {
    pValuesColsNCSAMPLE <- rep(NA,length(uniqueColsNCSAMPLE))
  }
  
  names(pValuesRowsNCSAMPLE) <- uniqueRowsNCSAMPLE
  names(pValuesColsNCSAMPLE) <- uniqueColsNCSAMPLE
  
  adjustedPValuesRowsNCSAMPLE <- p.adjust(pValuesRowsNCSAMPLE, method = adjustmentMethod)
  adjustedPValuesColsNCSAMPLE <- p.adjust(pValuesColsNCSAMPLE, method = adjustmentMethod)
  
  if (length(uniqueRowsPC) > 1 & length(uniqueColsPC) > 1){
    pValuesRowsPC <- sapply(uniqueRowsPC, rowMWtest, plateData = plateDataPC, featureName = featureName)
  } else {
    pValuesRowsPC <- rep(NA, length(uniqueRowsPC))
  }
  
  if (length(uniqueRowsPC) > 1 & length(uniqueColsPC) > 1){
    pValuesColsPC <- sapply(uniqueColsPC, colMWtest, plateData = plateDataPC, featureName = featureName)
  } else {
    pValuesColsPC <- rep(NA, length(uniqueColsPC))
  }
  
  names(pValuesRowsPC) <- uniqueRowsPC
  names(pValuesColsPC) <- uniqueColsPC
  
  adjustedPValuesRowsPC <- p.adjust(pValuesRowsPC, method = adjustmentMethod)
  adjustedPValuesColsPC <- p.adjust(pValuesColsPC, method = adjustmentMethod)
  
  if (length(uniqueRowsNC) > 1 & length(uniqueColsNC) > 1){
    pValuesRowsNC <- sapply(uniqueRowsNC, rowMWtest, plateData = plateDataNC, featureName = featureName)
  } else {
    pValuesRowsNC <- rep(NA, length(uniqueRowsNC))
  }
  
  if (length(uniqueRowsNC) > 1 & length(uniqueColsNC) > 1){
    pValuesColsNC <- sapply(uniqueColsNC, colMWtest, plateData = plateDataNC, featureName = featureName)
  } else {
    pValuesColsNC <- rep(NA, length(uniqueColsNC))
  }
  
  names(pValuesRowsNC) <- uniqueRowsNC
  names(pValuesColsNC) <- uniqueColsNC
  
  adjustedPValuesRowsNC <- p.adjust(pValuesRowsNC, method = adjustmentMethod)
  adjustedPValuesColsNC <- p.adjust(pValuesColsNC, method = adjustmentMethod)
  
  if(isPrimary==TRUE){
  wellTypeCodeRows <- c(rep("SAMPLE", length(uniqueRowsNCSAMPLE)),
      rep("NC", length(uniqueRowsNCSAMPLE)),
      rep("PC", length(uniqueRowsPC)))
  
  wellTypeCodeCols <- c(rep("SAMPLE", length(uniqueColsNCSAMPLE)),
      rep("NC", length(uniqueColsNCSAMPLE)),
      rep("PC", length(uniqueColsPC)))				  
  
  rowNr <- c(uniqueRowsNCSAMPLE, uniqueRowsNCSAMPLE, uniqueRowsPC)
  colNr <- c(uniqueColsNCSAMPLE, uniqueColsNCSAMPLE, uniqueColsPC)
  
  rowPValues <- c(adjustedPValuesRowsNCSAMPLE, adjustedPValuesRowsNCSAMPLE, adjustedPValuesRowsPC)
  colPValues <- c(adjustedPValuesColsNCSAMPLE, adjustedPValuesColsNCSAMPLE, adjustedPValuesColsPC)
  
    }else{
    wellTypeCodeRows <- c(rep("SAMPLE", length(uniqueRowsNC)),
                          rep("NC", length(uniqueRowsNC)),
                          rep("PC", length(uniqueRowsNC)))
    
    wellTypeCodeCols <- c(rep("SAMPLE", length(uniqueColsNC)),
                          rep("NC", length(uniqueColsNC)),
                          rep("PC", length(uniqueColsNC)))				  
    
    rowNr <- c(uniqueRowsNC, uniqueRowsNC, uniqueRowsNC)
    colNr <- c(uniqueColsNC, uniqueColsNC, uniqueColsNC)
    
    rowPValues <- c(adjustedPValuesRowsNC, adjustedPValuesRowsNC, adjustedPValuesRowsNC)
    colPValues <- c(adjustedPValuesColsNC, adjustedPValuesColsNC, adjustedPValuesColsNC)
    
  }

  rowPValueData <- data.frame(ROW_NR = rowNr, WELLTYPE_CODE = wellTypeCodeRows, intraPlateEffectRow = rowPValues)
  
  colPValueData <- data.frame(COL_NR = colNr, WELLTYPE_CODE = wellTypeCodeCols, intraPlateEffectCol = colPValues)
  
  minimalPlateData <- plateData[, c("ROW_NR", "COL_NR", "WELLTYPE_CODE")]
  
  plateDataWithColPValues <- merge(minimalPlateData, colPValueData, by = c("WELLTYPE_CODE", "COL_NR"), all.x = TRUE, sort = FALSE) 
  plateDataWithRowAndColPValues <- merge(plateDataWithColPValues, rowPValueData, by = c("WELLTYPE_CODE", "ROW_NR"), all.x = TRUE, sort = FALSE)
  
  return(plateDataWithRowAndColPValues)
  
}
