#!/bin/bash

corpusFolder=$1 #e.g."Corpus"
comaFile=$2
corpusServicesJarPath="/path/to/corpus-services-latest.jar"	

java -Xmx3g -jar ${corpusServicesJarPath} -i ${corpusFolder}/${comaFile} -o ${corpusFolder}/curation/isotei-report-output.html -s corpus-utilities/settings.param -o report-output.html -o tei-converter-report-output.html -c EXB2HIATISOTEI -f -e
