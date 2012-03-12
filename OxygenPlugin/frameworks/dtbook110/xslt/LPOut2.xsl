<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" omit-xml-declaration="no" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" encoding="Windows-1252" indent="yes"/>

  <xsl:strip-space elements ="li td"/>
  <xsl:template match="/">
    <xsl:apply-templates />
  </xsl:template>
  <!-- =========================================================================
GetLevDepth
	============================================================================== -->
  <xsl:template name="GetLevDepth">
    <xsl:param name="nodeset"/>
    <xsl:param name="dybde" select="$nodeset/@depth"/>
    <xsl:value-of select="$dybde"/>
  </xsl:template>
  <!-- =========================================================================
dtbook
	============================================================================== -->
  <xsl:template match="dtbook">
    <html>
      <xsl:apply-templates/>
    </html>
  </xsl:template>
  <!-- =========================================================================
head
	============================================================================== -->
  <xsl:template match="head">
    <head>
      <style type="text/css">
        DIV.sidebar
        {
        font-style:     normal;
        font-weight:  	bold;
        BORDER: 	black thin solid;
        PADDING: 	0.3em;
        MARGIN: 	0.3em 2em;
        BACKGROUND-COLOR: lightskyblue;
        }

        DIV.prodnote
        {
        font-style:     normal;
        font-weight:  	bold;
        BORDER: 	black thin solid;
        PADDING: 	0.3em;
        MARGIN: 	0.3em 0.5em;
        BACKGROUND-COLOR: #E0E0E0;
        }

        DIV.imgcaption
        {
        font-style:     normal;
        BORDER: 	black thin solid;
        PADDING: 	0.3em;
        MARGIN: 	0.3em 0.5em;
        }

        DIV.box

        {
        font-style:     normal;
        BORDER: 	black thin solid;
        PADDING: 	0.3em;
        MARGIN: 	0.3em 2em;
        BACKGROUND-COLOR: #ffffcc;

        }

        DIV.imggroup
        {
        font-style:     normal;
        BORDER: 	gray thin solid;
        PADDING: 	0.3em;
        MARGIN: 	0.3em 2em;
        }

        DIV
        {
        font-style:     normal;
        font-weight:  	normal;
        PADDING: 	0.3em;
        MARGIN: 0.3em 2em;
        }

        p.p_seperator
        {
        text-align:center;
        }

        p.hd
        {
        font-weight:  	bold;
        }

        p.precedingemptyline{margin-top:3em;}

      </style>
      <xsl:apply-templates/>
    </head>
  </xsl:template>
  <!-- =========================================================================
title
	============================================================================== -->
  <xsl:template match="title">
    <title>
      <xsl:apply-templates/>
    </title>
  </xsl:template>
  <!-- =========================================================================
link
	============================================================================== -->
  <xsl:template match="link">
    <link>
      <xsl:apply-templates/>
    </link>
  </xsl:template>
  <!-- =========================================================================
meta
	============================================================================== -->
  <xsl:template match="meta"></xsl:template>
  <!-- =========================================================================
style
	============================================================================== -->
  <xsl:template match="style"></xsl:template>
  <!-- =========================================================================
book
	============================================================================== -->
  <xsl:template match="book">
    <body>
      <xsl:if test="@lang">
        <xsl:attribute name="lang">
          <xsl:value-of select="@lang"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>

      <!-- Lydbøger skal afsluttes af denne konstruktion-->
      

        <h1>Her slutter...</h1>
        <p>Indlæst af:</p>


    </body>
  </xsl:template>
  <!-- =========================================================================
frontmatter
	============================================================================== -->
  <xsl:template match="frontmatter">
    <xsl:apply-templates/>
  </xsl:template>
  <!-- =========================================================================
bodymatter
	============================================================================== -->
  <xsl:template match="bodymatter">
    <xsl:apply-templates/>
  </xsl:template>
  <!-- =========================================================================
rearmatter
	============================================================================== -->
  <xsl:template match="rearmatter">
    <xsl:apply-templates/>
  </xsl:template>
  <!-- =========================================================================
