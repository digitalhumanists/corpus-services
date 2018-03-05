<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2"
    xmlns:sqf="http://www.schematron-quickfix.com/validator/process">
    
    <!--
    Set of rules applying to the entire document
  -->
    <sch:pattern>
        
        <sch:rule context="/basic-transcription/head/speakertable/speaker">
            
            <!-- Speaker abbreviation pattern -->
            <sch:report test="not(matches(abbreviation, '[A-Z0-9]+'))">Speaker abbreviation does not match regex pattern [A-Z0-9]+.</sch:report>
            
        </sch:rule>
        
    </sch:pattern>
    
</sch:schema>