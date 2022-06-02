############
# IMPORTANT: the following functions are copied from AssayCorrector:
# https://cran.r-project.org/src/contrib/Archive/AssayCorrector/
# This is a deprecated non-CRAN package.
# In case the code is not needed for glpgPhaedra, it can be removed.

#' @title Print assay summary
#' @method print assay
#' @description \code{print.assay} simply prints a summary of the HTS assay
#' @param x The assay you want to print
#' @param plate The plate number (Default:1)
#' @param ... Ellipsis to be passed to the default \code{print()} function
#' @return None
#' @export
print.assay<-function(x,...,plate=1){
  assay=x
  if(class(assay)!="assay")
    stop("Error: x is not an assay.")
  cat("HTS assay (",dim(assay$m)[1],"rows x ",dim(assay$m)[2],"columns x ",dim(assay$m)[3]," plates):\nPlate ",plate,"\n")
  print(assay$m[,,plate],...) # Print first plate raw measurements
  cat("Minimum value: ",min(assay$m)," Maximum value: ",max(assay$m))
}

#' @title Create a new \code{assay}
#' @description \code{create_assay} makes a new object of class assay. You should pass this object to \code{detect_bias()} and \code{correct_bias()} methods
#' @param m The assay you want to be corrected
#' @param ctrl An optional boolean array of the same dimensions as \code{m}. Each entry is 1 if the well is a control well, 0 otherwise. All control wells are excluded from all computations
#' @return assay The created assay object.
#' It containts the following fields:
#'
#' \code{n} The HTS matrix of raw measurements
#'
#' \code{ctrl} The binary matrix of control wells
#'
#' \code{biasPositions} The binary matrix where 1:well is biased, 0:well is unbiased, as suggested by Mann-Whitney test
#'
#' \code{mCorrected} The HTS matrix of corrected measurements, initilized to a zero array, and subsequently storing the corrected version of \code{m} via \code{correct_bias()}
#'
#' \code{biasType} Vector of length p, where p is the number of plates. It tells, for each plate of the assay, A:Additive trend, M:Multiplicative trend, U:Undetermined trend and C:Error-free plate.
#'
#' \code{biasModel} Vector of length p, where p is the number of plates. It tells, for each plate of the assay, the most likely spatial bias model (1 through 6)
#'
#' \code{biasConf} Vector of length p, where p is the number of plates. It tells, for each plate of the assay, the confidence in the model, (0 - lowest to 3-  highest). It is computed by counting the number of bias models (additive or mutliplicative) which agree together.
#' @export
create_assay<-function(m,ctrl=NA){
  if(!(class(m) %in% c("array","matrix")))
    stop("Error: m is not an array.")
  if(is.na(ctrl)){ # If no control pattern supplied, assuming none
    ctrl=m
    ctrl[]=0
  }
  if(any(!(ctrl%in%0:1))) # If non zero/one values detected
    stop("Control array must be binary")
  if(any(is.na(m)) || any(is.infinite(m))) # If values are missing or are infinite
    stop("NA in input assay")
  assay=NULL
  Rows=dim(m)[1]
  Columns=dim(m)[2]
  rownames(m)=if(Rows<=26)LETTERS[1:Rows]else{warning("Too many rows, naming them by numbers instead of letters.");sapply(1:Rows,toString)}
  colnames(m)=sapply(1:Columns,toString)
  rownames(ctrl)=rownames(m)
  colnames(ctrl)=colnames(m)
  assay$m=m
  assay$ctrl=ctrl
  assay$biasType=rep(NA,dim(m)[3])
  assay$biasModel=rep(NA,dim(m)[3])
  assay$biasConf=rep(NA,dim(m)[3])
  biasPositions=ctrl
  biasPositions[]=0
  assay$biasPositions=biasPositions
  assay$mCorrected=biasPositions
  class(assay)="assay"
  return(assay)
}