level
	============================================================================== -->
  <xsl:template match="level">

    <xsl:choose>
      <xsl:when test ="@class='box'">
        <div class="box">
          <xsl:apply-templates/>
        </div>
      </xsl:when>

      <xsl:otherwise >

        <xsl:choose>
          <xsl:when test ="child::levelhd"/>
          <xsl:otherwise>
            <h1>*</h1>
          </xsl:otherwise>
        </xsl:choose>

  
        <xsl:apply-templates/>

        <xsl:if test ="@class='DBBCopyright'">
          <p>Indlæst af...</p>
        </xsl:if>

      </xsl:otherwise>
    </xsl:choose>



  </xsl:template>
  <!-- =========================================================================
levelhd
	============================================================================== -->
  <xsl:template match="levelhd">
    <xsl:param name="strStyle">
      <xsl:call-template name="GetLevDepth">
        <xsl:with-param name="nodeset" select="self::*"/>
      </xsl:call-template>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="$strStyle='1'">
        <h1>
          <xsl:if test="@id">
            <xsl:attribute name="id">
              <xsl:value-of select="@id"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@class">
            <xsl:attribute name="class">
              <xsl:value-of select="@class"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@lang">
            <xsl:attribute name="lang">
              <xsl:value-of select="@lang"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:apply-templates/>
        </h1>
      </xsl:when>
      <xsl:when test="$strStyle='2'">
        <h2>
          <xsl:if test="@id">
            <xsl:attribute name="id">
              <xsl:value-of select="@id"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@class">
            <xsl:attribute name="class">
              <xsl:value-of select="@class"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@lang">
            <xsl:attribute name="lang">
              <xsl:value-of select="@lang"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:apply-templates/>
        </h2>
      </xsl:when>
      <xsl:when test="$strStyle='3'">
        <h3>
          <xsl:if test="@id">
            <xsl:attribute name="id">
              <xsl:value-of select="@id"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@class">
            <xsl:attribute name="class">
              <xsl:value-of select="@class"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@lang">
            <xsl:attribute name="lang">
              <xsl:value-of select="@lang"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:apply-templates/>
        </h3>
      </xsl:when>
      <xsl:when test="$strStyle='4'">
        <h4>
          <xsl:if test="@id">
            <xsl:attribute name="id">
              <xsl:value-of select="@id"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@class">
            <xsl:attribute name="class">
              <xsl:value-of select="@class"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@lang">
            <xsl:attribute name="lang">
              <xsl:value-of select="@lang"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:apply-templates/>
        </h4>
      </xsl:when>
      <xsl:when test="$strStyle='5'">
        <h5>
          <xsl:if test="@id">
            <xsl:attribute name="id">
              <xsl:value-of select="@id"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@class">
            <xsl:attribute name="class">
              <xsl:value-of select="@class"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@lang">
            <xsl:attribute name="lang">
              <xsl:value-of select="@lang"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:apply-templates/>
        </h5>
      </xsl:when>
      <xsl:when test="$strStyle='6'">
        <h6>
          <xsl:if test="@id">
            <xsl:attribute name="id">
              <xsl:value-of select="@id"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@class">
            <xsl:attribute name="class">
              <xsl:value-of select="@class"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@lang">
            <xsl:attribute name="lang">
              <xsl:value-of select="@lang"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:apply-templates/>
        </h6>
      </xsl:when>
      <xsl:otherwise>
        <p>
          <xsl:attribute name="class">
            <xsl:value-of select="concat('levelhd', $strStyle)"/>
          </xsl:attribute>
          <xsl:if test="@lang">
            <xsl:attribute name="lang">
              <xsl:value-of select="@lang"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:apply-templates/>
        </p>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- =========================================================================
linenum
	============================================================================== -->
  <xsl:template match="linenum">
    <span class="linenum">
      <xsl:apply-templates/>
    </span>
  </xsl:template>
  <!-- =========================================================================
	address
		============================================================================== -->
  <xsl:template match="address">
    <div class="address">
      <xsl:if test="@id">
        <xsl:attribute name="id">
          <xsl:value-of select="@id"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </div>
  </xsl:template>
  <!-- =========================================================================
author
	============================================================================== -->
  <xsl:template match="author">
    <p class="author">
      <xsl:apply-templates/>
    </p>
  </xsl:template>
  <!-- =========================================================================
notice
	============================================================================== -->
  <xsl:template match="notice">
    <p class="notice">
      <xsl:apply-templates/>
    </p>
  </xsl:template>
  <!-- =========================================================================
note
	============================================================================== -->
  <xsl:template match="note">
    <div class="note">
      <xsl:if test="@id">
        <xsl:attribute name="id">
          <xsl:value-of select="@id"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@class">
        <xsl:attribute name="class">
          <xsl:value-of select="@class"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </div>
  </xsl:template>
  <!-- =========================================================================
