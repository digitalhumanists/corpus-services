#!/bin/bash

corpusFolder=$1 #e.g."Corpus"
comaFile=$2
reportFolder=$3
corpusServicesJarPath="/path/to/corpus-services-latest.jar"	

echo "now starting the fixes"

java -Xmx3g -jar ${corpusServicesJarPath} -i ${corpusFolder}/${comaFile} -o ${reportFolder}/fix-report-output.html -c CorpusDataRegexReplacer -p "replace=\.\.\." -p "replacement=…" -p "xpathcontext=//tier[@type='t']/event" -f -e
java -Xmx3g -jar ${corpusServicesJarPath} -i ${corpusFolder}/${comaFile} -o ${reportFolder}/fix-report-output.html -c CorpusDataRegexReplacer -p "replace=(\r\n|\r|\n|\s{2,})" -p "replacement= " -p "xpathcontext=//event" -f -e
java -Xmx3g -jar ${corpusServicesJarPath} -i ${corpusFolder}/${comaFile} -o ${reportFolder}/fix-report-output.html -c GeneralTransformer -p "exb=true" -p "xsl=file:/data/INEL/utilities/inelutilities/set-format-table.xsl" -p "overwritefiles=true" -f -e
java -Xmx3g -jar ${corpusServicesJarPath} -i ${corpusFolder}/${comaFile} -o ${reportFolder}/fix-report-output.html -s corpus-utilities/settings.param -c RemoveEmptyEvents -c ComaKmlForLocations -c ComaTranscriptionsNameChecker -c ExbRefTierChecker -c ExbSegmentationChecker -c ComaSegmentCountChecker -c RemoveAbsolutePaths -c RemoveAutoSaveExb -c ComaApostropheChecker -f -e -j


#java -Xmx3g -jar $corpusServicesJarPath -i /data/INEL/${corpusName}/${corpus} -o ${reportFolder}/report-output.html -c CorpusDataRegexReplacer -p "replace=[«‹›„‚‟‘‛’]" -p "replacement=" -p "xpathcontext=//tier[@category='tx' or @category='ts' or @category='fe']/event" -e | tee -a "$reportPath" 
                                        #java -Xmx3g -jar $corpusServicesJarPath -i /data/INEL/${corpusName}/${corpus} -o ${reportFolder}/report-output.html -c CorpusDataRegexReplacer -p "replace=[«‹›‚‟‘‛”’]" -p "replacement=" -p "xpathcontext=//tier[@category='fg']/event" -e | tee -a "$reportPath" 
                                        #java -Xmx3g -jar $corpusServicesJarPath -i /data/INEL/${corpusName}/${corpus} -o ${reportFolder}/report-output.html -c CorpusDataRegexReplacer -p "replace=[‹›„‚“‟‘‛”’]" -p "replacement=" -p "xpathcontext=//tier[@category='fr']/event"  -e | tee -a "$reportPath" 
