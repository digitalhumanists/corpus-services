#!/bin/bash

corpusFolder=$1 #e.g."Corpus"
comaFile=$2
audio=$3 #e.g. true
corpusServicesJarPath="/path/to/corpus-services-latest.jar"	
corpusname=${comaFile%.*}

echo "now starting the zipping"

if [ "$audio" = "true" ]
    then
        corpusname="${comaFile%.*}_audio"
fi

echo "SOURCE_FOLDER=${corpusFolder} OUTPUT_ZIP_FILE=${corpusFolder}/resources/${corpusname}.zip AUDIO={$audio}"

java -Xmx3g -jar ${corpusServicesJarPath} -i ${corpusFolder}/${comaFile} -o ${corpusFolder}/curation/zip-report-output.html -c ZipCorpus -p "source_folder"= ${corpusFolder} -p "output_zip_file"="${corpusFolder}/resources/${corpusname}.zip" -p "audio"="${audio}" -e

