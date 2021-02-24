#!/bin/bash

scriptPath=`realpath $0`
scriptDirectory=`dirname $scriptPath`

json=`curl -v GET -H "X-Redmine-API-Key: XXXXXX" https://your.redmine.server/redmine/issues.json?tracker_id=XX`

cleanupcount=$(curl -v GET -H "X-Redmine-API-Key: XXXXXX" https://your.redmine.server/redmine/redmine/issues.json?tracker_id=XX | jq -r '.total_count')

max=7

if [ "$cleanupcount" -gt "$max" ]; then
              bash ${scriptDirectory}/../messenger/mattermost-message.sh "WARNING: There are ${cleanupcount} cleanup tasks in Redmine. Please close some before adding new tasks. " XXXXXX	
else
    echo "Number of cleanup tasks is $cleanupcount"
fi


