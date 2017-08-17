<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:exb="https://corpora.uni-hamburg.de/exmaralda"
    xmlns:svg="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:string="http://modiko.net/xpath-ns/string"
    exclude-result-prefixes="xsl xs string"
    version="2.0">
    
    <xsl:import href="https://corpora.uni-hamburg.de:8443/fedora/objects/hzsk:config/datastreams/general-functions-variables.xsl/content"/>
    
    <xsl:output method="xhtml" encoding="UTF-8" doctype-public="html" omit-xml-declaration="no"/>
    
    <xsl:param name="TRANSCRIPTION_ID" required="no" as="xs:string?"/>
    <xsl:param name="COMMUNICATION_ID" required="no" as="xs:string?"/>
    <xsl:param name="RECORDING_PATH" required="no" as="xs:string?"/>
    <xsl:param name="RECORDING_TYPE" required="no" as="xs:string?"/>
    
    <xsl:key name="time-by-id" match="tli/@time" use="../@id"/>
    <xsl:key name="highlight-times-by-event-id" match="elem/@time" use="../@id"/>
    <xsl:key name="elem-id-by-highlight-times" match="elem/@id" use="../@time"/>
    <xsl:key name="mime-type-by-extension" match="*:mime/@type" use="../@extension"/>
    
    <xsl:variable name="TLI_IDs" select="//common-timeline/tli/@id" as="xs:string+"/>
    <xsl:variable name="EVENT_HIGHLIGHT_TIMES">
        <xsl:for-each select="//event">
            <xsl:variable name="id" select="concat(parent::tier/@id, 'EV', count(preceding-sibling::event) + 1)" as="xs:string"/>
            <xsl:variable name="start" select="(key('time-by-id', @start)[.!=''], preceding-sibling::event/(key('time-by-id', @end), key('time-by-id', @start)))[1]" as="xs:double"/>
            <xsl:variable name="end" select="(key('time-by-id', @end)[.!=''], following-sibling::event/(key('time-by-id', @start), key('time-by-id', @end)))[1]" as="xs:double"/>
            <xsl:for-each select="xs:integer(round-half-to-even($start, 2) * 100) to xs:integer(round-half-to-even($end, 2) * 100)">
                <elem id="{$id}" time="{.}"/>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:variable>
    
    
    <xsl:template match="/">
        <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <title></title>
                <style type="text/css">
                    html, body{ margin:0;padding:0;font-family:"Open Sans",Helvetica,Verdana,Arial,sans-serif; }
                    
                    header, section#overview{ border-top:1px solid #aaa; border-bottom:1px solid #aaa; color:#888; background-color:#f0f0f0; }
                    
                    header{ clear:both; background-color:#f0f0f0; color:#333; padding:5px; overflow:auto; } 
                    header h1{ font-size:1em; float:left; margin:0; }
                    
                    section#controls{ float:right; padding:0; font-size:0.8em; }
                    section#sec1{ display:table; table-layout:fixed; width:100%; clear:both; }
                    section#transcript-sec{ overflow:scroll; margin-left:8em; }
                    div#media, div#diagram { display:table-cell; overflow-x:scroll; min-width:300px; text-align:center; vertical-align:top; background-color:#fafafa; }
                    section#overview{ width:100%; min-height: 10px; padding:0; font-size:0.8em; }
                    div#media video{ width:100%; min-width:250px; height: 100%; max-height:500px; min-height:200px; }
                    div#media audio{ width:100%; min-width:250px; }
                    footer{ width:100%; color:#aaa; font-size:0.8em; padding:10px; text-align:center; clear:both; border-top:1px solid #aaa; }                     
                    svg { background-color:#fafafa; min-width:100%; }
                    table#transcript{ font-size:0.8em; color: #666; border-collapse:separate; border-spacing:0 1px; background-color:#e0e0e0; }
                    table#transcript tr{ background-color:#e0e0e0; }
                    table#transcript tr:first-child td{ padding-top: 6px; }
                    table#transcript tr:last-child td{ padding-bottom: 6px; }
                    table#transcript td{ white-space:nowrap; padding: 3px 6px 3px 2px; }
                    table#transcript td.empty{ color:transparent; }
                    table#transcript td.event{ background-color:#fff; border:1px solid #aaa;}
                    table#transcript tr.timeline{ background-color:#aaa; color:#fff; font-size:0.7em; font-weight:bold; }
                    table#transcript tr.timeline td.tli{ cursor:pointer; }
                    table#transcript tr.tier{ margin-bottom:3px; }
                    table#transcript tr.tier td.tier-name{ position:absolute; width:8em; left:0; top:auto; margin-top:0; text-align:right; }
                    table#transcript tr.nv td.event, table#transcript tr.ref td.event{ font-style:italic; font-size:0.9em; }
                    form#autoActions{ margin:0; }
                    form#autoActions label{ margin-right: 10px; }
                </style>
                <script type="text/javascript">
                    <xsl:comment>
                     <![CDATA[
                     var activeEvents = new Array();
                     var highlightedBackground = "rgba(255,30,30,0.1)";                    
                     var highlightedStroke = "#ff3030";
                     var unhighlightedBackground = "#ffffff";
                     var unhighlightedFill = "transparent";
                     var currentlyScrolledToID = '';
                     
                     function bodyloaded(){
                        // fill some variables
                        mediaElem = document.getElementById('mediaElem');
                        transcriptSection = document.getElementById('transcript-sec');
                        diagramSection = document.getElementById('diagram').getElementsByTagName('div')[0];
                        highlightEvents = document.getElementById('highlightEventsInput').checked;
                        scrollEvents = document.getElementById('scrollEventsInput').checked;
                     }
                     
                     function mediaTimeUpdate(){
                        transcriptEventProcessor();
                     }
                     
                     function transcriptEventProcessor(){
                        idArray = eventTimes[Math.floor(mediaElem.currentTime * 100)]
                     
                        // first remove and dehighlight old IDs from activeEvents
                        countActiveEvents = activeEvents.length; //has to remain constant value
                        for (var i=0; i<countActiveEvents; i++) {
                            if(idArray.indexOf(activeEvents[countActiveEvents - i - 1]) < 0){
                                document.getElementById(activeEvents[countActiveEvents - i - 1]).style.backgroundColor = unhighlightedBackground;
                                activeEvents.splice(countActiveEvents - i - 1, 1);
                            }
                        }
                     
                        // then push and highlight new IDs
                        for (var i = 0; i < idArray.length; i++) {
                            if(!activeEvents.indexOf(idArray[i]) >= 0){
                                if(highlightEvents){ document.getElementById(idArray[i]).style.backgroundColor = highlightedBackground };
                                activeEvents.push(idArray[i]);
                            }
                        }
                        
                        // scroll to first event in activeEvents
                        if((idArray.length > 0) && scrollEvents){
                            transcriptSection.scrollLeft = document.getElementById(idArray[0]).offsetLeft;
                        }
                     }
                                          
                     ]]>
                     </xsl:comment>
                </script>
            </head>
            <body onload="bodyloaded();">
                <header>
                    <h1><xsl:value-of select="//transcription-name"/></h1>
                    <section id="controls">
                        <!--<span id="mediaTime"></span>-->
                        <form action="#" id="autoActions">
                            Automatic actions: 
                            <input id="highlightEventsInput" name="highlightEventsInput" checked="checked" type="checkbox" onchange="highlightEvents = this.checked;"/>
                            <label for="highlightEventsInput">highlight transcription</label>
                            <input id="scrollEventsInput" name="scrollEventsInput" checked="checked" type="checkbox" onchange="scrollEvents = this.checked;"/>
                            <label for="scrollEventsInput">scroll transcription</label>
                        </form>
                    </section>
                </header>
                <section id="sec1">
                    <div id="media">
                        <xsl:choose>
                            <xsl:when test="exists($RECORDING_PATH[not(matches(., '^\s+$'))]) and exists($RECORDING_TYPE[not(matches(., '^\s+$'))])">    
                                <xsl:comment>media found ($RECORDING_PATH=<xsl:value-of select="$RECORDING_PATH"/>; $RECORDING_TYPE=<xsl:value-of select="$RECORDING_TYPE"/>; mimetype=<xsl:value-of select="key('mime-type-by-extension', lower-case($RECORDING_TYPE), $MIMETYPES)"/>)</xsl:comment>
                                <xsl:for-each select="replace(key('mime-type-by-extension', lower-case($RECORDING_TYPE), $MIMETYPES), 'mpeg3', 'mpeg')">
                                    <xsl:element name="{tokenize(., '/')[1]}">
                                        <xsl:attribute name="controls" select="''"/>
                                        <xsl:attribute name="ontimeupdate" select="'mediaTimeUpdate();'"/>
                                        <xsl:attribute name="id" select="'mediaElem'"/>
                                        <xsl:attribute name="src" select="$RECORDING_PATH"/>
                                        Your browser does not support HTML5 audio. Please update or choose another browser.
                                    </xsl:element>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:comment>No media found ($RECORDING_PATH=<xsl:value-of select="$RECORDING_PATH"/>; $RECORDING_TYPE=<xsl:value-of select="$RECORDING_TYPE"/>; mimetype=<xsl:value-of select="key('mime-type-by-extension', lower-case($RECORDING_TYPE), $MIMETYPES)"/>)</xsl:comment>
                            </xsl:otherwise>
                        </xsl:choose>
                    </div>
                    <div id="diagram">
                        <div style="height:100%;overflow-y:scroll">
                            <!--<xsl:for-each select="document($DIAGRAM_URL)">
                                 <xsl:call-template name="process-svg"/>
                             </xsl:for-each>-->
                        </div>
                    </div>
                </section>
                <section id="overview">
                    <!--<xsl:call-template name="create-overview"/>-->
                </section>
                <section id="transcript-sec">
                    <xsl:call-template name="process-exb"/>
                </section>
                <footer>
                    <!--Copyright: MoDiKo 2015-->
                </footer>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template name="process-svg">
        <xsl:apply-templates mode="SVG"/>
    </xsl:template>
    
    <xsl:template name="process-exb">
        <table id="transcript">
            <tr class="timeline">
                <td></td>
                <xsl:for-each select="//common-timeline/tli">
                    <xsl:variable name="mins" select="xs:integer(floor(round-half-to-even(@time, 2) div 60))" as="xs:integer?"/>
                    <xsl:variable name="secs" select="xs:integer(floor(round-half-to-even(@time, 2)) -  ($mins * 60))" as="xs:integer?"/>
                    <xsl:variable name="hsecs" select="xs:integer(100 * (round-half-to-even(@time, 2) - floor(round-half-to-even(@time, 2))))" as="xs:integer?"/>
                    <td id="{@id}" class="tli" onclick="mediaElem.currentTime = this.getElementsByTagName('span')[0].id;mediaElem.play();" title="play from here">
                        <xsl:value-of select="position()"/>
                        <xsl:text> | </xsl:text>
                        <xsl:if test="exists(($mins, $secs, $hsecs)[3])">
                            <span id="{round-half-to-even(@time, 2)}"><xsl:value-of select="concat('0'[$mins &lt; 10], $mins, ':', '0'[$secs &lt; 10], $secs, '.', '0'[$hsecs &lt; 10], $hsecs)"/></span>
                        </xsl:if>
                    </td>
                </xsl:for-each>
            </tr>
            <xsl:for-each select="//tier">
                <tr id="{@id}" class="tier {@category}">
                    <td class="tier-name"><xsl:value-of select="@display-name"/></td>
                    <xsl:apply-templates/>
                    <xsl:if test="empty(event)"><td colspan="{count($TLI_IDs)}" class="empty">.</td></xsl:if>
                </tr>
            </xsl:for-each>
        </table>
        <script type="text/javascript">
            /* event IDs in array - position = hundred's second (1=0.01) */
            eventTimes = [<xsl:for-each select="0 to xs:integer(ceiling((round-half-to-even(max(//common-timeline/tli/@time), 2) * 100))) ">[<xsl:for-each select="key('elem-id-by-highlight-times', xs:string(.), $EVENT_HIGHLIGHT_TIMES)"><xsl:value-of select="concat('''', ., '''')"/><xsl:if test="position()!=last()">,</xsl:if></xsl:for-each>]<xsl:if test="position()!=last()">,</xsl:if></xsl:for-each>]
        </script>
    </xsl:template>
    
    <xsl:template match="event">
        <xsl:for-each select="if(exists(preceding-sibling::event[1])) 
            then (index-of($TLI_IDs, @start) - index-of($TLI_IDs, preceding-sibling::event[1]/@end))[.&gt;0]
            else (index-of($TLI_IDs, @start) - 1)[.&gt;0]">
            <td class="empty">
                <xsl:if test=".&gt;1"><xsl:attribute name="colspan" select="."/></xsl:if>
                <xsl:value-of select="'.'"/>
            </td>
        </xsl:for-each>
        <td class="event" id="{parent::tier/@id}EV{count(preceding-sibling::event) + 1}" exb:start="{key('time-by-id', @start)}" exb:end="{key('time-by-id', @end)}">
            <xsl:if test="parent::tier/@category='ref'">
                <xsl:variable name="IDs" select="for $xpPart in tokenize(substring-after(., '#'),'xpointer\(id\(''')[position()!=1] return substring-before($xpPart, '''))')" as="xs:string+"/>
                <xsl:attribute name="onmouseover" select="for $id in $IDs return concat('svgDoc.getElementById(''', $id, ''').style.stroke = highlightedStroke; svgDoc.getElementById(''', $id, ''').style.fill = highlightedBackground; ')"/>
                <xsl:attribute name="onmouseout" select="for $id in $IDs return concat('svgDoc.getElementById(''', $id, ''').style.stroke = unhighlightedFill; svgDoc.getElementById(''', $id, ''').style.fill = unhighlightedFill; ')"/>
            </xsl:if>
            <xsl:for-each select="(index-of($TLI_IDs, @end) - index-of($TLI_IDs, @start))[.&gt;1]">
                <xsl:attribute name="colspan" select="."/>
            </xsl:for-each>
            <xsl:value-of select="."/>
        </td>
        <xsl:if test="empty(following-sibling::event[1])">
            <td class="empty">
                <xsl:for-each select="(count($TLI_IDs) - index-of($TLI_IDs, @end) + 1)">
                    <xsl:if test=". &gt; 1"><xsl:attribute name="colspan" select="."/></xsl:if>
                    <xsl:value-of select="'.'"/>
                </xsl:for-each>
            </td>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="create-overview">
        <xsl:variable name="LINE_WIDTH" select="2" as="xs:integer"/>
        <svg version="1.1" xmlns="http://www.w3.org/2000/svg" width="100%" height="{(count(//tier[exists(event)]) * 5) + 5}" viewBox="0 0 {max(for $time in key('time-by-id', //event/@start) return ($time * 2))} {(count(//tier[exists(event)]) * 5) + 5}">
            <xsl:for-each select="//tier[exists(event)]">
                <xsl:variable name="pos" select="position()" as="xs:integer"/>
                <xsl:for-each select="event[exists(@start) and exists(@end)]">
                    <line x1="{key('time-by-id', @start) * 2}" y1="{$pos * 5}" x2="{key('time-by-id', @end) * 2}" y2="{$pos * 5}" style="stroke:rgb(255,0,0);stroke-width:2" />
                </xsl:for-each>
            </xsl:for-each>            
        </svg>
    </xsl:template>
    
    <xsl:function name="string:multi-replace">
        <xsl:param name="string" as="xs:string*"/>
        <xsl:param name="replacee" as="xs:string*"/>
        <xsl:param name="replacer" as="xs:string*"/>
        <xsl:if test="$string">
            <xsl:choose>
                <xsl:when test="exists($replacee[1]) and exists($replacer[1])">
                    <xsl:copy-of select="string:multi-replace(replace($string, $replacee[1], $replacer[1]), subsequence($replacee, 2), subsequence($replacer, 2))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$string"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:function>
    
</xsl:stylesheet>