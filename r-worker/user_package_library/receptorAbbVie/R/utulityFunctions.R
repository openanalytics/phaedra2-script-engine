# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################


is_in_interval <- function(x, interval){
	stopifnot(length(interval) == 2L)
	min(interval) <= x & x <= max(interval)
}

identify_range <- function(data, value) {
	ii <- 1
	nn <- ii + 1
	while (ii < nn & (ii < (nrow(data) + 1))) {
		interval_y <- c(data$response[ii], data$response[ii + 1])
		y_rang_test <- is_in_interval(value, interval_y)
		if (is.na(y_rang_test)) {
			NrowID <- NA
			ii <- ii + 1
			nn <- ii
		} else if (y_rang_test == FALSE) {
			ii <- ii + 1
			nn <- ii + 1
			NrowID <- NA
		} else if (y_rang_test == TRUE) {
			NrowID <- c(ii, ii + 1)
			ii <- ii + 1
			nn <- ii
		}
	}
	return(NrowID)
}

interpolate_concentration <- function(data, y) {
	y0 <- data$response[1]
	y1 <- data$response[2]
	x0 <- log10(data$dose[1])
	x1 <- log10(data$dose[2])
	if (y0 == y1) {
		stop("y0 and y1 must be different to perform interpolation.")}
	if (y < min(y0, y1) || y > max(y0, y1)) {
		warning("The specified y value is outside the range of interpolation.")}
	x <- x0 + (y - y0) * (x1 - x0) / (y1 - y0)
	return(x)
}

