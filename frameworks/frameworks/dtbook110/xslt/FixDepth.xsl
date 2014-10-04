<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">
    <xsl:output method="xml" omit-xml-declaration="no" indent="yes"/>
    
    <xsl:template match="level|levelhd">
        <xsl:copy>
            <xsl:attribute name="depth">
                <xsl:value-of select="count(ancestor-or-self::level)"/>
            </xsl:attribute>
            <xsl:apply-templates select="@*[local-name()!='depth']"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
        
</xsl:stylesheet>