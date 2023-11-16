# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################
#library(drc)
#library(minpack.lm)
#library(modelr)
#library(ggplot2)
#library(scales)
#' function to fit logistic dose-response model based on Dotmatics implementation
#' @param fitData a data frame with two columns, first one is the dose and second one is the reponse.
#' @param fixedMin default is NA, but if the min of the model is fixed, the fixed value should be provided.
#' @param fixedMax default is NA, but if the max of the model is fixed, the fixed value should be provided.
#' @param fixedSlope default is NA, but if the slope of the model is fixed, the fixed value should be provided.
#' @param useWeights logical parameter specifying whether Tukey's biweight method should be used to give outliers less weight (TRUE) or not (FALSE). Default value is FALSE. 
#' @param insertedHotRadioligandConcentration it the inserted hot radioligand concentration which is used to compute Ki. When this argument and Kd are given, Ki will also be computed.
#' @param Kd Kd which is needed for Ki computation. If this argument and insertedHotRadioligandConcentration are provided then Ki is computed.
#' @param maxiter maximum number of iterations in Levenberg-Marquardt Nonlinear Least-Squares Algorithm
#' @param ftol non-negative numeric. ftol measures the relative error desired in the sum of squares.
#' @param xlabel label of the x-axis in the plot
#' @param ylabel label of the y-axis in the plot
#' @return list of components 
#' 
#' @import drc
#' @import minpack.lm
#' @import modelr
#' @import ggplot2
#' @import scales
#' @import nleqslv
#' @import MASS
#' @author Vahid Nassiri
#' @export
fitLogisticModel <- function(fitData, fixedMin = NA, fixedMax = NA, fixedSlope = NA,useWeights = FALSE,insertedHotRadioligandConcentration = NA, Kd = NA,maxiter = 50, ftol = sqrt(.Machine$double.eps),xlabel = "Concentration (uM)", ylabel = "%Activation"){
  names(fitData) <- c("dose", "response")
	
	if (useWeights){
		fitResults <- fitRobustLogisticModel(fitData, fixedMin = fixedMin, fixedMax = fixedMax, fixedSlope = fixedSlope,insertedHotRadioligandConcentration = insertedHotRadioligandConcentration, Kd = Kd,maxiter = maxiter, ftol = ftol,xlabel = xlabel, ylabel = ylabel)
	}else{
		fitResults <- fitMeanLogisticModel(fitData, fixedMin = fixedMin, fixedMax = fixedMax, fixedSlope = fixedSlope,insertedHotRadioligandConcentration = insertedHotRadioligandConcentration, Kd = Kd,maxiter = maxiter, ftol = ftol,xlabel = xlabel, ylabel = ylabel, weights2use = rep(1,nrow(fitData)))
	}
	## Check slope
	if (!is.na(fixedSlope)){
		if (useWeights){
			fitSlope <- coef(MASS::rlm((response)~log10(dose), data = fitData))[2]
		}else{
			fitSlope <- coef(lm((response)~log10(dose), data = fitData))[2]
		}
		if (sign(fitSlope)!=sign(fixedSlope)){
			slopeMessage <- "The direction of the data does not align with the fixed slope."
		}else{
			slopeMessage <- "The direction of the data aligns with the fixed slope."
		}
	}else{
		slopeMessage <- "Slope is not fixed."
	}
	## preparing model fit message
	modelStatus <- list(modelFit = fitResults$tukeyMessage, 
			usingWeights = fitResults$extraResults$convergenceMessage, 
			slopeStatus = slopeMessage)
	## printing the status
	cat(
			"Model fit: ", fitResults$extraResults$convergenceMessage, 
			"\nUsing weights: ", fitResults$tukeyMessage, 
			"\nSlope status: ", slopeMessage,
			"\n"  # This adds a final newline character at the end
	)
  return(list(results = fitResults$results, resultsExplained = fitResults$resultsExplained, extraResults = fitResults$extraResults, weights = fitResults$weights2use, tukeyMessage = fitResults$tukeyMessage,
					status = modelStatus))
}



