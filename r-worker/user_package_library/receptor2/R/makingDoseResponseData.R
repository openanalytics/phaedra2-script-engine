# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################


#' creates suitable dose-response data out of the provided Excel files
#' 
#' function to create suitabel data frames with dose-response data to be used in fittingLogisticModel function.
#' @param assayData the outcome of the Excel file.
#' 
#' @return a list with suitabel dose-response data frames as its elements.
#' 
#' @author Vahid Nassiri
#' @export
makingDoseResponseData <- function(assayData, aggregatePlates = TRUE){
	## We assume the names are always like that, if not
	## this needs to be corrected. If we only know a uniqe part of 
	## the name then we may use strEndsWith function.
	colnames <- colnames(assayData)
	doseCol <- which(colnames == "CONCENTRATION")
	compNameCol <- which(colnames == "COMP_NAME")
	## again, here we assume two responses exxists, only when we have a colum called "Cell.Count.RAW_VALUE".
	## That could be wrong and needs to be adjusted.
	resp1Col <- which(colnames == "Cell.Count.RAW_VALUE") 
	resp2Col <- which(unlist(lapply(colnames, strEndsWith, "PIN.pos.median.NORMALIZED")))
	## Now obtain all compound names
	compNames <- unique(assayData[complete.cases(assayData[,compNameCol]),compNameCol])
	## Prepare names to give to the returned list of suitable data
	if (length(resp1Col) >0){
		names2use <- c(t(cbind(paste(compNames, "Cell.Count.RAW_VALUE",sep = "_"), 
				paste(compNames, "PIN.pos.median.NORMALIZED",sep = "_"))))
	}else{
		names2use <- compNames
	}
	data2return <- list()
	count <- 0
	for (iComp in compNames){
		count <- count + 1
		if (length(resp1Col) >0){
			data01 <- data.frame(dose = assayData[which(assayData[,compNameCol] == iComp),doseCol],
					response = assayData[which(assayData[,compNameCol] == iComp),resp1Col])
			data02 <- data.frame(dose = assayData[which(assayData[,compNameCol] == iComp),doseCol],
					response = assayData[which(assayData[,compNameCol] == iComp),resp2Col])
			if (aggregatePlates){
				aggData01 <- aggregate(response~dose, data01, mean)
				aggData02 <- aggregate(response~dose, data02, mean)
				data2return[[count]] <- aggData01
				count <- count + 1
				data2return[[count]] <- aggData02
			}else{
				data2return[[count]] <- data01
				count <- count + 1
				data2return[[count]] <- data02
			}
		}else{
			data0 <- data.frame(dose = assayData[which(assayData[,compNameCol] == iComp),doseCol],
					response = assayData[which(assayData[,compNameCol] == iComp),resp2Col])
			if (aggregatePlates){
				data2return[[count]] <- aggregate(response~dose, data0, mean)
			}else{
				data2return[[count]] <- data0
			}
		}
	}
	names(data2return) <- names2use
	return(data2return)
}