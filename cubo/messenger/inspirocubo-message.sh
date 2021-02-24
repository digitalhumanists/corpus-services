#!/bin/bash

message=$1 # e.g. "Hi @all, just wanted to remind you of the standup in 5 minutes... I'll go back to sleep now, already getting hungry for data again." 
webhooksha=$2 # e.g. "XXXXXXX" 
url="https://your.mattermost.server/mattermost/hooks/${webhooksha}"

readarray -t inspiroarray < /path/to/corpus-services/cubo/messenger/monday-morning-motivation.txt

#printf "%s\n" "${inspiroarray[@]}"

#declare -p inspiroarray

randompos=$[ $RANDOM % ${#inspiroarray[@]} ] 


inspmessage="$message${inspiroarray[$randompos]}"

curl -i -X POST --data-urlencode "payload={\"text\": \"${inspmessage}\"}" ${url}










