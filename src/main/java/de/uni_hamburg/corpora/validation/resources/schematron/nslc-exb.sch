<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2"
    xmlns:sqf="http://www.schematron-quickfix.com/validator/process">
    
    <sch:pattern>
        <sch:rule context="//speaker">
            <assert test="not(matches(@id, '[A-Za-z0-9]+'))">WARNING: The speaker ID does not conform to pattern '[A-Za-z0-9]+'.</assert>
        </sch:rule>
    </sch:pattern>
    
    <s
    
    <pattern name="Check structure">
        <rule context="Person">
            <assert test="@Title">The element Person must have a Title attribute.</assert>
            <assert test="count(*) = 2 and count(Name) = 1 and count(Gender) = 1">The element Person should have the child elements Name and Gender.</assert>
            <assert test="*[1] = Name">The element Name must appear before element Age.</assert>
        </rule>
    </pattern>
    <pattern name="Check co-occurrence constraints">
        <rule context="Person">
            <assert test="(@Title = 'Mr' and Gender = 'Male') or @Title != 'Mr'">If the Title is "Mr" then the gender of the person must be "Male".</assert>
        </rule>
    </pattern> 
    
</sch:schema>