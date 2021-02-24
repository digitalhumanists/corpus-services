#!/bin/bash

message=$1 # e.g. "Hi @all, just wanted to remind you of the standup in 5 minutes... I'll go back to sleep now, already getting hungry for data again." 
webhooksha=$2 # e.g. "XXXXXXX" 
url="https://your.mattermost.server/mattermost/hooks/${webhooksha}"

curl -i -X POST --data-urlencode "payload={\"text\": \"${message}\"}" ${url}
