% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/extractLogisticNoModelFit.R
\name{extractLogisticNoModelFit}
\alias{extractLogisticNoModelFit}
\title{extracts required stuff when no suitable model could be fitted}
\usage{
extractLogisticNoModelFit(
  inputData,
  responseName,
  isNarrowDataRange = FALSE,
  isNarrowEstimatedRange = FALSE,
  isInvalidSlope = FALSE,
  isInvalidpIC50 = FALSE,
  inactiveSuperpotentParams,
  slope = c("ascending", "descending"),
  inputDataPlot,
  validRespRange,
  fixedBottom,
  fixedTop
)
}
\arguments{
\item{inputData}{a data frame with two columns: column one the numeric dose, and column2 the response}

\item{responseName}{string specifying the response name to be diplayed on the plot}

\item{isNarrowDataRange}{TRUE/FALSE, if TRUE that means range of observed responses is below validRespRange}

\item{isNarrowEstimatedRange}{TRUE/FALSE, if TRUE mean estimated Top - estimated Bottom is below validRange.}

\item{isInvalidSlope}{TRUE/FALSE, if TRUE that means the estimated slope does not agree with expected slope.}

\item{isInvalidpIC50}{TRUE/FALSE, if TRUE means the model could be fittd but with an invalid pIC50, so it is not dislayed}

\item{inactiveSuperpotentParams}{a numeric vector of length 2. In cases where the response rage is below validRespRange, 
the first element of this vecotr shows the  response threshold to distinguish between inactive and suerpotent compounds, 
the second one shows the minimum proportion of response values that should be larger than this threshold to consider a compound superpotent. 
The default value for this argument is (50, 0.5) which means if more than half of data are larger than 50, then we consider the compound superpotent, 
and otherwise inactive.}

\item{slope}{the slope of the curve: either "ascending" or "descending"}

\item{inputDataPlot}{the complete inputData with accept column}

\item{validRespRange}{valid difference of minimum and maximum response (default is 30), if the range is smaller than this, no analysis will be done.}

\item{fixedBottom}{the fixed lower limit parameter of the logistic model, default is NULL, if provided then the lower limit will be fixed.}

\item{fixedTop}{the fixed upper limit parameter of the logistic model, default is NULL, if provided then the upper limit will be fixed.}
}
\description{
extracts required stuff when no suitable model could be fitted
}
\author{
Vahid Nassiri
}
