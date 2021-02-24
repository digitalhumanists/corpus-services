#!/bin/bash

#REDMINE instance, SUBJECT, DESCRIPTION, CFVALUE,
REDMINE="https://your.redmine.server/redmine"
SUBJECT=$1 # e.g. "Hi @all, just wanted to remind you of the standup in 5 minutes... I'll go back to sleep now, already getting hungry for data again." 
DESCRIPTION=$2 # e.g. "XXXXXX" 
CFVALUE=$3

#sending to Redmine API and piping to stdout and use grep to get issue ID from response
ISSUE=$( curl -vs POST -H "X-Redmine-API-Key: XXXXXX" -H "Content-Type: application/json" --data "{ \"issue\": { \"project_id\" : \"project\", \"subject\": \"$SUBJECT\", \"description\": \"$DESCRIPTION\", \"priority_id\": X, \"tracker_id\": 26, \"watcher_user_ids\": [X, X, X, X, X, X], \"assigned_to_id\": XX, \"custom_fields\": [ {\"value\":\"$CFVALUE\", \"id\":X} ] } }" "$REDMINE/issues.json"  2>&1 | grep -Po '"issue":{"id":\K[^,]*' )
echo "$REDMINE/issues/$ISSUE"