annotation
	============================================================================== -->
  <xsl:template match="annotation">
    <a>
      <xsl:if test="@id">
        <xsl:attribute name="name">
          <xsl:value-of select="@id"/>
        </xsl:attribute>
        <xsl:apply-templates/>
      </xsl:if>
    </a>
    <div class="annotation">
      <xsl:if test="@id">
        <xsl:attribute name="id">
          <xsl:value-of select="@id"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@class">
        <xsl:attribute name="class">
          <xsl:value-of select="@class"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </div>
  </xsl:template>
  <!-- =========================================================================
noteref
	============================================================================== -->
  <xsl:template match="noteref">
    
    <!--<span class="noteref">
			<xsl:if test="@id">
				<xsl:attribute name="id">
					<xsl:value-of select="@id"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@idref">
				<xsl:attribute name="href">
					<xsl:value-of select="@idref"/>
				</xsl:attribute>
			</xsl:if>
			
		</span>-->

    <xsl:apply-templates/>
  </xsl:template>
  <!-- =========================================================================
annoref
	============================================================================== -->
  <xsl:template match="annoref">
    <a class="annoref">
      <xsl:if test="@idref">
        <xsl:attribute name="href">
          <xsl:value-of select="@idref"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </a>
  </xsl:template>
  <!-- =========================================================================
line
	============================================================================== -->
  <!--Erstattet af line der tager hensyn til "stanza"-->

  <!--<xsl:template match="line">
    <p class="line">
      <xsl:if test="@id">
        <xsl:attribute name="id">
          <xsl:value-of select="@id"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </p>
  </xsl:template>-->

  <!-- =========================================================================
a
	============================================================================== -->
  <xsl:template match="a">
    <a>
      <xsl:if test="@id">
        <xsl:attribute name="id">
          <xsl:value-of select="@id"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@href">
        <xsl:attribute name="href">
          <xsl:value-of select="@href"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:if test="@class">
        <xsl:attribute name="class">
          <xsl:value-of select="@class"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:apply-templates/>
    </a>
  </xsl:template>
  <!-- =========================================================================
strong
	============================================================================== -->
  <xsl:template match="strong">
    <strong>
      <xsl:apply-templates/>
    </strong>
  </xsl:template>
  <!-- =========================================================================
em
	============================================================================== -->
  <xsl:template match="em">
    <em>
      <xsl:apply-templates/>
    </em>
  </xsl:template>
  <!-- =========================================================================
dfn
	============================================================================== -->
  <xsl:template match="dfn">
    <span>
      <xsl:apply-templates/>
    </span>
  </xsl:template>
  <!-- =========================================================================
kbd
	============================================================================== -->
  <xsl:template match="kbd">
    <code>
      <xsl:apply-templates/>
    </code>
  </xsl:template>
  <!-- =========================================================================
code
	============================================================================== -->
  <xsl:template match="code">
    <code>
      <xsl:apply-templates/>
    </code>
  </xsl:template>
  <!-- =========================================================================
sub
	============================================================================== -->
  <xsl:template match="sub">
    <sub>
      <xsl:apply-templates/>
    </sub>
  </xsl:template>
  <!-- =========================================================================
sup
	============================================================================== -->
  <xsl:template match="sup">
    <sup>
      <xsl:apply-templates/>
    </sup>
  </xsl:template>

  <!-- =========================================================================
acronym
	============================================================================== -->

  <xsl:template match ="acronym">
    <acronym>
      <xsl:apply-templates/>
    </acronym>
  </xsl:template>

  <!-- =========================================================================
abbr
	============================================================================== -->

  <xsl:template match ="abbr">
    <abbr>
      <xsl:apply-templates/>
    </abbr>
  </xsl:template>
  <!-- =========================================================================
span
	============================================================================== -->
  <xsl:template match="span">
    <!--<span>
			<xsl:if test="@id">
				<xsl:attribute name="id">
					<xsl:value-of select="@id"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@class">
				<xsl:attribute name="class">
					<xsl:value-of select="@class"/>
				</xsl:attribute>
			</xsl:if>
			
		</span>-->

    <xsl:apply-templates/>
  </xsl:template>
  <!-- =========================================================================
