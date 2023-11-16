# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################

#' extracts required stuff from a fitted drc model
#' 
#' function to extract required stuff from a fitted drc model
#' @param fittedModel a drc object
#' @param inputData a data frame with two columns: column one the numeric dose, and column2 the response
#' @param coef2return scheme of the estimated coeffiient to return.
#' @param responseName string specifying the response name to be diplayed on the plot
#' @param confLevel confidence level for the confidence intervals and significance tests, default is 0.95.
#' @param slope the slope of the curve: either "ascending" or "descending"
#' @param inactiveSuperpotentParams a numeric vector of length 2. In cases where the response rage is below validRespRange, 
#' the first element of this vecotr shows the  response threshold to distinguish between inactive and suerpotent compounds, 
#' the second one shows the minimum proportion of response values that should be larger than this threshold to consider a compound superpotent. 
#' The default value for this argument is (50, 0.5) which means if more than half of data are larger than 50, then we consider the compound superpotent, 
#' and otherwise inactive.
#' @param inputDataPlot the complete inputData with accept column
#' @param respLevpICx scalar between 0 and 100 to specify response level for computing user-speficied pICx, for example  respLevpICx = 30 will return pIC30.
#' @param nPointsPredict scalar specifying the number of points to be used to make fitted curve with its confidence bands
#' @import drc
#' @import ggplot2
#' @author Vahid Nassiri
#' @export
extractLogisticModelFit <- function(fittedModel, inputData, coef2return, responseName, confLevel, slope = c("ascending", "descending"),
		inactiveSuperpotentParams, inputDataPlot, respLevpICx, nPointsPredict){
	## find rangeRes for averaged response
	averagedResp <- aggregate(response~dose, data = inputData, mean)
	respMin <- min(averagedResp$response)
	whichMin <- which.min(averagedResp$response)
	respMax <- max(averagedResp$response)
	whichMax <- which.max(averagedResp$response)
	rangeRes <- averagedResp[c(whichMin, whichMax),]
	rownames(rangeRes) <- c("eMin", "eMax")
	## compute AIC and BIC
	pAICc <- length(coef(fittedModel))
	nAICc <- nrow(fittedModel$data)
	coefAICc <- (2*pAICc*(pAICc+1))/(nAICc-pAICc-1)
	IC <- c(AIC(fittedModel), AIC(fittedModel) + coefAICc,BIC(fittedModel))
	names(IC) <- c("AIC", "AICc", "BIC")
	## Check of the covariance matrix if PD
	eigenValsCov <- try(eigen(vcov(fittedModel))$values)
	warningPD <- "The covariance matrix is positive definite."
	if (sum(eigenValsCov<0)>0){
		warningPD <- "The covariance matrix is not positive definite."
	}
	## obtain model coefficients with thier sifnificance tests, as well as 95% conficen interval
	modelCoefs <- suppressWarnings(try(summary(fittedModel)$coefficients))
	if (is.character(modelCoefs)){
		modelCoefs <- matrix(NA, length(coef(fittedModel)),4)
		rownames(modelCoefs) <- c("Slope", "Bottom", "Top", "-log10ED50")
		colnames(modelCoefs) <- c("Estimate", "Std. Error", "t-value", "p-value")
	}else{
		#rownames(modelCoefs) <- c("Slope", "Bottom", "Top", "-log10ED50")
		colnames(modelCoefs) <- c("Estimate", "Std. Error", "t-value", "p-value")
	}
	confidenceInterval <-  suppressWarnings(try(confint(fittedModel, level = confLevel)))
	if (is.character(confidenceInterval)){
		confidenceInterval <- matrix(NA, nrow(modelCoefs), 2)
		#rownames(confidenceInterval) <- c("Slope", "Bottom", "Top", "-log10ED50")
		colnames(confidenceInterval) <- c("LowerCI", "upperCI")
	}else{
		#rownames(confidenceInterval) <- c("Slope", "Bottom", "Top", "-log10ED50")
		colnames(confidenceInterval) <- c("LowerCI", "upperCI")
	}
	coef2return0 <- cbind(modelCoefs, confidenceInterval)
	## replace emoty cells in coef2return with coef2return0
	idxEmpty <- which(is.na(coef2return[,1]))
	coef2return[idxEmpty,] <- coef2return0
	rownames(coef2return) <- c("Slope", "Bottom", "Top", "-log10ED50")
	## Check if the model error is finite and positive
	residulaVariance <- suppressWarnings(try(summary(fittedModel)$resVar))
	if (is.character(residulaVariance)){
		residulaVariance <- NA
	}else{
		if (!is.finite(residulaVariance) | residulaVariance<=0){
			residulaVariance <- "Residulal variance is negative or infinite"
		}
	}
	## Now we can make a plot of the data and the fitted Model as well.
	inputData2plot <- inputDataPlot
	inputData2plot$dose[inputData2plot$accept == 1] <- inputData$dose
	inputData2plot$dose[inputData2plot$accept == 0] <- log(10^inputData2plot$dose[inputData2plot$accept == 0])
	
	inputData2plot$accept <- as.character(inputData2plot$accept)
	plot2return <- ggplot() + 
			geom_point(data = inputData2plot, aes_string(x = "dose", y = "response", shape = "accept", color = "accept", size = "weights"), alpha = 0.8, show.legend = FALSE) +
			scale_shape_manual(values=c("0" = 4, "1" = 19),guide=FALSE) + scale_color_manual(values = c("0" = "red", "1" = "blue"), guide=FALSE)+
			xlab("log10(Molar)") + theme_bw() + ylab(responseName) 
	## Find average per dose and add it to individual points plot
#		aggregatedRespose <- aggregate(response ~ dose, inputData, mean)
#		aggregatedRespose$dose <- unique(inputData$dose[!is.na(inputData$resp)])
#		plot2return <- plot2return + geom_point(data = aggregatedRespose, aes_string(x = "dose", y = "response"), 
#				size = 2.4, color = "darkorchid4", shape = 19)  
	##add fitted model to the plot
	x <- seq(min(inputData$dose),max(inputData$dose),abs(min(inputData$dose)-max(inputData$dose))/(nPointsPredict-1))
	dataPred <- suppressWarnings(data.frame(dose = x, response = predict(fittedModel, newdata = data.frame(dose = x))))
	dataPred2plot <- dataPred
	## see if we want to make it negative
	dataPred2plot$dose <- dataPred$dose
	plot2return <- plot2return + geom_line(data = dataPred, aes(x = dose, y = response), color = "cornflowerblue", size = 1.3)
	## now add confidence bounds if a PD cpvariance matrix has been obtained.
	if (warningPD == "The covariance matrix is positive definite."){
		dataPredBounds <- suppressWarnings(try(data.frame(dose = x, predict(fittedModel, newdata = data.frame(dose = x), interval = "confidence"))))
		if (!is.character(dataPredBounds)){
			plot2return <- plot2return + geom_ribbon(data = dataPredBounds, aes(x = dose,  ymin = Lower, ymax = Upper), 
					alpha = 0.2, fill = "yellow")
		}
	}else{
		dataPredBounds <- dataPred
	}
	plot2return <- plot2return + geom_point(data = inputData2plot, aes_string(x = "dose", y = "response", shape = "accept", color = "accept"), size = 2) 
	plot2return <- plot2return + geom_line(data = dataPred, aes(x = dose, y = response), color = "cornflowerblue", size = 1.3)
	## Now we add the parameters to the plot
	if (!is.na(coef2return[2,1])){
		plot2return <- plot2return + geom_hline(yintercept = coef2return[2,1], color = "red4", linetype = "dotted", size = 1.2)
	}
	if (!is.na(coef2return[3,1])){
		plot2return <- plot2return + geom_hline(yintercept = coef2return[3,1], color = "green4", linetype = "dotted", size = 1.2)
	}
	if (!is.na(coef2return[4,1])){
		if (coef2return[4,1]>min(inputData$dose) & coef2return[4,1]<max(inputData$dose)){
			xpos <- coef2return[4,1]
			ypos <- suppressWarnings(predict(fittedModel,newdata = data.frame(dose = xpos)))
			pIC50Location <- c(xpos, ypos)
			yRange <- ggplot_build(plot2return)$layout$panel_params[[1]]$y.range 
			xRange <- ggplot_build(plot2return)$layout$panel_params[[1]]$x.range 
			plot2return <- plot2return + 
					geom_segment(aes(x = xpos, y = -Inf, xend = xpos, yend = ypos), linetype = "dotted", size = 1.2) +
					geom_segment(aes(x = xpos, y = ypos, xend = +Inf, yend = ypos), linetype = "dotted", size = 1.2) 
			
		}else{
			pIC50Location <- c(NA, NA)
		}
	}else{
		pIC50Location <- c(NA, NA)
	}
	## obtain breaks and labels 
	labels2use0 <- ggplot_build(plot2return+ scale_x_reverse())$layout$panel_params[[1]]$x.range
	breaks2use0 <- ggplot_build(plot2return)$layout$panel_params[[1]]$x.range
	## change them to half
	labels2use <-  round(log10(exp(rev(seq(min(labels2use0), max(labels2use0), 0.5)))), 1)
	breaks2use <- seq(min(breaks2use0), max(breaks2use0), 0.5)
	plot2return <- plot2return + 
			scale_x_reverse(labels = labels2use,
					breaks = breaks2use) 
	## ticks for y
	### obtain current ticks
	yTicks <- ggplot_build(plot2return)$layout$panel_params[[1]]$y.range
#	plot2return <- plot2return + scale_y_continuous(breaks = scales::pretty_breaks(n = length(seq(min(yTicks), max(yTicks), 10)))) # used to be this
	#plot2return <- plot2return + scale_y_continuous(breaks = scales::pretty_breaks(n = length(seq(min(yTicks), max(yTicks), round((max(yTicks)-min(yTicks))/30)))))
	plot2return <- plot2return + scale_y_continuous(breaks = scales::pretty_breaks(n = 30))
	## If ED 50 could be computed within the range add it to the plot
	## Note that, once we agree on censoring rules, we will directly apply the on ED50 and if it passes, then we 
	## report it. 
	## Obtain ED50 results: we knwo always trhe last row is ED50
	resED50 <- coef2return[nrow(coef2return),c(1,2,5,6)]
	#validED50 <- validEDx(resED50, inputData$dose, 50)
	## For transformed values
	validED50 <- validEDx(transformEstimates(resED50, fittedModel), inputDataPlot$dose, 50)
	
	## ED20
	resED20 <- suppressWarnings(ED(fittedModel, respLev = 20, interval = "delta", display = FALSE))
	#validED20 <- validEDx(resED20, inputData$dose, 20)
	validED20 <- validEDx(transformEstimates(resED20, fittedModel), inputDataPlot$dose, 20)
	## ED80
	resED80 <- suppressWarnings(ED(fittedModel, respLev = 80, interval = "delta", display = FALSE))
	#validED80 <- validEDx(resED80, inputData$dose, 80)
	validED80 <- validEDx(transformEstimates(resED80, fittedModel), inputDataPlot$dose, 80)

	## EDx
	if (!is.null(respLevpICx)){
		resEDx <- suppressWarnings(ED(fittedModel, respLev = respLevpICx, interval = "delta", display = FALSE))
		#validEDx <- validEDx(resEDx, inputData$dose, respLevpICx)
	validEDx <- validEDx(transformEstimates(resEDx, fittedModel), inputDataPlot$dose, respLevpICx)
	}else{
		resEDx <- rep(NA, 4)
		validEDx <- list()
		validEDx$isSaneEDxRes <- rep(NA, 5)
		validEDx$EDxMessage <- NULL
	}

	## For IC50 alone, we make the message outselves: based on the created flowchart
	if (is.na(resED50[1])){
		slope2use <- determineTheSlope(inputData, slope, coef2return[1,1])
		messagepIC50 <- compoundStatus(inputDataPlot[which(inputDataPlot$accept==1),], inactiveSuperpotentParams, slope2use)[2]
	}else{
		if (resED50[1]>= sort(unique(inputData$dose))[2] & resED50[1] <= max(inputData$dose)){
			messagepIC50 <- paste(round(log10(exp(resED50[1])),3))
		}else if(resED50[1]>= min(inputData$dose) & resED50[1] < sort(unique(inputData$dose))[2]){
			messagepIC50 <- paste("<", round(sort(unique(inputDataPlot$dose[which(inputDataPlot$accept==1)]))[2],3))
		}else{
			slope2use <- determineTheSlope(inputData, slope, coef2return[1,1])
			messagepIC50 <- compoundStatus(inputDataPlot[which(inputDataPlot$accept==1),], inactiveSuperpotentParams, slope2use)[2]
		}
	}
	return(list(coef2return = coef2return, residulaVariance = residulaVariance, 
					plot2return = plot2return, 
					respIC50 = resED50, validpIC50 = validED50$validpICx, checkpIC50 = validED50$isSaneEDxRes, messagepIC50 = messagepIC50,
					respIC20 = resED20, validpIC20 = validED20$validpICx, checkpIC20 = validED20$isSaneEDxRes, messagepIC20 = validED20$EDxMessage,
					respIC80 = resED80, validpIC80 = validED80$validpICx, checkpIC80 = validED80$isSaneEDxRes, messagepIC80 = validED80$EDxMessage,
					respICx = resEDx, validpICx = validEDx$validpICx, checkpICx = validEDx$isSaneEDxRes, messagepICx = validEDx$EDxMessage,
					warningPD= warningPD, rangeRes = rangeRes, xIC = IC, dataPredict2Plot = dataPredBounds, pIC50Location = pIC50Location, 
					xAxisLabels = labels2use, xAxisBreaks = breaks2use))
}
