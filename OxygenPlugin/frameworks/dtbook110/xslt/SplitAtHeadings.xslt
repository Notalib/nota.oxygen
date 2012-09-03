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
    
    <xsl:template match="html:html">
        <xsl:apply-templates select="html:body"/>
    </xsl:template>
    
    <xsl:template match="html:body">
        <xsl:variable name="hrefPrefix" select="concat(substring-before(document-uri(/), '.'), '-')"/>
        <xsl:variable name="headings" select="html:h1|html:h2"></xsl:variable>
        <xsl:for-each select="$headings">
            <xsl:result-document href="{concat($hrefPrefix,index-of($headings,.),'.htm')}">
                <html>
                    <xsl:call-template name="head"/>
                    <body>
                        <xsl:apply-templates select="."/>
                        <xsl:apply-templates select="following-sibling::*[(preceding-sibling::html:h1|preceding-sibling::html:h2)[last()]=current()][not(self::html:h1) and not(self::html:h2)]"/>
                    </body>
                </html>
            </xsl:result-document>            
        </xsl:for-each>
        
    </xsl:template>
    
    <xsl:template name="head">
        <head>
            <xsl:apply-templates select="/html:html/html:head/*"/>
        </head>
    </xsl:template>
    
    
    <!-- Copy everything else -->    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>