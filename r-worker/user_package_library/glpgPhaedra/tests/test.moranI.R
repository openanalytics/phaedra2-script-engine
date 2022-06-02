library(glpgPhaedra)

## HB test code
dataFile <- system.file("extdata", "190226_GT1917_for Heidi final.txt", package = "glpgPhaedra")

plateData <- read.table(file = dataFile, sep = "\t", header = TRUE)
plate914 <- plateData[plateData$PLATE_ID == 914, ]

# Test on 1 plate, low concentration wells
# should be p-value of 0
plate914NC <- subset(plate914, subset = plate914$WELLTYPE_CODE == 'NC')
calcMoran(onePlateData = plate914NC, targetColumn = 'CalcResultI.RAW_VALUE',
    distMethod = 'euclidean')

# Now on all plates (using different method)
plateDataNC <- subset(plateData, subset = plateData$WELLTYPE_CODE == 'NC')
runMoranPlates(plateDataNC, targetColumn = 'CalcResultI.RAW_VALUE',
    distMethod = 'manhattan',splitColumn ="PLATE_ID")




