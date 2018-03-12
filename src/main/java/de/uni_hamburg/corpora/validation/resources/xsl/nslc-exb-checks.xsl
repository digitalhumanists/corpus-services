<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:output method="text" encoding="UTF-8"/>
    
    <xsl:template match="/">
        
        <!-- Check speaker ID pattern -->
        <xsl:for-each select="//*:speaker/*:abbreviation[not(matches(text(), '^[A-Za-z0-9]+$'))]">
            <xsl:value-of select="concat('The speaker abbreviation ', ., ' does not conform to pattern ''[A-Za-z0-9]+''')"/>
        </xsl:for-each>
        
    </xsl:template>
    
</xsl:stylesheet>