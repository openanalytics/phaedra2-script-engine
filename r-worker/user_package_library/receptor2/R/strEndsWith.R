# TODO: Add comment
# 
# Author: Vahid Nassiri
###############################################################################

## Got it from here: https://stackoverflow.com/questions/26188663/how-to-determine-if-a-string-ends-with-another-string-in-r

#' checks if a strings ends with certain characters
#' 
#' @param haystack the main string
#' @param needle the ending string
#' 
#' @return TRUE or FALSE
#' 
#' @author https://stackoverflow.com/questions/26188663/how-to-determine-if-a-string-ends-with-another-string-in-r
#' @export
strEndsWith <- function(haystack, needle)
{
	hl <- nchar(haystack)
	nl <- nchar(needle)
	if(nl>hl)
	{
		return(F)
	} else
	{
		return(substr(haystack, hl-nl+1, hl) == needle)
	}
}

