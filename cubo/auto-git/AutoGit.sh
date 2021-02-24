#!/bin/bash

today=$(date)
timestamp=$(date +%Y%m%d%H%M%S)
remote=$1 # e.g. "origin" 
branch=$2 # e.g. "master" 
user=$3 # e.g. "corpus-services"	
corpusPath=$4 # e.g. "/path/to/Corpus"		   
comaFile=$5 # e.g. "corpus.coma"
reportFolder=$6 # e.g. "/path/to/reports/auto-fix-and-check-scripts/${corpus}"		
mode=$7 # e.g. "commitfirst" "commitfirstandprettyprint" "check" "fixandcheck" "fixandcheckold" "push" "normalize" "visualize" "exporttei" "zip"	"zipaudio" "cmdicheck"													 
reportPath="${reportFolder}/${timestamp}-report.txt"
conflictPath="${reportFolder}/${timestamp}-conflict.txt"
corpusServicesJarPath="/path/to/corpus-services-latest.jar"	
conflict="Please resolve conflict manually in ${corpusPath}."
changes="There are unstaged changes present. Please commit or remove them in ${corpusPath}."
corruptpull="A pull was not executed properly. Please resolve that issue manually in ${corpusPath}."
commitfirst="commitfirst"
check="check"
commitfirstandprettyprint="commitfirstandprettyprint"
fixandcheck="fixandcheck"
fixandcheckold="fixandcheckold"
push="push"
normalize="normalize"
visualize="visualize"
exporttei="exporttei"
zip="zip"
zipaudio="zipaudio"
cmdicheck="cmdicheck"
software="corpus-services"
DESCRIPTION="Solving Git conflicts manually: https://link.to.wiki \n\n${conflict}"
DESCRIPTIONmerge="Fixing a corrupt merge: https://link.to.wiki"
scriptPath=`realpath $0`
scriptDirectory=`dirname $scriptPath`
if [[ "$(git config --get remote.${remote}.url)" == *corpus.git ]]; then
    mattermosthash="XXXXXXX"
elif [[ "$(git config --get remote.${remote}.url)" == *corpus.git ]]; then
     mattermosthash="XXXXXXX"
else
    mattermosthash="XXXXXXX"
fi

# create directories if they do not exist
[ ! -d "$(dirname $reportPath)" ] && mkdir -p "$(dirname $reportPath)"
[ ! -d "$(dirname $conflictPath)" ] && mkdir -p "$(dirname $conflictPath)"

echo "Started running the AutoGit script." >> $reportPath 2>&1

cd $corpusPath

found_conflict () {
        #there is a conflict
        echo "There is a merge conflict. Aborting" >> $reportPath 2>&1
        git merge --abort >> $reportPath 2>&1
		echo ${conflict} >> $reportPath 2>&1
		echo ${conflict} >> $conflictPath 2>&1
		issue=$( bash ${scriptDirectory}/../messenger/redmine-issue.sh "${conflict}" "${DESCRIPTION}" "${corpusPath}" )
        bash ${scriptDirectory}/../messenger/mattermost-message.sh "${conflict} $issue" "${mattermosthash}"							
		exit 1
}		
corrupt_pull () {
        #a corrupt pull happened
        echo ${corruptpull} >> $reportPath 2>&1
        echo ${corruptpull} >> $conflictPath 2>&1
        issue=$( bash ${scriptDirectory}/../messenger/redmine-issue.sh "${corruptpull}" "${DESCRIPTIONmerge}" "${corpusPath}" )
        bash ${scriptDirectory}/../messenger/mattermost-message.sh "${corruptpull} $issue (the corrupt pull happened during the AutoGit script)" "${mattermosthash}"	
        #create an index.lock file so no other git operations will run on this corrupted repository
        echo "This file can be deleted after the corrupted pull is fixed. " >> .git/index.lock		
        exit 1
}
unstanged_changes () {
        #unstaged changes present
        echo ${changes} >> $reportPath 2>&1
        echo ${changes} >> $conflictPath 2>&1
        bash ${scriptDirectory}/../messenger/mattermost-message.sh "${changes}
" "${mattermosthash}"
        exit 1
}
update () {
        git fetch >> $reportPath 2>&1
        #a conflict could happen here
        git merge ${remote}/${branch} >> $reportPath 2>&1
        git log -1 >> $reportPath 2>&1
}
CONFLICTS=$(git ls-files -u | wc -l)
if [ "$CONFLICTS" -gt 0 ]
#there are conflicts present already
then
        #there is a conflict
        found_conflict