img
	============================================================================== -->
  <xsl:template match="img">
    <img src="{@src}" id="{@id}" alt="{@alt}" width="{@width}" height="{@height}" />
    <!--
		<xsl:if test="@id">			
			<xsl:attribute name="id">				
				<xsl:value-of select="@id"/>			
			</xsl:attribute>		
		</xsl:if>		
		<xsl:if test="@alt">			
			<xsl:attribute name="alt">				
				<xsl:value-of select="@alt"/>			
			</xsl:attribute>		
		</xsl:if>		
		<xsl:if test="@width">			
			<xsl:attribute name="width">				
				<xsl:value-of select="@width"/>			
			</xsl:attribute>		
		</xsl:if>		
		<xsl:if test="@height">			
			<xsl:attribute name="height">				
				<xsl:value-of select="@height"/>			
			</xsl:attribute>		
		</xsl:if>		
		<xsl:if test="@longdesc">			
			<xsl:attribute name="longdesc">				
				<xsl:value-of select="@longdesc"/>			
			</xsl:attribute>		
		</xsl:if>		
		-->
    <xsl:apply-templates/>
  </xsl:template>
  <!-- =========================================================================
imggroup
	============================================================================== -->
  <xsl:template match="imggroup">
    <div class="imggroup">
      <xsl:if test="@id">
        <xsl:attribute name="id">
          <xsl:value-of select="@id"/>
        </xsl:attribute>
      </xsl:if>
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
      <xsl:apply-templates/>
    </blockquote>
  </xsl:template>
  <!-- =========================================================================
list
	============================================================================== -->
  <xsl:template match="list">
    <xsl:choose>
      <xsl:when test="@type[.='ul']">
        <ul>
          <xsl:if test ="@bullet[.='none']">
            <xsl:attribute name="style">list-style:none;</xsl:attribute>
          </xsl:if>
          <xsl:apply-templates/>
        </ul>
      </xsl:when>
      <xsl:when test="@type[.='ol']">
        <ol>
          <xsl:apply-templates/>
        </ol>
      </xsl:when>
      <xsl:otherwise>
        <ul>
          <xsl:apply-templates/>
        </ul>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- =========================================================================
li
	============================================================================== -->
  <xsl:template match="li">

    <!--Rettet 01.10.2010 pga af inspirationsliste
    Alle li skal indeholde et <p> - pga indiske filer
    -->

    <li>
      <xsl:if test="@id">
        <xsl:attribute name="id">
          <xsl:value-of select="@id"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:choose>
        <xsl:when test="child::p|child::list">
          <xsl:apply-templates/>
        </xsl:when>

        <xsl:otherwise>
          <p>
            <xsl:apply-templates/>
          </p>
        </xsl:otherwise>
      </xsl:choose>

    </li>

  </xsl:template>

  <!-- =========================================================================
hd - indsat 2009.08.27
============================================================================== -->

  <xsl:template match ="hd">
    <li>
      <p class="hd">
        <xsl:if test="@id">
          <xsl:attribute name="id">
            <xsl:value-of select="@id"/>
          </xsl:attribute>
        </xsl:if>

        <xsl:apply-templates/>

      </p>
    </li>
  </xsl:template>
  <!-- =========================================================================
doctitle
	============================================================================== -->
  <xsl:template match="doctitle"></xsl:template>
  <!-- =========================================================================
br
	============================================================================== -->
  <xsl:template match="br">
    <br id="{@id}" />
  </xsl:template>
  <!-- =========================================================================
table
	============================================================================== -->
  <xsl:template match="table">
    <table rules="all">
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
      <xsl:apply-templates/>
    </tbody>
  </xsl:template>
  <!-- =========================================================================
tr
============================================================================== -->
  <xsl:template match="tr">
    <tr>
      <xsl:apply-templates/>
    </tr>
  </xsl:template>
  <!-- =========================================================================
td
============================================================================== -->
  <xsl:template match="td">
    <td>


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

      <!--<xsl:apply-templates/>-->
      
      <xsl:choose>
        <xsl:when test ="child::p">
          <xsl:apply-templates/>
         
        </xsl:when>

        <xsl:otherwise>
          <p>
            <xsl:apply-templates/>
          </p>
        </xsl:otherwise>
      </xsl:choose>

    </td>
  </xsl:template>
  <!-- =========================================================================
caption
	============================================================================== -->
  <xsl:template match="caption">
    <caption>
      <xsl:apply-templates/>
    </caption>
  </xsl:template>
  <!-- =========================================================================
