#!/bin/bash

message=$1 # e.g. "Hi @all, just wanted to remind you of the standup in 5 minutes... I'll go back to sleep now, already getting hungry for data again." 
webhooksha=$2 # e.g. "XXXXXXX" 
order=$3 # e.g. "order"
url="https://your.mattermost.server/mattermost/hooks/${webhooksha}"

participants=('Name1' 'Name2' 'Name3' 'Name4' 'Name5' 'Name6' 'Name7')
participants=( $(shuf -e "${participants[@]}") )
#printf "%s" "${participants[@]}"
  
standupmessage="${message}\n***\nDie Reihenfolge ist heute:\n* ${participants[0]}\n* ${participants[1]}\n* ${participants[2]}\n* ${participants[3]}\n* ${participants[4]}\n* ${participants[5]}\n* ${participants[6]}"

holidaymessage="Today is a holiday, so there is no meeting. Have a nice day everyone! \n ![Beach](https://media.giphy.com/media/WWYSFIZo4fsLC/giphy.gif)"

TODAY=$(date +%Y-%m-%d)

printf "$TODAY"

if grep -q $TODAY /path/to/corpus-services/cubo/info/holidays
    then
	curl -i -X POST --data-urlencode "payload={\"text\": \"${holidaymessage}\"}" ${url}
    else
	 if      [ "$order" == "order" ]
            then
                curl -i -X POST --data-urlencode "payload={\"text\": \"${standupmessage}\"}" ${url}
            else
                curl -i -X POST --data-urlencode "payload={\"text\": \"${message}\"}" ${url}
        fi
fi


