#!/bin/bash
for i in `find . -name "*.wav"`
    do 
        ffmpeg -i "$i" -ab 192k "${i%wav}mp3" 
        #sox "$i" "${i%wav}ogg" 
    done
