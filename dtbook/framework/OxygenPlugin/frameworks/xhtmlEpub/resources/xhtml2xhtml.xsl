<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:xhtml="http://www.w3.org/1999/xhtml" exclude-result-prefixes="#all"
    xpath-default-namespace="http://www.w3.org/1999/xhtml">
    <xsl:output omit-xml-declaration="yes"/>
    <xsl:template match="body">
        <xsl:apply-templates select="node()" mode="convert"/>
    </xsl:template>
    <xsl:template match="text()"/>
    <xsl:template match="*|text()|@*" mode="convert">
        <xsl:copy copy-namespaces="no">
            <xsl:apply-templates mode="convert" select="@*"/>
            <xsl:apply-templates mode="convert"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>