#' @title Detect the type of bias present in the assay
#' @description \code{detect}  (1) identifies rows and columns of all plates of the assay affected by spatial bias (following the results of the Mann-Whitney U test); (2) identifies well locations (i.e., well positions scanned across all plates of a given assay) affected by spatial bias (also following the results of the Mann-Whitney U test).
#' @param assay The assay to be corrected. Has to be an \code{assay} object.
#' @param alpha Significance level threshold (defaults to 0.01)
#' @param type \code{P}:plate-specific, \code{A}:assay-specific, \code{PA}:plate then assay-specific, \code{AP}:assay then plate-specific
#' @param test \code{KS}:Kolmogorov-Smirnov (1933), \code{AD}:Anderson-Darling (1952), 
#' \code{CVM}:Cramer-von-Mises (1928). Note the latter is not supported anymore. 
#' 
#' @importFrom stats ks.test
#' @importFrom utils capture.output
#' @importFrom utils tail
#' @importFrom kSamples ad.test
#' 
#' @return The corrected assay (\code{assay} object)
#' @export
detect_bias <- function(assay,alpha=0.01,type="P",test="AD"){
  if(class(assay)!="assay")
    stop("Error: This is not an assay.")
  ac=as.character
  m=assay$m
  ctrl=assay$ctrl
  biasType=assay$biasType
  biasModel=assay$biasModel
  biasConf=assay$biasConf
  PMPmapping=c(1,4,5,3,2,6) # Due to a change in the order of methods, need to convert publication notation to code one
  m.E<-new.env()
  for (model in 1:6){
    m.E[[ac(model)]]=ctrl # Initialize empty corrected assay
  }
  dimensions=dim(m)
  Depth=dimensions[3]
  .test.f=NULL
  if(test=="KS")
    .test.f=function(x,y)ks.test(x,y)$p.value
  else if(test=="AD")
    .test.f=function(x,y)as.numeric(tail(strsplit(capture.output(kSamples::ad.test(x,y))[17]," ")[[1]],1))
  else if(test=="CVM") #.test.f=function(x,y)RVAideMemoire::CvM.test(x,y)$p.value
    print('Requires the RVAideMemoire package which is not supported at the moment')
  else{
    stop("Error: This is not a valid test. Please use KS, AD or CVM")
  }
  if(type=="AP") # If we want assay->plate correction, first apply assay-wise correction
    m=.assay(m,ctrl,alpha)
  for (k in 1:Depth){
    for (model in 1:6){
      m.E[[ac(model)]][,,k]=try(.PMP(m[,,k],ctrl[,,k],PMPmapping[model],alpha)) # Correct the plate k using PMP
      if(class(m.E[[ac(model)]][,,k])=="try-error")
        stop("PMP encountered a problem") # Problem in PMP - check your data
    }
    mww=(m.E[[ac(1)]][,,k]!=m[,,k])*1 # Determine which rows and columns the Mann-Whitney test detected as biased (both technologies have same bias locations, hence using additive)
    assay$biasPositions[,,k]=mww # Save the bias positions suggested by the Mann-Whitney test
    biased.E=new.env()
    unbiased=list()
    for(i in 1:dimensions[1]){
      for(j in 1:dimensions[2]){
        if(mww[i,j]&!ctrl[i,j,k]){ # If the MW test flaged this row/column AND this cell is not a control well
          for(model in 1:6){
            biased.E[[ac(model)]]=c(biased.E[[ac(model)]],m.E[[ac(model)]][i,j,k]) # Add well (i,j) corrected by PMP to set of corrected wells
          }
        }
        else if(!mww[i,j]&!ctrl[i,j,k]) # If the MW did not flag this row/column AND this cell is not a control well
          unbiased=c(unbiased,m.E[[ac(model)]][i,j,k]) # Add this well to set of unbiased wells
      }
    }
    for(model in 1:6){
      biased.E[[ac(model)]]=unlist(biased.E[[ac(model)]])
    }
    unbiased=unlist(unbiased)
    if(Reduce(function(x,y)length(biased.E[[ac(y)]])*x,1:6,1)==0){ # 100% unbiased (computed via fold left)
      biasType[k]='C'
      next
    }
    pvalue.E=new.env()
    for(model in 1:6){
      pvalue.E[[ac(model)]]=.test.f(biased.E[[ac(model)]],unbiased)
    }
    aMethods=1:3
    mMethods=4:6
    p=function(model)pvalue.E[[ac(model)]]
    if(all(sapply(aMethods,p) < alpha) & any(sapply(mMethods,p) > alpha)){ # mPMP did better
      biasType[k]='M' # Bias is multiplicative
      biasModel[k]=3+which.max(sapply(mMethods,p)) # Model number (between 4 and 6)
      biasConf[k]=sum(sapply(mMethods,p) > alpha) # Number of good corrections
    }
    else if(all(sapply(mMethods,p) < alpha) & any(sapply(aMethods,p) > alpha)){ # aPMP did better
      biasType[k]='A' # Bias is additive
      biasModel[k]=which.max(sapply(aMethods,p)) # Model number (between 1 and 3)
      biasConf[k]=sum(sapply(aMethods,p) > alpha) # Number of good corrections
    }
    else if(all(sapply(mMethods,p) < alpha) & all(sapply(aMethods,p) < alpha)){ # Undetermined, both are bad
      biasType[k]='U'
    }
    else if(all(sapply(mMethods,p) > alpha) & all(sapply(aMethods,p) > alpha)){ # Undetermined, both are good
      biasModel[k]=which.max(sapply(c(aMethods,mMethods),p)) # Model number (between 1 and 6)
      biasConf[k]=sum(sapply(c(aMethods,mMethods),p) > alpha) # Number of good corrections
      biasType[k]=ifelse(biasModel[k]%in%aMethods,'A', # Bias is additive
          'M' # Bias is multiplicative
      )
    }
    else{
      biasType[k]='U' # Undetermined type of bias, not enough agreement
    }
  }
  assay$biasType=biasType # Write the resulting vectors back
  assay$biasModel=biasModel
  assay$biasConf=biasConf
  return(assay)
}
#' @title Correct the bias present in the assay, previously detected by the \code{detect_bias()} method
#' @description \code{correct_bias()} (1) uses either of the three additive or either of the three multiplicative PMP (Partial Mean Polish) methods (the most appropriate spatial bias model can be either specified or determined by the program following the results of the Kolmogorov-Smirnov, Anderson-Darling or Cramer-von-Mises two-sample test) to correct the assay measurements if the plate-specific correction is specified; (2) carries out the assay-specific correction if specified.
#' @param assay The assay to be corrected. Has to be an \code{assay} object.
#' @param method \code{NULL}:autodetect (default), \code{1}:additive, \code{2}:multiplicative
#' @param alpha Significance level threshold (defaults to 0.05)
#' @param type \code{P}:plate-specific, \code{A}:assay-specific, \code{PA}:plate then assay-specific, \code{AP}:assay then plate-specific
#' @return The corrected assay (\code{assay} object)
#' @export
correct_bias<-function(assay,method=NULL,alpha=0.05,type="PA"){
  if(class(assay)!="assay")
    stop("Error: This is not an assay.")
  m=assay$m
  ctrl=assay$ctrl
  biasType=assay$biasType
  biasModel=assay$biasModel
  dimensions=dim(m)
  Depth=dimensions[3]
  if(is.null(biasType))
    stop("Run detect() first.")
  mCorrected=m # Initialize the corrected assay to the raw one
  if(type=="P"){
    for (k in 1:Depth){
      if(!is.null(method)) # Correct using given method
        mCorrected[,,k]=try(.PMP(m[,,k],ctrl[,,k],method,alpha))
      else{ # Autodetect method for each plate
        if(biasType[k]!='U' & !is.na(biasModel[k]))
          mCorrected[,,k]=try(.PMP(m[,,k],ctrl[,,k],biasModel[k],alpha))
        # else, if the bias is undefined, we cannot apply the correction algorithm, so we skip it
      }
    }
  }
  else if(type=="A"){
    mCorrected=.assay(m,ctrl,alpha)
  }
  else if(type=="PA"){
    # Part 1: Plate-wise
    for (k in 1:Depth){
      if(!is.null(method)) # Correct using given method
        mCorrected[,,k]=try(.PMP(m[,,k],ctrl[,,k],method,alpha))
      else{ # Autodetect method for each plate
        if(biasType[k]!='U' & !is.na(biasModel[k]))
          mCorrected[,,k]=try(.PMP(m[,,k],ctrl[,,k],biasModel[k],alpha))
        # else, if the bias is undefined, we cannot apply the correction algorithm, so we skip it
      }
    }
    # Part 2: Assay-wise
    mCorrected=.assay(m,ctrl,alpha)
  }
  else if(type=="AP"){
    # Part 1: Assay-wise
    mCorrected=.assay(m,ctrl,alpha)
    # Part 2: Plate-wise
    for (k in 1:Depth){
      if(!is.null(method)) # Correct using given method
        mCorrected[,,k]=try(.PMP(m[,,k],ctrl[,,k],method,alpha))
      else{ # Autodetect method for each plate
        if(biasType[k]!='U' & !is.na(biasModel[k]))
          mCorrected[,,k]=try(.PMP(m[,,k],ctrl[,,k],biasModel[k],alpha))
        # else, if the bias is undefined, we cannot apply the correction algorithm, so we skip it
      }
    }
  }
  else{
    stop("Unknown correction type.")
  }
  assay$mCorrected=mCorrected
  return(assay)
}