thead
	============================================================================== -->
  <xsl:template match="thead">
    <thead>
      <xsl:apply-templates/>
    </thead>
  </xsl:template>
  <!-- =========================================================================
tfoot
	============================================================================== -->
  <xsl:template match="tfoot">
    <tfoot>
      <xsl:apply-templates/>
    </tfoot>
  </xsl:template>
  <!-- =========================================================================
colgroup
	============================================================================== -->
  <xsl:template match="colgroup">
    <colgroup>
      <xsl:apply-templates/>
    </colgroup>
  </xsl:template>
  <!-- =========================================================================
col
	============================================================================== -->
  <xsl:template match="col">
    <col>
      <xsl:apply-templates/>
    </col>
  </xsl:template>
  <!-- =========================================================================
th
	============================================================================== -->
  <xsl:template match="th">
    <th>
      <!--Ændret 2011.05.18-->
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

      <xsl:choose>
        <xsl:when test ="child::p">
          <xsl:apply-templates/>

        </xsl:when>

        <xsl:otherwise>
          <p>
            <xsl:apply-templates/>
          </p>
        </xsl:otherwise>
      </xsl:choose>

      <!--<xsl:if test="@rowspan">
        <xsl:attribute name="rowspan">
          <xsl:value-of select="@rowspan"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@colspan">
        <xsl:attribute name="colspan">
          <xsl:value-of select="@colspan"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>-->
    </th>
  </xsl:template>
  <!-- =========================================================================
p
	============================================================================== -->
  <xsl:template match="p">

    <!--Det skal checkes om der er en precedingseperator eller empty line
    før der bliver dannet et nyt element
    
    Hvis der er skal der dannes endnu et element før-->
    <xsl:if test ="@class">
      <xsl:choose>
        <xsl:when test ="@class='precedingseparator'">
          <br/>
          <p class="precedingseparator">*</p>
        </xsl:when>

        <!--Denne bliver overtaget af style--><!--
        <xsl:when test ="@class='precedingemptyline'">
          <p class="precedingseparator"></p>
          <br/>
        </xsl:when>-->
      </xsl:choose>
    </xsl:if>

    <xsl:choose>
      <xsl:when test="@class='blankline'">
        <p>
          <br></br>
        </p>
        <xsl:attribute name="id">
          <xsl:value-of select="@id"/>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <p>
          <xsl:attribute name="id">
            <xsl:value-of select="@id"/>
          </xsl:attribute>
          <xsl:if test="@class">
            <xsl:attribute name="class">
              <xsl:value-of select="@class"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@lang">
            <xsl:attribute name="lang">
              <xsl:value-of select="@lang"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:apply-templates/>
        </p>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- =========================================================================
prodnote
	============================================================================== -->
  <xsl:template match="prodnote">

    <xsl:choose>

      <xsl:when test ="@class='caption'">
        <div class="imgcaption">
          <p>Billedtekst:</p>
          <xsl:apply-templates/>
        </div>
      </xsl:when>

      <xsl:when test ="@class='imgprodnote'">
        <div class="imgprodnote">
          <p>Billedbeskrivelse:</p>
          <xsl:apply-templates/>
        </div>
      </xsl:when>

      <xsl:otherwise>
        <div class="prodnote">
          <xsl:if test="@lang">
            <xsl:attribute name="lang">
              <xsl:value-of select="@lang"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@title">
            <p>
              <xsl:value-of select="@title"/>
            </p>
          </xsl:if>
          <xsl:apply-templates/>
        </div>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- =========================================================================
sidebar
	============================================================================== -->
  <xsl:template match="sidebar">
    <div class="sidebar">
      <xsl:if test="@lang">
        <xsl:attribute name="lang">
          <xsl:value-of select="@lang"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@title">
        <p>
          <xsl:value-of select="@title"/>
        </p>
      </xsl:if>
      <xsl:apply-templates/>
    </div>
  </xsl:template>
  <!-- =========================================================================
