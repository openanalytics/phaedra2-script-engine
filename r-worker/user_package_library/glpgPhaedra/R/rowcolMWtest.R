#' For a row calculate the Man Withney U test statistic comparing the row with all other rows
#'
#' @param rowNumber row number
#' @param plateData plate data
#' @param featureName variable to be tested for intra-plate effect
#'
#' @importFrom stats wilcox.test
#' @importFrom stats as.formula
#' 
#' @return Man Withney U test statistic p-values
#' @export
rowMWtest <- function(rowNumber, plateData, featureName="RAW_DATA"){
  tryCatch({
        wilcox.test(as.formula(paste(featureName, "~ as.factor(ROW_NR==rowNumber)")), 
            data = plateData)$p.value
      },
      error = function(err){
        message <- paste0('Could not run wilcox.test (returning NA), got the following message: ', err)
        print(message)
        NA
      })
  
}


#' For a column calculate the Man Withney U test statistic comparing the row with all other rows
#'
#' @param columnNumber column number
#' @param plateData plate data
#' @param featureName variable to be tested for intra-plate effect; default value \code{"RAW_DATA"}
#' 
#' @return Man Withney U test statistic p-values
#' @importFrom stats as.formula
#' 
#' @export
colMWtest <- function(columnNumber, plateData, featureName = "RAW_DATA"){
  tryCatch({
        wilcox.test(as.formula(paste(featureName, "~ as.factor(COL_NR==columnNumber)")), 
            data = plateData)$p.value
      },
      error = function(err){
        message <- paste0('Could not run wilcox.test (returning NA), got the following message: ', err)
        print(message)
        NA
      })
  
}

