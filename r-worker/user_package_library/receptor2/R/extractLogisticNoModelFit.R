# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################


#' extracts required stuff when no suitable model could be fitted
#' @param inputData a data frame with two columns: column one the numeric dose, and column2 the response
#' @param responseName string specifying the response name to be diplayed on the plot
#' @param isNarrowDataRange TRUE/FALSE, if TRUE that means range of observed responses is below validRespRange 
#' @param isNarrowEstimatedRange TRUE/FALSE, if TRUE mean estimated Top - estimated Bottom is below validRange.
#' @param isInvalidSlope  TRUE/FALSE, if TRUE that means the estimated slope does not agree with expected slope.
#' @param isInvalidpIC50 TRUE/FALSE, if TRUE means the model could be fittd but with an invalid pIC50, so it is not dislayed
#' @param inactiveSuperpotentParams a numeric vector of length 2. In cases where the response rage is below validRespRange, 
#' the first element of this vecotr shows the  response threshold to distinguish between inactive and suerpotent compounds, 
#' the second one shows the minimum proportion of response values that should be larger than this threshold to consider a compound superpotent. 
#' The default value for this argument is (50, 0.5) which means if more than half of data are larger than 50, then we consider the compound superpotent, 
#' and otherwise inactive.
#' @param slope the slope of the curve: either "ascending" or "descending"
#' @param inputDataPlot the complete inputData with accept column
#' @param validRespRange valid difference of minimum and maximum response (default is 30), if the range is smaller than this, no analysis will be done.
#' @param fixedBottom the fixed lower limit parameter of the logistic model, default is NULL, if provided then the lower limit will be fixed.
#' @param fixedTop the fixed upper limit parameter of the logistic model, default is NULL, if provided then the upper limit will be fixed.
#' @import ggplot2
#' @author Vahid Nassiri
#' @export