PMP_EPSILON = 0.0001
PMP_MAX_ITERATIONS = 1000
THRESHOLD=2.5
.zify=function(m)(m-mean(m))/ifelse(!sd(m),PMP_EPSILON,sd(m))
.rify=function(m)(m-median(m))/ifelse(!mad(m),PMP_EPSILON,mad(m))
.PMP=function(m,ctrl,method,alpha=0.05){
  bak=m
  ctrl[is.na(ctrl)] <- 1 # All missing controls and reals are excluded (flagged as control wells)
  ctrl[is.na(m)] <- 1
  Rows=dim(m)[1]
  Columns=dim(m)[2]
  Loop=1
  tmp.c=colSums(ctrl)==Rows
  tmp.r=rowSums(ctrl)==Columns
  if(any(tmp.c)){m=m[,-which(tmp.c)];ctrl=ctrl[,-which(tmp.c)]}
  if(any(tmp.r)){m=m[-which(tmp.r),];ctrl=ctrl[-which(tmp.r),]}
  Rows.b=Rows
  Columns.b=Columns
  Rows=dim(m)[1]
  Columns=dim(m)[2]
  bak2=m
  CFlag=rep(FALSE,Columns)
  RFlag=rep(FALSE,Rows)
  n=0
  ctrlS=sum(ctrl)
  while (Loop< min(Rows-2,Columns-2) && (n < .5*(length(RFlag)*length(CFlag)-ctrlS))){
    p.values=data.frame(r=numeric(),c=numeric(),p=numeric())
    Loop=Loop+1
    Cind=which(CFlag)
    Rind=which(RFlag)
    for (i in 1:Columns){
      if (i %in% Cind)next
      tmp1=list()
      tmp2=list()
      for(j in 1:Rows){
        for(k in 1:Columns){
          if(!(j %in% Rind) & (k == i) & !ctrl[j,k]) tmp1=c(tmp1,m[j,k])
          if(!(j %in% Rind) & (k != i) & !ctrl[j,k] & !(k %in% Cind)) tmp2=c(tmp2,m[j,k])
        }
      }
      tmp1=unlist(tmp1)
      tmp2=unlist(tmp2)
      if(!length(tmp1)||!length(tmp2))next
      p.v=try(wilcox.test(tmp1,tmp2,correct = FALSE)$p.value,TRUE)
      if(class(p.v)!="try-error")p.values=rbind(p.values,data.frame(c=i,r=-1,p=ifelse(is.nan(p.v),1,p.v)))
    }
    for (j in 1:Rows){
      if (j %in% Rind)next
      tmp1=list()
      tmp2=list()
      ct=0
      for(i in 1:Rows){
        for(k in 1:Columns){
          if(!(k %in% Cind) & (j == i) & !ctrl[i,k]) tmp1=c(tmp1,m[i,k])
          if(!(k %in% Cind) & (j != i) & !ctrl[i,k] & !(i %in% Rind)) tmp2=c(tmp2,m[i,k])
          if(ctrl[i,k]) ct=ct+1
        }
      }
      tmp1=unlist(tmp1)
      tmp2=unlist(tmp2)
      if(!length(tmp1)||!length(tmp2))next
      p.v=try(wilcox.test(tmp1,tmp2,correct = FALSE)$p.value,TRUE)
      if(class(p.v)!="try-error")p.values=rbind(p.values,data.frame(c=-1,r=j,p=ifelse(is.nan(p.v),1,p.v)))
    }
    # Sorted p-values in increasing order (most evidence to reject -> least evidence to reject)
    p.values=p.values[order(p.values$p),]
    # If all p-values are greater than alpha, break
    if (all(p.values$p >=alpha))break
    if(p.values[1,]$c==-1){
      i=p.values[1,]$r
      RFlag[i]=TRUE
    }else{
      j=p.values[1,]$c
      CFlag[j]=TRUE
    }
    n=(sum(RFlag)*length(CFlag)+(length(RFlag)-sum(RFlag))*sum(CFlag))
  }
  # Number of row/column/total errors
  NR=max(0,sum(RFlag,na.rm=TRUE))
  NC=max(0,sum(CFlag,na.rm=TRUE))
  N=NR+NC
  if(N==0){
    i.m=1
    for (i in 1:Rows.b){
      j.m=1
      for (j in 1:Columns.b){
        if(!tmp.r[i]&!tmp.c[j])
          bak[i,j]=m[i.m,j.m]
        if(!tmp.c[j])
          j.m=j.m+1
      }
      if(!tmp.r[i])
        i.m=i.m+1
    }
    return(bak)
  }
  Mu=list()
  for(i in 1:Rows){
    if (RFlag[i])next
    for(j in 1:Columns)
    {
      if (CFlag[j] || ctrl[i,j])next
      Mu=c(Mu,m[i,j]) # median
    }
  }
  # Mu=median(unlist(Mu))
  Mu=mean(unlist(Mu))
  Rmu=rep(0,Rows)
  Cmu=rep(0,Columns)
  Loop=1
  Converge=0
  repeat{
    Rmu=rep(0,Rows)
    Cmu=rep(0,Columns)
    Diff=0
    Converge=0
    for (i in 1:Rows){
      intersection=0
      q=list()
      for (j in 1:Columns){
        if(ctrl[i,j])next
        if(RFlag[i]&&CFlag[j]){
          intersection=intersection+1
          next
        }
        q=c(q,m[i,j]) # median
      }
      # Rmu[i]=median(unlist(q))
      Rmu[i]=mean(unlist(q))
    }
    for (j in 1:Columns){
      intersection=0
      q=list()
      for (i in 1:Rows){
        if(ctrl[i,j])next
        if(RFlag[i]&&CFlag[j]){
          intersection=intersection+1
          next
        }
        q=c(q,m[i,j]) # median
      }
      # Cmu[j]=median(unlist(q))
      Cmu[j]=mean(unlist(q))
    }
    if (method %in% c(1,2)){
      for (i in 1:Rows){
        if(!RFlag[i]) next
        Diff=Mu-Rmu[i]
        Converge=Converge+abs(Diff)
        for (j in 1:Columns){
          if(ctrl[i,j])next
          m[i,j]=switch(method,
              m[i,j]+Diff, # Method 1
              m[i,j]*abs(Mu/Rmu[i]) # Method 2
          )
        }
      }
      
      for (j in 1:Columns){
        if(!CFlag[j]) next
        Diff=Mu-Cmu[j]
        Converge=Converge+abs(Diff)
        for (i in 1:Rows){
          if(ctrl[i,j])next
          m[i,j]=switch(method,
              m[i,j]+Diff, # Method 1
              m[i,j]*abs(Mu/Cmu[j]) # Method 2
          )
        }
      }}
    if (method %in% c(3,4,5,6)){
      for (i in 1:Rows){
        for (j in 1:Columns){
          if(ctrl[i,j])next
          if (CFlag[j] && !RFlag[i]){
            Diff=Mu-Cmu[j]
            Converge=Converge+abs(Diff)
            m[i,j]=switch(method-2,abs(Mu*m[i,j]/(Cmu[j])), # Method 3
                m[i,j]-(Cmu[j]-Mu), # Method 4
                m[i,j]-(Cmu[j]-Mu), # Method 5
                abs(Mu*m[i,j]/Cmu[j])) # Method 6
          }
          else if (!CFlag[j] && RFlag[i]){
            Diff=Mu-Rmu[i]
            Converge=Converge+abs(Diff)
            m[i,j]=switch(method-2,abs(Mu*m[i,j]/(Rmu[i])), # Method 3
                m[i,j]-(Rmu[i]-Mu), # Method 4
                m[i,j]-(Rmu[i]-Mu), # Method 5
                abs(Mu*m[i,j]/Rmu[i])) # Method 6
          }
        }
      }
    }
    # Convergence condition
    if(class(Converge)!="numeric")Converge=.Machine$double.xmax
    Loop=Loop+1
    if(! (Converge >PMP_EPSILON && Loop<PMP_MAX_ITERATIONS) ){
      break
    }
  }
  # Intersection polishing
  if(method %in% c(3,4,5,6)){
    Rmu=rep(0,Rows)
    Cmu=rep(0,Columns)
    for (i in 1:Rows){
      intersection=0
      q=list()
      for (j in 1:Columns){
        if(ctrl[i,j])next
        if(RFlag[i]&&CFlag[j]){
          intersection=intersection+1
          next
        }
        z=switch(method-2,
            bak2[i,j]/m[i,j], # Method 3
            bak2[i,j]-m[i,j], # Method 4
            bak2[i,j]-m[i,j], # Method 5
            bak2[i,j]/m[i,j]) # Method 6
        q=c(q,z) # avg
      }
      # Rmu[i]=median(unlist(q)) # median
      Rmu[i]=mean(unlist(q)) # median
    }
    for (j in 1:Columns){
      intersection=0
      q=list()
      for (i in 1:Rows){
        if(ctrl[i,j])next
        if(RFlag[i]&&CFlag[j]){
          intersection=intersection+1
          next
        }
        z=switch(method-2,
            bak2[i,j]/m[i,j], # Method 3
            bak2[i,j]-m[i,j], # Method 4
            bak2[i,j]-m[i,j], # Method 5
            bak2[i,j]/m[i,j]) # Method 6
        q=c(q,z) # median
      }
      # Cmu[j]=median(unlist(q))
      Cmu[j]=mean(unlist(q))
    }
    for (i in 1:Rows){
      for (j in 1:Columns){
        if(ctrl[i,j])next
        if (CFlag[j] && RFlag[i]){
          Diff=2*Mu-Cmu[j]-Rmu[i]
          Converge=Converge+abs(Diff)
          m[i,j]=switch(method-2,
              m[i,j]/abs(Rmu[i]+Cmu[j]),          # Method 3
              m[i,j]-(Rmu[i]*Cmu[j]),          # Method 4
              m[i,j]-(Rmu[i]+Cmu[j])/2,        # Method 5
              m[i,j]/sqrt(abs(Rmu[i]*Cmu[j]))) # Method 6
        }
      }
    }
  }
  i.m=1
  for (i in 1:Rows.b){
    j.m=1
    for (j in 1:Columns.b){
      if(!tmp.r[i]&!tmp.c[j]){
        bak[i,j]=m[i.m,j.m]
      }
      if(!tmp.c[j])
        j.m=j.m+1
    }
    if(!tmp.r[i])
      i.m=i.m+1
  }
  return(bak)
}

