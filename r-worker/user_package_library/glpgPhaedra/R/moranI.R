# Project: glpgPhaedra
###############################################################################

# Making R CMD Check happy with some global variables
utils::globalVariables(c('::', ':::'))

#' Moran I's test on one plate
#' 
#' Moran’s I is a measure of spatial autocorrelation.  
#' 
#' @param onePlateData data for one plate containing rows and columns for a specific well type.
#' Note that subsetting plates into negative/positive controls should be done outside this function.
#' @param targetColumn character, the column with the raw data 
#' @param distMethod string indicating which distance metric to be used.
#' Can be "euclidean", "maximum", "manhattan", "canberra", "binary" or "minkowski" with euclidean as default.
#' 
#' @importFrom ape Moran.I
#' @importFrom stats dist
#' 
#' @return returns the p-value of the null hypothesis 
#' that there is zero spatial autocorrelation present in targetColumn
#' 
#' @author hwouters (adapted, hbossier)
#' @export 
calcMoran <- function(onePlateData, targetColumn = 'CalcResultI.RAW_VALUE', 
    distMethod = 'euclidean'){
  # Check whether row NR and col NR are present
  if(any(!c('ROW_NR', 'COL_NR') %in% names(onePlateData))) stop('Data must contain columns ROW_NR and COL_NR')
  
  # Check argument
  distMethod <- match.arg(distMethod, 
      choices = c("euclidean", "maximum", "manhattan", "canberra", "binary" , "minkowski"),
      several.ok = FALSE)
    
  # First calculate distances
  dists <- as.matrix(dist(cbind(onePlateData$ROW_NR, onePlateData$COL_NR), method = distMethod))
  # Invert and set diagonal to 0
  dists.inv <- 1/dists
  diag(dists.inv) <- 0
  
  # Run Moran I test
  MoranIEuclPval <- try(Moran.I(x = as.vector(t(onePlateData[,targetColumn])), 
      weight = dists.inv, na.rm = TRUE)$p.value, silent = TRUE)

  # Add check
  if(class(MoranIEuclPval)[1] == 'try-error'){
    warning(paste0('Could not calculate p-value on this plate. Is the plate layout valid?'))
  } else {
    return(MoranIEuclPval)
  }
}


#' Moran I's test on multiple plates
#' 
#' Moran’s I is a measure of spatial autocorrelation.  
#' 
#' @param plateData plate data containing rows and columns with negative controls
#' @inheritParams calcMoran 
#' @param splitColumn character, the column with the plate IDs (where to split)
#' 
#' @return returns the p-values of the null hypotheses
#' that there is zero spatial autocorrelation present in targetColumn for each plate
#' 
#' @author hbossier
#' @export 
runMoranPlates <- function(plateData, targetColumn = 'CalcResultI.RAW_VALUE', 
    distMethod = 'euclidean', splitColumn){
  # Check whether row NR and col NR are present
  if(any(!c('ROW_NR', 'COL_NR') %in% names(plateData))) stop('Data must contain columns ROW_NR and COL_NR')
  
  # Check argument
  distMethod <- match.arg(distMethod, 
      choices = c("euclidean", "maximum", "manhattan", "canberra", "binary" , "minkowski"),
      several.ok = FALSE)
  
  # Run on multiple plates: split into multiple plates
  input <- split(plateData, f = as.factor(plateData[,splitColumn]))
  runMorans <- lapply(input, calcMoran, targetColumn, distMethod)
  
  # out
  output <- data.frame('pvalue.MoranI' = do.call('rbind', runMorans), 
      stringsAsFactors = FALSE)
  output$plate <- row.names(output)
  row.names(output) <- NULL
  
  # End
  return(output)  
}






