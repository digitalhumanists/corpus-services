# Introduction

The HZSK Corpus Services project bundles functionality used for maintenance, curation, conversion, and visualization of corpus data at the HZSK.  

# Gitlab artifacts

The latest compiled .jar can be found here: 
https://gitlab.rrz.uni-hamburg.de/hzsk/hzsk-corpus-services/-/jobs/artifacts/develop/browse?job=compile_withmaven



Additional info on the Corpus services can be found here:
https://lab.multilingua.uni-hamburg.de/redmine/projects/redmine/wiki/How_to_use_the_Corpus_Validator

# functions

To see the currently available functions, check the method getAllExistingCFs() in the class src/main/java/de/uni_hamburg/corpora/CorpusMagician.java

You can get a rough idea by listing java classes, e.g. `ls src/main/java/de/uni_hamburg/corpora/validation/` for validator tools.

# Compilation

To use the validator for HZSK corpora, compile it using `mvn clean compile assembly:single`.
(See https://stackoverflow.com/questions/574594/how-can-i-create-an-executable-jar-with-dependencies-using-maven)


# Usage

## Running Corpus Functions


Add the generated .jar to the Folder /Korpora/HZSK and execute something like:

```
java -cp hzsk-corpus-services-0.1-jar-with-dependencies.jar de.uni_hamburg.corpora.CorpusMagician -i E:\user\corpus -o E:\user\corpus\report-output.html -c function1 -c function2 -f
```

-i is the input, it should be the name of the folder or single corpus file you want to process. Functions can be run on a folder, a metadata file representing a list of files, or a single file. 

-o is the location, where the output will be found. Depending if you end the filenem with ".txt" or ".html" a simple text file or a sort- and filterable html table will be generated. At the same place an additional EXMARaLDA error list will be created. 

-c are the corpus functions you want to run. See the method getAllExistingCFs() in the class src/main/java/de/uni_hamburg/corpora/CorpusMagician.java for all available function strings. 

-f this flag is for fixing, if it exists, all possible functions will fix the data automatically (that means it changes the input data).

## Listing Checks 

If you've some form of bash_completion installed you can get a neat list of validation checks by just typing this:

```
java -cp hzsk-corpus-services-0.1-jar-with-dependencies.jar de.uni_hamburg.corpora.validation.
```
and hitting tab twice.   

## Help

You can see the complete command line functionality by running

```
 java -cp  hzsk-corpus-services-0.1-jar-with-dependencies.jar de.uni_hamburg.corpora.CorpusMagician -h
```