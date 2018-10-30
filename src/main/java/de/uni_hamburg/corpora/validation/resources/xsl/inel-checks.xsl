<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:output method="text" encoding="UTF-8"/>
    
    <xsl:variable name="ROOT" select="/"/>
    <xsl:variable name="NEWLINE"><xsl:text>
</xsl:text></xsl:variable>
    
    <xsl:template match="/">
        
        <!-- *** General checks *** -->
        
        <!-- check for elements with text content consisting of only question marks (and whitespace) -->
        <xsl:for-each select="//*[empty(*) and matches(text(), '^(\s*\?\s*)+$')]">
            <xsl:value-of select="concat('WARNING;Element ''', local-name(), ''' contains text value ''', text(), '''', $NEWLINE)"/>
        </xsl:for-each>
        
        
        <!-- *** Coma checks *** -->
        
        <xsl:for-each select="$ROOT//*:Communication">                        
            <xsl:variable name="COM_NAME" select="@Name" as="xs:string"/>
            
            <!-- Check transcription name against communication name -->
            <xsl:for-each select="*:Transcription[not(*:Name = $COM_NAME)]">
                <xsl:value-of select="concat('CRITICAL;The transcription name ''', *:Name, ''' differs from communication name ''', $COM_NAME, '''', $NEWLINE)"/>
            </xsl:for-each>
            
            <!-- Check transcription file name against communication name -->
            <xsl:for-each select="*:Transcription[not(matches(*:Filename, concat('^', $COM_NAME, '(\.exb|_s\.exs)$')))]">
                <xsl:value-of select="concat('CRITICAL;The transcription file name ''', *:Filename, ''' differs from communication name ''', $COM_NAME, '''', $NEWLINE)"/>
            </xsl:for-each>
            
            <!-- Compare transcription Filename and NSLink -->
            <xsl:for-each select="*:Transcription[not(ends-with(*:NSLink, *:Filename))]">
                <xsl:value-of select="concat('CRITICAL;The transcription file name ''', *:Filename, ''' differs from NSLink ''', *:NSLink, '''', $NEWLINE)"/>
            </xsl:for-each>
            
            <!-- Check recording name against communication name -->            
            <xsl:for-each select="*:Recording[not(*:Name = $COM_NAME)]">
                <xsl:value-of select="concat('CRITICAL;The recording name ''', *:Name, ''' differs from communication name ''', $COM_NAME, '''', $NEWLINE)"/>
            </xsl:for-each>
            
            <!-- Compare recording Filename and communication name -->
            <xsl:for-each select="*:Recording/*:Media[not(ends-with(string-join(tokenize(tokenize(*:NSLink, '/')[last()], '\.')[position()!=last()], '.'), $COM_NAME))]">
                <xsl:value-of select="concat('CRITICAL;The transcription NSLink ''', *:NSLink, ''' differs from communication name ''', $COM_NAME, '''', $NEWLINE)"/>
            </xsl:for-each>
            
            <!-- check if paths are relative -->
            <xsl:for-each select="(descendant::*:NSLink | descendant::*:relPath | descendant::*:absPath)[matches(text(), '^(file:[/\\]+)?[A-Za-z]:')]">
                <xsl:value-of select="concat('WARNING;The file reference ''', text(), ''' appears to be an absolute path', $NEWLINE)"/>
            </xsl:for-each>
            
        </xsl:for-each>
        
        
        <!-- *** EXB checks *** -->
        
        <xsl:for-each select="$ROOT//*:speakertable/*:speaker">
                        
            <!-- Check speaker ID pattern -->
            <xsl:for-each select="*:abbreviation[not(matches(text(), '^[A-Za-z0-9]+$'))]">
                <xsl:value-of select="concat('CRITICAL;The speaker abbreviation ', ., ' does not conform to pattern ''[A-Za-z0-9]+''', $NEWLINE)"/>
            </xsl:for-each>
            
        </xsl:for-each>
        
        <xsl:for-each select="$ROOT//*:tier[@category = ('mp', 'ge', 'gg', 'gr')]/*:event">
                        
            <!-- Check dashes in INEL morph glosses -->
            <!-- The tiers mp, ge, gg and gr need to have the same as the mb tier -->
            <xsl:variable name="annValue" select="text()"/>
            <xsl:variable name="morpheme-annotation-start" select="./@start"/>
            <xsl:variable name="morpheme-annotation-end" select="./@end"/>
            <xsl:variable name="annotation-name" select="../@category"/>
            <xsl:variable name="mbValue" select="//*:tier[@category = 'mb']/*:event[@start = $morpheme-annotation-start and @end = $morpheme-annotation-end]/text()"/>
            <xsl:if test="count(tokenize($annValue, '-')) != count(tokenize($mbValue, '-'))">
             <xsl:value-of select="concat('CRITICAL;the number of dashes does not match the number of dashes in matching mb tier, fix ', $annValue, ' vs. ', $mbValue,  ' at ', $morpheme-annotation-start, '-', $morpheme-annotation-end, ' in tier ', $annotation-name, $NEWLINE)"/>
	    </xsl:if>
                

            
        </xsl:for-each>
        
        
    </xsl:template>
    
</xsl:stylesheet>