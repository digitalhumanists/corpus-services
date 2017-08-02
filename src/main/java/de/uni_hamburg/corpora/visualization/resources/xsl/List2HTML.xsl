<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:exmaralda="http://www.exmaralda.org/xml" exclude-result-prefixes="#all" version="2.0">
	
	<xsl:output encoding="UTF-8" method="xhtml" omit-xml-declaration="yes"/>

	<!-- **** -->
	<!-- Keys -->
	<!-- **** -->     
	<xsl:key name="speaker-by-id" match="speaker" use="@id"/>
	<xsl:key name="annotation-by-name" match="annotation" use="@name"/>
	<xsl:key name="time-by-tli-id" match="tli/@time" use="../@id"/>


	<!-- ************************ -->
	<!-- Parameters Declaration   -->
	<!-- ************************ -->        
	<xsl:param name="TRANSCRIPTION_ID" required="no" as="xs:string"/>
	<xsl:param name="COMMUNICATION_ID" required="no" as="xs:string"/>
	<xsl:param name="RECORDING_PATH" required="no" as="xs:string"/>
	<xsl:param name="RECORDING_TYPE" required="no" as="xs:string"/>
	<xsl:param name="LABEL" required="no" as="xs:string"/>
	<xsl:param name="EMAIL_ADDRESS" required="no" as="xs:string"/>
	<xsl:param name="WEBSERVICE_NAME" required="no" as="xs:string"/>
	<xsl:param name="HZSK_WEBSITE" required="no" as="xs:string"/>

        
	<!-- ***************************** -->
	<!-- Global variables Declaration  -->
	<!-- ***************************** -->
	
	<!-- The displayed name of the corpus; e.g. occurs in the navigation bar -->	
	<xsl:variable name="CORPUS_NAME" select="//project-name" as="xs:string?"/>
	
	<!-- The displayed name of the transcription -->
	<!-- occurs, for example in the navigation bar -->
	<xsl:variable name="TRANSCRIPTION_NAME" select="//transcription-name" as="xs:string?"/>
	
	<!-- the base of the filename from which the names of all linked files are derived -->
	<xsl:variable name="BASE_FILENAME" select="substring-before(//referenced-file[1]/@url,'.')" as="xs:string?"/>
	<!-- <xsl:value-of select="//ud-information[@attribute-name='Code']"/> -->
	
	<!-- the path to the folder with resources -->
	<xsl:variable name="TOP_LEVEL_PATH" select="'https://corpora.uni-hamburg.de/drupal/sites/default/files/visualization/'" as="xs:string"/>
	
	<!-- <xsl:variable name="DATASTREAM">
		<xsl:value-of select="concat('https://corpora.uni-hamburg.de/drupal/de/islandora/object/', $TRANSCRIPTION_ID, '/datastream')"/>
	</xsl:variable>-->

	<xsl:variable name="DATASTREAM_VIDEO" select="$RECORDING_PATH"/>

	<xsl:variable name="DATASTREAM_AUDIO" select="$RECORDING_PATH"/>

	<!-- whether or not the transcription contains video -->
	<xsl:variable name="HAS_VIDEO" select="$RECORDING_TYPE=('WEBM', 'MPEG', 'MPG')" as="xs:boolean"/>

	<!-- whether or not the transcription contains video -->
	<xsl:variable name="HAS_AUDIO" select="$RECORDING_TYPE=('WAV', 'OGG', 'MP3')" as="xs:boolean"/>

	<!-- ******************************************************************************************************************************************** -->

	<!-- ... and then specify those which are only valid for this kind of visualisation document -->

	<!-- the path to the CSS stylesheet to be used with this HTML visualisation -->
    <xsl:variable name="CSS_PATH" select="concat($TOP_LEVEL_PATH, 'VisualizationFormat.css')" as="xs:string"/>
	<xsl:variable name="CSS_PATH_LIST" select="concat($TOP_LEVEL_PATH, 'ListFormat-new.css')" as="xs:string"/>

	<!-- a suffix to be used with the flash player ID to make sure flash players do not interact across documents -->
	<xsl:variable name="DOCUMENT_SUFFIX" select="'u'" as="xs:string"/>

	<!-- ************************ -->
	<!--    Top level template   -->
	<!-- ************************ -->

	<xsl:template match="/">
		<html>
			<head>
				<xsl:call-template name="HEAD_DATA"/>
				<!-- <xsl:call-template name="CSS_STYLES"/> -->
			    <style type="text/css">
			        /* ************************************************** */
			        /*                 styles for main document parts            */
			        /* ************************************************** */
			        
			        body{ margin:0; overflow:hidden; font-family:calibri, helvetica, sans-serif; }
			        
			        /* the header */
			        div#head{ background-color:#40627C; color:white; font-size:12pt; font-weight:bold; padding:7px; z-index:100; position:absolute;
			        	width:100%; height:45px; top:0; left:0; }
			        
			        
			        div#content{ position:absolute; top:55px; bottom:30px; width:100%; }
			        
			        div#controls{ float:left; /*min-width:300px;*/ /*width:20%;*/ }
			        
			        div.sidebarcontrol{ padding:5px; padding-top:20px; float:left; clear:left; min-width:320px; }
			        			        
			        div#main{ /*padding-top: 20px;*/ /*padding-bottom: 200px;*/ border:2px solid #cfd6de; /*margin:0 auto;*/ margin:20px 0px 10px 340px;
			        	height: 97%; /*height:800px;*/ /*width:75%;*/ /*float:right;*/ /*height:98%;*/ /*width:80%;*/ /*margin-left:300px;*/ /*clear: right;*/
			        	/*min-width:1100px;*/ }
			        
			        div#transcription{ overflow:auto; /*min-width:1100px;*/ /*height:800px;*/ max-height:98%; } 
			        
			        div#footer{ position:absolute; height:35px; bottom:0; left:5px; color:gray; background-color:white; border:1px solid gray; text-align:center;
			       		font-size:10pt; margin-top:0px; min-width:320px;  }
			        
			        div#mediaplayer{ }
			        
			        .collapse_box{ border:2px solid black; padding:0; }
			        
			        .collapse_box .collapse_title{ background-color:#adc1d6; font-weight:bold; height:17px; padding-left:5px; padding-bottom:2px; }
			        
			        .collapse_box .collapse_content{ background-color:#e4e9f5; padding:7px 5px 3px 5px; margin:0; }
			        
			        span#corpus-title{ color:blue; }
			        
			        #head a{ text-decoration:none; color:white; }
			        
			        #previous-doc, #next-doc{ font-size:10pt; }
			        
			        a.HeadLink{ font-family:Sans-Serif; font-size:16pt; font-style:normal; font-weight:bold; text-decoration:none }
			        
			    	<!-- LIST CSS -->
			        span.subscript { font-size:8pt; font-weight:bold; vertical-align:sub; padding-left:2px; padding-right:2px }
			        span.numbering { color:rgb(150,150,150) }
			        span.non-verbal { color:rgb(100,100,100) }
			        a.numbering { text-decoration:none; }
			        
			        table { margin-left:50px; margin-top:0px; margin-right:100px; border-collapse:collapse; }
			        
			        td { vertical-align:top; padding:3px; }
			        
			        *.numbering { font-size:8pt; font-weight:bold; color:rgb(200,200,200); }
			        td.speaker { font-size:11pt; font-weight:bold; padding-right:8px; padding-left:4px }
			        td.text { font-size:12pt; font-weight:normal; padding-left:8px; padding-top:5px; padding-bottom:5px; }
			        
			        td.translation { font-size:9pt; font-weight:normal; color:rgb(0,0,240); padding-left:8px }
			        
			        td.Child{ border:1px solid blue; }
			        
			        *.even { background-color:rgb(245,245,245); padding-left:10px; }
			        
			        *.odd { background-color:white; font-size:13pt; padding-left:10px; }
			        
			        span.pho{ color:rgb(0,0,240); font-family:"Arial Unicode MS"; }
			        
			        span.syll{ color:rgb(100,100,100); font-size:10pt; margin-right:5px; }
			        
			        span.type, span.lang{ color:rgb(100,100,100); font-size:8pt; margin-right:5px;  }
			        span.utt-no{ color:rgb(100,100,100); font-size:8pt; font-weight:bold; }
			        
			        td.anno{ border-top:1px dotted gray; border-bottom:1px dotted gray; }
			        
			        span.pho{ font-size:11pt; color:blue; }
			        
			        span.syll{ font-size:9pt; color:rgb(80,80,80); }
			    </style>
			</head>
			<body>
				<xsl:call-template name="MAKE_TITLE"/>
				<div id="content">
					<div id="controls">
						<xsl:call-template name="MAKE_PLAYER_DIV"/>
				    	<xsl:call-template name="MAKE_WEB_SERVICE_INFO"/>
			     <!--	<xsl:call-template name="MAKE_DOWNLOAD_FILES_CONTROL"/> -->
			     <!--   <xsl:call-template name="MAKE_FOOTER"/> -->
					</div>
					<div id="main">
						<div id="transcription">
							<table>
								<!-- ... and process the speaker contributions -->
								<xsl:apply-templates select="list-transcription/list-body/speaker-contribution"/>
							</table>
							<p>
							<br/>
							</p>
						</div>
					</div>
				</div>
			</body>
		</html>
	</xsl:template>

	<!-- for the speaker contributions ... -->
	<xsl:template match="speaker-contribution">
		<!-- ... make a table row ... -->
		<tr>			
			<!-- ... link for playing audio from here ... -->
			<xsl:call-template name="AUDIOLINK"/>
			
			<!-- ... with one cell for numbering ... -->
			<xsl:call-template name="NUMBERING"/>
			
			<!-- ... one cell for the speaker abbreviation ... -->
			<xsl:call-template name="SPEAKER_ABBR"/>
			
			<!-- ... more cells ... -->
			<xsl:call-template name="TEXT_CELL"/>			
			<xsl:call-template name="ANNO_CELLS"/>
		</tr>
	</xsl:template>

	<!-- for the mains... -->
	<xsl:template match="main">
		<xsl:apply-templates/>
	</xsl:template>


	<xsl:template match="nts | ats">
		<xsl:choose>
			<xsl:when test="text()=' '">
				<xsl:apply-templates/>
			</xsl:when>
			<xsl:otherwise>
				<span class="non-verbal">
					<xsl:apply-templates/>
				</span>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="text()">
		<xsl:choose>
			<xsl:when test="name(..)='ta' or name(..)='ats' or name(..)='nts' or (name(..)='ts' and ../@n='HIAT:w')">
				<xsl:value-of select="."/>
			</xsl:when>
			<xsl:otherwise>
				<!-- do nothing -->
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="ts">
		<xsl:apply-templates/>
	</xsl:template>


	<!-- for the dependents and annotations... -->
	<xsl:template match="dependent">
		<!-- do nothing -->
	</xsl:template>

	<xsl:template match="annotation">
		<xsl:apply-templates/>
	</xsl:template>

	<!-- *********************************************************************************** -->
	<!-- *********************************************************************************** -->
	<!-- ************************** HTML Templates ***************************************** -->
	<!-- *********************************************************************************** -->
	<!-- *********************************************************************************** -->

	<!-- Generates the HTML head information for this transcription document -->
	<!-- i.e. the document title, the document encoding etc. -->
	<xsl:template name="HEAD_DATA">
		<title>
			<xsl:value-of select="$CORPUS_NAME"/>: <xsl:value-of select="$TRANSCRIPTION_NAME"/>
		</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
		<script type="text/javascript" src="{$TOP_LEVEL_PATH}jsfunctions.js"></script>
		
		<!-- placeholder for js script, inserted by Java -->
		<script type="text/javascript">
			<xsl:comment>jsholder</xsl:comment>
		</script>
	</xsl:template>

	<!-- makes a reference to a CSS stylesheet -->
	<xsl:template name="CSS_STYLES">
		<link rel="stylesheet" type="text/css" href="{$CSS_PATH}"/>
		<link rel="stylesheet" type="text/css" href="{$CSS_PATH_LIST}"/>
	</xsl:template>


	<!-- makes the navigation bar displayed at the top of diverse documents -->
	<!-- This part used to be much more complex, now it was very much simplified -Niko -->

	<xsl:template name="MAKE_TITLE">
		<div id="head">
			<span id="document-title">
				<xsl:value-of select="concat($CORPUS_NAME, ' | ',$TRANSCRIPTION_NAME)"/>
			</span>
		</div>
	</xsl:template>

	<xsl:template name="MAKE_PLAYER_DIV">
		<div id="mediaplayer" class="sidebarcontrol">
			<xsl:choose>
				<xsl:when test="$HAS_VIDEO">
					<video controls="controls" width="320" height="240" data-tlid="media">
						<source src="{$DATASTREAM_VIDEO}" type="video/webm"/>
					</video>
				</xsl:when>
				<xsl:when test="$HAS_AUDIO">
					<audio controls="controls" data-tlid="media">
						<source src="{$DATASTREAM_AUDIO}" type="audio/ogg"/>
					</audio>
				</xsl:when>
			</xsl:choose>
		</div>
	</xsl:template>

    <xsl:template name="MAKE_WEB_SERVICE_INFO">
        <div class="sidebarcontrol">
            <div class="collapse_box" id="tier_display">
                <div class="collapse_title"> Webservice information </div>
                <div class="collapse_content" style="width: 310px;">
                    <p>Visualization generated on <xsl:value-of  select="current-dateTime()"/> with <xsl:value-of select="$WEBSERVICE_NAME"/> (available on <a href="https://github.com/hzsk/visualizations">GitHub</a>).</p>
                	<p>Contact the <a href="{$HZSK_WEBSITE}">HZSK - Hamburger Zentrum für Sprachkorpora</a> for more information.</p>
                </div>                
            </div>
        </div>
    </xsl:template>
	
	<xsl:template name="AUDIOLINK">
		<td class="audiolink">
			<!-- if this entitiy has a start point with an absolute time value... -->
			<xsl:if test="//tli[@id=current()/descendant::ts[1]/@s]/@time">
				<xsl:variable name="TIME" select="0 + //tli[@id=current()/descendant::ts[1]/@s]/@time"/>					
				<a onclick="jump('{format-number(($TIME - 0.1), '#.##')}');">
					<img class="media" title="{exmaralda:FORMAT_TIME($TIME)}&#x0020;-&#x0020;Click to start player" src="{$TOP_LEVEL_PATH}play_button.gif"/>
				</a>
			</xsl:if>
		</td>
	</xsl:template>
	
	<xsl:template name="NUMBERING">
		<td class="numbering ">
			<!-- ... and anchors for all start timeline references included in this sc -->
			<!-- ********************* TODO ************************* - doesn't work.. -->
			<!-- Now it works, but I'm not sure if this substring-before-after-concat -mess is the prettiest way to do it,
				     the problem was, as far as I understand, in the way how the same xpath has to be able to pick meaningful part from
				     both values like T100 and T100.100. -->
			<xsl:for-each select="*//@s">
				<a name="{.}"/>
			</xsl:for-each>
			<!--<xsl:element name="a">
					<xsl:attribute name="class">numbering</xsl:attribute>
					<xsl:attribute name="href">
						<xsl:value-of select="$DATASTREAM"/>
						<xsl:text>/SCORE#</xsl:text>
						<xsl:value-of select="substring-before(concat(current()/descendant::ts[1]/@s, '.'),'.')"/>
					</xsl:attribute>
					<xsl:value-of select="position()"/>
					<span style="font-size:6pt; vertical-align:sub; color:rgb(150,150,150);">
						<xsl:text>&#x00A0;[</xsl:text>
						<xsl:value-of select="substring-before(concat(substring-after(current()/descendant::ts[1]/@s,'T'), '.'), '.')"/>
						<xsl:text>]</xsl:text>
					</span>
				</xsl:element>-->
		</td>
	</xsl:template>
	
	<xsl:template name="SPEAKER_ABBR">
		<td class="abbreviation speaker">		
			<xsl:if	test="not((preceding-sibling::speaker-contribution[1]/@speaker = current()/@speaker) and (preceding-sibling::speaker-contribution[1]/descendant::ts[1]/@e = current()/descendant::ts[1]/@s))">
				<xsl:value-of select="translate(key('speaker-by-id', current()/@speaker)/abbreviation,' ' , '&#x00A0;')"/>
			</xsl:if>
		</td>
	</xsl:template>
	
	<xsl:template name="TEXT_CELL">
		<xsl:variable name="EVEN_ODD" select="if(position() mod 2=0) then 'even' else if(position() mod 2&gt;0) then 'odd' else ''" as="xs:string"/>
		<xsl:variable name="time-start" select="key('time-by-tli-id', main/ts[ends-with(@n, ':u')]/@s)" as="xs:string?"/>
		<xsl:variable name="time-end" select="key('time-by-tli-id', main/ts[ends-with(@n, ':u')]/@e)" as="xs:string?"/>
		<td class="text {$EVEN_ODD}">
			<xsl:if test="exists(($time-start, $time-end)[2]) and (xs:double($time-start) &lt; xs:double($time-end))">
				<xsl:attribute name="data-tl" select="concat($time-start, '-', $time-end)"/>
			</xsl:if>
			<xsl:apply-templates select="main"/>
		</td>
	</xsl:template>
	
	<xsl:template name="ANNO_CELLS">
		<xsl:variable name="EVEN_ODD" select="if(position() mod 2=0) then 'even' else if(position() mod 2&gt;0) then 'odd' else ''" as="xs:string"/>
		<xsl:if test="key('annotation-by-name', 'de')">
			<td class="translation {$EVEN_ODD}">
				<xsl:apply-templates select="annotation[@name='de']"/>
			</td>
		</xsl:if>
		<xsl:if test="key('annotation-by-name', 'en')">
			<td class="translation {$EVEN_ODD}">
				<xsl:apply-templates select="annotation[@name='en']"/>
			</td>
		</xsl:if>
	</xsl:template>
	
	<xsl:function name="exmaralda:FORMAT_TIME" as="xs:string">
		<xsl:param name="TIME"/>
		<xsl:variable name="totalseconds" select="0 + $TIME"/>
		<xsl:variable name="hours" select="0 + floor($totalseconds div 3600)"/>
		<xsl:variable name="minutes" select="0 + floor(($totalseconds - 3600*$hours) div 60)"/>
		<xsl:variable name="seconds" select="0 + ($totalseconds - 3600*$hours - 60*$minutes)"/>
		<xsl:value-of select="concat(concat('0', $hours)[$hours+0 &lt; 10 and $hours &gt;0], '00'[$hours + 0 = 0], ':', '0'[$minutes+0 &lt; 10], $minutes, ':', '0'[$seconds+0 &lt; 10], (round($seconds*100) div 100))"/>
	</xsl:function>
		

</xsl:stylesheet>