#no merge conflict present
else
        if [[ "$(git rev-parse --abbrev-ref --symbolic-full-name @{u})" != ${remote}/${branch} ]]
            #error: trying to use script with branch you are not currently on
            #todo: maybe also check the remote?
            then            
                echo "ATTENTION: The script is called with remote/branch ${remote}/${branch} on remote/branch $(git rev-parse --abbrev-ref --symbolic-full-name @{u}) in ${corpusPath}." >> $reportPath 2>&1
                bash ${scriptDirectory}/../messenger/mattermost-message.sh "ATTENTION: The script is called with remote/branch ${remote}/${branch} on remote/branch $(git rev-parse --abbrev-ref --symbolic-full-name @{u}) in ${corpusPath}." "${mattermosthash}"	
                exit 1
        else
            if [ -z "$(git status --porcelain)" ] 
            then                  
            #there are no changes present - check if there is something to do
            #mode=$7 # e.g. "commitfirst" "commitfirstandprettyprint" "check" "fixandcheck" "push" "normalize" "visualize" "exporttei" "zip" "zipaudio" "cmdicheck"	
                    if [ "$mode" == "update" ] || [ "$mode" == "check" ] || [ "$mode" == "fixandcheck" ] || [ "$mode" == "fixandcheckold" ] || [ "$mode" == "push" ] || [ "$mode" == "normalize" ] || [ "$mode" == "visualize" ] || [ "$mode" == "exporttei" ] || [ "$mode" == "zip" ] || [ "$mode" == "zipaudio" ] || [ "$mode" == "cmdicheck" ]
	                        then
                                echo "No merge conflict to begin with." >> $reportPath 2>&1		
                                    update					                				                
	                                CONFLICTS=$(git ls-files -u | wc -l)
	                                if [ "$CONFLICTS" -gt 0 ]
		                                then
                                            #there is a conflict
                                            found_conflict
	                                    else
                                            #there is no conflict
                                            #but a corrupted merge could have happened
                                            if [ -z "$(git status --porcelain)" ] 
		                                        then
			                                        echo "Merging was successful or not needed." >> $reportPath 2>&1
	                                        else                                           
                                                    #a corrupt pull happened
                                                    corrupt_pull
	                                        fi	
                                            if	[ "$mode" == "$check" ] && [ -n "$(git log --since='20 hours ago')" ]
                                                then
			                                        #run the checks on the existing data
			                                        bash ${scriptDirectory}/../curation-bot/checks.sh ${corpusPath} ${comaFile} >> $reportPath 2>&1 
                                                    #now add the checker files
                                                    git add curation/CorpusServices_Errors.xml >> $reportPath 2>&1 
			                                        git add curation/coma_overview.html >> $reportPath 2>&1
			                                        git add curation/report-output.html >> $reportPath 2>&1 
			                                        git add curation/report-statistics.html >> $reportPath 2>&1 
			                                        git add curation/tier_overview.html >> $reportPath 2>&1 		
                                                    #read -p "Press enter to continue" 
                                                    git commit -m "[$software] Automatic checks by $user on $today" >> $reportPath 2>&1 
                                                    #maybe do git checkout $branch here instead to make sure nothing else is left?
                                                    git push $remote $branch >> $reportPath 2>&1
                                            fi		
                                            if	[ "$mode" == "$fixandcheck" ] && [ -n "$(git log --since='20 hours ago')" ]
                                                then
			                                        #run the checks on the existing data
                                                    bash ${scriptDirectory}/../curation-bot/checks.sh ${corpusPath} ${comaFile} >> $reportPath 2>&1 
                                                    #read -p "Press enter to continue" 
                                                    #create statistics
                                                    bash ${scriptDirectory}/../curation-bot/statistics.sh ${corpusPath} ${comaFile}  ${reportFolder} >> $reportPath 2>&1 
			                                        #pretty print the data
                                                    bash ${scriptDirectory}/../curation-bot/prettyprint.sh ${corpusPath} ${comaFile}  ${reportFolder} >> $reportPath 2>&1 
			                                        #add the pretty printed files and commit
                                                    git add -A >> $reportPath 2>&1 
                                                    git reset -- curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
		                                        git reset -- curation/coma_overview.html >> $reportPath 2>&1
		                                        git reset -- curation/report-output.html >> $reportPath 2>&1 
		                                        git reset -- curation/report-statistics.html >> $reportPath 2>&1 
    		                                        git reset -- curation/tier_overview.html >> $reportPath 2>&1 	                                                  
                                                    git checkout curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git checkout curation/coma_overview.html >> $reportPath 2>&1
			                                        git checkout curation/report-output.html >> $reportPath 2>&1 
			                                        git checkout curation/tier_overview.html >> $reportPath 2>&1 	
                                                    git commit -m "[$software] Pretty printed by $user on $today" >> $reportPath 2>&1 
                                                    #read -p "Press enter to continue" 
			                                        # now fix the data
                                                    bash ${scriptDirectory}/../curation-bot/fixes.sh ${corpusPath} ${comaFile} ${reportFolder} >> $reportPath 2>&1 
	                                            #now add the fixed data and commit
                                                    git add -A >> $reportPath 2>&1
                                                    git reset -- curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git checkout curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git commit -m "[$software] Automatic fixes by $user on $today" >> $reportPath 2>&1 
                                                    #read -p "Press enter to continue" 
			                                        # now run the checks on the fixed data
			                                        bash ${scriptDirectory}/../curation-bot/checks.sh ${corpusPath} ${comaFile} >> $reportPath 2>&1 
                                                    #read -p "Press enter to continue" 
                                                    #now add the checker files
                                                    git add curation/CorpusServices_Errors.xml >> $reportPath 2>&1 
  	                                            git add curation/coma_overview.html >> $reportPath 2>&1
	                                            git add curation/report-output.html >> $reportPath 2>&1 
	                                            git add curation/report-statistics.html >> $reportPath 2>&1 
	                                            git add curation/tier_overview.html >> $reportPath 2>&1 		
                                                    git add curation/spellcheck-en-output.html >> $reportPath 2>&1
						    git add curation/spellcheck-de-output.html >> $reportPath 2>&1 
						    git add curation/spellcheck-ru-output.html >> $reportPath 2>&1 
                                                    #create statistics
                                                    bash ${scriptDirectory}/../curation-bot/statistics.sh ${corpusPath} ${comaFile} ${reportFolder} >> $reportPath 2>&1 			
			                                        #add statistics file and commit 
                                                    git add curation/report-statistics.html >> $reportPath 2>&1 
                                                    #read -p "Press enter to continue" 
                                                    git commit -m "[$software] Automatic checks by $user on $today" >> $reportPath 2>&1 
			                                        git checkout curation/CorpusServices_Errors.xml
                                                    #maybe do git checkout $branch here instead to make sure nothing else is left?
                                                    git push $remote $branch >> $reportPath 2>&1
                                            fi
                                            if	[ "$mode" == "$fixandcheckold" ]
                                                then
			                                        #run the checks on the existing data
                                                    bash ${scriptDirectory}/../curation-bot/checks.sh ${corpusPath} ${comaFile} >> $reportPath 2>&1 
                                                    #read -p "Press enter to continue" 
                                                    #create statistics
                                                    bash ${scriptDirectory}/../curation-bot/statistics.sh ${corpusPath} ${comaFile}  ${reportFolder} >> $reportPath 2>&1 
			                                        #pretty print the data
                                                    bash ${scriptDirectory}/../curation-bot/prettyprint.sh ${corpusPath} ${comaFile}  ${reportFolder} >> $reportPath 2>&1 
			                                        #add the pretty printed files and commit
                                                    git add -A >> $reportPath 2>&1 
                                                    git reset -- curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
		                                        git reset -- curation/coma_overview.html >> $reportPath 2>&1
		                                        git reset -- curation/report-output.html >> $reportPath 2>&1 
		                                        git reset -- curation/report-statistics.html >> $reportPath 2>&1 
    		                                        git reset -- curation/tier_overview.html >> $reportPath 2>&1 	                                                  
                                                    git checkout curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git checkout curation/coma_overview.html >> $reportPath 2>&1
			                                        git checkout curation/report-output.html >> $reportPath 2>&1 
			                                        git checkout curation/tier_overview.html >> $reportPath 2>&1 	
                                                    git commit -m "[$software] Pretty printed by $user on $today" >> $reportPath 2>&1 
                                                    #read -p "Press enter to continue" 
			                                        # now fix the data
                                                    bash ${scriptDirectory}/../curation-bot/fixes.sh ${corpusPath} ${comaFile} ${reportFolder} >> $reportPath 2>&1 
	                                            #now add the fixed data and commit
                                                    git add -A >> $reportPath 2>&1
                                                    git reset -- curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git checkout curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git commit -m "[$software] Automatic fixes by $user on $today" >> $reportPath 2>&1 
                                                    #read -p "Press enter to continue" 
			                                        # now run the checks on the fixed data
			                                        bash ${scriptDirectory}/../curation-bot/checks.sh ${corpusPath} ${comaFile} >> $reportPath 2>&1 
                                                    #read -p "Press enter to continue" 
                                                    #now add the checker files
                                                    git add curation/CorpusServices_Errors.xml >> $reportPath 2>&1 
  	                                            git add curation/coma_overview.html >> $reportPath 2>&1
	                                            git add curation/report-output.html >> $reportPath 2>&1 
	                                            git add curation/report-statistics.html >> $reportPath 2>&1 
	                                            git add curation/tier_overview.html >> $reportPath 2>&1 		
                                                    git add curation/spellcheck-en-output.html >> $reportPath 2>&1
						    git add curation/spellcheck-de-output.html >> $reportPath 2>&1 
						    git add curation/spellcheck-ru-output.html >> $reportPath 2>&1 
                                                    #create statistics
                                                    bash ${scriptDirectory}/../curation-bot/statistics.sh ${corpusPath} ${comaFile} ${reportFolder} >> $reportPath 2>&1 			
			                                        #add statistics file and commit 
                                                    git add curation/report-statistics.html >> $reportPath 2>&1 
                                                    #read -p "Press enter to continue" 
                                                    git commit -m "[$software] Automatic checks by $user on $today" >> $reportPath 2>&1 
			                                        git checkout curation/CorpusServices_Errors.xml
                                                    #maybe do git checkout $branch here instead to make sure nothing else is left?
                                                    git push $remote $branch >> $reportPath 2>&1
                                            fi
                                            if	[ "$mode" == "$push" ]
                                                 then
			                                        #push the current state without any fixes or checks
                                                    echo "Merging was successful or not needed." >> $reportPath 2>&1
                                                    git push $remote $branch >> $reportPath 2>&1
                                            fi
                                            if	[ "$mode" == "$normalize" ]
                                                 then
			                                        #normalize the data and then push it
                                                    # now normalize the data
                                                    bash ${scriptDirectory}/../curation-bot/normalize.sh ${corpusPath} ${comaFile} >> $reportPath 2>&1 
                                                    #now add the fixed data and commit
                                                    git add -A >> $reportPath 2>&1
                                                    git reset -- curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git checkout curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git commit -m "[$software] Normalized by $user $today" >> $reportPath 2>&1 
                                                    git push $remote $branch >> $reportPath 2>&1
                                            fi
                                            if	[ "$mode" == "$visualize" ]
                                                 then
			                                        # export tei from the data and then push it
                                                    bash ${scriptDirectory}/../curation-bot/visualize.sh ${corpusPath} ${comaFile} >> $reportPath 2>&1 
                                                    #now add the fixed data and commit
                                                    git add -A >> $reportPath 2>&1
                                                    git reset -- curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git checkout curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git commit -m "[$software] Visualizations added by $user $today" >> $reportPath 2>&1 
                                                    git push $remote $branch >> $reportPath 2>&1
                                            fi
                                            if	[ "$mode" == "$exporttei" ]
                                                 then
			                                        # export ISO TEI the data and then push it
                                                    bash ${scriptDirectory}/../curation-bot/isoteiinel.sh ${corpusPath} ${comaFile} >> $reportPath 2>&1 
                                                    #now add the fixed data and commit
                                                    git add -A >> $reportPath 2>&1
                                                    git reset -- curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git checkout curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git commit -m "[$software] ISO TEI exported by $user $today" >> $reportPath 2>&1 
                                                    git push $remote $branch >> $reportPath 2>&1
                                            fi
                                            if	[ "$mode" == "$zip" ]
                                                 then
			                                        # export ISO TEI the data and then push it
                                                    bash ${scriptDirectory}/../curation-bot/zipcorpus.sh ${corpusPath} ${comaFile} false >> $reportPath 2>&1 
                                                    #now add the fixed data and commit
                                                    git add -A >> $reportPath 2>&1
                                                    git reset -- curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git checkout curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git commit -m "[$software] Zip created by $user $today" >> $reportPath 2>&1 
                                                    git push $remote $branch >> $reportPath 2>&1
                                            fi
                                            if	[ "$mode" == "$zipaudio" ]
                                                 then
			                                        # export ISO TEI the data and then push it
                                                    bash ${scriptDirectory}/../curation-bot/zipcorpus.sh ${corpusPath} ${comaFile} true >> $reportPath 2>&1 
                                                    #now add the fixed data and commit
                                                    git add -A >> $reportPath 2>&1
                                                    git reset -- curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git checkout curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git commit -m "[$software] Zip created exported by $user $today" >> $reportPath 2>&1 
                                                    git push $remote $branch >> $reportPath 2>&1
                                            fi
                                            if	[ "$mode" == "$cmdicheck" ]
                                                 then
			                                        # Check cmdi data in a report and then push it
                                                    bash ${scriptDirectory}/../curation-bot/cmdicheck.sh ${corpusPath} ${comaFile} >> $reportPath 2>&1 
                                                    #now add the fixed data and commit
                                                    git add -A >> $reportPath 2>&1
                                                    git reset -- curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git checkout curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
                                                    git commit -m "[$software] CMDI checked by $user $today" >> $reportPath 2>&1 
                                                    git push $remote $branch >> $reportPath 2>&1
                                            fi
	                                fi
	                                if [ -z "$(git status --porcelain)" ] 
		                                then
			                                echo "Everything was successful." >> $reportPath 2>&1
	                                else
                                            #there are unstaged changes
                                            unstanged_changes
	                                fi			
		                        exit 0
                       else 
                            echo "Nothing done with the mode $mode and no unstaged changes." >> $reportPath 2>&1                           
                       fi
			                else
                            #there are changes present - either commit them or throw an error	
                                    if	[ "$mode" == "$commitfirst" ]
                                        then
                                            			git add -A >> $reportPath 2>&1
			                                            git commit -m "Auto-committed by $user on $today" >> $reportPath 2>&1
                                        else
	                                            if	[ "$mode" == "$commitfirstandprettyprint" ]
                                                    then    
                                                            git add -A >> $reportPath 2>&1
			                                                git commit -m "Auto-committed by $user on $today" >> $reportPath 2>&1
                                                            #pretty print the data
                                                            bash ${scriptDirectory}/../curation-bot/prettyprint.sh ${corpusPath} ${comaFile} ${reportFolder} >> $reportPath 2>&1 
			                                                git add -A >> $reportPath 2>&1
			                                                git reset -- curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
			                                                git checkout curation/CorpusServices_Errors.xml  >> $reportPath 2>&1
			                                                git commit -m "Automatically pretty printed by $user on $today" >> $reportPath 2>&1
                                                    else
                                                            #unstaged changes present
                                                            echo "There can't be unstaged changes with the mode ${mode}." >> $reportPath 2>&1  
                                                            unstanged_changes
                                                fi
                        
                                    fi		
                                    
             fi	
        fi
fi			

