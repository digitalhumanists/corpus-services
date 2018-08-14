# Introduction

The HZSK Corpus Services project bundles functionality used for maintenance, curation, conversion, and visualization of corpus data at the HZSK.  

# Gitlab artifacts

The latest compiled .jar can be found here: 
https://gitlab.rrz.uni-hamburg.de/hzsk/hzsk-corpus-services/-/jobs/artifacts/develop/browse?job=compile_withmaven



Additional info on the Corpus services can be found here:
https://lab.multilingua.uni-hamburg.de/redmine/projects/redmine/wiki/How_to_use_the_Corpus_Validator

# functions

To see the currently available functions, check the Doxygen files.

You can get a rough idea by listing java classes, e.g. `ls src/main/java/de/uni_hamburg/corpora/validation/` for validator tools.

# Compilation

To use the validator for HZSK corpora, compile it using `mvn clean compile assembly:single`.
(See https://stackoverflow.com/questions/574594/how-can-i-create-an-executable-jar-with-dependencies-using-maven)


# Usage

See here for the usage of the corpus services:

https://lab.multilingua.uni-hamburg.de/redmine/projects/redmine/wiki/How_to_use_the_Corpus_Validator
