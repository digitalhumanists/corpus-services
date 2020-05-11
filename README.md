# Introduction

The HZSK Corpus Services project bundles functionality used for maintenance, curation, conversion, and visualization of corpus data at the HZSK.  

# Gitlab artifacts

The latest compiled .jar can be found here: 
https://gitlab.rrz.uni-hamburg.de/hzsk/hzsk-corpus-services/-/jobs/artifacts/develop/browse?job=compile_withmaven


# Info

Additional info on the Corpus services can be found here:
* https://lab.multilingua.uni-hamburg.de/redmine/projects/redmine/wiki/How_to_use_the_Corpus_Validator
* https://lab.multilingua.uni-hamburg.de/redmine/projects/infrastruktur/wiki/Setting_up_-_Libraries
* https://lab.multilingua.uni-hamburg.de/redmine/projects/infrastruktur/wiki/How-to_add_a_new_function

Advanced: https://lab.multilingua.uni-hamburg.de/redmine/projects/redmine/wiki/Git-flow_with_CI_from_GitLab

# Functions

The usable functions can be found in the help output:

`java -jar hzsk-corpus-services-1.0-jar-with-dependencies.jar -h`

# Compilation

To use the validator for HZSK corpora, compile it using `mvn clean compile assembly:single`.
(See https://stackoverflow.com/questions/574594/how-can-i-create-an-executable-jar-with-dependencies-using-maven)
or use a pregenerated artifact form gitlab (see Gitlab artifacts). 

# Usage

See here for the usage of the corpus services:

https://lab.multilingua.uni-hamburg.de/redmine/projects/redmine/wiki/How_to_use_the_Corpus_Validator
