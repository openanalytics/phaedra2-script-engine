
library(glpgPhaedra)

dataFile <- system.file("extdata", "190226_GT1917_for Heidi final.txt", package = "glpgPhaedra")

plateData <- read.table(file = dataFile, sep = "\t", header = TRUE)
plate914 <- plateData[plateData$PLATE_ID == 914, ]

result <- checkIntraPlateEffect(plateData = plate914, featureName = "CalcResultI.RAW_VALUE")

result
str(result)

# Small test to see if NA's are generated when the MW test fails (e.g. one of the columns has missing value).
plate914[plate914$COL_NR == 47, 'CalcResultI.RAW_VALUE'] <- NA
checkFail <- checkIntraPlateEffect(plateData = plate914, featureName = "CalcResultI.RAW_VALUE")
checkFail[1,]
head(checkFail[checkFail$COL_NR == 47, ])


