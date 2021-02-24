#!/bin/bash

corpusFolder=$1 #e.g."Corpus"
comaFile=$2
corpusServicesJarPath="/path/to/corpus-services-latest.jar"	

java -Xmx3g -jar ${corpusServicesJarPath} -i ${corpusFolder}/${comaFile} -o ${corpusFolder}/curation/mp3-report-output.html -o report-output.html -c ExbMP3Next2WavAdder -f -e
