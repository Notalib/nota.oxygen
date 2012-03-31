<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml">
	<xsl:output method="xml" omit-xml-declaration="no" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" encoding="Windows-1252" indent="yes"/>
	<xsl:template match="/">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="dtbook">
		<html>
			<xsl:attribute name="lang">
				<xsl:text disable-output-escaping="yes">en</xsl:text>
			</xsl:attribute>
			<xsl:apply-templates/>
		</html>
	</xsl:template>
	<xsl:template match="head">
		<head>
			<xsl:apply-templates/>
			<xsl:element name="meta">
				<xsl:attribute name="name">
					<xsl:text disable-output-escaping="yes">Content-Type</xsl:text>
				</xsl:attribute>
				<xsl:attribute name="content">
					<xsl:text disable-output-escaping="yes">text/html; charset=windows-1252</xsl:text>
				</xsl:attribute>
			</xsl:element>
			<xsl:element name="style">
				<xsl:attribute name="type">
					<xsl:text disable-output-escaping="yes">text/css</xsl:text>
				</xsl:attribute>
				<xsl:text disable-output-escaping="no">
				/*<![CDATA[*/
    body {font-style: normal; font-weight: normal; font-family: arial, verdana, sans-serif; margin-right: 10%; margin-left: 10%;}
h1 {color:#006400;}
h2 {color:#006400;}
h3 {color:#006400;}
h4 {color:#006400;}
h5 {color:#006400;}
h6 {color:#006400;}
span.titellinie{FONT-WEIGHT: bold;}
span.typedescription{FONT-WEIGHT: bold; FONT-STYLE: italic;}
span.masternummer{FONT-WEIGHT: bold;FONT-STYLE: italic;}
span.othereditions{FONT-WEIGHT: bold;FONT-STYLE: italic;}
span.OEE{FONT-WEIGHT: bold;FONT-STYLE: italic;}
span.OEP{FONT-WEIGHT: bold;FONT-STYLE: italic;}
span.OEL{FONT-WEIGHT: bold;FONT-STYLE: italic;}
/*]]>*/

				
				</xsl:text>
			</xsl:element>
		</head>
	</xsl:template>
	<xsl:template match="title">
		<title>
			<xsl:apply-templates/>
		</title>
	</xsl:template>
	<xsl:template match="book">
		<body>
			<xsl:apply-templates/>
		</body>
	</xsl:template>
	<xsl:template match="level">
		<div>
			<xsl:if test="@class">
				<xsl:attribute name="class">
					<xsl:value-of select="@class"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates />
		</div>
	</xsl:template>
	<xsl:template match="levelhd">
		<xsl:if test="@depth='1'">
			<h1>
				<xsl:copy-of select="@class"/>
				<xsl:call-template name="aName"/>
			</h1>
		</xsl:if>
		<xsl:if test="@depth='2'">
			<h2>
				<xsl:copy-of select="@class"/>
				<xsl:call-template name="aName"/>
			</h2>
		</xsl:if>
		<xsl:if test="@depth='3'">
			<h3>
				<xsl:copy-of select="@class"/>
				<xsl:call-template name="aName"/>
			</h3>
		</xsl:if>
		<xsl:if test="@depth='4'">
			<h4>
				<xsl:copy-of select="@class"/>
				<xsl:call-template name="aName"/>
			</h4>
		</xsl:if>
		<xsl:if test="@depth='5'">
			<h5>
				<xsl:copy-of select="@class"/>
				<xsl:call-template name="aName"/>
			</h5>
		</xsl:if>
		<xsl:if test="@depth='6'">
			<h6>
				<xsl:copy-of select="@class"/>
				<xsl:call-template name="aName"/>
			</h6>
		</xsl:if>
	</xsl:template>
	<xsl:template match="p">
		<p>
			<xsl:copy-of select="@class"/>
			<xsl:apply-templates/>
		</p>
	</xsl:template>
	<xsl:template match="doctitle"></xsl:template>
	<xsl:template match="div">
		<div>
			<xsl:copy-of select="@class"/>
			<xsl:apply-templates/>
		</div>
	</xsl:template>
	<!-- lists -->
	<xsl:template match="list">
		<xsl:if test="@type='ul'">
			<ul>
				<xsl:copy-of select="@class"/>
				<xsl:apply-templates/>
			</ul>
		</xsl:if>
		<xsl:if test="@type='ol'">
			<ol>
				<xsl:copy-of select="@class"/>
				<xsl:apply-templates/>
			</ol>
		</xsl:if>
	</xsl:template>
	<xsl:template match="li">
		<li>
			<xsl:copy-of select="@class"/>
			<xsl:apply-templates/>
		</li>
	</xsl:template>
	<xsl:template match="a">
		<a>
			<xsl:copy-of select="@href"/>
			<xsl:apply-templates/>
		</a>
	</xsl:template>
	<xsl:template match="strong">
		<strong>
			<xsl:apply-templates/>
		</strong>
	</xsl:template>
	<xsl:template match="br">
		<br/>
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="img">
		<img>
			<xsl:copy-of select="@id|@src|@alt|@width|@height|@longdesc"/>
			<xsl:apply-templates/>
		</img>
	</xsl:template>
	<xsl:template match="imggroup">
		<div class="imggroup">
			<xsl:copy-of select="@id"/>
			<xsl:apply-templates/>
		</div>
	</xsl:template>
	<!-- =========================================================================
hr
	============================================================================== -->
	<!-- =========================================================================
blockqoute
	============================================================================== -->
	<xsl:template match="blockquote">
		<blockquote>
			<xsl:copy-of select="@id"/>
			<xsl:apply-templates/>
		</blockquote>
	</xsl:template>
	<!-- =========================================================================
strong
	============================================================================== -->
	<xsl:template match="strong">
		<strong>
			<xsl:copy-of select="@id"/>
			<xsl:apply-templates/>
		</strong>
	</xsl:template>
	<!-- =========================================================================
em
	============================================================================== -->
	<xsl:template match="em">
		<em>
			<xsl:copy-of select="@id"/>
			<xsl:apply-templates/>
		</em>
	</xsl:template>
	<!-- =========================================================================
table
	============================================================================== -->
	<xsl:template match="table">
		<table rules="all">
			<xsl:copy-of select="@id"/>
			<xsl:if test="@border">
				<xsl:attribute name="border">
					<xsl:value-of select="@border"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates/>
		</table>
	</xsl:template>
	<!-- =========================================================================
tbody
	============================================================================== -->
	<xsl:template match="tbody">
		<tbody>
			<xsl:copy-of select="@id"/>
			<xsl:apply-templates/>
		</tbody>
	</xsl:template>
	<!-- =========================================================================
tr
============================================================================== -->
	<xsl:template match="tr">
		<tr>
			<xsl:copy-of select="@id"/>
			<xsl:apply-templates/>
		</tr>
	</xsl:template>
	<!-- =========================================================================
td
============================================================================== -->
	<xsl:template match="td">
		<td>
			<xsl:copy-of select="@id"/>
			<xsl:if test="@rowspan">
				<xsl:attribute name="rowspan">
					<xsl:value-of select="@rowspan"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@colspan">
				<xsl:attribute name="colspan">
					<xsl:value-of select="@colspan"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates/>
		</td>
	</xsl:template>
	<!-- =========================================================================
caption
	============================================================================== -->
	<xsl:template match="caption">
		<caption>
			<xsl:copy-of select="@id"/>
			<xsl:apply-templates/>
		</caption>
	</xsl:template>
	<!-- =========================================================================
span
	============================================================================== -->
	<xsl:template match="span">
		<span>
			<xsl:copy-of select="@id"/>
			<xsl:if test="@class">
				<xsl:attribute name="class">
					<xsl:value-of select="@class"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates/>
		</span>
	</xsl:template>
	<!-- =========================================================================
thead
	============================================================================== -->
	<xsl:template match="thead">
		<thead>
			<xsl:copy-of select="@id"/>
			<xsl:apply-templates/>
		</thead>
	</xsl:template>
	<!-- =========================================================================
tfoot
	============================================================================== -->
	<xsl:template match="tfoot">
		<tfoot>
			<xsl:copy-of select="@id"/>
			<xsl:apply-templates/>
		</tfoot>
	</xsl:template>
	<!-- =========================================================================
colgroup
	============================================================================== -->
	<xsl:template match="colgroup">
		<colgroup>
			<xsl:copy-of select="@id"/>
			<xsl:apply-templates/>
		</colgroup>
	</xsl:template>
	<!-- =========================================================================
col
	============================================================================== -->
	<xsl:template match="col">
		<col>
			<xsl:copy-of select="@id"/>
			<xsl:apply-templates/>
		</col>
	</xsl:template>
	<!-- =========================================================================
th
	============================================================================== -->
	<xsl:template match="th">
		<th>
			<xsl:copy-of select="@id"/>
			<xsl:if test="@rowspan">
				<xsl:attribute name="rowspan">
					<xsl:value-of select="@rowspan"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@colspan">
				<xsl:attribute name="colspan">
					<xsl:value-of select="@colspan"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates/>
		</th>
	</xsl:template>
	<!-- =========================================================================
normalize space
	============================================================================== -->
	<xsl:template match="*/text()[normalize-space()]">
		<xsl:value-of select="normalize-space()"/>
	</xsl:template>
	
	<xsl:template match="*/text()[not(normalize-space())]" />
	<!-- =========================================================================
insert a name
	============================================================================== -->
	<xsl:template name="aName">
		<xsl:element name="a">
			<xsl:attribute name="name">
				<xsl:choose>
					<xsl:when test="@id">
						<xsl:value-of select="@id"/>		
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="generate-id(.)"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>

</xsl:stylesheet>