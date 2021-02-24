#!/bin/bash

corpusFolder=$1 #e.g."Corpus"
comaFile=$2
corpusServicesJarPath="/path/to/corpus-services-latest.jar"	

echo "now starting to check CMDI files"
java -Xmx3g -jar ${corpusServicesJarPath} -i ${corpusFolder}/${comaFile} -o ${corpusFolder}/curation/cmdi-report-output.html -c CmdiChecker

