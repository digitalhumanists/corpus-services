<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:string="https://inel.corpora.uni-hamburg.de/xmlns/string"
    xmlns:coma="http://www.exmaralda.org/xml/comacorpus.xsd"
    exclude-result-prefixes="xs string"
    version="2.0">
    
    <xsl:output method="text" media-type="application/json" encoding="UTF-8"/>
    
    <!-- ### Global parameters ### -->    
    <!-- obligatory -->
    <xsl:param name="elastic-index" as="xs:string"/>
    <xsl:param name="elastic-doctype" as="xs:string"/>
    <xsl:param name="corpus-name" as="xs:string"/>
    <!-- params with defaults -->
    <xsl:param name="extract-tiers" select="'st', 'ts', 'fe', 'fg', 'fr', 'fe_usas', 'fe-N_usas', 'fe-V_usas'" as="xs:string+"/>
    <xsl:param name="tokenizable-fields" as="xs:string+">
        <!-- tokenizable corpus description keys -->
        <xsl:sequence select="'hzsk:keywords', 'olac:data-inputter', 'olac:developer', 'olac:researcher'"/>
        <!-- not listing communication description keys (all set to tokenizable below) -->
        <!-- not listing speaker description keys (all set to tokenizable below) -->
    </xsl:param>
    <xsl:param name="tokenizer-regex" select="'\s*[;,]+\s*'" as="xs:string"/>
    
    
    
    
    <!-- ### Global variables ### -->
    <xsl:variable name="file-name" select="tokenize(base-uri(.), '/')[last()]" as="xs:string"/>
    <xsl:variable name="NEWLINE" as="xs:string"><xsl:text>
