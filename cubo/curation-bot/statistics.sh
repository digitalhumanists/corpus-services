#!/bin/bash

corpusFolder=$1 #e.g."Corpus"
comaFile=$2
reportFolder=$3
corpusServicesJarPath="/path/to/corpus-services-latest.jar"	

echo "now creating the statistics"
java -Xmx3g -jar ${corpusServicesJarPath} -i ${corpusFolder}/${comaFile} -o ${reportFolder}/statistics-output.html -c ReportStatistics -e