div
	============================================================================== -->
  <xsl:template match ="div">
    <div>

      <xsl:if test ="@id">
        <xsl:attribute name ="id">
          <xsl:value-of select ="@id"/>
        </xsl:attribute>
      </xsl:if>

      <!-- Test for klasse
      poem og stanza skal behandles specielt
      -->

      <xsl:if test ="@class">

        <xsl:attribute name ="class">
          <xsl:value-of select ="@class"/>
        </xsl:attribute>

        <xsl:choose>
          <xsl:when test ="@class='stanza'">

            <p>
              <xsl:for-each select ="child::line">
                <xsl:apply-templates/>
                <br/>
              </xsl:for-each>
            </p>

          </xsl:when>
        </xsl:choose>
      </xsl:if>

      <xsl:apply-templates/>

    </div>
  </xsl:template>

  <xsl:template match ="line">
    <!--
    line skal behandles specielt
    Hvis parent er en div class "stanza" skal den ignoreres, ellers oversaettes til et <p>
    -->

    <xsl:choose>
      <xsl:when test ="parent::div[@class='stanza']"/>
      <xsl:otherwise>
        <p class="line">
          <xsl:apply-templates/>
        </p>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--============================================================================
Pagenum/list template
   ================================================================================-->

  <xsl:template name ="ExecutePagenumList">
    <xsl:param name ="PNum"/>
    <xsl:variable name ="PnumParent" select =".."/>
    <xsl:variable name ="ListName" select ="string($PnumParent/@type)"/>
    <xsl:variable name="Bulletname" select ="string($PnumParent/@bullet)"/>
    <!--Test om listen har bullet-->

    <xsl:text disable-output-escaping="yes">&lt;/</xsl:text>
    <xsl:value-of select ="$ListName"/>
    <xsl:text disable-output-escaping="yes">&gt;</xsl:text>

    <span>
      <xsl:if test="@id">
        <xsl:attribute name="id">
          <xsl:value-of select="@id"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="@page[.='front']">
          <xsl:attribute name="class">
            <xsl:text>page-front</xsl:text>
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="@page[.='normal']">
          <xsl:attribute name="class">
            <xsl:text>page-normal</xsl:text>
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="@page[.='special']">
          <xsl:attribute name="class">
            <xsl:text>page-special</xsl:text>
          </xsl:attribute>
        </xsl:when>
      </xsl:choose>
      <xsl:apply-templates/>
    </span>

    <xsl:choose>
      <xsl:when test ="$Bulletname=''">
        <xsl:text disable-output-escaping="yes">&lt;</xsl:text>
        <xsl:value-of select ="$ListName"/>
        <xsl:text disable-output-escaping="yes">&gt;</xsl:text>
      </xsl:when>

      <xsl:otherwise>
        <xsl:text disable-output-escaping="yes">&lt;</xsl:text>
        <xsl:value-of select ="$ListName"/>
        <xsl:text disable-output-escaping="yes"> style='list-style:none;'</xsl:text>
        <xsl:text disable-output-escaping="yes">&gt;</xsl:text>

      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- =========================================================================
