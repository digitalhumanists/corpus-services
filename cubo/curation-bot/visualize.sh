#!/bin/bash

corpusFolder=$1 #e.g."Corpus"
comaFile=$2
corpusServicesJarPath="/path/to/corpus-services-latest.jar"	

echo "now starting the visualization"
java -Xmx3g -jar ${corpusServicesJarPath} -i ${corpusFolder}/${comaFile} -o ${corpusFolder}/visualize-report-output.html -s corpus-utilities/settings.param -c ScoreHTML -c ListHTML -c ComaOverviewGeneration -e

