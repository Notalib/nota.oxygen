<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs html"
    version="2.0">
    <xsl:output 
        encoding="iso-8859-1"
        doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
    
    <xsl:param name="sizeFactor" as="xs:float">1.0</xsl:param>
    
    <xsl:param name="createPageHeadings" as="xs:boolean">true</xsl:param>
    <xsl:param name="pageHeadingPrefix"><xsl:text>Side </xsl:text></xsl:param>
    <xsl:param name="pageHeadingElementName"><xsl:text>h2</xsl:text></xsl:param>
    
    <!-- Added styles -->
    <xsl:template match="html:head">
        <head>
            <xsl:apply-templates/>
            <style type="text/css">
    div.page {position:relative;top:0px;left:0px;zoom:50%;} 
    div.area {position:absolute;border:dotted;border-width:1px;margin:0px;padding:0px;}
    div.page img {position:absolute;top:0px;left:0px;z-index:2;filter:alpha(opacity=70); opacity:0.7;}
            </style>
        </head>
    </xsl:template>
    
    <!-- Page numbers -->
    <xsl:template match="html:span[@class='page-normal']">
        <xsl:choose>
            <xsl:when test="$createPageHeadings">
                <xsl:element name="{$pageHeadingElementName}">
                    <xsl:value-of select="$pageHeadingPrefix"/>
                    <xsl:apply-templates/>        
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select="@*|node()"/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- Skip existing styles -->
    <xsl:template match="html:style">
    </xsl:template>
    
    <!-- Converts map/area structure into nested div.page/div.area structure -->
    <xsl:template match="html:map">
        <xsl:variable name="img" select="//html:img[@usemap=concat('#', current()/@name)]"/>
        <xsl:variable name="width" as="xs:int" select="xs:int($sizeFactor*number($img/@width))"></xsl:variable>
        <xsl:variable name="height" as="xs:int" select="xs:int($sizeFactor*number($img/@height))"></xsl:variable>
        <xsl:variable name="curPageSpan" select="preceding-sibling::*[1][self::html:span][@class='page-normal']"></xsl:variable>
        <div class="page">
            <xsl:copy-of select="@id"/>
            <xsl:attribute 
                name="style" 
                select="concat('height:', $height, 'px;width:', $width, 'px;')"/>
            <img class="page">
                <xsl:attribute 
                    name="style" 
                    select="concat('height:', $height, 'px;width:', $width, 'px;')"/>
                <xsl:apply-templates select="$img/@id|$img/@src|$img/@alt"/>
                <xsl:if test="$curPageSpan and not($img/@alt)">
                    <xsl:attribute name="alt"><xsl:value-of select="concat('Side ', $curPageSpan)"/></xsl:attribute>
                </xsl:if>
            </img>
            <xsl:for-each select="html:area[@shape='rect']">
                <xsl:sort select="position()" order="descending"/>
                <xsl:variable name="x1" as="xs:int" select="xs:int($sizeFactor*number(replace(@coords, '(\d+),(\d+),(\d+),(\d+)$', '$1')))"/>
                <xsl:variable name="y1" as="xs:int" select="xs:int($sizeFactor*number(replace(@coords, '(\d+),(\d+),(\d+),(\d+)$', '$2')))"/>
                <xsl:variable name="x2" as="xs:int" select="xs:int($sizeFactor*number(replace(@coords, '(\d+),(\d+),(\d+),(\d+)$', '$3')))"/>
                <xsl:variable name="y2" as="xs:int" select="xs:int($sizeFactor*number(replace(@coords, '(\d+),(\d+),(\d+),(\d+)$', '$4')))"/>
                <div class="area">
                    <xsl:copy-of select="@id"/>
                    <xsl:attribute name="style">
                        <xsl:value-of select="concat('left:', $x1, 'px;')"></xsl:value-of>
                        <xsl:value-of select="concat('top:', $y1, 'px;')"></xsl:value-of>
                        <xsl:value-of select="concat('width:', $x2 - $x1, 'px;')"></xsl:value-of>
                        <xsl:value-of select="concat('height:', $y2 - $y1, 'px;')"></xsl:value-of>
                    </xsl:attribute>
                    <xsl:text> </xsl:text>
                </div>
            </xsl:for-each>
        </div>
    </xsl:template>
    
    <!-- Skip images that refer to maps -->
    <xsl:template match="html:img[@usemap]">
        
    </xsl:template>
    
    <!-- Copy everything else -->    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>