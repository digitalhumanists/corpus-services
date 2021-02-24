#!/bin/bash

timestamp=$(date +%Y%m%d%H%M%S)
reportPath="/path/to/reports/"
reportFolders=(auto-fix-and-check-scripts/dolgan auto-fix-and-check-scripts/corpus auto-fix-and-check-scripts/corpus auto-fix-and-check-scripts/corpus silent-git/corpus silent-git/Corpus silent-git/folder silent-git/Corpus silent-git/Corpus silent-git/Corpus silent-git/Corpus)
reportName="*-report.txt"
scriptPath=`realpath $0`
scriptDirectory=`dirname $scriptPath`


for i in "${reportFolders[@]}";
     do 
        echo "$i"
        report=${reportPath}${i}/
        echo $report
        if [[ $(find "$report" -name ${reportName} -mtime -2 -type f) ]]
            then
                echo "File ${report}${reportName} exists and is newer than 2 days" 
            else
                conflictPath="${report}${timestamp}-conflict.txt"
                echo "ERROR: report wasn't written in ${report}" >> $conflictPath
			    issue=$( bash ${scriptDirectory}/../messenger/redmine-issue.sh "Report not written in ${report}" "ERROR: report wasn't written in ${report}" "${report}" )
                bash ${scriptDirectory}/../messenger/mattermost-message.sh "ERROR: report wasn't written in ${report} ${issue}" XXXXXXX	
        fi
    done



