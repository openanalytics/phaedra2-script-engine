# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################


#' fits a logistic model to the dose-response data
#' 
#' function to fit a logisitic model to the data already in log-sclae
#' @param inputData a data frame with two columns: column one the numeric dose, and column2 the response
#' @param fixedBottom the fixed lower limit parameter of the logistic model, default is NULL, if provided then the lower limit will be fixed.
#' @param fixedTop the fixed upper limit parameter of the logistic model, default is NULL, if provided then the upper limit will be fixed.
#' @param fixedSlope the fixed hill slope parameter of the logistic model, default is NULL, if provided then the hill slope will be fixed.
#' @param confLevel confidence level for the confidence intervals and significance tests, default is 0.95.
#' @param robustMethod character with three possibilities: mean for the non-robust fit (default) and tukey and median for the robust fit.
#' @param responseName string specifying the response name to be diplayed on the plot
#' @param slope takes values from "ascending","descending", "free", specifying the expected pattern.
#' @param validRespRange valid difference of minimum and maximum response (default is 30), if the range is smaller than this, no analysis will be done.
#' @param receptorStyleModel the general model to fit is a logistic model, and one can fix different parameters of it via fixedBottom, 
#' fixedTop, and fixedSlope. However, if case one is intersted to use the style in receptor package, then the model can be specified here. One may selected a model from  "PL4", "PL3L", "PL3U", "2PL", "PL4H1", "PL3LH1", "PL3UH1", "PL2H1", similar to what we have in receptor. 
#' If this argument is left empty, then the drc concentions are used.   
#' @param inactiveSuperpotentParams a numeric vector of length 2. In cases where the response rage is below validRespRange, 
#' the first element of this vecotr shows the  response threshold to distinguish between inactive and suerpotent compounds, 
#' the second one shows the minimum proportion of response values that should be larger than this threshold to consider a compound superpotent. 
#' The default value for this argument is (50, 0.5) which means if more than half of data are larger than 50, then we consider the compound superpotent, 
#' and otherwise inactive.
#' @param accept a vector the same length as the sample size (including replications) with 1's and 0's. Zero's will correspond to the points that will excluded 
#' from the analysis. If nothing is given then all the points are used.
#' @param respLevpICx scalar between 0 and 100 to specify response level for computing user-speficied pICx, for example  respLevpICx = 30 will return pIC30.
#' @param nPointsPredict scalar specifying the number of points to be used to make fitted curve with its confidence bands
#' @return a list with the following objects
#' respIC50 a vector with raw estimated pIC50 results
#' validpIC50 a vector with valid pIC50 results (after sanity checks)
#' checkpIC50 results of sanity checks
#' respIC20 a vector with raw estimated pIC20 results
#' validpIC20 a vector with valid pIC20 results (after sanity checks)
#' checkpIC20 results of sanity checks
#' respIC80 a vector with raw estimated pIC80 results
#' validpIC80 a vector with valid pIC80 results (after sanity checks)
#' checkpIC80 results of sanity checks
#' respICx a vector with raw estimated pICx results
#' validpICx a vector with valid pICx results (after sanity checks)
#' checkpICx results of sanity checks
#' modelCoefs a matrix with model parameter estimates, standard errors, significance tests and confidence intervals.
#' residulaVariance scalar providing error variance
#' warningPD character showing if the parameter covariance matrix is positive definite or not.
#' fittedModel a drc object of the fitted model
#' plot a ggplot2 object with the plotted data, fitted model, confidence bands and pIC50.
#' AIC, AICc and BIC of the fitted model.
#' dataPredict2Plot a data frame with predicted values for the curve and its confidence bands (if they can estimated)
#' pIC50Location a vector of length 2 with the coordinates of pIC50 on x and y axis for plotting (if they can be estimated)
#' xAxisLabels as the doses are transformed a couple of times, the real values on x-axis are different from the ones which are plotted, so to make it easier for plotting later on the labels we used are also provided.
#' xAxisBreaks x axix breaks used to produce the plot are also provided.
#' weights if robustMethod = "tukey" then the obtained weights are returned. The same weights are used to make the plots (size of the points).
#' tukeyMessage a message showing the convergence situation of the tukey biweight (obustMethod = "tukey")
#' @import drc
#' @import ggplot2
#' @author Vahid Nassiri
#' @export
fittingLogisticModel <- function(inputData, fixedBottom = NA, fixedTop = NA, fixedSlope = NA, confLevel = 0.95, 
		robustMethod = c("mean", "tukey", "median"), responseName, slope = c("ascending","descending", "free"), validRespRange = 30, 
		receptorStyleModel = c("PL4", "PL3L", "PL3U", "2PL", "PL4H1", "PL3LH1", "PL3UH1", "PL2H1"), inactiveSuperpotentParams = c(50, 0.5),
		accept = NULL, respLevpICx = NULL, nPointsPredict = 100){
	## Check if we have at least two different dose
	uniqDoses <- unique(inputData$dose)
	if (length(uniqDoses)<2){
		stop(paste("The provided dose only takes:", uniqDoses))
	}
	## checking arguments
	robustMethod <- match.arg(robustMethod)	
	if (robustMethod == "tukey"){
		robustMethod <- "mean"
		useWeights <- TRUE
	}else{
		useWeights <- FALSE
	}
	slope <- match.arg(slope)	
	if (missing(receptorStyleModel)){
		receptorStyleModel <- NULL
	}else{
		receptorStyleModel <- match.arg(receptorStyleModel)	
	}
	if (confLevel<0 | confLevel>1){
		stop("confLevel should be in (0,1)")
	}
	if (is.null(accept)){
		accept <- rep(1, nrow(inputData))
	}
	## Adding weights
	inputData$weights <- rep(1, nrow(inputData))
	## Chaning base of log
	inputDataPlot <- inputData
	inputData$dose <- log(10^inputData$dose)
	
	
	
	#inputDataPlot <- inputDataOrig
	inputDataPlot$accept <- accept
	inputData <- inputData[which(accept == 1),]
	## Making the model
	if (!is.null(receptorStyleModel)){
		if (receptorStyleModel == "PL4"){
			fixedSlope <- NA
			fixedBottom <- NA
			fixedTop <- NA
		}else if(receptorStyleModel == "PL3L"){
			fixedSlope <- NA
			fixedTop <- NA
			if (is.na(fixedBottom)){
				stop("You have selected PL3L, please provide a fixed bottom via fixedBottom.")
			}
		}else if(receptorStyleModel == "PL3U"){
			fixedSlope <- NA
			fixedBottom <- NA
			if (is.na(fixedTop)){
				stop("You have selected PL3U, please provide a fixed top via fixedTop.")
			}
		}else if(receptorStyleModel == "2PL"){
			if(any(is.na(c(fixedBottom, fixedTop)))){
				stop("You have selected 2PL, please provide a fixed top and a fixed bottom via fixedTop and fixedBottom, respectively.")
			}
		}else if(receptorStyleModel == "PL4H1"){
			fixedSlope <- 1
			fixedBottom <- NA
			fixedTop <- NA
		}else if(receptorStyleModel == "PL3LH1"){
			fixedSlope <- 1
			fixedTop <- NA
			if (is.na(fixedBottom)){
				stop("You have selected PL3LH1, please provide a fixed bottom via fixedBottom.")
			}
		}else if(receptorStyleModel == "PL3UH1"){
			fixedSlope <- 1
			fixedBottom <- NA
			if (is.na(fixedTop)){
				stop("You have selected PL3UH1, please provide a fixed top via fixedTop.")
			}
		}else if(PL3UH1 == "PL2H1"){
			fixedSlope <- 1
			if(any(is.na(c(fixedBottom, fixedTop)))){
				stop("You have selected PL2H1, please provide a fixed top and a fixed bottom via fixedTop and fixedBottom, respectively.")
			}
		}
	}
	## Specify tukey message
	tukeyMessage <- NULL
	## Create the empty coef2return
	modelCoefs <- matrix(NA, 4,4)
	rownames(modelCoefs) <- c("Slope", "Bottom", "Top", "-log10ED50")
	colnames(modelCoefs) <- c("Estimate", "Std. Error", "t-value", "p-value")
	confidenceInterval <- matrix(NA, 4, 2)
	rownames(confidenceInterval) <- c("Slope", "Bottom", "Top", "-log10ED50")
	colnames(confidenceInterval) <- c("LowerCI", "upperCI")
	coef2return <- cbind(modelCoefs, confidenceInterval)
	coef2return[1:3,1] <- c(fixedSlope, fixedBottom, fixedTop)
	coef2returnInitial <- coef2return
	## If range of the data is smaller than 30 we just do nothing.
	if ((max(inputData$response) - min(inputData$response)) < validRespRange){
		slope2use <- determineTheSlope(inputData, slope)
		res2return <- extractLogisticNoModelFit(inputData, responseName, isNarrowDataRange = TRUE, isNarrowEstimatedRange = FALSE, 
				isInvalidSlope = FALSE,isInvalidpIC50 = FALSE, inactiveSuperpotentParams, slope = slope2use, inputDataPlot, validRespRange, fixedBottom, fixedTop)
		res2return$coef2return <- coef2return
		slopeWarning <- NULL
		fittedModel <- NULL
	}else{
		## Note that, in the attached codes fct is fixed as l4, which is 4parameter log-logistic model, I think it's not a good
		## idea to use log-logistic model, as the dose is alrweady in log, and if we do log-logistic, it's yet another log. 
		fittedModel <- try(drm(response~dose, data = inputData, fct = L.4(fixed = c(fixedSlope, fixedBottom, fixedTop, NA), 
								names = c("Slope", "Bottom", "Top", "-logED50")), robust = robustMethod))
		if (is.character(fittedModel)){
			## In case it does not converge, we use a simpler model.
			fittedModel <- try(drm(response~dose, data = inputData, fct = L.4(fixed = c(fixedSlope, fixedBottom, fixedTop, NA), 
									names = c("Slope", "Bottom", "Top", "-logED50")), 
							robust = robustMethod, control = drmc(method = "SANN")))
		}
		## If weights should be used we iteratively fit the model
		
		if (useWeights){
			if (!is.character(fittedModel)){
				estDiff <- estDiffOld <- sum(coef(fittedModel)^2)
				fittedModelOld <- fittedModel
				tukeyConstant <- 4.685061
				counter <- 0
				isCoverging <- TRUE
				while(estDiff>0.0001 & counter <1000 & isCoverging){
					counter <- counter + 1
					weightConstant <- tukeyConstant * mad(residuals(fittedModel), 0)
					weights2use <- (1-((residuals(fittedModel)/weightConstant)^2))^2
					weights2use[which(abs(residuals(fittedModel))>=weightConstant)] <- 0
					fittedModel <- try(drm(response~dose, data = inputData, fct = L.4(fixed = c(fixedSlope, fixedBottom, fixedTop, NA), 
											names = c("Slope", "Bottom", "Top", "-logED50")), robust = robustMethod, weights = weights2use))
					if (is.character(fittedModel)){
						## In case it does not converge, we use a simpler model.
						fittedModel <- try(drm(response~dose, data = inputData, fct = L.4(fixed = c(fixedSlope, fixedBottom, fixedTop, NA), 
												names = c("Slope", "Bottom", "Top", "-logED50")), 
										weights = weights2use,
										robust = robustMethod, control = drmc(method = "SANN")))
					}
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
					tukeyMessage <- paste("The procedure converged in", counter, "steps.")
				}else{
					tukeyMessage <- "This procedure could not converge, the best results that could be obtained are provided."
				}
#				inputData$weights <- weights2use
				inputDataPlot$weights <- rep(1, nrow(inputDataPlot))
				inputDataPlot$weights[which(accept ==1)] <- weights2use
#				if (any(weights2use ==0)){
#					inputDataPlot$accept[which(weights2use ==0)] <- 0
#				}

			}
			
		}
			
		
		## iteratively model fitting ends here
		
		
		if (is.character(fittedModel)){
			slope2use <- determineTheSlope(inputData, slope)
			res2return <- extractLogisticNoModelFit(inputData, responseName, isNarrowDataRange = FALSE, isNarrowEstimatedRange = FALSE, 
					isInvalidSlope = FALSE,isInvalidpIC50 = FALSE, inactiveSuperpotentParams, slope = slope2use, inputDataPlot, validRespRange, fixedBottom, fixedTop)
			res2return$coef2return <- coef2return
			slopeWarning <- NULL
		}else{
			## finding slope to check the specified slope
			if (is.na(fixedSlope)){
				estimatedSlope <- coef(fittedModel)[1]
				## now check the slope
				if (slope=="free"){
					## Now we check the estimated range
					estRange0 <- estimateRange(fittedModel, fixedBottom, fixedTop)
					#estRange <- max(estRange0) - min(estRange0)
					estRange <- estRange0[2]-estRange0[1]
					if (estRange > validRespRange){
						if (!exists("estimatedSlope")){
							estimatedSlope <- NULL
						}
						isED50RangeFine <- checkED50Range(fittedModel, inputData, slope, estimatedslope, inactiveSuperpotentParams)
						if (isED50RangeFine){
							slope2use <- determineTheSlope(inputData, slope, estimatedSlope)
							res2return <- extractLogisticModelFit(fittedModel, inputData, coef2return, responseName, confLevel, slope = slope2use,
									inactiveSuperpotentParams, inputDataPlot, respLevpICx, nPointsPredict)
							slopeWarning <- NULL
						}else{
							slope2use <- determineTheSlope(inputData, slope)
							res2return <- extractLogisticNoModelFit(inputData, responseName, isNarrowDataRange = FALSE, isNarrowEstimatedRange = FALSE, 
									isInvalidSlope = FALSE, isInvalidpIC50 = TRUE, inactiveSuperpotentParams, slope = slope2use, inputDataPlot, validRespRange, fixedBottom, fixedTop)
							slopeWarning <- NULL
						}
					}else{
						slope2use <- determineTheSlope(inputData, slope)
						res2return <- extractLogisticNoModelFit(inputData, responseName, isNarrowDataRange = FALSE, isNarrowEstimatedRange = TRUE, 
								isInvalidSlope = FALSE, isInvalidpIC50 = FALSE, inactiveSuperpotentParams, slope = slope2use, inputDataPlot, validRespRange, fixedBottom, fixedTop)
						res2return$coef2return <- coef2return
						res2return$warningPD <- paste("Estimated Top-Bottom [", round(estRange,2),"] is below the required value [",validRespRange,"].", sep = "")
						slopeWarning <- NULL
					}
				}else if(slope == "ascending" & estimatedSlope >=0){
					## Now we check the estimated range
					estRange0 <- estimateRange(fittedModel, fixedBottom, fixedTop)
					#estRange <- max(estRange0) - min(estRange0)
					estRange <- estRange0[2]-estRange0[1]
					if (estRange > validRespRange){
						if (!exists("estimatedSlope")){
							estimatedSlope <- NULL
						}
						isED50RangeFine <- checkED50Range(fittedModel, inputData, slope, estimatedslope, inactiveSuperpotentParams)
						if (isED50RangeFine){
							slope2use <- determineTheSlope(inputData, slope, estimatedSlope)
							res2return <- extractLogisticModelFit(fittedModel, inputData, coef2return, responseName, confLevel, slope = slope2use,
									inactiveSuperpotentParams, inputDataPlot, respLevpICx, nPointsPredict)
							slopeWarning <- NULL
						}else{
							slope2use <- determineTheSlope(inputData, slope)
							res2return <- extractLogisticNoModelFit(inputData, responseName, isNarrowDataRange = FALSE, isNarrowEstimatedRange = FALSE, 
									isInvalidSlope = FALSE, isInvalidpIC50 = TRUE, inactiveSuperpotentParams, slope = slope2use, inputDataPlot, validRespRange, fixedBottom, fixedTop)
							slopeWarning <- NULL
						}
						
					}else{
						slope2use <- determineTheSlope(inputData, slope)
						res2return <- extractLogisticNoModelFit(inputData, responseName, isNarrowDataRange = FALSE, isNarrowEstimatedRange = TRUE, 
								isInvalidSlope = FALSE, isInvalidpIC50 = FALSE,inactiveSuperpotentParams, slope = slope2use, inputDataPlot, validRespRange, fixedBottom, fixedTop)
						res2return$coef2return <- coef2return
						res2return$warningPD <- paste("Estimated Top-Bottom [", round(estRange,2),"] is below the required value [",validRespRange,"].", sep = "")
						slopeWarning <- NULL
					}
				}else if(slope == "descending" & estimatedSlope <=0){
					## Now we check the estimated range
					estRange0 <- estimateRange(fittedModel, fixedBottom, fixedTop)
					#estRange <- max(estRange0) - min(estRange0)
					estRange <- estRange0[2]-estRange0[1]
					if (estRange > validRespRange){
						if (!exists("estimatedSlope")){
							estimatedSlope <- NULL
						}
						isED50RangeFine <- checkED50Range(fittedModel, inputData, slope, estimatedslope, inactiveSuperpotentParams)
						if (isED50RangeFine){
							slope2use <- determineTheSlope(inputData, slope, estimatedSlope)
							res2return <- extractLogisticModelFit(fittedModel, inputData, coef2return, responseName, confLevel, slope = slope2use,
									inactiveSuperpotentParams, inputDataPlot, respLevpICx, nPointsPredict)
							slopeWarning <- NULL
						}else{
							slope2use <- determineTheSlope(inputData, slope)
							res2return <- extractLogisticNoModelFit(inputData, responseName, isNarrowDataRange = FALSE, isNarrowEstimatedRange = FALSE, 
									isInvalidSlope = FALSE, isInvalidpIC50 = TRUE, inactiveSuperpotentParams, slope = slope2use, inputDataPlot, validRespRange, fixedBottom, fixedTop)
							slopeWarning <- NULL
						}
						
					}else{
						slope2use <- determineTheSlope(inputData, slope)
						res2return <- extractLogisticNoModelFit(inputData, responseName, isNarrowDataRange = FALSE, isNarrowEstimatedRange = TRUE, 
								isInvalidSlope = FALSE, isInvalidpIC50 = FALSE, inactiveSuperpotentParams, slope = slope2use, inputDataPlot, validRespRange, fixedBottom, fixedTop)
						res2return$coef2return <- coef2return
						res2return$warningPD <- paste("Estimated Top-Bottom [", round(estRange,2),"] is below the required value [",validRespRange,"].", sep = "")
						slopeWarning <- NULL
					}
				}else{
					slope2use <- determineTheSlope(inputData, slope)
					res2return <- extractLogisticNoModelFit(inputData, responseName, isNarrowDataRange = FALSE, isNarrowEstimatedRange = FALSE, 
							isInvalidSlope = TRUE,isInvalidpIC50 = FALSE, inactiveSuperpotentParams, slope = slope2use, inputDataPlot, validRespRange, fixedBottom, fixedTop)
					res2return$coef2return <- coef2return
					slopeWarning <- "The estimated model does not follow the specified slope."
				}
			}else{
				## Now we check the estimated range
				estRange0 <- estimateRange(fittedModel, fixedBottom, fixedTop)
				#estRange <- max(estRange0) - min(estRange0)
				estRange <- estRange0[2]-estRange0[1]
				if (estRange > validRespRange){
					if (!exists("estimatedSlope")){
						estimatedSlope <- NULL
					}
					isED50RangeFine <- checkED50Range(fittedModel, inputData, slope, estimatedslope, inactiveSuperpotentParams)
					if (isED50RangeFine){
						slope2use <- determineTheSlope(inputData, slope, estimatedSlope)
						res2return <- extractLogisticModelFit(fittedModel, inputData, coef2return, responseName, confLevel, slope = slope2use,
								inactiveSuperpotentParams, inputDataPlot, respLevpICx, nPointsPredict)
						slopeWarning <- NULL
					}else{
						slope2use <- determineTheSlope(inputData, slope)
						res2return <- extractLogisticNoModelFit(inputData, responseName, isNarrowDataRange = FALSE, isNarrowEstimatedRange = FALSE, 
								isInvalidSlope = FALSE, isInvalidpIC50 = TRUE, inactiveSuperpotentParams, slope = slope2use, inputDataPlot, validRespRange, fixedBottom, fixedTop)
						slopeWarning <- NULL
					}
				}else{
					slope2use <- determineTheSlope(inputData, slope)
					res2return <- extractLogisticNoModelFit(inputData, responseName, isNarrowDataRange = FALSE, isNarrowEstimatedRange = TRUE, 
							isInvalidSlope = FALSE, isInvalidpIC50 = FALSE,inactiveSuperpotentParams, slope = slope2use, inputDataPlot, validRespRange, fixedBottom, fixedTop)
					res2return$coef2return <- coef2return
					res2return$warningPD <- paste("Estimated Top-Bottom [", round(estRange,2),"] is below the required value [",validRespRange,"].", sep = "")
					slopeWarning <- NULL
				}
			}
		}
	}
	pIC50toReport <- res2return$messagepIC50 
	## Final check for inactive/super-potent
	## This will only be applicable for the cases where none of the no model fit situations are occurring:
	### the span is below 30
	###  the estimated TOP-BOTTOM is below 30
	###  when slope is opposite of what is expected
	###  when IC50 is estimated to be below concentration range in case of inactive compound
	### in case of ascending curve inactive compound is defined as a compound for which more than 50% of the PIN values stay below 50
	### in case of descending curve inactive compound is defined as a compound for which more than 50% of the PIN values stay above 50
#	if (!is.character(fittedModel)){
#		
#	}
	## chaing scale for the reported ICx's
	res2return$respIC50 <- transformEstimates(res2return$respIC50, fittedModel)
	res2return$respIC20 <- transformEstimates(res2return$respIC20, fittedModel)
	res2return$respIC80 <- transformEstimates(res2return$respIC80, fittedModel)
	res2return$respICx <- transformEstimates(res2return$respICx, fittedModel)
	
	## Transform IC50 in the modelCoefs
	if (is.null(res2return$coef2return) | identical(coef2returnInitial, res2return$coef2return)){
		res2return$coef2return <- modelCoefs
	}else{
		if (!is.na(res2return$coef2return[4,1])){
			tmpIC50 <- res2return$respIC50[1]
		}else{
			tmpIC50 <- NA
		}
		if (!is.na(res2return$coef2return[4, 2])){
			tmpIC50 <- c(tmpIC50, res2return$respIC50[2])
			tmpTval <- res2return$respIC50[1]/res2return$respIC50[2]
			tmpPvalue <- 2*pt(-abs(tmpTval), fittedModel$df.residual)		
		}else{
			tmpIC50 <- c(tmpIC50, NA)
			tmpTval <- NA
			tmpPvalue <- NA
		}
		if (!is.na(res2return$coef2return[4,5])){
			tmpLower <- tmpIC50[1]-(tmpIC50[2] *qt(0.975, fittedModel$df.residual))
		}else{
			tmpLower <- NA
		}
		if (!is.na(res2return$coef2return[4,6])){
			tmpUpper <- tmpIC50[1]+(tmpIC50[2] *qt(0.975, fittedModel$df.residual))
		}else{
			tmpUpper <- NA
		}
		res2return$coef2return[4,] <- c(tmpIC50, tmpTval, tmpPvalue, tmpLower, tmpUpper)
	}
	## Weights to return
	if (exists("weights2use")){
		weights2return <- weights2use
	}else{
		weights2return <- rep(1, nrow(inputData))
	}
	
	##
	return(list(pIC50toReport = pIC50toReport,
					respIC50 = res2return$respIC50, validpIC50 = res2return$validpIC50, checkpIC50 = res2return$checkpIC50, messagepIC50 = res2return$messagepIC50,
					respIC20 = res2return$respIC20, validpIC20 = res2return$validpIC20, checkpIC20 = res2return$checkpIC20, messagepIC20 = res2return$messagepIC20,
					respIC80 = res2return$respIC80, validpIC80 = res2return$validpIC80, checkpIC80 = res2return$checkpIC80, messagepIC80 = res2return$messagepIC80,
					respICx = res2return$respICx, validpICx = res2return$validpICx, checkpICx = res2return$checkpICx, messagepICx = res2return$messagepICx,
					modelCoefs = res2return$coef2return, 
					residulaVariance = res2return$residulaVariance, warningFit = res2return$warningPD, 
					fittedModel = fittedModel, plot = res2return$plot2return, slopeWarning = slopeWarning, 
					rangeResults = res2return$rangeRes, xIC = res2return$xIC, dataPredict2Plot = res2return$dataPredict2Plot, 
					pIC50Location = res2return$pIC50Location,
					xAxisLabels = res2return$xAxisLabels, xAxisBreaks = res2return$xAxisBreaks,
					weights = weights2return, 
					tukeyMessage = tukeyMessage))
}


