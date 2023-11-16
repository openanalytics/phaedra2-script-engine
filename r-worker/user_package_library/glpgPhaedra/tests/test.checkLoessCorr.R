library(glpgPhaedra)

## HB test code
dataFile <- system.file("extdata", "190226_GT1917_for Heidi final.txt", package = "glpgPhaedra")

plateData <- read.table(file = dataFile, sep = "\t", header = TRUE)
plate914 <- plateData[plateData$PLATE_ID == 914, ]

# Test loess correction
corType <- 'NC'
loesCorrRes <- glpgPhaedra::loessProcedure(plateData = plateData,
    featureName = 'CalcResultI.RAW_VALUE', correction_type = corType,splitColumn ="PLATE_ID")
head(loesCorrRes)


