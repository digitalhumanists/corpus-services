<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:decimal-format name="european" decimal-separator="."/>
    <xsl:template match="/">
        <html>
            <head>
                <xsl:call-template name="GENERATE_STYLES"/>
                <link rel="stylesheet" type="text/css" href="http://cdn.datatables.net/1.10.16/css/jquery.dataTables.css"/>
                <script type="text/javascript" charset="utf8" src="https://code.jquery.com/jquery-3.2.1.min.js"/>
                <script type="text/javascript" charset="utf8" src="http://cdn.datatables.net/1.10.16/js/jquery.dataTables.js"/>
                <script type="text/javascript" class="init">
                    
                    $(document).ready(function() {
                    $('table.compact').DataTable();
                    } );           
                </script>
            </head>
            <body> </body>
        </html>

        <xsl:call-template name="GET_STATISTICAL_INFO_ON_WHOLE_CORPUS"/>
        <xsl:call-template name="GET_INEL_SHORT_OVERVIEW"/>
        <xsl:call-template name="GET_DESCRIPTIONS_WITH_ADDITIONAL_INFO"/>
        <!--        <xsl:call-template name="GET_DESCRIPTIONS">
            <xsl:with-param name="PARENT">Communication</xsl:with-param>
        </xsl:call-template>-->
        <xsl:call-template name="GET_DESCRIPTIONS">
            <xsl:with-param name="PARENT">Setting</xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="GET_DESCRIPTIONS">
            <xsl:with-param name="PARENT">Speaker</xsl:with-param>
        </xsl:call-template>
        <!--        <xsl:call-template name="GET_DESCRIPTIONS">
            <xsl:with-param name="PARENT">Transcription</xsl:with-param>
        </xsl:call-template>-->
        <!--  <xsl:call-template name="GET_DESCRIPTIONS">
            <xsl:with-param name="PARENT">Recording</xsl:with-param>
        </xsl:call-template>-->
        <!--    <xsl:call-template name="GET_DESCRIPTIONS">
            <xsl:with-param name="PARENT">AsocFile</xsl:with-param>
        </xsl:call-template>-->
        <xsl:call-template name="GET_KEYS_LOC">
            <xsl:with-param name="PARENT">Communication</xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="GET_KEYS_LOC">
            <xsl:with-param name="PARENT">Speaker</xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="GET_KEYS_LANG">
            <xsl:with-param name="PARENT">Communication</xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="GET_KEYS_LANG">
            <xsl:with-param name="PARENT">Speaker</xsl:with-param>
        </xsl:call-template>
    </xsl:template>


    <xsl:template name="GET_STATISTICAL_INFO_ON_WHOLE_CORPUS">
        <h1> Statistical Info on Whole Corpus</h1>
        <table id="" class="info">
            <thead>
                <tr>
                    <th class="info">Number of Utterances Whole Corpus</th>
                    <th class="info">Number of Words Whole Corpus</th>
                    <th class="info">Duration of Audio (hh:mm:ss) Whole Corpus</th>
                    <th class="info">Number Exb Whole Corpus</th>
                    <th class="info">Number Exs Whole Corpus</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td class="info">
                        <xsl:value-of select="sum(//Transcription/Description/Key[@Name = '# HIAT:u'])"/>
                    </td>
                    <td class="info">
                        <xsl:value-of select="sum(//Transcription/Description/Key[@Name = '# HIAT:w'])"/>
                    </td>
                    <td class="info">
                        <xsl:variable name="milliseconds">
                            <xsl:value-of select="sum(//Recording/RecordingDuration)"/>
                        </xsl:variable>
                        <xsl:variable name="hours" select="floor($milliseconds div (1000 * 3600))"/>
                        <xsl:variable name="minutes" select="($milliseconds mod (1000 * 3600)) div (1000 * 60)"/>
                        <xsl:variable name="seconds" select="(($milliseconds mod (1000 * 60)) div 1000)"/>
                        <xsl:value-of select="concat(format-number($hours, '#00'), ':', format-number($minutes, '00'), ':')"/>
                        <xsl:value-of select="format-number($seconds, '00', 'european')"/>
                    </td>
                    <td class="info">
                        <xsl:value-of select="count(//Transcription/Description/Key[@Name = 'segmented' and text() = 'false'])"/>
                    </td>
                    <td class="info">
                        <xsl:value-of select="count(//Transcription/Description/Key[@Name = 'segmented' and text() = 'true'])"/>
                    </td>
                </tr>
            </tbody>
        </table>
    </xsl:template>

    <xsl:template name="GET_INEL_SHORT_OVERVIEW"/>

    <xsl:template name="GET_DESCRIPTIONS_WITH_ADDITIONAL_INFO">
        <xsl:if test="//Communication/Description/Key[not(starts-with(@Name, '#'))]">
            <h1> Communication Descriptions</h1>
            <table id="" class="compact">
                <xsl:variable name="temp">
                    <xsl:for-each-group select="//Communication/Description/Key[not(starts-with(@Name, '#'))]" group-by="@Name">
                        <xsl:sort select="current-grouping-key()"/>
                        <group>
                            <xsl:value-of select="current-grouping-key()"/>
                        </group>
                    </xsl:for-each-group>
                </xsl:variable>

                <xsl:variable name="columnheaders" as="text()">
                    <xsl:value-of select="$temp/group/text()"/>
                </xsl:variable>


                <thead>
                    <tr>
                        <th>Communication Name</th>
                        <th>Number of Utterances</th>
                        <th>Number of Words</th>
                        <th>Duration of Audio (hh:mm:ss)</th>
                        <th>Has Exb</th>
                        <xsl:for-each-group select="//Communication/Description/Key[not(starts-with(@Name, '#'))]" group-by="@Name">
                            <xsl:sort select="current-grouping-key()"/>
                            <th>
                                <xsl:value-of select="current-grouping-key()"/>
                            </th>
                        </xsl:for-each-group>
                    </tr>
                </thead>
                <tbody>
                    <xsl:for-each-group select="//Communication" group-by="@Name">
                        <xsl:sort select="current-grouping-key()"/>
                        <xsl:variable name="commElement" select="."/>
                        <tr>
                            <td class="firstcolumn">
                                <xsl:value-of select="current-grouping-key()"/>
                            </td>
                            <td>
                                <xsl:choose>
                                    <xsl:when test="$commElement/Transcription/Description/Key[@Name = 'segmented']/text() = 'true'">
                                        <!--     and it need to be a wav file too-->
                                        <xsl:value-of select="$commElement/Transcription/Description/Key[@Name = '# HIAT:u']/text()"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        no exs
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td>
                                <xsl:choose>
                                    <xsl:when test="$commElement/Transcription/Description/Key[@Name = 'segmented']/text() = 'true'">
                                        <!--     and it need to be a wav file too-->
                                        <xsl:value-of select="$commElement/Transcription/Description/Key[@Name = '# HIAT:w']/text()"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        no exs
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td>
                                <xsl:choose>

                                    <xsl:when test="not($commElement/Recording[1]/Media/NSLink/ends-with(text(), '.wav'))"
                                        >
                                        no wav
                                    </xsl:when>
                                    <xsl:when test="$commElement/Recording/RecordingDuration/text()">

                                        <xsl:variable name="milliseconds">
                                            <xsl:value-of select="$commElement/Recording/RecordingDuration/text()"/>
                                        </xsl:variable>
                                        <xsl:variable name="hours" select="floor($milliseconds div (1000 * 3600))"/>
                                        <xsl:variable name="minutes" select="($milliseconds mod (1000 * 3600)) div (1000 * 60)"/>
                                        <xsl:variable name="seconds" select="(($milliseconds mod (1000 * 60)) div 1000)"/>
                                        <xsl:value-of select="concat(format-number($hours, '#00'), ':', format-number($minutes, '00'), ':')"/>
                                        <xsl:value-of select="format-number($seconds, '00', 'european')"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                       no duration added
                                    </xsl:otherwise>
                                </xsl:choose>

                            </td>
                            <td>
                                <xsl:choose>
                                    <xsl:when test="$commElement/Transcription/Description/Key[@Name = 'segmented']/text() = 'false'"
                                        >
                                        yes
                                    </xsl:when>
                                    <xsl:otherwise>
                                    no
                                </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <xsl:for-each select="1 to count($temp/group)">
                                <xsl:variable name="number" select="."/>
                                <xsl:choose>
                                    <xsl:when test="$commElement/Description/Key[@Name = $temp/group[$number]]/text()">
                                        <td>
                                            <xsl:value-of select="$commElement/Description/Key[@Name = $temp/group[$number]]/text()"/>
                                        </td>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <td/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:for-each>
                            <!-- <td>
                                    <xsl:variable name="DISTINCT_VALUES" select="count(distinct-values(current-group()/text()))"/>
                                    <i> <xsl:value-of select="$DISTINCT_VALUES"/> distinct values</i>
                                    <br/>
                                    <xsl:for-each-group select="current-group()" group-by="text()">
                                        <xsl:sort select="count(current-group())" order="descending"/>
                                        <xsl:if test="$DISTINCT_VALUES &lt; 8 or count(current-group()) > 3">
                                            <xsl:value-of select="substring(current-grouping-key(), 0, 50)"/>
                                            <xsl:if test="string-length(current-grouping-key()) > 50">
                                                <i> [...]</i>
                                            </xsl:if>
                                            <xsl:text> (</xsl:text>
                                            <xsl:value-of select="count(current-group())"/>
                                            <xsl:text>)</xsl:text>
                                            <br/>
                                        </xsl:if>
                                    </xsl:for-each-group>
                                </td>-->


                        </tr>
                    </xsl:for-each-group>
                </tbody>
            </table>
        </xsl:if>
    </xsl:template>

    <xsl:template name="GET_KEYS_LOC">
        <xsl:param name="PARENT"/>
        <xsl:if test="//*[name() = $PARENT]/Description/Key[not(starts-with(@Name, '#'))]">
            <h1><xsl:value-of select="$PARENT"/> Locations</h1>
            <table id="" class="compact">
                <xsl:variable name="temp">
                    <xsl:for-each-group select="//*[name() = $PARENT]/Location/Description/Key[not(starts-with(@Name, '#'))]" group-by="@Name">
                        <xsl:sort select="current-grouping-key()"/>
                        <group>
                            <xsl:value-of select="current-grouping-key()"/>
                        </group>
                    </xsl:for-each-group>
                </xsl:variable>


                <xsl:variable name="columnheaders" as="text()">
                    <xsl:value-of select="$temp/group/text()"/>
                </xsl:variable>
                <thead>
                    <tr>
                        <th><xsl:value-of select="$PARENT"/> Name</th>
                        <th>Type</th>
                        <xsl:for-each-group select="//*[name() = $PARENT]/Location/Description/Key[not(starts-with(@Name, '#'))]" group-by="@Name">
                            <xsl:sort select="current-grouping-key()"/>
                            <th>
                                <xsl:value-of select="current-grouping-key()"/>
                            </th>
                        </xsl:for-each-group>
                    </tr>
                </thead>
                <tbody>
                    <xsl:for-each select="//*[name() = $PARENT]/Location">
                        <xsl:variable name="commElement" select="."/>
                        <tr>
                            <td class="firstcolumn">
                                <xsl:choose>
                                    <xsl:when test="./../Sigle">
                                        <xsl:value-of select="./../Sigle"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./../@Name"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td>
                                <xsl:value-of select="./@Type"/>
                            </td>
                            <xsl:for-each select="1 to count($temp/group)">
                                <xsl:variable name="number" select="."/>
                                <xsl:choose>
                                    <xsl:when test="$commElement/Description/Key[@Name = $temp/group[$number]]/text()">
                                        <td>
                                            <xsl:value-of select="$commElement/Description/Key[@Name = $temp/group[$number]]/text()"/>
                                        </td>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <td/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:for-each>
                        </tr>
                    </xsl:for-each>
                </tbody>
            </table>
        </xsl:if>
    </xsl:template>

    <xsl:template name="GET_KEYS_LANG">
        <xsl:param name="PARENT"/>
        <xsl:if test="//*[name() = $PARENT]/Description/Key[not(starts-with(@Name, '#'))]">
            <h1><xsl:value-of select="$PARENT"/> Languages</h1>
            <table id="" class="compact">
                <xsl:variable name="temp">
                    <xsl:for-each-group select="//*[name() = $PARENT]/Language/Description/Key[not(starts-with(@Name, '#'))]" group-by="@Name">
                        <xsl:sort select="current-grouping-key()"/>
                        <group>
                            <xsl:value-of select="current-grouping-key()"/>
                        </group>
                    </xsl:for-each-group>
                </xsl:variable>


                <xsl:variable name="columnheaders" as="text()">
                    <xsl:value-of select="$temp/group/text()"/>
                </xsl:variable>
                <thead>
                    <tr>
                        <th><xsl:value-of select="$PARENT"/> Name</th>
                        <th>Type</th>
                        <th>LanguageCode</th>
                        <xsl:for-each-group select="//*[name() = $PARENT]/Language/Description/Key[not(starts-with(@Name, '#'))]" group-by="@Name">
                            <xsl:sort select="current-grouping-key()"/>
                            <th>
                                <xsl:value-of select="current-grouping-key()"/>
                            </th>
                        </xsl:for-each-group>
                    </tr>
                </thead>
                <tbody>
                    <xsl:for-each select="//*[name() = $PARENT]/Language">
                        <xsl:variable name="commElement" select="."/>
                        <tr>
                            <td class="firstcolumn">
                                <xsl:choose>
                                    <xsl:when test="./../Sigle">
                                        <xsl:value-of select="./../Sigle"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./../@Name"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td>
                                <xsl:value-of select="./@Type"/>
                            </td>
                            <td>
                                <xsl:value-of select="./LanguageCode/text()"/>
                            </td>
                            <xsl:for-each select="1 to count($temp/group)">
                                <xsl:variable name="number" select="."/>
                                <xsl:choose>
                                    <xsl:when test="$commElement/Description/Key[@Name = $temp/group[$number]]/text()">
                                        <td>
                                            <xsl:value-of select="$commElement/Description/Key[@Name = $temp/group[$number]]/text()"/>
                                        </td>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <td/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:for-each>
                        </tr>
                    </xsl:for-each>
                </tbody>
            </table>
        </xsl:if>
    </xsl:template>

    <xsl:template name="GET_DESCRIPTIONS">
        <xsl:param name="PARENT"/>
        <xsl:if test="//*[name() = $PARENT]/Description/Key[not(starts-with(@Name, '#'))]">
            <h1><xsl:value-of select="$PARENT"/> Descriptions</h1>
            <table id="" class="compact">
                <xsl:variable name="temp">
                    <xsl:for-each-group select="//*[name() = $PARENT]/Description/Key[not(starts-with(@Name, '#'))]" group-by="@Name">
                        <xsl:sort select="current-grouping-key()"/>
                        <group>
                            <xsl:value-of select="current-grouping-key()"/>
                        </group>
                    </xsl:for-each-group>
                </xsl:variable>


                <xsl:variable name="columnheaders" as="text()">
                    <xsl:value-of select="$temp/group/text()"/>
                </xsl:variable>
                <thead>
                    <tr>
                        <th> <xsl:choose> <xsl:when test="$PARENT = 'Speaker' or $PARENT = 'Communication'"> <xsl:value-of select="$PARENT"/></xsl:when>
                            </xsl:choose>
                          
                            
                            Name</th>
                        <xsl:for-each-group select="//*[name() = $PARENT]/Description/Key[not(starts-with(@Name, '#'))]" group-by="@Name">
                            <xsl:sort select="current-grouping-key()"/>
                            <th>
                                <xsl:value-of select="current-grouping-key()"/>
                            </th>
                        </xsl:for-each-group>
                    </tr>
                </thead>
                <tbody>
                    <xsl:for-each select="//*[name() = $PARENT]">
                        <xsl:variable name="commElement" select="."/>
                        <tr>
                            <td class="firstcolumn">
                                <xsl:choose>
                                    <xsl:when test="./Sigle">
                                        <xsl:value-of select="./Sigle"/>
                                    </xsl:when>
                                    <xsl:when test="./../Sigle">
                                        <xsl:value-of select="./../Sigle"/>
                                    </xsl:when>
                                    <xsl:when test="./@Name">
                                        <xsl:value-of select="./@Name"/>
                                    </xsl:when>
                                    <xsl:when test="./../@Name">
                                        <xsl:value-of select="./../@Name"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="./@Id"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <xsl:for-each select="1 to count($temp/group)">
                                <xsl:variable name="number" select="."/>
                                <xsl:choose>
                                    <xsl:when test="$commElement/Description/Key[@Name = $temp/group[$number]]/text()">
                                        <td>
                                            <xsl:value-of select="$commElement/Description/Key[@Name = $temp/group[$number]]/text()"/>
                                        </td>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <td/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:for-each>
                        </tr>
                    </xsl:for-each>
                </tbody>
            </table>
        </xsl:if>
    </xsl:template>

    <xsl:template name="GENERATE_STYLES">
        <style type="text/css">
            body{
                font-family: calibri, helvetica, sans-serif;
                font-size: 10pt;
            }
            th{
                border: 1px solid silver;
                font-size: 9pt;
                background-color: silver;
            }
            th.info{
                border: 1px solid silver;
                font-size: 9pt;
                background-color: #ADD8E6;
                max-width: 75px;
            }
            td{
                border: 1px solid silver;
                font-size: 9pt;
                vertical-align: top;
            }
            td.firstcolumn{
                border: 1px solid silver;
                font-size: 9pt;
                vertical-align: top;
                background-color: #e6e6e6;
            }
            td.info{
                border: 1px solid silver;
                font-size: 9pt;
                vertical-align: top;
                background-color: #ebf5f9;
            }
            h1{
                font-size: 11pt;
                padding: 3px;
                max-width: 500px;
            }</style>
    </xsl:template>

</xsl:stylesheet>