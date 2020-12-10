A function in the corpus-services is a routine to be carried out on corpus data (e.g. transcriptions) that processes it or changes it (e.g. for automatic fixes) and creates an error report and possibly newly created files (e.g. for data conversion into other formats).

See the list of all currently available validation functions [List of corpus functions](https://gitlab.rrz.uni-hamburg.de/corpus-services/corpus-services/-/raw/develop/doc/List_of_corpus_functions.md).


# How-to add a new function to the corpus-services

## 1. Create a new class with a describing name to the matching package (e.g. `PrettyPrintData` in validation package)

* The class needs to extend the abstract class of the package (e.g. in validation the class called `Checker`) and it needs to implement the Interface CorpusFunction(`corpus-services\src\main\java\de\uni_hamburg\corpora\CorpusFunction.java`)
* The constructor needs to overwrite the constructor of the abstract class it implements, using the super(Boolean) method
** the super method takes a Boolean as a parameter, `false` means you class does not have a fixing option, `true` means it does have one
** example:

<pre><code class="java">
public MyNewFunction(){
super(false);
}
</code></pre>

* the new function needs to give back a `Report`, the fix option also gives back a `Report` but additionally needs to use a write() method in the Class CorpusIO`(corpus-services\src\main\java\de\uni_hamburg\corpora\CorpusIO.java`) so the fix will be done
* a fix should add a 'FIX' report item to the report when the fix was carried out successfully (Report.addFix(...) method)
* you need to implement the methods function(CorpusData cd, Boolean fix) and the function(Corpus c, Boolean fix). The function for the Corpus object will iterate over the files present in the corpus and then call the CorpusData function for each, using the Report.merge(Report) function to merge the different Reports together
* the `getIsUsableFor()` method needs to add all the Classes (in our java project, e.g. `BasicTranscriptionData`,`SegmentedTranscriptionData`,`ComaData`,`UnspecifiedXMLData` to the IsUsableFor Collection
* example:
<pre><code class="language-java">
    /**
     * Default function which determines for what type of files (basic
     * transcription, segmented transcription, coma etc.) this feature can be
     * used.
     */
    @Override
    public Collection<Class<? extends CorpusData>> getIsUsableFor() {
        try {
            Class cl = Class.forName("de.uni_hamburg.corpora.BasicTranscriptionData");
            IsUsableFor.add(cl);
        } catch (ClassNotFoundException ex) {
            report.addException(ex, " usable class not found");
        }
        return IsUsableFor;
    }
</code></pre>

## 2. How to create correct errors/ReportItems

You can issue WARNING, CRITICAL, CORRECT and FIX report items using a corpus data object.
<pre><code class="java">
`Override
    public Report function(CorpusData cd, Boolean fix) // check whether there's any illegal apostrophes '
            throws SAXException, IOException, ParserConfigurationException, URISyntaxException, TransformerException, XPathExpressionException {
        Report stats = new Report();         // create a new report
   ...
            if (fix) {
                ...
                cio.write(cd, cd.getURL());    // write back to coma file with allowed apostrophes ´
                stats.addFix(function, cd, "Corrected the apostrophes"); // fix report
            } else {
                stats.addWarning(function, cd, "Coma file is containing apostrophe(s) ’");
            }
        } else {
            stats.addCorrect(function, cd, "Coma file does not contain apostrophes");
        }
        return stats; // return the report with warnings
    }
</code></pre>




When it makes sense to display the Errors in an Exmaralda ErrorList (that can be opened with the PartiturEditor), you can add the exma error additionally.
To do so,  add `import static de.uni_hamburg.corpora.CorpusMagician.exmaError;` to your class, and add the error directly after the ErrorList errors like 
` exmaError.addError(EXB_REFS, cd.getURL().getFile(), tierID, eventStart, false, "Error: File in referenced-file NOT found");` 
with the method: 
`addError(String statId, String fileName, String tierID, String eventStart, boolean done, String description)`

<pre><code class="java">
exmaError.addError(function, filename, fsme.getTierID(), fsme.getTLI(), false, text);
</pre>

## 3. Fill out the getDescription() method

This method should return a simple string describing what the new function is about
Example:
<pre><code class="java">
   /**
     * Default function which returns a two/three line description of what this
     * class is about.
     */
    @Override
    public String getDescription() {
        String description = "This class checks whether or not the coma file "
                + "contains an apostrophe '. If it does then these all apostrophes"
                + " ' are changed to apostrophes ’.";
        return description;
    }
</code></pre>

## 4. when you're done, add changes in the CorpusMagician (`corpus-services\src\main\java\de\uni_hamburg\corpora\CorpusMagician.java`)

* in the method `public Collection<String> getAllExistingCFs()` add your new Class as a String 
* in the method `Collection<CorpusFunction> corpusFunctionStrings2Classes()` add a string to the switch statement that will instantiate your new class, you can also use parameters given from the command line to set variables in your class etc.

## (Optional: 4. Write JUnit tests for your newly created class)

In Netbeans there is the plugin "JUnit" (installed by default) which you can use to create an empty JUnit test class for your class. 
To use it: 
 * right-click class
 * `>Tools >Create/Update Test` this creates an empty template test class, that gives errors by default
Fill this class with sensible test, one existing test class for reference is corpus-services\src\test\java\de\uni_hamburg\corpora\validation\PrettyPrintDataTest.java
There are some test files in the code with mockup audio and video  located here: corpus-services\src\test\java\de\uni_hamburg\corpora\resources\example
Run the test and only merge into develop if the test passes.
