# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################
#library(drc)
#library(minpack.lm)
#library(modelr)
#library(ggplot2)
#library(scales)
# library(nleqslv)
#' function to fit logistic dose-response model based on Dotmatics implementation
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
fitMeanLogisticModel <- function(fitData, fixedMin = NA, fixedMax = NA, fixedSlope = NA,insertedHotRadioligandConcentration = NA, Kd = NA,maxiter = 50, ftol = sqrt(.Machine$double.eps),xlabel = "Concentration (uM)", ylabel = "%Activation", weights2use){
  names(fitData) <- c("dose", "response")
  ## Obtain starting values
  initialHill <- sign(coef(lm(response~dose, data = fitData))[2])
  whichmedian <- function(x) which.min(abs(x - median(x)))
  ## Obtain starting values using drc
  drmFit <- suppressMessages(try(drm(response~dose, data = fitData, 
                    fct = LL.4(fixed = c(fixedSlope, fixedMin, fixedMax, NA), names = c("Slope", "Min", "Max", "EC50")), weights = weights2use ), silent = TRUE))
  if (!is.character(drmFit)){
    coefDRC <- coef(drmFit)
  }else{
    drmFit <- try(drm(response~dose, data = fitData, 
                      fct = LL.4(fixed = c(NA, fixedMin, fixedMax, NA), names = c("Slope", "Min", "Max", "EC50")), control = drmc(method = "SANN"), weights = weights2use ), silent = TRUE)
    if (!is.character(drmFit)){
      coefDRC <- coef(drmFit)
    }else{
      coefDRC <- rep(NA, 4)
      names(coefDRC) <- c("Slope:(Intercept)","Min:(Intercept)","Max:(Intercept)","EC50:(Intercept)")
    }
  }
  # making initials
  drcInitials <- list(Slope = NA, Min = NA, Max = NA, EC50 = NA)
  drcInitials$Slope <- -1*coefDRC[which(names(coefDRC) == "Slope:(Intercept)")]
  drcInitials$Min <- coefDRC[which(names(coefDRC) == "Min:(Intercept)")]
  drcInitials$Max <- coefDRC[which(names(coefDRC) == "Max:(Intercept)")]
  drcInitials$EC50 <- coefDRC[which(names(coefDRC) == "EC50:(Intercept)")]
  ## Fit the model for different conditions
  if (is.na(fixedMin) & is.na(fixedMax) & is.na(fixedSlope)){
		
		
    fitModel <- try(nlsLM(response~(Min) + ((Max-(Min))/(1+((EC50/dose)^Slope))),
                          data = fitData,
                          start = list(Max = max(fitData$response) , Slope = initialHill, 
															EC50 = fitData$dose[whichmedian(fitData$response)] , Min = min(fitData$response)), control = nls.lm.control(maxiter = maxiter, ftol = ftol), weights = weights2use ), silent = TRUE)
    if (is.character(fitModel)){
      fitModel <- try(nlsLM(response~(Min) + ((Max-(Min))/(1+((EC50/dose)^Slope))),
                            data = fitData,
                            start = list(Max = drcInitials$Max , Slope = drcInitials$Slope, EC50 = drcInitials$EC50 , Min = drcInitials$Min), control = nls.lm.control(maxiter = maxiter, ftol = ftol),
														weights = weights2use ), silent = TRUE)
    }
		if (!is.character(fitModel)){
			EC50 <- coef(fitModel)[3]
			Min <- coef(fitModel)[4]
			Max <- coef(fitModel)[1]
			Slope <- coef(fitModel)[2]
			
		}else{
			EC50 <- Min <- Max <- Slope <- NA
		}
   }else if(!is.na(fixedMin)&is.na(fixedMax)& is.na(fixedSlope)){
    fitModel <- try(nlsLM(response~(fixedMin) + ((Max-(fixedMin))/(1+((EC50/dose)^Slope))),
                          data = fitData,
                          start = list(Max = max(fitData$response) , Slope = initialHill, EC50 = fitData$dose[whichmedian(fitData$response)]), control = nls.lm.control(maxiter = maxiter, ftol = ftol), 
													weights = weights2use ), silent = TRUE)
    if (is.character(fitModel)){
      fitModel <- try(nlsLM(response~(fixedMin) + ((Max-(fixedMin))/(1+((EC50/dose)^Slope))),
                            data = fitData,
                            start = list(Max = drcInitials$Max , Slope = drcInitials$Slope, EC50 = drcInitials$EC50), control = nls.lm.control(maxiter = maxiter, ftol= ftol), weights = weights2use), silent = TRUE)
    }
    Min <- fixedMin
		if (!is.character(fitModel)){
			EC50 <- coef(fitModel)[3]
			Min <- fixedMin
			Max <- coef(fitModel)[1]
			Slope <- coef(fitModel)[2]
		}else{
			EC50 <- Max <- Slope <- NA
		}
		
		
		
  }else if(is.na(fixedMin)& !is.na(fixedMax)& is.na(fixedSlope)){
    fitModel <- try(nlsLM(response~(Min) + ((fixedMax-(Min))/(1+((EC50/dose)^Slope))),
                          data = fitData,
                          start = list(Slope = initialHill, EC50 = fitData$dose[whichmedian(fitData$response)] , Min = min(fitData$response)), control = nls.lm.control(maxiter = maxiter, ftol = ftol), 
													weights = weights2use), silent = TRUE)
    if (is.character(fitModel)){
      fitModel <- try(nlsLM(response~(Min) + ((fixedMax-(Min))/(1+((EC50/dose)^Slope))),
                            data = fitData,
                            start = list(Slope = drcInitials$Slope, EC50 = drcInitials$EC50 , Min = drcInitials$Min), control = nls.lm.control(maxiter = maxiter, ftol = ftol), weights = weights2use), silent = TRUE)
    }
		Max <- fixedMax
		
		if (!is.character(fitModel)){
			EC50 <- coef(fitModel)[2]
			Min <- coef(fitModel)[3]
			Slope <- coef(fitModel)[1]
		}else{
			EC50 <- Min <- Slope <- NA
		}
		
    
  }else if(!is.na(fixedMin)& !is.na(fixedMax)& is.na(fixedSlope)){
    fitModel <- try(nlsLM(response~(fixedMin) + ((fixedMax-(fixedMin))/(1+((EC50/dose)^Slope))),
                          data = fitData,
                          start = list(Slope = initialHill, EC50 = fitData$dose[whichmedian(fitData$response)]), control = nls.lm.control(maxiter = maxiter, ftol = ftol), weights = weights2use), silent = TRUE)
    if (is.character(fitModel)){
      fitModel <- try(nlsLM(response~(fixedMin) + ((fixedMax-(fixedMin))/(1+((EC50/dose)^Slope))),
                            data = fitData,
                            start = list(Slope = drcInitials$Slope, EC50 = drcInitials$EC50), control = nls.lm.control(maxiter = maxiter, ftol = ftol), weights = weights2use), silent = TRUE)
    }
    EC50 <- coef(fitModel)[2]
    Min <- fixedMin
    Max <- fixedMax
    Slope <- coef(fitModel)[1]
		if (!is.character(fitModel)){
			EC50 <- coef(fitModel)[2]
			Slope <- coef(fitModel)[1]
		}else{
			EC50 <- Slope <- NA
		}
  }else  if (is.na(fixedMin) & is.na(fixedMax) & !is.na(fixedSlope)){
		fitModel <- try(nlsLM(response~(Min) + ((Max-(Min))/(1+((EC50/dose)^fixedSlope))),
						data = fitData,
						start = list(Max = max(fitData$response) , EC50 = fitData$dose[whichmedian(fitData$response)] , Min = min(fitData$response)), control = nls.lm.control(maxiter = maxiter, ftol = ftol), weights = weights2use), silent = TRUE)
		if (is.character(fitModel)){
			fitModel <- try(nlsLM(response~(Min) + ((Max-(Min))/(1+((EC50/dose)^fixedSlope))),
							data = fitData,
							start = list(Max = drcInitials$Max ,EC50 = drcInitials$EC50 , Min = drcInitials$Min), control = nls.lm.control(maxiter = maxiter, ftol = ftol), weights = weights2use), silent = TRUE)
		}
		Slope <- fixedSlope
		if (!is.character(fitModel)){
			EC50 <- coef(fitModel)[2]
			Min <- coef(fitModel)[3]
			Max <- coef(fitModel)[1]
			
			
		}else{
			EC50 <- Min <- Max <-  NA
		}
	}else if (is.na(fixedMin) & !is.na(fixedMax) & !is.na(fixedSlope)){
		fitModel <- try(nlsLM(response~(Min) + ((fixedMax-(Min))/(1+((EC50/dose)^fixedSlope))),
						data = fitData,
						start = list(EC50 = fitData$dose[whichmedian(fitData$response)] , Min = min(fitData$response)), control = nls.lm.control(maxiter = maxiter, ftol = ftol), weights = weights2use), silent = TRUE)
		if (is.character(fitModel)){
			fitModel <- try(nlsLM(response~(Min) + ((fixedMax-(Min))/(1+((EC50/dose)^fixedSlope))),
							data = fitData,
							start = list(EC50 = drcInitials$EC50 , Min = drcInitials$Min), control = nls.lm.control(maxiter = maxiter, ftol = ftol), weights = weights2use), silent = TRUE)
		}
		Max <- fixedMax
		Slope <- fixedSlope
		if (!is.character(fitModel)){
			EC50 <- coef(fitModel)[1]
			Min <- coef(fitModel)[2]
		}else{
			Min <- Slope <- NA
		}
	}else if (!is.na(fixedMin) & is.na(fixedMax) & !is.na(fixedSlope)){
		fitModel <- try(nlsLM(response~(fixedMin) + ((Max-(fixedMin))/(1+((EC50/dose)^fixedSlope))),
						data = fitData,
						start = list(Max = max(fitData$response) , EC50 = fitData$dose[whichmedian(fitData$response)]), control = nls.lm.control(maxiter = maxiter, ftol = ftol), weights = weights2use), silent = TRUE)
		if (is.character(fitModel)){
			fitModel <- try(nlsLM(response~(fixedMin) + ((Max-(fixedMin))/(1+((EC50/dose)^fixedSlope))),
							data = fitData,
							start = list(Max = drcInitials$Max , EC50 = drcInitials$EC50), control = nls.lm.control(maxiter = maxiter, ftol = ftol), weights = weights2use), silent = TRUE)
		}
		Min <- fixedMin
		Slope <- fixedSlope
		if (!is.character(fitModel)){
			EC50 <- coef(fitModel)[2]
			Max <- coef(fitModel)[1]
			
		}else{
			EC50 <- Max<- NA
		}
	}else if (!is.na(fixedMin) & !is.na(fixedMax) & !is.na(fixedSlope)){
		fitModel <- try(nlsLM(response~(fixedMin) + ((fixedMax-(fixedMin))/(1+((EC50/dose)^fixedSlope))),
						data = fitData,
						start = list(EC50 = fitData$dose[whichmedian(fitData$response)]), control = nls.lm.control(maxiter = maxiter, ftol = ftol), weights = weights2use), silent = TRUE)
		if (is.character(fitModel)){
			fitModel <- try(nlsLM(response~(fixedMin) + ((fixedMax-(fixedMin))/(1+((EC50/dose)^fixedSlope))),
							data = fitData,
							start = list(EC50 = drcInitials$EC50), control = nls.lm.control(maxiter = maxiter, ftol = ftol), weights = weights2use), silent = TRUE)
		}
		Min <- fixedMin
		Max <- fixedMax
		Slope <- fixedSlope
		if (!is.character(fitModel)){
			EC50 <- coef(fitModel)[1]
		}else{
			EC50 <- NA
		}
	}
   ## Compute model quality stuff
  if (!is.character(fitModel)){
    rSquared <- modelr::rsquare(fitModel, fitData)
    MSE <- modelr::mse(fitModel, fitData)
    RMSE <- modelr::rmse(fitModel, fitData)
    AIC <- AIC(fitModel)
    BIC <- BIC(fitModel)
		## extract covergence message
		convStat <- summary(fitModel)$convInfo
		if (convStat$isConv){
			convergenceMessage <- paste("Model could be fitted and the procedure converged in", convStat$finIter, "iterations. The relative error in the sum of squares is at", convStat$finTol)
			Sigma <- summary(fitModel)$sigma
			summaryEstimates <- summary(fitModel)$parameters
			rownames(summaryEstimates)[which(rownames(summaryEstimates)=="Slope.dose")] <- "Slope"
		}else{
			convergenceMessage <- paste("Model could not be fitted. The number of iterations reached", maxiter, "without convergence.")
			Sigma <- NA
			summaryEstimates <- NULL
		}
		
		## Plot the results
		newDose <- seq(min(fitData$dose), max(fitData$dose), ,10000)
		predResponse <- predict(fitModel, newdata = data.frame(dose = newDose))
		dataPred <- data.frame(dose = newDose, response = predResponse)
		fitData$weights <- weights2use
		plotData <- ggplot() + geom_line(data = dataPred, aes(x = dose, y = response), color = "royalblue", linewidth = 1.3) + 
				geom_point(data = fitData, aes(x = dose, y = response, size = weights),color = "blue")+ theme(panel.grid.major = element_blank(), panel.grid.minor = element_blank(),
						panel.background = element_blank(), axis.line = element_line(colour = "black"))+scale_x_continuous(trans='log10', 
						breaks=trans_breaks('log10', function(x) 10^x))+xlab(xlabel) + ylab(ylabel)
  }else{
    rSquared <- MSE <- RMSE <- AIC <- BIC <- Sigma <- NA
		convergenceMessage <- "No model could be fitted."
		summaryEstimates <- dataPred <- NULL
		## plot data
		plotData <- ggplot() + geom_point(data = fitData, aes(x = dose, y = response),color = "blue", size = 2)+ theme(panel.grid.major = element_blank(), panel.grid.minor = element_blank(),
						panel.background = element_blank(), axis.line = element_line(colour = "black"))+scale_x_continuous(trans='log10', 
						breaks=trans_breaks('log10', function(x) 10^x))+xlab(xlabel) + ylab(ylabel)
		
  }
  ## Compute extra variables
  averagedData <- aggregate(response~dose, mean, data = fitData)
  yMinObs <- min(averagedData$response)
  yMaxObs <- max(averagedData$response)
  ## X at Y 50
  if (any(fitData$response>50)){
    Y <- 50
    tmp1 <- log(EC50)
    tmp2 <- 1/Slope
    tmp3 <- (Max-Min)/(Y-Min)
    tmp4 <- log(tmp3+1)*tmp2
    computedDose <- exp(tmp1-tmp4)
    func <- function(dose){
      ((Min) + ((Max-(Min))/(1+((EC50/dose)^Slope))))-Y
    }
    X_at_Y50 <- nleqslv(min(fitData$dose),func)$x
    X_at_Y50message <- paste("Threre are", length(unique(fitData$dose[which(fitData$response>50)])),"concentrations with activity exceeding 50%.")
  }else{
    X_at_Y50 <- NA
    X_at_Y50message <- "Threre is no concentrations with activity exceeding 50%."
  }
  ## 
  YatMinX <- averagedData$response[which.min(averagedData$dose)]
  YatMaxX <- averagedData$response[which.max(averagedData$dose)]
  ##
  XatMinY <- averagedData$dose[which.min(averagedData$response)]
  XatMaxY <- averagedData$dose[which.max(averagedData$response)]
  
    ## the classification is "no fit" by default, if all the 3 conditions above are correct, it will be: good curve, 
  ## if any of the 3 are violated, it will be "inactive".
	if (is.character(fitModel)){
		curveClassification <- "no fit"
		
	}else{
		conditionSpan <- yMaxObs-yMinObs >= 30
		conditionMAxResp <- yMaxObs>50
		conditionSlope <- Slope > 0
		if (conditionSpan & conditionMAxResp & conditionSlope){
			curveClassification <- "good curve"
		}else{
			curveClassification <- "inactive"
		}
		
	}
	
	## Compute Ki
	if (!is.na(EC50) & !is.na(Kd) & !is.na(insertedHotRadioligandConcentration)){
		Ki <- EC50/(1+insertedHotRadioligandConcentration/Kd)
	}else{
		Ki <- NA
	}
	
  ## making output
	requestedResults <- list(EC50 = EC50, Min = Min, Max = Max, Slope = Slope, rSquared = rSquared, YMAX_YMIN = Max-Min, 
			yMinObs = yMinObs, yMaxObs = yMaxObs, yMinObs_yMin = yMinObs-Min ,yMaxObs_yMax = yMaxObs-Max,
			X_at_Y50 = X_at_Y50, YatMinX = YatMinX, YatMaxX = YatMaxX, XatMinY = XatMinY, XatMaxY = XatMaxY,
			MIN_CONC = min(fitData[,1], na.rm = TRUE),
			MAX_CONC = max(fitData[,1], na.rm = TRUE),
			Classification = curveClassification,
			Ki = Ki)
	resultsExplanined <- data.frame(
			Name = c("EC50", "Min", "Max", "Slope", "rSquared", "Max-Min", "Y Min Obs", "Y Max Obs", "Y Min Obs - Min", "Y Max Obs - Max",
					"X At Y 50", "Y At Min X", "Y At Max X", "X At Min Y", "X At Max Y", "MIN_CONC","MAX_CONC" ,"Classification", "Ki"),
			Explanation = c("the EC50 value (half maximal [relative]activity)", "the upper plateau", "the lower plateau", "Slope of the curve", "Regression of the fitting", 
					"Maximum effect of the tested compound", "Lowest measured activity", "Highest measured activity", "Deviation of lowest measured activity from lower plateau",
					"Deviation of highest measured activity from upper plateau", "Concentration of EC50 at which compound shows 50% of ist maximum activity", 
					"Activity at lowest tested concentration", "Activity at highest tested concentration",
					"Concentration of lowest measured activity", "Concentration of highest measured activity", 
					"Lowest Tested Concentration", "Highest tested concentration",
					"If Y Max Obs > 50, Y Max Obs -  Y Min Obs >= 30, and Slope > 0, the curve is classified as good curve. If any of these three conditions is violated, it is classified as inactive. And otherwise, it is classified as no fit.", "Ki"),
			values = unlist(requestedResults))
	rownames(resultsExplanined) <- NULL
	extraResults <- list( Sigma = Sigma, AIC = AIC, BIC = BIC, MSE = MSE, RMSE = RMSE, detailedEstimates = summaryEstimates, convergenceMessage = convergenceMessage, 
			X_at_Y50message = X_at_Y50message, data2plot = dataPred, plot = plotData)
	tukeyMessage <- "Non-robust method is used."
  return(list(results = requestedResults, resultsExplained = resultsExplanined, extraResults = extraResults, fittedModel = fitModel, weights2use = weights2use, tukeyMessage = tukeyMessage))
  
}

