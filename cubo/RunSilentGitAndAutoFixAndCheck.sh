#!/bin/bash

#bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh remote branch user corpusPath reportFolder mode
#remote=$1 # e.g. "origin" 
#branch=$2 # e.g. "master" 
#user=$3 # e.g. "corpus-services"	
#corpusPath=$4 # e.g. "/path/to/corpus"		 
#comaFile=$5 # e.g. "corpus.coma"
#reportFolder=$6 # e.g. "/path/to/reports/auto-fix-and-check-scripts/${corpus}"		
#mode=$7 # e.g. "commitfirst" "commitfirstandprettyprint" "fixandcheck" "push"	

#Cubo messages
hello='Good night everyone, now it is my time to shine again, I will take care of the corpora!\n![Cubo](https://media.giphy.com/media/Zipmry4xUxD8s/giphy.gif =200 \"Cubo\")'
goodbye='I finished my quest and will now fly back into my cave again.\nHere are the reports I created:\ninsert.link.here\n![Cubo](https://media.giphy.com/media/joqrWzf3Bz9jG/giphy.gif =200 \"Cubo\")'

#Cubo says hello
bash /path/to/corpus-services/cubo/messenger/mattermost-message.sh "${hello}" XXXXXXXX


#first run all silent git scripts
bash /path/to/corpus-services/cubo/messenger/mattermost-message.sh "Running silent git scripts for all corpora now. " XXXXXXXX
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin main User /path/to/user/repository/folder/CorpusFolder corpus.coma /path/to/reports/silent-git/folder commitfirstandprettyprint
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin main User /path/to/user/repository/folder/CorpusFolder corpus.coma /path/to/reports/silent-git/folder push
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin main User /path/to/user/repository/folder/CorpusFolder corpus.coma /path/to/reports/silent-git/folder commitfirstandprettyprint
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin main User /path/to/user/repository/folder/CorpusFolder corpus.coma /path/to/reports/silent-git/folder push
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin main User /path/to/user/repository/folder/CorpusFolder corpus.coma /path/to/reports/silent-git/folder commitfirstandprettyprint
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin main User /path/to/user/repository/folder/CorpusFolder corpus.coma /path/to/reports/silent-git/folder push
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin main User /path/to/user/repository/folder/CorpusFolder corpus.coma /path/to/reports/silent-git/folder commitfirstandprettyprint
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin main User /path/to/user/repository/folder/CorpusFolder corpus.coma /path/to/reports/silent-git/folder push

#then run all auto-fix-an-checks
bash /path/to/corpus-services/cubo/messenger/mattermost-message.sh "Running all fixes and checks on the corpora now. " XXXXXXXX
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin main cubo /path/to/.cubo/folder corpus.coma /path/to/reports/auto-fix-and-check-scripts/folder fixandcheck
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin main cubo /path/to/.cubo/folder corpus.coma /path/to/reports/auto-fix-and-check-scripts/folder fixandcheck
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin main cubo /path/to/.cubo/folder corpus.coma /path/to/reports/auto-fix-and-check-scripts/folder fixandcheck
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin main cubo /path/to/.cubo/folder corpus.coma /path/to/reports/auto-fix-and-check-scripts/folder fixandcheck

#then update all silent git folders with the fixed and checked version
bash /path/to/corpus-services/cubo/messenger/mattermost-message.sh "Updating all corpus folders with the fixed and checked version. " XXXXXXXX
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin main User /path/to/user/repository/folder/CorpusFolder corpus.coma /path/to/reports/silent-git/folder update
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin main User /path/to/user/repository/folder/CorpusFolder corpus.coma /path/to/reports/silent-git/folder update
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin main User /path/to/user/repository/folder/CorpusFolder corpus.coma /path/to/reports/silent-git/folder update
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin main User /path/to/user/repository/folder/CorpusFolder corpus.coma /path/to/reports/silent-git/folder update

bash /path/to/corpus-services/cubo/messenger/mattermost-message.sh "Running silent git scripts on other folders now. " XXXXXXXX
# different repository with no checks and fixes and without pretty printing
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin master User /path/to/user/repository/folder nocoma /path/to/reports/silent-git/folder commitfirst
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin master User /path/to/user/repository/folder nocoma /path/to/reports/silent-git/folder push

#Cubo says goodbye
bash /path/to/corpus-services/cubo/messenger/mattermost-message.sh "${goodbye}" XXXXXXXX
