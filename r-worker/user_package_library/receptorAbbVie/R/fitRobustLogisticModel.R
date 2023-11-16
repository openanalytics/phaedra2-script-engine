# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################
#library(drc)
#library(minpack.lm)
#library(modelr)
#library(ggplot2)
#library(scales)
#' function to fit logistic dose-response model using Tukey's method
#' @param fitData a data frame with two columns, first one is the dose and second one is the reponse.
#' @param fixedMin default is NA, but if the min of the model is fixed, the fixed value should be provided.
#' @param fixedMax default is NA, but if the max of the model is fixed, the fixed value should be provided.
#' @param fixedSlope default is NA, but if the slope of the model is fixed, the fixed value should be provided.
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
#' @author Vahid Nassiri
#' @export
fitRobustLogisticModel <- function(fitData, fixedMin = NA, fixedMax = NA, fixedSlope = NA,insertedHotRadioligandConcentration = NA, Kd = NA,maxiter = 50, ftol = sqrt(.Machine$double.eps),xlabel = "Concentration (uM)", ylabel = "%Activation"){
	names(fitData) <- c("dose", "response")
	## Fit inital non-robust model
	fittedModel <- fitMeanLogisticModel(fitData, fixedMin = fixedMin, fixedMax = fixedMax, fixedSlope = fixedSlope,insertedHotRadioligandConcentration = insertedHotRadioligandConcentration, Kd = Kd,maxiter = maxiter, ftol = ftol,xlabel = xlabel, ylabel = ylabel, weights2use = rep(1,nrow(fitData)))$fittedModel
	## DO iterations for Tukey
	if (!is.character(fittedModel)){
		estDiff <- estDiffOld <- sum(coef(fittedModel)^2)
		fittedModelOld <- fittedModel
		tukeyConstant <- 4.685061
		counter <- 0
		isCoverging <- TRUE
		while(estDiff>0.0001 & counter <5000 & isCoverging){
			counter <- counter + 1
			weightConstant <- tukeyConstant * mad(residuals(fittedModel), 0)
			weights2use <- (1-((residuals(fittedModel)/weightConstant)^2))^2
			weights2use[which(abs(residuals(fittedModel))>=weightConstant)] <- 0
			meanRes <- fitMeanLogisticModel(fitData, fixedMin = fixedMin, fixedMax = fixedMax, fixedSlope = fixedSlope,insertedHotRadioligandConcentration = insertedHotRadioligandConcentration, Kd = Kd,maxiter = maxiter, ftol = ftol,xlabel = xlabel, ylabel = ylabel, weights2use = weights2use)
			fittedModel <- meanRes$fittedModel
			estDiff <- sum((coef(fittedModel)-coef(fittedModelOld))^2)
			if (counter == 1){
				isCoverging <- TRUE
				estDiffOld <- estDiff
				fittedModelOld <- fittedModel
				weights2useOld <- weights2use
			}else if (counter >1 & estDiff<estDiffOld){
				isCoverging <- TRUE
				estDiffOld <- estDiff
				fittedModelOld <- fittedModel
				weights2useOld <- weights2use
			}else if(counter >1 & estDiff>estDiffOld){
				isCoverging <- FALSE
				fittedModel <- fittedModelOld
				weights2use <- weights2useOld
			}
		}
		if (isCoverging){
			tukeyMessage <- paste("Weights are used and the procedure converged in", counter, "steps.")
		}else{
			tukeyMessage <- "Weights are used but the procedure could not converge. However, the best results that could be obtained are provided."
		}
		weightedModel <- meanRes
	}else{
		tukeyMessage <- "Weights are not used as no initial non-robust model could be fitted."
		weightedModel <- fitMeanLogisticModel(fitData, fixedMin = fixedMin, fixedMax = fixedMax, fixedSlope = fixedSlope,insertedHotRadioligandConcentration = insertedHotRadioligandConcentration, Kd = Kd,maxiter = maxiter, ftol = ftol,xlabel = xlabel, ylabel = ylabel, weights2use = rep(1,nrow(fitData)))
		weights2use <- rep(1,nrow(fitData))
	}
	
	
	return(list(results = weightedModel$results, resultsExplained = weightedModel$resultsExplained, extraResults = weightedModel$extraResults, fittedModel = fittedModel, weights2use = weights2use, tukeyMessage = tukeyMessage))
	
}

