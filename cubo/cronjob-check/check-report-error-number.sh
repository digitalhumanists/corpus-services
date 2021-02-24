#!/bin/bash

timestamp=$(date +%Y%m%d%H%M%S)
reportOutputPath="/path/to/"
reportPath="/path/to/reports/"
reportOutputFolders=(corpus corpus corpus corpus)
curationFolder="/curation/"
reportName="report-output.html"
max=$1
#e.g. 2500
scriptPath=`realpath $0`
scriptDirectory=`dirname $scriptPath`
remote="origin"

for i in "${reportOutputFolders[@]}";
     do 
        cd ${reportOutputPath}${i}/
        echo "$(git config --get remote.${remote}.url)"
        if [[ "$(git config --get remote.${remote}.url)" == *corpus.git ]]; then
            mattermosthash="XXXXXX"
        elif [[ "$(git config --get remote.${remote}.url)" == *corpus.git ]]; then
             mattermosthash="XXXXXX"
        else
            mattermosthash="XXXXXX"
        fi
        echo "$i"
        report=${reportOutputPath}${i}${curationFolder}
        echo $report
        if [[ $(find "$report" -name ${reportName} -type f) ]]
            then
                echo "Report Output File ${report}${reportName} exists." 
                f="${report}${reportName}" 
                echo "$f"
                #result=$(echo "$f" | cut -c1-17)
                while read -r line ; do
                    result="${result} $(echo "$line"  | tr '\n' ' ')" 
                done <<<$(grep 'Total: ' $f)
                #now we have  Total: 100 %: 2 OK, 0 bad, 0 warnings and 0 unknown. = 2 items. - we only want to check the bad value
                echo "$result"
                #echo "$result" | cut -d' ' -f 7
                result=$( echo "$result" | cut -d' ' -f 7)
               	result="${result//./}"
		echo "$result"
                if [[ "$result" -gt "$max" ]]
                    then
                        echo "WARNING: there are ${result} errors in ${report}"
                        bash ${scriptDirectory}/../messenger/mattermost-message.sh "WARNING: There are **${result}** errors the file ${reportName} in corpus ${i}.\n![Warning](https://media.giphy.com/media/Zsx8ZwmX3ajny/giphy.gif =200)" "${mattermosthash}"
                else
                     echo "File ${report}${reportName}: number of errors is under ${max}" 
                fi
            else

                echo "ERROR: report wasn't written in ${report}"
                bash ${scriptDirectory}/../messenger/mattermost-message.sh "ERROR: ${reportName} wasn't found in ${report}." "${mattermosthash}"
        fi
    done



