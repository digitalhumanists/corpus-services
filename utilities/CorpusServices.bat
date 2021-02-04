@echo off
set /p CorpusPath=Welcher KorpusOrdner soll gecheckt werden?
set /p CorpusFunctions=Welche KorpusFunktionen sollen gecheckt werden?(Mit Semikolon getrennt) 
java -Xmx3g -jar corpus-services-1.0.jar -i %CorpusPath% -o report-output.html -c %CorpusFunctions% -e
echo Die Datei report-output.html wurde gespeichert.
PAUSE
