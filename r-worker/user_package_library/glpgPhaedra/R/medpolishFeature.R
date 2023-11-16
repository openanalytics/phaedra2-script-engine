
#' Apply the medpolish to a Feature
#' @param plateData data frame with columns  \code{WELLTYPE_CODE}, \code{ROW_NR}, \code{COL_NR} and the feature specified
#'   in the \code{featureName} argument
#' @param featureName name of the column that contains the feature data to be polished
#' @param nRows number of rows on the plate (numeric); default value \code{32}
#' @param nCols number of columns on the plate (numeric); default value \code{48}
#' @param correctionType correction type; should be one of the values of the \code{WELLTYPE_CODE} column or,
#'   alternatively, \code{"fullPlateRestoreMedians"}; default value \code{"NC"}
#' @param pes logical; default value \code{FALSE}
#' @param maxiter maximum number of iterations for the medpolish procedure 
#' 
#' @importFrom stats aggregate
#' @importFrom stats median
#' @importFrom stats mad 
#' @importFrom stats medpolish
#' @importFrom stats reshape
#' 
#' @export
medpolishFeature <- function(plateData, featureName, 
    nRows = 32, nCols = 48, correctionType, pes = FALSE, maxiter = 60){
  
  negControlData <- if ("fullPlateRestoreMedians" %in% correctionType){
        
        subset(plateData, plateData$WELLTYPE_CODE %in% c("NC", "PC", "SAMPLE"))
        
      } else {
        
        subset(plateData, plateData$WELLTYPE_CODE %in% correctionType)
      }
  
  
  mediansByWellType <- aggregate(get(featureName) ~ WELLTYPE_CODE,
      data = plateData, FUN = function(x) median(x, na.rm = TRUE))
  
  colnames(mediansByWellType)[colnames(mediansByWellType) == 'get(featureName)'] <- "median"
  
  unRows<-unique(negControlData$ROW_NR)
  unCols<-unique(negControlData$COL_NR)
  
  fullPlateLayout <- expand.grid(ROW_NR = unRows, COL_NR = unCols, KEEP.OUT.ATTRS = FALSE)
  dataToPolish <- negControlData[ , c("ROW_NR", "COL_NR", featureName)]
  fullPlateData <- merge(fullPlateLayout, dataToPolish, by = c("ROW_NR", "COL_NR"), all.x = TRUE)
  
  fullPlateData <-fullPlateData[order(fullPlateData$COL_NR),]
  fullPlateData <-fullPlateData[order(fullPlateData$ROW_NR),]
  rowData <- split(fullPlateData[[featureName]], fullPlateData$ROW_NR)
  
  plateMatrix <- do.call("rbind", rowData)
  
  # median polish
  polishedData <- medpolish(
      plateMatrix,
      trace.iter = FALSE,
      na.rm = TRUE,
      maxiter = maxiter
  )
  
  # assembly of the background
  bkg <- matrix(polishedData$overall, length(unRows), length(unCols))
  colnames(bkg) <- sort(unCols)
  rownames(bkg) <- sort(unRows)
  
  for (row in 1:length(unRows)) {
    if (!is.na(polishedData$row[row])){
      bkg[row, ] <- bkg[row, ] + polishedData$row[row]
    }
  }
  
  for (col in 1:length(unCols)) {
    if (!is.na(polishedData$col[col])){
      bkg[, col] <- bkg[, col] + polishedData$col[col]
    } 
  }
  
  # add results to the data frame
  
  if(nrow(bkg) != length(unRows) | ncol(bkg) != length(unCols)){ # check whether assumption of equal size matrix is correct
    stop("bkg dimension incorrect")
  }
  
  bkg <- as.data.frame(bkg)
  
  bkg$ROW_NR <- rownames(bkg)
  bkg <- reshape(bkg, idvar = "ROW_NR", varying = 1:length(unCols),  v.names = "pred_neg", direction = "long")
  colnames(bkg)[colnames(bkg)=="time"] <- "COL_NR"
  
  bkg$COL_NR<-sort(unCols)[bkg$COL_NR]
  
  bkg$ROW_NR <- as.integer(bkg$ROW_NR)
  bkg$COL_NR <- as.integer(bkg$COL_NR)
  
  plateData <- merge(plateData, bkg, by = c("ROW_NR", "COL_NR"), all.x = TRUE )
  plateData$correction <- plateData[ , featureName] - plateData$pred_neg
  plateData$correction_type <- "medpolish"
  
  #if (correctionType == "fullPlateRestoreMedians"){
  
  plateData <- merge(plateData, mediansByWellType, by = "WELLTYPE_CODE", all.x = TRUE)
  plateData$pred_neg = plateData$pred_neg - plateData$median
  plateData$correction = plateData$correction + plateData$median
  
  #} else {
  if (!("fullPlateRestoreMedians" %in% correctionType)){
    
    plateData <- computeBScore(plateData, correctionType = correctionType)
    plateData <- computeRobustZ(plateData, correctionType = correctionType)
    
  }
  
  if (pes) {
    
    plateData <- computePes(plateData)
    
  }
  
  return(plateData)
  
}
