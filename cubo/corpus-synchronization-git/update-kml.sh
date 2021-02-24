gitPath="/data/geodata"
cd $gitPath
curl -o temp.zip https://mapsengine.google.com/map/kml?mid=1urgo2sFym7O7VFaNccz61BNo7KJrImPy; unzip temp.zip; rm temp.zip; rm -R images; mv doc.kml kml/INEL-working-copy.kml
git add kml/INEL-working-copy.kml
git commit -m "updated KML file"
bash /path/to/corpus-services/cubo/auto-git/AutoGit.sh origin master cubo /data/geodata nocoma /path/to/reports/corpus-synchronization-git/geodata push
