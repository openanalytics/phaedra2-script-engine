% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/separateMedpolishFeature.R
\name{separateMedpolishFeature}
\alias{separateMedpolishFeature}
\title{Apply the medpolish to a Feature separately for different WELLTYPE_CODEs}
\usage{
separateMedpolishFeature(
  plateData,
  featureName,
  separateWELLTYPE_CODEs,
  pes = FALSE,
  maxiter = 60
)
}
\arguments{
\item{plateData}{data frame with columns  \code{WELLTYPE_CODE}, \code{ROW_NR}, \code{COL_NR} and the feature specified
in the \code{featureName} argument}

\item{featureName}{name of the column that contains the feature data to be polished}

\item{separateWELLTYPE_CODEs}{list of which WELLTYPE_CODEs should be separated from other WELLTYPE_CODEs for the calculation of medpolish
values; should be one of the values of the \code{WELLTYPE_CODE} column; default value \code{"PC"}}

\item{pes}{logical; default value \code{FALSE}}

\item{maxiter}{maximum number of iterations for the medpolish procedure}
}
\description{
Apply the medpolish to a Feature separately for different WELLTYPE_CODEs
}
