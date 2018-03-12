<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2"
    xmlns:sqf="http://www.schematron-quickfix.com/validator/process">
    
    <sch:pattern>
        <sch:rule context="//speaker">
            <assert test="not(matches(@id, '[A-Za-z0-9]+'))">WARNING: The speaker ID does not conform to pattern '[A-Za-z0-9]+'.</assert>
        </sch:rule>
    </sch:pattern>
        
</sch:schema>