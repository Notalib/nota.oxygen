<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="text"/>
    <xsl:template match="*[local-name()='body']">
        <xsl:variable name="newline"><xsl:text>
</xsl:text></xsl:variable>
        <!-- 
            Print [Tags] section listing all elements in html body 
            - all elements except br are included as tags
        -->
        <xsl:value-of select="concat('[Tags]', $newline)"/>
        <xsl:for-each-group select=".//*[local-name()!='br']" group-by="concat(local-name(), '.', @class)">
            <xsl:value-of select="concat(position(), '=', local-name())"/>
            <xsl:if test="@class">
                <xsl:value-of select="concat('.', @class)"/>
            </xsl:if>
            <xsl:value-of select="$newline"/>
        </xsl:for-each-group>
        <!-- 
            Print section for each tag 
        -->
        <xsl:for-each-group select=".//*[local-name()!='br']" group-by="concat(local-name(), '.', @class)">
            <xsl:value-of select="concat($newline, '[', local-name())"/>
            <xsl:if test="@class">
                <xsl:value-of select="concat('.', @class)"/>
            </xsl:if>
            <xsl:value-of select="concat(']', $newline)"/>
            <xsl:value-of select="concat('Desc=', $newline)"/>
            <xsl:value-of select="concat('Name=', local-name(), $newline)"/>
            <xsl:value-of select="concat('Class=', @class, $newline)"/>
            <!-- Only headings, page numbers and div.group are copied to the NCC and have LinkBack-->
            <xsl:choose>
                <xsl:when test="(h1|h2|h3|h4|h5|h6) or (span[@class='page-front' or @class='page-normal' or @class='page-special']) or (div[@class='group'])">
                    <xsl:value-of select="concat('NCC=yes', $newline)"/>
                    <xsl:value-of select="concat('LinkBack=yes', $newline)"/>
                </xsl:when>
                <xsl:otherwise>            
                    <xsl:value-of select="concat('NCC=no', $newline)"/>
                    <xsl:value-of select="concat('LinkBack=no', $newline)"/>
                </xsl:otherwise>
            </xsl:choose>
            <!-- Headings gets their respective levels - all others get Level=0 -->
            <xsl:choose>
                <xsl:when test="h1">
                    <xsl:value-of select="concat('Level=1', $newline)"/>                    
                </xsl:when>
                <xsl:when test="h2">
                    <xsl:value-of select="concat('Level=2', $newline)"/>                    
                </xsl:when>
                <xsl:when test="h3">
                    <xsl:value-of select="concat('Level=3', $newline)"/>                    
                </xsl:when>
                <xsl:when test="h4">
                    <xsl:value-of select="concat('Level=4', $newline)"/>                    
                </xsl:when>
                <xsl:when test="h5">
                    <xsl:value-of select="concat('Level=5', $newline)"/>                    
                </xsl:when>
                <xsl:when test="h6">
                    <xsl:value-of select="concat('Level=6', $newline)"/>                    
                </xsl:when>
                <xsl:otherwise>            
                    <xsl:value-of select="concat('Level=0', $newline)"/>
                </xsl:otherwise>
            </xsl:choose>
            <!-- No elements are nested (is this corrent?) -->
            <xsl:value-of select="concat('Nested=no', $newline)"/>
            <xsl:value-of select="concat('ID=yes', $newline)"/>
        </xsl:for-each-group>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>