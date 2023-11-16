
library(glpgPhaedra)

dataFile <- system.file("extdata", "190226_GT1917_for Heidi final.txt", package = "glpgPhaedra")

plateData <- read.table(file = dataFile, sep = "\t", header = TRUE)
plate914 <- plateData[plateData$PLATE_ID == 914, ]

result <- medpolishFeature(plateData = plate914, featureName = "CalcResultI.RAW_VALUE", correctionType = "NC")
head(result)

str(result)

result <- medpolishFeature(plateData = plate914, featureName = "CalcResultI.RAW_VALUE",
		correctionType = "fullPlateRestoreMedians")
head(result)
result
str(result)

