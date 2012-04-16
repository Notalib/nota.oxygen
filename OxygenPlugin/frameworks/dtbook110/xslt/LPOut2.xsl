<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs">
  <xsl:output method="xml" omit-xml-declaration="no" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" encoding="Windows-1252" indent="yes"/>

  <!-- =========================================================================
  paraInTableCells controls how table cells are rendered:
  when true, the content of table cells are wrapped in p element(s)
  when false, the content of table cells are are text nodes and possibly inline elements
  ============================================================================== -->
  <xsl:param name="paraInTableCells" as="xs:boolean">true</xsl:param>

  <xsl:strip-space elements ="li td"/>
  <xsl:template match="/">
    <xsl:apply-templates />
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
      <xsl:copy-of select="@lang"/>
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
      <xsl:otherwise>
        <xsl:if test="not(child::levelhd)">
          <h1>*</h1>
        </xsl:if>
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
    <xsl:variable name="depth">
      <xsl:value-of select="count(ancestor::level)"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="1&lt;=$depth and $depth&lt;=6">
        <xsl:element name="h{$depth}">
          <xsl:copy-of select="@id|@class|@lang"/>
          <xsl:apply-templates/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <p>
          <xsl:copy-of select="@id"/>
          <xsl:attribute name="class">
            <xsl:value-of select="concat('levelhd', $depth)"/>
          </xsl:attribute>
          <xsl:copy-of select="@lang"/>
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
      <xsl:copy-of select="@id"/>
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
      <xsl:copy-of select="@id|@class"/>
      <xsl:apply-templates/>
    </div>
  </xsl:template>
  
  <!-- =========================================================================
  annotation
  ============================================================================== -->
  <xsl:template match="annotation">
    <a>
      <xsl:copy-of select="@id"/>
    </a>
    <div class="annotation">
      <xsl:copy-of select="@id|@class"/>
      <xsl:apply-templates/>
    </div>
  </xsl:template>
  
  <!-- =========================================================================
  noteref
  ============================================================================== -->
  <xsl:template match="noteref">
    <xsl:apply-templates/>
  </xsl:template>
  
  <!-- =========================================================================
  annoref
  ============================================================================== -->
  <xsl:template match="annoref">
    <a class="annoref">
      <xsl:copy-of select="@id"/>
      <xsl:apply-templates/>
    </a>
  </xsl:template>
  
  <!-- =========================================================================
  a
  ============================================================================== -->
  <xsl:template match="a">
    <a>
      <xsl:copy-of select="@id|@href|@class"/>
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
    <xsl:apply-templates/>
  </xsl:template>
  
  <!-- =========================================================================
  img
  ============================================================================== -->
  <xsl:template match="img">
    <xsl:element name="img">
      <xsl:copy-of select="@src|@id|@alt|@width|@height"/>
    </xsl:element>
    <xsl:apply-templates/>
  </xsl:template>
  
  <!-- =========================================================================
  imggroup
  ============================================================================== -->
  <xsl:template match="imggroup">
    <div class="imggroup">
      <xsl:copy-of select="@id"/>
      <xsl:apply-templates/>
    </div>
  </xsl:template>
  
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
    <xsl:variable name="listName">
      <xsl:choose>
        <xsl:when test="@type='ol'"><xsl:value-of select="'ol'"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="'ul'"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="ancestor::list">
        <xsl:element name="{$listName}">
          <xsl:if test ="@bullet[.='none']">
            <xsl:attribute name="style">list-style:none;</xsl:attribute>
          </xsl:if>
          <xsl:apply-templates select="li"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each-group select="*" group-ending-with="*[descendant-or-self::pagenum]">
          <xsl:if test="current-group()[self::li]">
            <xsl:element name="{$listName}">
              <xsl:if test ="@bullet[.='none']">
                <xsl:attribute name="style">list-style:none;</xsl:attribute>
              </xsl:if>
              <xsl:apply-templates select="current-group()[self::li]"/>
            </xsl:element>
          </xsl:if>
          <xsl:for-each select="current-group()">
            <xsl:apply-templates select="descendant-or-self::pagenum"/>
          </xsl:for-each>
        </xsl:for-each-group>
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
      <xsl:copy-of select="@id"/>
  
      <xsl:choose>
        <xsl:when test="child::p|child::list">
          <xsl:apply-templates><xsl:with-param name="postfixPagenum" select="false"/></xsl:apply-templates>
        </xsl:when>
  
        <xsl:otherwise>
          <p>
            <xsl:apply-templates select="text()|*[not(self::pagenum)]"/>
          </p>
        </xsl:otherwise>
      </xsl:choose>
  
    </li>
  </xsl:template>
  
  <!-- =========================================================================
  hd - indsat 2009.08.27
  ============================================================================== -->
  <xsl:template match="hd">
    <li>
      <p class="hd">
        <xsl:copy-of select="@id"/>
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
    <br/>
  </xsl:template>
  
  <!-- =========================================================================
  table
  ============================================================================== -->
  <xsl:template match="table">
    <table rules="all">
      <xsl:copy-of select="@border"/>
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
      <xsl:copy-of select="@rowspan|@colspan"/>
      <xsl:call-template name="tableCellContent"/>
    </td>
  </xsl:template>
  
  <!-- =========================================================================
  th
  ============================================================================= -->
  <xsl:template match="th">
    <th>
      <xsl:copy-of select="@rowspan|@colspan"/>
      <xsl:call-template name="tableCellContent"/>
    </th>
  </xsl:template>
  
  <xsl:template name="tableCellContent">
    <xsl:if test="child::div">
      <xsl:message terminate="no" select="concat('WARNING: Table cell with div - near element with id ', ancestor-or-self::*[@id][1]/@id)"/>
    </xsl:if>
    <xsl:if test="child::p and child::text()[normalize-space()!='']">
      <xsl:message terminate="no" select="concat('WARNING: Table cell with mixed content - near element with id ',  ancestor-or-self::*[@id][1]/@id)"/>
    </xsl:if>
    <xsl:choose>
      <xsl:when test="$paraInTableCells">
        <xsl:choose>
          <xsl:when test="child::p">
            <xsl:apply-templates/>
          </xsl:when>
          <xsl:otherwise>
            <p>
              <xsl:apply-templates/>
            </p>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="child::p">
            <xsl:for-each select="*">
              <xsl:apply-templates/>
              <xsl:if test="following-sibling::*"><br/></xsl:if>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates/>
          </xsl:otherwise>
        </xsl:choose>        
      </xsl:otherwise>
    </xsl:choose>
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
  p
  ============================================================================== -->
  <xsl:template match="p">
    <xsl:param name="postfixPagenum" select="true"/>
    <!--Det skal checkes om der er en precedingseperator eller empty line
    før der bliver dannet et nyt element
    
    Hvis der er skal der dannes endnu et element før-->
    <xsl:if test ="@class='precedingseparator'">
      <p class="precedingseparator">*</p>
    </xsl:if>
    <xsl:choose>
      <xsl:when test="@class='blankline'">
        <xsl:element name="p">
          <xsl:copy-of select="@id"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <p>
          <xsl:copy-of select="@id|@class|@lang"/>
          <xsl:apply-templates/>
        </p>
        <xsl:if test="$postfixPagenum">
          <xsl:apply-templates mode="PAGENUM"/>
        </xsl:if>
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
          <xsl:copy-of select="@lang"/>
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
      <xsl:copy-of select="@lang"/>
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
      <xsl:copy-of select="@id"/>
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
    <xsl:if test="parent::level">
      <xsl:apply-templates mode="PAGENUM"/>
    </xsl:if>
  </xsl:template>
  
  <!-- =========================================================================
  line
  ============================================================================== -->
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
  
  <!-- =========================================================================
  pagenum
  ============================================================================== -->
  <xsl:template match="pagenum" mode="PAGENUM">
    <span>
      <xsl:copy-of select="@id"/>
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
      <xsl:apply-templates mode="#default"/>
    </span>
  </xsl:template>
  
  <xsl:template match="*" mode="PAGENUM">
    <xsl:apply-templates mode="PAGENUM"/>
  </xsl:template>
  
  <!-- In normal mode, only pagenums that are children of level are handled -->
  <xsl:template match="pagenum">
    <xsl:if test="parent::level">
      <xsl:apply-templates select="." mode="PAGENUM"/>
    </xsl:if>
  </xsl:template>
  
  <!-- =========================================================================
  dl
  ============================================================================== -->
  <xsl:template match="dl">
    <dl>
      <xsl:copy-of select="@id"/>
      <xsl:apply-templates/>
    </dl>
  </xsl:template>
  
  <!-- =========================================================================
  dt
  ============================================================================== -->
  <xsl:template match="dt">
    <dt>
      <xsl:copy-of select="@id"/>
      <xsl:apply-templates/>
    </dt>
  </xsl:template>
  
  <!-- =========================================================================
  dd
  ============================================================================== -->
  <xsl:template match="dd">
    <dd>
      <xsl:copy-of select="@id"/>
      <xsl:apply-templates/>
    </dd>
  </xsl:template>
  
  <!-- =========================================================================
  normalize space
  ============================================================================== -->
  <xsl:template match="*/text()[normalize-space()]">
   <xsl:if test="matches(., '^\s.*')">
      <xsl:value-of select="' '"/>
    </xsl:if>
    <xsl:value-of select="normalize-space()"/>
    <xsl:if test="matches(., '.*\s$')">
      <xsl:value-of select="' '"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="*/text()[not(normalize-space())]" />
</xsl:stylesheet>