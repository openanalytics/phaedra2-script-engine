#' Create matrix with data from specific plate - long format to plate format
#'
#' @param plateData data.frame, data from specific plate; should at least include variable names specified in \code{c(rowName, columnName, valueName)}
#' @param rowName character, variable name in \code{plateData} that specifies the plate row of each data point
#' @param columnName character, variable name in \code{plateData} that specifies the plate row of each data point
#' @param plateName character, variable name in \code{plateData} that specifies the plate name
#' @param valueName character, variable name in \code{plateData} that specifies the value of each data point
#'
#' @return matrix, with data from \code{plateData} in long format to plate format
#' 
#' @author hwouters reused code from mvarewyck
#' @export 
createPlate <- function(plateData,
    rowName,
    columnName,
    plateName = NULL,
    valueName) {
  
  fullPlateLayout <-
      expand.grid(
          plateName = unique(plateData[, plateName]),
          rowName = unique(plateData[, rowName]),
          columnName = unique(plateData[, columnName]),
          KEEP.OUT.ATTRS = FALSE
      )
  fullPlateData <-
      merge(
          plateData,
          fullPlateLayout,
          by.y = c("plateName", "rowName" , "columnName"),
          by.x = c(plateName, rowName, columnName),
          all.y = TRUE
      )
  
  sortedData <-
      fullPlateData[order(fullPlateData[, plateName], fullPlateData[,
                  rowName], fullPlateData[, columnName]),]
  plateNames <- unique(sortedData[, plateName])
  rowNames <- unique(sortedData[, rowName])
  colNames <- unique(sortedData[, columnName])
  toReturn <-
      array(sapply(plateNames, function(iPlate)
                matrix(
                    sortedData[sortedData[,
                            plateName] == iPlate, valueName],
                    nrow = length(rowNames),
                    ncol = length(colNames),
                    byrow = TRUE
                )),
          dim = c(length(rowNames),
              length(colNames), length(plateNames)))
  dimnames(toReturn) <- list(rowNames, colNames, plateNames)
  toReturn
}