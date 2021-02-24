#!/bin/bash

corpusFolder=$1 #e.g."Corpus"
comaFile=$2
reportFolder=$3
corpusServicesJarPath="/path/to/corpus-services-latest.jar"	

echo "now starting to pretty print"
java -Xmx3g -jar ${corpusServicesJarPath} -i ${corpusFolder}/${comaFile} -o ${reportFolder}/prettyprint-report-output.html -c PrettyPrintData -f -e
