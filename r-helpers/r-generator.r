require(tools)
require(utils)

args <- commandArgs(TRUE)
packageNames <- .packages(all = TRUE)
searchPath <- search()

is.identifier <- function(str) {
 return(grepl("^([[:alpha:]]|_|\\.)([[:alpha:]]|[[:digit:]]|_|\\.)*$", str) == TRUE)
}
is.contains.endl <- function(str) {
  return(grepl(".*\\n$",str)==TRUE)
}
findText <- function(rd_fragment) {
    result = character()
    for(tag in rd_fragment){
      if(is.character(text <- tag[1])) {
        result = c(result,text)
      } else {
        result = c(result, findText(tag))
      }
    }
    return(result)
}
findArgs <- function(arg){
  arg.tag <- tools:::RdTags(arg)
  items = arg[which(arg.tag == "\\item")]
  result = list(names = character(), desc = list())
  for(item in items){
    if(length(item[[1]])>0){
      if(is.character(text <- item[[1]][[1]][1])){
        result$names = c(result$names,text)
      } else {
        result$names = c(result$names,"...")
      }
    } else {
      result$names = c(result$names,"    ")
    }
    result$desc[[length(result$desc)+1]] = findText(item[[2]])
  }
  return(result)
}
getDocumentation <- function(topic,package) {
  print(topic)
  print(package)
  paths <- utils:::index.search(topic,find.package(package))
  paths <- unique(paths)
  if(length(paths)>0) {
    Rd <- utils:::.getHelpFile(paths[1L])
    tags <- tools:::RdTags(Rd)
    title = findText(Rd[[which(tags == "\\title")]])
    if(length(which(tags == "\\arguments"))>0) {
      args = findArgs(Rd[[which(tags == "\\arguments")]])
    } else {
      args = list(names = list())
    }
    return(list(title = title, args = args))
  } else {
    return(list())
  }
}
appendDocumentation = function(doc) {

<<<<<<< HEAD

=======
    if(length(doc$title)>0) {
      cat("# ")
      for( str in doc$title) {
        cat(str)
        if( is.contains.endl(str)) {
          cat("# ")
        }

      }
      cat("\n")
    }
    if(length(doc$args$names)>0) {
      cat("# Args:\n")
      for( i in seq(length(doc$args$names))) {
        cat("# ")
        cat(doc$args$names[i])
        cat(":  ")
        descriptions = doc$args$desc[[i]]
        for(str in descriptions)  {
          cat(str)
            if( is.contains.endl(str)) {
              cat("# ")
            }
        }
        cat("\n")
      }
    }
}
>>>>>>> Parsing of arguments
for (name in packageNames) {
    if (paste(name, "r", sep=".") %in% list.files(path=args[1])) {
        next
    }
    shouldLoadLibrary = FALSE
    pName = paste("package", name, sep=":")
    if (!pName %in% searchPath)
        shouldLoadLibrary = TRUE
    if (shouldLoadLibrary) {
        library(package=name, character.only=TRUE)
    }

    functions <- as.character(lsf.str(paste("package", name, sep=":")))

    dirName = paste(args[1], name, sep="/")
    dir.create(dirName)

<<<<<<< HEAD
    for (symbol in functions) {
    	obj <- get(symbol)
    	if (class(obj) != "function") {
    	    next
    	}
    	name_without_extension <- ifelse(grepl("/", symbol), gsub("/", "slash", symbol), symbol)
        fileName <- paste(paste(dirName, name_without_extension, sep="/"), "r", sep=".")
=======
    for(symbol in symbolList) {
        obj <- get(symbol)
        docs <- getDocumentation(symbol,name)
        print(docs)
        fileName <- paste(paste(dirName, symbol, sep="/"), "r", sep=".")
>>>>>>> Parsing of arguments
        tmpFileName <- tempfile(pattern = "tmp", tmpdir = tempdir(), fileext = "")
        sink(tmpFileName)
        appendDocumentation(docs)
        if (is.identifier(symbol))
          cat(symbol)
        else {
          cat("\"")
          cat(symbol)
          cat("\"")
        }
        cat(" <- ")
        print(obj)
        sink()

        fileObj <- file(tmpFileName)
        lines <- readLines(fileObj)
        close(fileObj)

        errors <- try(sink(fileName))
        if (!inherits(errors, "try-error")) {
            for (line in lines) {
                sub <- substring(line, 0, 10)
                if (sub == "<bytecode:"  || sub == "<environme") break
                cat(line, append=TRUE)
                cat("\n", append=TRUE)
            }
            sink()
        }
    }

    if (shouldLoadLibrary) {
        detach(pName, character.only=TRUE)
        diff <- setdiff(search(), searchPath)
        for (p in diff) {
            detach(p, character.only=TRUE)
        }
    }
}