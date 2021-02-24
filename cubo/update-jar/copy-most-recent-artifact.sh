#!/bin/bash
cd /path/to/folder
#we don't need a private token since the project is publicly accessible
curl --header "" 'https://gitlab.rrz.uni-hamburg.de/api/v4/projects/279/jobs/artifacts/develop/download?job=compile_withmaven' --output artifact.zip; unzip artifact.zip; rm artifact.zip; rm -f corpus-services-latest.jar; cp target/* corpus-services-latest.jar; mv target/* .; rmdir target
cd ../../path/to/other/folder
curl --header "" 'https://gitlab.rrz.uni-hamburg.de/api/v4/projects/279/jobs/artifacts/develop/download?job=compile_withmaven' --output artifact.zip; unzip artifact.zip; rm artifact.zip; rm -f corpus-services-latest.jar; cp target/* corpus-services-latest.jar; mv target/* .; rmdir target
