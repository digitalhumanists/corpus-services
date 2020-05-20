<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:string="https://inel.corpora.uni-hamburg.de/xmlns/string"
    exclude-result-prefixes="xs string"
    version="2.0">
    
    <xsl:output method="text" media-type="application/json" encoding="UTF-8"/>
    
    <!-- ### Global parameters ### -->    
    <!-- obligatory -->
    <xsl:param name="elastic-index" as="xs:string"/>
    <xsl:param name="elastic-doctype" as="xs:string"/>
    <xsl:param name="corpus-name" as="xs:string"/>
    <!-- params with defaults -->
    <xsl:param name="base-tier-category" select="'fe_usas'" as="xs:string"/>
    <xsl:param name="usas-tier-categories" select="'fe_usas', 'fe-N_usas', 'fe-V_usas'" as="xs:string+"/>
    <xsl:param name="extract-tiers" select="'st', 'ts', 'fe', 'fg', 'fr'" as="xs:string+"/>
    
    
    <!-- ### Global variables ### -->
    <xsl:variable name="file-name" select="tokenize(base-uri(.), '/')[last()]" as="xs:string"/>
    <xsl:variable name="NEWLINE" as="xs:string"><xsl:text>
</xsl:text></xsl:variable>
    
    
    <!-- ######### Keys ######### -->
    <xsl:key name="event-by-tier-category-and-speaker-and-start-end" match="event" use="concat(../@category, '#', ../@speaker, '#', @start, '#', @end)"></xsl:key>
    
    
    <!-- ###### Templates ###### -->
    <xsl:template match="/">
        
        <!-- test if processing makes sense -->
        <xsl:if test="empty(//tier[@category=$base-tier-category]/event)">
            <xsl:message select="'***WARNING: base tier is empty, nothing processed.'" terminate="yes"/>
        </xsl:if>
        
        <!-- start processing -->
        <xsl:variable name="doc" select="."/>
        <xsl:for-each select="//tier[@category=$base-tier-category]/event">
            <xsl:variable name="speaker" select="../@speaker" as="xs:string"/>
            <xsl:variable name="start" select="@start" as="xs:string"/>
            <xsl:variable name="end" select="@end" as="xs:string"/>
            
            <xsl:value-of select="concat('{ &quot;index&quot;: { &quot;_index&quot;: &quot;', $elastic-index, '&quot;, &quot;_type&quot;: &quot;', $elastic-doctype, '&quot; }}', $NEWLINE)"/>
            <!-- open JSON for this utterance -->
            <xsl:value-of select="'{ &quot;doc&quot; : { '"/>
			<!-- some general metadata fields-->
			<xsl:value-of select="concat('&quot;corpus&quot; : &quot;', $corpus-name, '&quot;, ')"/>
            <xsl:value-of select="concat('&quot;file&quot; : &quot;', $file-name, '&quot;, ')"/>
            <xsl:value-of select="concat('&quot;speaker&quot; : &quot;', $speaker, '&quot;, ')"/>
			<!-- some specific metadata fields from corpus metadata (Coma) -->
			<!-- tbd -->
            <!-- fields for USAS annotation -->
            <xsl:for-each select="$usas-tier-categories">
                <xsl:variable name="usas-annotations-array" select="concat('[&quot;', string-join( (for $anno in tokenize(key('event-by-tier-category-and-speaker-and-start-end', concat(., '#', $speaker, '#', $start, '#', $end), $doc), '\s+') return replace($anno, '([^\[]+)\[.*$', '$1') ), '&quot;, &quot;'), '&quot;]')" as="xs:string"/>
                <xsl:value-of select="concat('&quot;', ., '&quot; : ', ($usas-annotations-array[not(matches(., '^\[&quot;&quot;\]$'))],'null')[1], ', ')"/>
            </xsl:for-each>
            <!-- fields for more tiers from transcript -->
            <xsl:for-each select="$extract-tiers">
                <xsl:value-of select="concat('&quot;', ., '&quot; : ', (concat('&quot;', key('event-by-tier-category-and-speaker-and-start-end', concat(., '#', $speaker, '#', $start, '#', $end), $doc), '&quot;')[not(matches(., '^&quot;\s*&quot;$'))],'null')[1], ', ')"/>
            </xsl:for-each>
            <!-- field for ref to transcript HTML -->
            <xsl:value-of select="concat('&quot;html-ref&quot; : &quot;https://corpora.uni-hamburg.de/repository/transcript:', $corpus-name, '_', substring-before($file-name, '.exb'), '/SCORE/', substring-before($file-name, '.exb'), '-score.html#', $start, '&quot;')"/>
            <!-- close JSON for this utterance -->
            <xsl:value-of select="concat('} }', $NEWLINE)"/>                    
        </xsl:for-each>
    </xsl:template>

    
</xsl:stylesheet>