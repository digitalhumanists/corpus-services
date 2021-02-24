#!/bin/bash

corpusFolder=$1 #e.g."Corpus"
comaFile=$2
corpusServicesJarPath="/path/to/corpus-services-latest.jar"	

echo "now starting the checks"
java -Xmx3g -jar ${corpusServicesJarPath} -i ${corpusFolder}/${comaFile} -o ${corpusFolder}/curation/report-output.html -s corpus-utilities/settings.param -c ComaKmlForLocations -c ComaFileCoverageChecker -c ComaNSLinksChecker -c ComaSegmentCountChecker -c ExbFileReferenceChecker -c ExbStructureChecker -c ExbSegmentationChecker -c ComaFedoraIdentifierLengthChecker -c ComaOverviewGeneration -c XSLTChecker -c ComaTranscriptionsNameChecker -c ComaTierOverviewCreator -c ExbEventLinebreaksChecker -c ExbRefTierChecker -c ExbTierDisplayNameChecker -e
java -Xmx3g -jar ${corpusServicesJarPath} -i ${corpusFolder}/${comaFile} -o ${corpusFolder}/curation/spellcheck-en-output.html -c LanguageToolChecker -p spelllang=en -p tier=fe -e
java -Xmx3g -jar ${corpusServicesJarPath} -i ${corpusFolder}/${comaFile} -o ${corpusFolder}/curation/spellcheck-de-output.html -c LanguageToolChecker -p spelllang=de -p tier=fg -e
java -Xmx3g -jar ${corpusServicesJarPath} -i ${corpusFolder}/${comaFile} -o ${corpusFolder}/curation/spellcheck-ru-output.html -c LanguageToolChecker -p spelllang=ru -p tier=fr -e
