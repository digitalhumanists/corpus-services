#!/bin/bash

# code from https://stackoverflow.com/questions/12850030/git-getting-all-previous-version-of-a-specific-file-folder

# we'll write all git versions of the file to this folder:
#EXPORT_TO=/path/to/all_git_versions_exported
EXPORT_TO=$1
# take relative path to the file to inspect
FILE_PATH=$2

USAGE="Please cd to the root of your git project and specify the path to file you with to inspect (example: $0 some/path/to/file)"

# check if got argument
if [ "${FILE_PATH}" == "" ]; then
    echo "error: no arguments given. ${USAGE}" >&2
    exit 1
fi

# check if file exist
if [ ! -f ${FILE_PATH} ]; then
    echo "error: File '${FILE_PATH}' does not exist. ${USAGE}" >&2
    exit 1
fi

# extract just a filename from given relative path (will be used in result file names)
FILE_NAME="$(basename "$FILE_PATH")"

# create folder to store all revisions of the file
if [ ! -d ${EXPORT_TO} ]; then
    echo "creating folder: ${EXPORT_TO}"
    mkdir ${EXPORT_TO}
fi

## uncomment next line to clear export folder each time you run script
#rm ${EXPORT_TO}/*

# reset coutner
#COUNT=0

# iterate all revisions
#git rev-list --all --objects -- ${FILE_PATH} | \
#    cut -d ' ' -f1 | \
#while read h; do \
#     COUNT=$((COUNT + 1)); \
#     COUNT_PRETTY=$(printf "%04d" $COUNT); \
#     COMMIT_DATE=`git show $h | head -3 | grep 'Date:' | awk '{print $4"-"$3"-"$6}'`; \
#     if [ "${COMMIT_DATE}" != "" ]; then \
#         git cat-file -p ${h}:${FILE_PATH} > ${EXPORT_TO}/${COUNT_PRETTY}.${COMMIT_DATE}.${h}.#${GIT_SHORT_FILENAME};\
#     fi;\
#done    

echo "Writing files to '$EXPORT_TO'"
git log --diff-filter=d --date-order --reverse --format="%ad %H" --date=iso-strict "$FILE_PATH" | grep -v '^commit' | \
    while read LINE; do \
        COMMIT_DATE=`echo $LINE | cut -d '+' -f 1`; \
        COMMIT_DATE=${COMMIT_DATE//:};\
        COMMIT_SHA=`echo $LINE | cut -d ' ' -f 2`; \
        printf '.' ; \
        git cat-file -p "$COMMIT_SHA:$FILE_PATH" > "$EXPORT_TO/$COMMIT_DATE.$COMMIT_SHA.$FILE_NAME" ; \
    done
echo

# return success code
#echo "result stored to ${EXPORT_TO}"

exit 0
