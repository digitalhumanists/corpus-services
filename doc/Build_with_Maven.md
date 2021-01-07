Corpus Services is a Maven project. All sources can be added via Maven, except the EXMARaLDA.jar.

Use the EXMARaLDA.jar in the [lib folder](https://gitlab.rrz.uni-hamburg.de/corpus-services/corpus-services/-/tree/develop/lib).
You can also download the software [from its website](https://exmaralda.org/de/vorschau-version/) or build the code yourself from [Github](https://github.com/Exmaralda-Org/exmaralda). 

For compiling it is necessary to add the EXMARaLDA.jar manually to the Maven project:

## Using Maven

Using maven you can just run `mvn clean compile assembly:single` and add the EXMARaLDA.jar manually. See http://maven.apache.org/general.html#importing-jars.
Use the parameters from the pom.xml file and run something like:

<pre>
mvn install:install-file -Dfile=lib/EXMARaLDA-Preview-20201130.jar -DgroupId=org.exmaralda -DartifactId=EXMARaLDA -Dversion=Preview-20201130 -Dpackaging=jar
</pre>

## Using an IDE like Netbeans

If you use an IDE (like Netbeans, Eclipse), You can just open the project in there. You will get a lot of errors because the EXMARaLDA.jar isn't included in the maven repos. To fix this, in the IDE go to the dependencies, look for the exmaralda.jar and right click on it. Choose "Manually install artifact" and choose the location where you put your EXMARaLDA.jar (preferably in the lib folder of corpus-services). The errors should be solved then and everything should compile. 

<a href="https://gitlab.rrz.uni-hamburg.de/corpus-services/corpus-services/-/raw/develop/images/manually-install-artifact.png">
    <img src="https://gitlab.rrz.uni-hamburg.de/corpus-services/corpus-services/-/raw/develop/images/manually-install-artifact.png" alt="Logo" width="350" height="300">
  </a>

To get the automatic Doxygen creation to work to without receiving errors, for Windows download the doxygen.exe from [here](https://www.doxygen.nl/download.html) and put it directly into the corpus-services folder or install Doxygen for linux. 
