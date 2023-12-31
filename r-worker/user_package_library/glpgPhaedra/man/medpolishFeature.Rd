% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/medpolishFeature.R
\name{medpolishFeature}
\alias{medpolishFeature}
\title{Apply the medpolish to a Feature}
\usage{
medpolishFeature(
  plateData,
  featureName,
  nRows = 32,
  nCols = 48,
  correctionType,
  pes = FALSE,
  maxiter = 60
)
}
\arguments{
\item{plateData}{data frame with columns  \code{WELLTYPE_CODE}, \code{ROW_NR}, \code{COL_NR} and the feature specified
in the \code{featureName} argument}

\item{featureName}{name of the column that contains the feature data to be polished}

\item{nRows}{number of rows on the plate (numeric); default value \code{32}}

\item{nCols}{number of columns on the plate (numeric); default value \code{48}}

\item{correctionType}{correction type; should be one of the values of the \code{WELLTYPE_CODE} column or,
alternatively, \code{"fullPlateRestoreMedians"}; default value \code{"NC"}}

\item{pes}{logical; default value \code{FALSE}}

\item{maxiter}{maximum number of iterations for the medpolish procedure}
}
\description{
Apply the medpolish to a Feature
}