</xsl:text></xsl:variable>
    
    <xsl:variable name="coma-metadata" select="//xenoData/coma:Corpus"/>
    
    <!-- fetching the corpus-wide metadata for re-use -->
    <xsl:variable name="json-coma-corpus-metadata">
        <xsl:for-each select="$coma-metadata">
            <xsl:value-of select="concat('&quot;Corpus / Name&quot;: &quot;', //coma:Corpus/@Name, '&quot;, ')"/>
            <xsl:for-each select="//coma:Corpus/coma:Description/coma:Key">
                <xsl:value-of select="concat('&quot;Corpus / ', @Name, '&quot;: ', string:valuefy(., $tokenizer-regex, (@Name = $tokenizable-fields)), ', ')"/>                    
            </xsl:for-each>
        </xsl:for-each>
    </xsl:variable>
    
    <!-- fetching the communication metadata for re-use -->
    <xsl:variable name="json-coma-communication-metadata">
        <xsl:if test="exists($coma-metadata//coma:Communication[2])">
            <!-- would need to get Communication name from somewhere in TEI to avoid this -->
            <xsl:message select="'***ERROR: multiple Communication elements not yet supported.'"></xsl:message>
        </xsl:if>
        <xsl:for-each select="$coma-metadata//coma:Communication">
            <communication id="{@Id}" name="{@Name}">
                <xsl:value-of select="concat('&quot;Communication / Name&quot;: &quot;', @Name, '&quot;, ')"/>
                <xsl:for-each select="coma:Description/coma:Key">
                    <xsl:value-of select="concat('&quot;Communication / ', @Name, '&quot;: &quot;', string:valuefy(., $tokenizer-regex, true()), '&quot;, ')"/>                    
                </xsl:for-each>
                <xsl:for-each select="coma:Setting, coma:Language, coma:Location">
                    <xsl:variable name="element-type" select="local-name()" as="xs:string"/>
                    <xsl:variable name="category-type" select="@Type" as="xs:string?"/>
                    <xsl:if test="$element-type = 'Language'">
                        <xsl:value-of select="concat('&quot;Communication / ', $element-type, concat(' / ', $category-type)[exists($category-type)], ' / LanguageCode&quot;: &quot;', coma:LanguageCode, '&quot;, ')"/>
                    </xsl:if>
                    <xsl:for-each select="coma:Description/coma:Key">
                        <xsl:value-of select="concat('&quot;Communication / ', $element-type, concat(' / ', $category-type)[exists($category-type)], ' / ', @Name, '&quot;: &quot;', string:valuefy(., $tokenizer-regex, true()), '&quot;, ')"/>
                    </xsl:for-each>
                </xsl:for-each>                
            </communication>            
        </xsl:for-each>
    </xsl:variable>
    
    <!-- fetching speaker/s metadata or re-use -->
    <xsl:variable name="json-coma-speaker-metadata">
        <xsl:for-each select="$coma-metadata//coma:Speaker">
            <speaker Id="{@Id}" sigle="{coma:Sigle}">
                <xsl:for-each select="coma:Sigle, coma:Pseudo, coma:KnownHuman, coma:Sex">
                    <xsl:value-of select="concat('&quot;Speaker / ', local-name(), '&quot;: &quot;', string:valuefy(., $tokenizer-regex, true()), '&quot;, ')"/> 
                </xsl:for-each>
                <xsl:for-each select="coma:Description/coma:Key">
                    <xsl:value-of select="concat('&quot;Speaker / ', @Name, '&quot;: &quot;', string:valuefy(., $tokenizer-regex, true()), '&quot;, ')"/>                    
                </xsl:for-each>
                <xsl:for-each select="coma:Language, coma:Location">
                    <xsl:variable name="element-type" select="local-name()" as="xs:string"/>
                    <xsl:variable name="category-type" select="@Type" as="xs:string"/>
                    <xsl:if test="$element-type = 'Language'">
                        <xsl:value-of select="concat('&quot;Speaker / ', $element-type, concat(' / ', $category-type)[exists($category-type)], ' / LanguageCode&quot;: &quot;', coma:LanguageCode, '&quot;, ')"/>
                    </xsl:if>
                    <xsl:for-each select="coma:Description/coma:Key">
                        <xsl:value-of select="concat('&quot;Speaker / ', $element-type, concat(' / ', $category-type)[exists($category-type)], ' / ', @Name, '&quot;: &quot;', string:valuefy(., $tokenizer-regex, true()), '&quot;, ')"/>
                    </xsl:for-each>
                </xsl:for-each>
            </speaker>
        </xsl:for-each>
    </xsl:variable>
    
    
    <!-- ######### Keys ######### -->
    <xsl:key name="event-by-tier-category-and-speaker-and-start-end" match="event" use="concat(../@category, '#', ../@speaker, '#', @start, '#', @end)"></xsl:key>
    <xsl:key name="speaker-sigle-by-id" match="person/@n" use="../xml:id"></xsl:key>
    <xsl:key name="speaker-json-by-sigle" match="speaker" use="@sigle"/>
    <xsl:key name="span-by-from-to-spanGrp-type" match="span" use="concat(@from, '#', @to, '#', ../@type)"/>
    
    <!-- ###### Templates ###### -->
    <xsl:template match="/">
        
        <!-- start processing -->
        <xsl:variable name="doc" select="."/>
        <xsl:for-each select="//seg[@type='utterance']">
            <xsl:variable name="seg-id" select="@xml:id" as="xs:string"/>
            <xsl:variable name="speaker-sigle" select="key('speaker-sigle-by-id', ../../@who, $doc)" as="xs:string"/>
            <xsl:variable name="unit-ids" select="*/@xml:id" as="xs:string+"/>
            
            <xsl:value-of select="concat('{ &quot;index&quot;: { &quot;_index&quot;: &quot;', $elastic-index, '&quot;, &quot;_type&quot;: &quot;', $elastic-doctype, '&quot; }}', $NEWLINE)"/>
            <!-- open JSON for this utterance -->
            <xsl:value-of select="'{ &quot;doc&quot; : { '"/> 
            
            <!-- some general metadata fields-->
            <!--<xsl:value-of select="concat('&quot;corpus&quot; : &quot;', $corpus-name, '&quot;, ')"/>-->
            <xsl:value-of select="concat('&quot;file&quot; : &quot;', $file-name, '&quot;, ')"/>
            
            <!-- speaker metadata fields -->
            <xsl:value-of select="key('speaker-json-by-sigle', $speaker-sigle, $json-coma-speaker-metadata)"/>
            
            <!-- communication metadata fields -->
            <xsl:value-of select="$json-coma-communication-metadata/communication[1]"/>
            
            <!-- fields for tiers from transcript -->
            <xsl:for-each select="$extract-tiers">
                <xsl:choose>
                    <xsl:when test=". = ('ref', 'st', 'ts')">
                        <xsl:value-of select="concat('&quot;', ., '&quot; : ', (concat('&quot;', key('span-by-from-to-spanGrp-type', concat($seg-id, '#', $seg-id, '#', .), $doc), '&quot;')[not(matches(., '^&quot;\s*&quot;$'))],'null')[1], ', ')"/>
                        
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- morpheme-based -->
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            
            <!-- field for ref to transcript HTML -->
            <xsl:value-of select="concat('&quot;html-ref&quot; : &quot;https://corpora.uni-hamburg.de/repository/transcript:', $corpus-name, '_', substring-before($file-name, '.exb'), '/SCORE/', substring-before($file-name, '.exb'), '-score.html#', $start, '&quot;')"/>
            
            <!-- close JSON for this utterance -->
            <xsl:value-of select="concat('} }', $NEWLINE)"/>                    
        </xsl:for-each>
    </xsl:template>
    
    
    <xsl:function name="string:valuefy">
        <xsl:param name="value" as="xs:string"/>
        <xsl:param name="tokenizer-regex" as="xs:string+"/>
        <xsl:param name="tokenizable" as="xs:boolean"/>
        <xsl:choose>
            <xsl:when test="$tokenizable">
                <xsl:value-of select="'['"/>
                <xsl:for-each select="tokenize($value, $tokenizer-regex)[not(matches(., '^\s*$'))] ">
                    <xsl:value-of select="concat('&quot;', ., '&quot;')"/>
                    <xsl:if test="position() != last()"><xsl:value-of select="', '"/></xsl:if>
                </xsl:for-each>
                <xsl:value-of select="']'"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="(concat('&quot;', $value, '&quot;')[not(matches(., '^&quot;\s*&quot;$'))],'null')[1]"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    
</xsl:stylesheet>