.assay=function(m,ctrl,alpha){
  ctrl[is.na(ctrl)] <- 1
  ctrl[is.na(m)] <- 1
  dims=dim(m)
  Depth=dims[3]
  raw.bak=m
  for(k in 1:Depth){
    m[,,k]=.rify(m[,,k])
  }
  bak=m
  bak2=ctrl
  m=array(dim=c(dims[3],dims[1]*dims[2]))
  ctrl=array(dim=c(dims[3],dims[1]*dims[2]))
  for(i in 1:Depth){
    m[i,]=as.vector(bak[,,i])
    ctrl[i,]=as.vector(bak2[,,i])
  }
  rm(bak,bak2)
  Columns=dim(m)[2]
  CFlag=rep(FALSE,Columns)
  Loop=0
  n=0
  ctrlS=sum(ctrl)
  CFlag=rep(TRUE,Columns) # AD HOC
  if(!sum(CFlag))return(raw.bak) # If no errors, return non normalized matrix
  if(Depth>1){
    for(i in 1:length(CFlag)){ # Normalize biased wells to correct error
      if(!CFlag[i]) next
      mu=mean(m[,i],na.rm = TRUE)
      sd=sd(m[,i],na.rm = TRUE)
      tmp=array(dim=c(length(m[,i])))
      for(j in 1:length(m[,i])){
        if (!is.na(m[j,i])&(!(m[j,i]>=mu+THRESHOLD*sd || m[j,i]<=mu-THRESHOLD*sd)))tmp[j]=m[j,i]
      }
      tmp=(tmp-mean(tmp,na.rm=TRUE))/ifelse(!sd(tmp,na.rm = TRUE),PMP_EPSILON,sd(tmp,na.rm = TRUE))
      for(j in 1:length(m[,i])){
        if (!is.na(tmp[j]))m[j,i]=tmp[j]
      }
    }
  }
  m=array(as.vector(t(m)),dim=dims)
  return(m)
}