extractLogisticNoModelFit <- function(inputData, responseName, isNarrowDataRange = FALSE, isNarrowEstimatedRange = FALSE,
		isInvalidSlope = FALSE, isInvalidpIC50 = FALSE, inactiveSuperpotentParams, 
		slope = c("ascending", "descending"), inputDataPlot, validRespRange, fixedBottom, fixedTop){
	## find rangeRes for averaged response
	averagedResp <- aggregate(response~dose, data = inputData, mean)
	respMin2report <- min(averagedResp$response)
	whichMin2report <- which.min(averagedResp$response)
	respMax2report <- max(averagedResp$response)
	whichMax2report <- which.max(averagedResp$response)
	rangeRes2report <- averagedResp[c(whichMin2report, whichMax2report),]
	rownames(rangeRes2report) <- c("eMin", "eMax")
	## Find range
	respMin <- min(inputData$response)
	whichMin <- which.min(inputData$response)
	respMax <- max(inputData$response)
	whichMax <- which.max(inputData$response)
	rangeRes <- inputData[c(whichMin, whichMax),]
	rownames(rangeRes) <- c("eMin", "eMax")
	if (isNarrowDataRange){
		warningPD <- paste("The Observed span of the data [",round(max(inputData$response) - min(inputData$response),2), "] is below required value [",round(validRespRange,2),"].", sep = "")
	}else if(isNarrowEstimatedRange){
		warningPD <- "Estimated Top-Bottom is below required value."
	}else if(isInvalidSlope){
		warningPD <- "The estimated model does not follow the specified slope."
	}else{
		warningPD <- "The model could not be fitted."
	}

	residulaVariance <- NA
#	modelCoefs <- matrix(NA, 4,4)
#	rownames(modelCoefs) <- c("Slope", "Bottom", "Top", "-log10ED50")
#	colnames(modelCoefs) <- c("Estimate", "Std. Error", "t-value", "p-value")
#	confidenceInterval <- matrix(NA, 4, 2)
#	rownames(confidenceInterval) <- c("Slope", "Bottom", "Top", "-log10ED50")
#	colnames(confidenceInterval) <- c("LowerCI", "upperCI")
#	coef2return <- cbind(modelCoefs, confidenceInterval)
	## Now we can make a plot of the data and the fitted Model as well.
#	plot2return <- ggplot() + 
#			geom_point(data = inputData, aes_string(x = "dose", y = "response"), size = 1, shape = 1, color = "maroon") +
#			xlab("-log10(dose)")
	inputData2plot <- inputDataPlot
	inputData2plot$dose <- inputDataPlot$dose
	inputData2plot$accept <- as.character(inputData2plot$accept)
	plot2return <- ggplot() + 
			geom_point(data = inputData2plot, aes_string(x = "dose", y = "response", shape = "accept", color = "accept"), size = 2) +
			scale_shape_manual(values=c("0" = 4, "1" = 19),guide=FALSE) + scale_color_manual(values = c("0" = "red", "1" = "blue"), guide=FALSE)+
			xlab("log10(Molar)") + theme_bw() + ylab(responseName)
	## Find average per dose and add it to individual points plot
#		aggregatedRespose <- aggregate(response ~ dose, inputData, mean)
#		aggregatedRespose$dose <- unique(inputData$dose[!is.na(inputData$resp)])
#		plot2return <- plot2return + geom_point(data = aggregatedRespose, aes_string(x = "dose", y = "response"), 
#				size = 2.4, color = "darkorchid4", shape = 19)  
	##add fitted model to the plot
	## obtain breaks and labels 
	labels2use0 <- ggplot_build(plot2return+ scale_x_reverse())$layout$panel_params[[1]]$x.range
	breaks2use0 <- ggplot_build(plot2return)$layout$panel_params[[1]]$x.range
	## change them to half
	labels2use <- round(rev(seq(min(labels2use0), max(labels2use0), 0.5)),1)
	breaks2use <- seq(min(breaks2use0), max(breaks2use0), 0.5)
	plot2return <- plot2return + 
			scale_x_reverse(labels = labels2use,
					breaks = breaks2use) 
	### obtain current ticks
	yTicks <- ggplot_build(plot2return)$layout$panel_params[[1]]$y.range
	#plot2return <- plot2return + scale_y_continuous(breaks = scales::pretty_breaks(n = length(seq(min(yTicks), max(yTicks), 10))))
	#plot2return <- plot2return + scale_y_continuous(breaks = scales::pretty_breaks(n = length(seq(min(yTicks), max(yTicks), round((max(yTicks)-min(yTicks))/30)))))
	plot2return <- plot2return + scale_y_continuous(breaks = scales::pretty_breaks(n = 30))

	
	## adding eMin and eMax
	## Now asked by Frederick to replace the top and bottom horizontal lines by the fixed top and bottom, if they are there, otherwise we use data min and max as below
	if(is.na(fixedBottom)){
		plot2return <- plot2return + geom_hline(yintercept = respMin, color = "red4", linetype = "dotted", size = 1.2)
	}else{
		plot2return <- plot2return + geom_hline(yintercept = fixedBottom, color = "red4", linetype = "dotted", size = 1.2)
	}
	if(is.na(fixedTop)){
		plot2return <- plot2return + geom_hline(yintercept = respMax, color = "green4", linetype = "dotted", size = 1.2)
	}else{
		plot2return <- plot2return + geom_hline(yintercept = fixedTop, color = "green4", linetype = "dotted", size = 1.2)
	}
#	plot2return <- plot2return + geom_hline(yintercept = respMin, color = "red4", linetype = "dotted", size = 1.2) + 
#			geom_hline(yintercept = respMax, color = "green4", linetype = "dotted", size = 1.2)
	## effective doses
	respIC50 <- respIC20 <- respIC80 <- respICx<- rep(NA, 4)
	validpIC50 <- validpIC20 <- validpIC80 <- validpICx <- rep(NA, 4)
	checkpIC50 <- checkpIC20 <- checkpIC80 <- checkpICx <- rep(NA, 5)
	messagepIC50 <- messagepIC20 <- messagepIC80 <- messagepICx <- NA
	## Now we need to correct it for inactive and superpotent compound,
	## that will only include IC50 and not the other IC's.
	
	messagepIC50 <- compoundStatus(inputDataPlot[which(inputDataPlot$accept==1),], inactiveSuperpotentParams, slope)[2]
#	if ((isNarrowDataRange | isNarrowEstimatedRange )| isInvalidSlope){
#		if (sum(inputData$response>inactiveSuperpotentParams[1])/nrow(inputData) > inactiveSuperpotentParams[2]){
#			messagepIC50 <- paste(paste("pIC", 50, sep = ""), ">", round(max(inputData$dose),3))
#		}else{
#			messagepIC50 <- paste(paste("pIC", 50, sep = ""), "<", round(min(inputData$dose),3))
#		}
#	}
	IC <- c(NA, NA, NA)
	if (isInvalidpIC50){
		warningPD <- "Model could be fitted but the estimated pIC50 was not valid."
	}
	names(IC) <- c("AIC", "AICc","BIC")
	return(list(residulaVariance = residulaVariance, 
					plot2return = plot2return, 
					respIC50 = respIC50, validpIC50 = validpIC50, checkpIC50 = checkpIC50, messagepIC50 = messagepIC50,
					respIC20 = respIC20, validpIC20 = validpIC20, checkpIC20 = checkpIC20, messagepIC20 = messagepIC20,
					respIC80 = respIC80, validpIC80 = validpIC80, checkpIC80 = checkpIC80, messagepIC80 = messagepIC80,
					respICx = respICx, validpICx = validpICx, checkpICx = checkpICx, messagepICx = messagepICx,
					warningPD= warningPD, rangeRes = rangeRes2report, xIC = IC, dataPredict2Plot = NULL, pIC50Location = c(NA, NA),
					xAxisLabels = labels2use, xAxisBreaks = breaks2use))
}
