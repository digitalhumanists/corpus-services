#!/bin/bash

corpusFolder=$1 #e.g."Corpus"
comaFile=$2
corpusServicesJarPath="/path/to/corpus-services-latest.jar"	

echo "now starting to normalize"
java -Xmx3g -jar ${corpusServicesJarPath} -i ${corpusFolder}/${comaFile} -o ${corpusFolder}/curation/normalize-report-output.html -s corpus-utilities/settings.param -c RemoveAbsolutePaths -c RemoveAutoSaveExb -c ComaApostropheChecker -c ExbSegmentationChecker -c ComaSegmentCountChecker -c RemoveEmptyEvents -c ComaTranscriptionsNameChecker -c ExbSegmentationChecker -c RemoveAbsolutePaths -c RemoveAutoSaveExb -c ComaApostropheChecker -c PrettyPrintData -f
