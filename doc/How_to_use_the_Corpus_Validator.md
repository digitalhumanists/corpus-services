# How to use the CorpusFunctions in the Corpus Services

# Compile

## Manual compile

For compiling it can be necessary to add EXMARaLDA.jar manually to Maven project (i.e. "manually install artifact":https://lab.multilingua.uni-hamburg.de/redmine/projects/infrastruktur/wiki/Setting_up_-_Libraries#hzsk-corpus-services-code).

To use the validator for HZSK corpora, compile it using 
<pre>
mvn clean compile assembly:single
</pre>

(See https://stackoverflow.com/questions/574594/how-can-i-create-an-executable-jar-with-dependencies-using-maven)
The JAR with dependencies (hzsk-corpus-services-{version.number}-jar-with-dependencies.jar) will be generated in the target folder.

## Gitlab

You can also download the pre-compiled jar from gitlab: https://gitlab.rrz.uni-hamburg.de/hzsk/hzsk-corpus-services/-/jobs/artifacts/develop/browse?job=compile_withmaven

## Server

A recent version of the jar is also continuously available on the servers:

/data/HZSK/hzsk-corpus-services/
/data/INEL/utilities//hzsk-corpus-services/

#  Use of the corpus functions

Add the generated .jar to the Folder above /Korpora/HZSK and execute something similar to:

`java -Xmx3g -jar ../hzsk-corpus-services-1.0-jar-with-dependencies.jar -i E:\user\corpus -o E:\user\corpus\report-output.html -c function1 -c function2 -f `

Output of the help parameter:
<pre>
usage: hzsk-corpus-services -i <FILE PATH> -o <FILE PATH> -c <CORPUS
       FUNCTION> [-p <property=value>] [-f] [-h] [-e] [-j] [-s <FILE
       PATH>]
Specify a corpus folder or file and a function to be applied

 -i,--input <FILE PATH>                  input file path (coma file for
                                         corpus, folder or other file for
                                         unstructured data)
 -o,--output <FILE PATH>                 output file
 -c,--corpusfunction <CORPUS FUNCTION>   corpus function
 -p,--property <property=value>          use value for given properties
 -f,--fix                                fixes problems automatically
 -h,--help                               display help
 -e,--errorsonly                         output only errors
 -j,--fixesjson                          output json file for fixes
 -s,--settingsfile <FILE PATH>           settings file path
</pre>

*-i* is the input, it should be the name of the folder or single corpus file you want to process. Functions can be run on a folder, a metadata file representing a list of files, or a single file (e.g. exb). If the input is a folder or a single file that is not an EXMARaLDA coma file, it will be treated as unstructured data. If the input file is an EXMARaLDA coma file, it will be treated as a structured corpus and all files linked in it will also be checked, depending on the chosen functions.

*-o* is the location, where the output will be found. Depending if you end the filenem with ".txt" or ".html" a simple text file or a sort - and filterable html table will be generated. At the same place an additional EXMARaLDA error list will be created. 

*-c* are the corpus functions you want to run. See the method getAllExistingCFs() in the class src/main/java/de/uni_hamburg/corpora/CorpusMagician.java for all available function strings. 

*-f* this flag is for fixing, if it exists, all possible functions will fix the data automatically (that means it changes the input data and rewrites them).

*-h* is the help flag to generate a very detailed help output

* -e* outputs only errors (Warnings and Criticals), no Notes or Correct ReportItems

* -j* outputs an additional json file for fixes that can be used for statistical evaluation of the curation

Only use one of the following:

*-p* key=value is used for different parameters of the checks

OR

*-s* specifies a path to a settings file containing all needed p parameter pairs
The settings file needs to look like:
<pre><code class="xml">
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties> 
    <entry key="SEGMENTATION">HIAT</entry>
    <entry key="FSM">L:\\utilities\\inelutilities\\INEL_Segmentation_FSM_noColons.xml</entry>
    <entry key="CORPUSNAME">Kamas Corpus 1.0</entry>
</properties>
</code></pre>


## Listing Functions

To get a list of all the available functions with their description and if they have a fixing option use the help parameter:

`java -Xmx3g -jar hzsk-corpus-services-1.0-jar-with-dependencies.jar -h `

The usable functions can be found [[List of validation functions|here]].

## Help

You can see the complete command line functionality by running
`
java -Xmx3g -jar hzsk-corpus-services-1.0-jar-with-dependencies.jar -h
`

## Examples

* Conversion to ISO/TEI for HIAT
`
java -Xmx3g -jar hzsk-corpus-services-1.0-jar-with-dependencies.jar -i euroWiss -o eurowiss.html -c EXB2HIATISOTEI
`

Further example calls in scripts can be found here on Gitlab: https://gitlab.rrz.uni-hamburg.de/hzsk/corpus-curation/tree/master/corpus-services-calls

# Trouble Shooting

* the correct oracle java version needs to be installed (64bit) for the jar to be allowed to be allocated enough heap space!
