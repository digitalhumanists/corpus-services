Corpus-services is a Maven project. All sources can be added via Maven, except the EXMARaLDA.jar.

Use the EXMARaLDA.jar in the [lib folder](https://gitlab.rrz.uni-hamburg.de/corpus-services/corpus-services/lib).
You can download the software [from its website](https://exmaralda.org/de/vorschau-version/) or build the code yourself from [Github](https://github.com/Exmaralda-Org/exmaralda). 

For compiling it is necessary to add the EXMARaLDA.jar manually to the Maven project:

## Using Maven

Using maven you can just run `mvn clean compile assembly:single` and add the Exmaralda Jar manually. See http://maven.apache.org/general.html#importing-jars.
Use the parameters from the pom.xml file and run something like:

<pre>
mvn install:install-file -Dfile=C:\Path\2\repository\corpus-services\lib\EXMARaLDA.jar -DgroupId=org.exmaralda -DartifactId=exmaralda -Dversion=1.10 -Dpackaging=jar
</pre>

## Using an IDE like Netbeans

If you use an IDE (like Netbeans, Eclipse), You can just open the project in there. You will get a lot of errors because the EXMARaLDA.jar isn't included in the maven repos. To fix this, in the IDE go to the dependencies, look for the exmaralda.jar and right click on it. Choose "Manually install artifact" and choose the location where you put your EXMARaLDA.jar (preferably in the lib folder of corpus-services). The errors should be solved then and everything should compile. 

(If IDE reports sth like `Cannot run program "cmd" (in directory "C:\Users\Administrator\Desktop\INEL\corpus-services"): Malformed argument has embedded quote: "C:\Program Files\NetBeans 8.2\java\maven\bin\mvn.bat" -DartifactId=exmaralda -DgroupId=org.exmaralda -Dversion=1.10 -Dpackaging=jar -Dfile=C:\Users\Administrator\Desktop\INEL\corpus-services\lib\EXMARaLDA.jar -DgeneratePom=false -Dmaven.ext.class.path="C:\Program Files\NetBeans 8.2\java\maven-nblib\netbeans-eventspy.jar" -Dfile.encoding=UTF-8 install:install-file` try the Maven command from command line)

!https://gitlab.rrz.uni-hamburg.de/corpus-services/corpus-services/images/manually-install-artifact.png!

To get the automatic doxygen creation to work to without receiving errors, for Windows download the doxygen.exe from here http://www.stack.nl/~dimitri/doxygen/download.html and put it directly into the corpus-services folder or install doxygen for linux 
