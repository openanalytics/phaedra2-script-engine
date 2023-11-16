# Project: glpgPhaedra
#
# Author: hbossier
###############################################################################



#' Runs Loess correction 
#' 
#' This function will fit a local polynomial regression model. Predicted values are 
#' calculated and subtracted from RAW_VALUES. Then the median value of the RAW_VALUES
#' is added to obtain loess corrected values for the entire plate.
#' 
#' @details The following model is fit:
#' loess(RAW_DATA ~ Row + Column + Row * Column,
#' neg_control_data, family="symmetric",
#' control=loess.control(surface="direct"), ...)
#'
#' 
#' @param plateData plate data containing rows and columns with negative controls
#' @param moranIPthresh threshold to determine statistical significance of the the Moran I test.
#' This is only used to flag plates as a warning message (default = 0.05).
#' @param featureName Name of the measurement column (single feature)
#' @param correction_type default \code{"NC"} based on entries from the 
#' WELLTYPE_CODE column. Character vector with 1 or several of 
#' NC, PC or SAMPLE entries.
#' The specified WELLTYPE_CODE type should be distributed all over the plate.
#' This is needed for e.g. rescreens with biased samples.
#' @param span parameter for loess (default = 0.9), see \code{help(loess)}
#' @param degree parameter for loess (default = 1)
#' @param ... parameters passed to \code{runMoranPlates()}.
#' 
#' @return a data frame containing input data with addition of corrected columns
#' 
#' @author SWink, HBossier
#' @export
loessProcedure <- function(plateData, moranIPthresh = 0.05, featureName,
    correction_type = 'NC', span = 0.9, degree = 1,...){
  
  # input validation
  stopifnot(exprs ={
        correction_type %in% c("NC", "SAMPLE", "PC")
      })
  
  # Run the Moran I test, if the P-values are significant, return warning
  moranPvals <- runMoranPlates(plateData, targetColumn = featureName, ...)
  if(any(moranPvals$pvalue.MoranI <= moranIPthresh)){
    IDs <- which(moranPvals$pvalue.MoranI <= moranIPthresh, arr.ind = TRUE)
    stringWarn <- paste(moranPvals[IDs, 'plate'], sep = '', collapse = ', ')
    warning(paste0('Moran I test detected significant spatial autocorrelation for the following plates: ', stringWarn))
   }
  
  # Use loess function
  # Run on each plate separately and then combine results
  input <- split(plateData, f = plateData$PLATE_ID)
  output <- lapply(input, glpgPhaedra::run_loess, featureName, correction_type,
      span, degree)
  output_full <- do.call('rbind', output)
  # Re-order rows
  output_full <- output_full[order(output_full$PLATE_ID, output_full$ROW_NR, output_full$COL_NR),]
  
  # End
  return(output_full)
}



#' @title Calculate loess plate correction
#' 
#' @description Calculates residuals of a loess plate correction
#'
#'
#' It requires the presence of the columns 'COL_NR' and 'ROW_NR', and the
#' column 'WELLTYPE_CODE', as well as the column with name RAW_VALUE 
#' which contains the measured data.
#'
#' @param input one plate dataframe
#' @inheritParams loessProcedure
#' 
#' @importFrom stats loess
#' @importFrom stats loess.control
#' @importFrom stats median
#' @importFrom stats predict
#' 
#' @author Steven Wink, adapted by Han Bossier
#' 
#' @return the input data frame with additional columns: the corrected values and 
#' an identifier to denote the type of correction ('loess')
#' 
#' @export
run_loess <- function(input, featureName,  correction_type,
    span = 0.9, degree = 1) {
  
  # input = input[[1]]
  if(!is.integer(input$ROW_NR) | !is.integer(input$COL_NR)){
    stop("ROW_NR or COL_NR non integer")
  }
  
  # Make sure row order is correct
  input <- input[ order(input$COL_NR, input$ROW_NR), ]
  
  # select plate correction fitting control data
  neg_control_data <- input[input$WELLTYPE_CODE %in% correction_type,]
  
  # create model based on negative controls
  EEmodel <- loess(get(featureName) ~ ROW_NR + COL_NR + ROW_NR * COL_NR,
      data = neg_control_data, family="symmetric",
      control = loess.control(surface="direct"), span = span, degree = degree)
  
  # calculate corrections
  input$pred_neg <- predict(EEmodel, newdata = input)
  input$correction <- input[, featureName] - input$pred_neg
  input$correction <- input$correction + median(input$pred_neg)
  input$correction_type = "loess"
  
  # End
  return(input)
}