pagenum
	============================================================================== -->
  <xsl:template match="pagenum">

    <xsl:choose>

      <!--
      Checker om parent er et <list> element
      Hvis det er, så skal <list> afsluttes, pagenum indsættes og 
      <list> påbegyndes igen - med den rigtige type attribut
      -->

      <xsl:when test ="name(..)='list'">

        <xsl:call-template name ="ExecutePagenumList">
          <xsl:with-param name ="PNum" select ="."/>
        </xsl:call-template>
      </xsl:when>

      <xsl:otherwise>

        <!-- 
        Her anbringes andre alternativer
        f.eks. om parent er et p...
        -->
        <xsl:choose>
          <xsl:when test="name(..)='p'">
            <xsl:text disable-output-escaping="yes">&lt;/p&gt;</xsl:text>
            <span>
              <xsl:if test="@id">
                <xsl:attribute name="id">
                  <xsl:value-of select="@id"/>
                </xsl:attribute>
              </xsl:if>
              <xsl:choose>
                <xsl:when test="@page[.='front']">
                  <xsl:attribute name="class">
                    <xsl:text>page-front</xsl:text>
                  </xsl:attribute>
                </xsl:when>
                <xsl:when test="@page[.='normal']">
                  <xsl:attribute name="class">
                    <xsl:text>page-normal</xsl:text>
                  </xsl:attribute>
                </xsl:when>
                <xsl:when test="@page[.='special']">
                  <xsl:attribute name="class">
                    <xsl:text>page-special</xsl:text>
                  </xsl:attribute>
                </xsl:when>
              </xsl:choose>
              <xsl:apply-templates/>
            </span>
            <xsl:text disable-output-escaping="yes">&lt;p&gt;</xsl:text>
          </xsl:when>

          <xsl:when test="name(..)='li'">
            
            <!--Rettet 2011.04.13-->
            <!--Afslut li, afslut liste, indsæt sidetal i span
            begynd liste og begynd li-->
            
            <!--Find listeværdier-->
            <xsl:variable name ="PnumParent" select ="../.."/>
            <xsl:variable name ="ListName" select ="string($PnumParent/@type)"/>
            <xsl:variable name="Bulletname" select ="string($PnumParent/@bullet)"/>
            <!--Test om listen har bullet-->

            <!--Afslut liste-->
            <xsl:text disable-output-escaping="yes">&lt;/</xsl:text>
            <xsl:value-of select ="$ListName"/>
            <xsl:text disable-output-escaping="yes">&gt;</xsl:text>


            <!--Afslut li-->
            <xsl:text disable-output-escaping="yes">&lt;/li&gt;</xsl:text>
            <span>
              <xsl:if test="@id">
                <xsl:attribute name="id">
                  <xsl:value-of select="@id"/>
                </xsl:attribute>
              </xsl:if>
              <xsl:choose>
                <xsl:when test="@page[.='front']">
                  <xsl:attribute name="class">
                    <xsl:text>page-front</xsl:text>
                  </xsl:attribute>
                </xsl:when>
                <xsl:when test="@page[.='normal']">
                  <xsl:attribute name="class">
                    <xsl:text>page-normal</xsl:text>
                  </xsl:attribute>
                </xsl:when>
                <xsl:when test="@page[.='special']">
                  <xsl:attribute name="class">
                    <xsl:text>page-special</xsl:text>
                  </xsl:attribute>
                </xsl:when>
              </xsl:choose>
              <xsl:apply-templates/>
            </span>
            
            
            <!--Påbegynd en ny liste og et nyt li-->
            <xsl:choose>
              <xsl:when test ="$Bulletname=''">
                <xsl:text disable-output-escaping="yes">&lt;</xsl:text>
                <xsl:value-of select ="$ListName"/>
                <xsl:text disable-output-escaping="yes">&gt;</xsl:text>
              </xsl:when>

              <xsl:otherwise>
                <xsl:text disable-output-escaping="yes">&lt;</xsl:text>
                <xsl:value-of select ="$ListName"/>
                <xsl:text disable-output-escaping="yes"> style='list-style:none;'</xsl:text>
                <xsl:text disable-output-escaping="yes">&gt;</xsl:text>

              </xsl:otherwise>
            </xsl:choose>
            
            <xsl:text disable-output-escaping="yes">&lt;li&gt;</xsl:text>
          </xsl:when>

          <!--Bare helt almindeligt sidetal-->
          <xsl:otherwise>
            <span>
              <xsl:if test="@id">
                <xsl:attribute name="id">
                  <xsl:value-of select="@id"/>
                </xsl:attribute>
              </xsl:if>
              <xsl:choose>
                <xsl:when test="@page[.='front']">
                  <xsl:attribute name="class">page-front</xsl:attribute>
                </xsl:when>
                <xsl:when test="@page[.='normal']">
                  <xsl:attribute name="class">page-normal</xsl:attribute>
                </xsl:when>
                <xsl:when test="@page[.='special']">
                  <xsl:attribute name="class">page-special</xsl:attribute>
                </xsl:when>
              </xsl:choose>
              <xsl:apply-templates/>
            </span>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>


  </xsl:template>

  <!-- =========================================================================
dl
	============================================================================== -->

  <xsl:template match="dl">
    <dl>
      <xsl:attribute name="id">
        <xsl:value-of select="@id"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </dl>
  </xsl:template>
  <!-- =========================================================================
dt
	============================================================================== -->

  <xsl:template match="dt">
    <dt>
      <xsl:attribute name="id">
        <xsl:value-of select="@id"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </dt>
  </xsl:template>
  <!-- =========================================================================
dd
	============================================================================== -->

  <xsl:template match="dd">
    <dd>
      <xsl:attribute name="id">
        <xsl:value-of select="@id"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </dd>
  </xsl:template>
  <!-- =========================================================================
normalize space
	============================================================================== -->
  <xsl:template match="*/text()[normalize-space()]">
    <xsl:value-of select="normalize-space()"/>
  </xsl:template>
  
  <xsl:template match="*/text()[not(normalize-space())]" />
</xsl:stylesheet>