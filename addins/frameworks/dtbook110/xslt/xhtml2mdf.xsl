<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="text"/>
    
    <xsl:template name="Nest">
        <xsl:choose>
            <xsl:when test="local-name()='div' and @class='page'"><xsl:text>yes</xsl:text></xsl:when>
            <xsl:otherwise><xsl:text>no</xsl:text></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="IncludeInMDF">
        <xsl:choose>
            <xsl:when test="local-name()='h1' or local-name()='h2' or local-name()='h3' or local-name()='h4' or local-name()='h5' or local-name()='h6'">
                <xsl:value-of select="1"/>
            </xsl:when>
            <xsl:when test="local-name()='p'">
                <xsl:value-of select="1"/>
            </xsl:when>
            <xsl:when test="local-name()='ol' or local-name()='ul' or local-name()='li'">
                <xsl:value-of select="1"/>
            </xsl:when>
            <xsl:when test="local-name()='dt' or local-name()='dd'">
                <xsl:value-of select="1"/>
            </xsl:when>
            <xsl:when test="local-name()='caption'">
                <xsl:value-of select="1"/>
            </xsl:when>
            <xsl:when test="local-name()='table'">
                <xsl:choose>
                    <xsl:when test="descendant::*[local-name()='td' or local-name()='th']/*[local-name()='p']">
                        <xsl:value-of select="0"/>
                    </xsl:when>
                    <xsl:otherwise><xsl:value-of select="1"/></xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="local-name()='div' and (@class='note' or @class='page' or @class='area')">
                <xsl:value-of select="1"/>
            </xsl:when>
            <xsl:when test="local-name()='span' and (@class='page-normal' or @class='page-special' or @class='page-front' or @class='noteref')">
                <xsl:value-of select="1"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="0"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="*[local-name()='body']">
        <xsl:variable name="newline"><xsl:text>
</xsl:text></xsl:variable>
        <xsl:variable name="elements">
            <elements>
                <xsl:for-each-group select=".//*[local-name()!='br']" group-by="concat(local-name(), '.', tokenize(@class, '\s')[1])">
                    <xsl:sort select="local-name()"/>
                    <xsl:variable name="incl"><xsl:call-template name="IncludeInMDF"/></xsl:variable>
                    <xsl:if test="$incl=1">
                        <xsl:element name="{local-name()}">
                            <xsl:for-each select="@*">
                                <xsl:copy/>
                            </xsl:for-each>
                            <xsl:attribute name="nest"><xsl:call-template name="Nest"/></xsl:attribute>
                        </xsl:element>
                    </xsl:if>
                </xsl:for-each-group>
            </elements>
        </xsl:variable>

        <!-- 
            Print [Tags] section listing all elements in html body 
            - all elements except br are included as tags
        -->
        <xsl:value-of select="concat('[Tags]', $newline)"/>
        <xsl:for-each select="$elements/elements/*">
            <xsl:value-of select="concat(position(), '=', local-name())"/>
            <xsl:if test="@class">
                <xsl:value-of select="concat('.', tokenize(@class, '\s')[1])"/>
            </xsl:if>
            <xsl:value-of select="$newline"/>
        </xsl:for-each>
        <!-- 
            Print section for each tag 
        -->
        <xsl:for-each select="$elements/elements/*">
            <xsl:value-of select="concat($newline, '[', local-name())"/>
            <xsl:if test="@class">
                <xsl:value-of select="concat('.', tokenize(@class, '\s')[1])"/>
            </xsl:if>
            <xsl:value-of select="concat(']', $newline)"/>
            <xsl:value-of select="concat('Desc=', $newline)"/>
            <xsl:value-of select="concat('Name=', local-name(), $newline)"/>
            <xsl:value-of select="concat('Class=', @class, $newline)"/>
            <!-- Only headings, page numbers and div.group are copied to the NCC and have LinkBack-->
            <xsl:choose>
                <xsl:when test="self::*[local-name()='h1' or local-name()='h2' or local-name()='h3' or local-name()='h4' or local-name()='h5' or local-name()='h6' or (local-name()='span' and (@class='page-front' or @class='page-normal' or @class='page-special')) or (local-name()='div' and @class='group')]">
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
                <xsl:when test="local-name()='h1'">
                    <xsl:value-of select="concat('Level=1', $newline)"/>                    
                </xsl:when>
                <xsl:when test="local-name()='h2'">
                    <xsl:value-of select="concat('Level=2', $newline)"/>                    
                </xsl:when>
                <xsl:when test="local-name()='h3'">
                    <xsl:value-of select="concat('Level=3', $newline)"/>                    
                </xsl:when>
                <xsl:when test="local-name()='h4'">
                    <xsl:value-of select="concat('Level=4', $newline)"/>                    
                </xsl:when>
                <xsl:when test="local-name()='h5'">
                    <xsl:value-of select="concat('Level=5', $newline)"/>                    
                </xsl:when>
                <xsl:when test="local-name()='h6'">
                    <xsl:value-of select="concat('Level=6', $newline)"/>                    
                </xsl:when>
                <xsl:otherwise>            
                    <xsl:value-of select="concat('Level=0', $newline)"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="concat('Nested=', @nest, $newline)"/>
            <xsl:value-of select="concat('ID=yes', $newline)"/>
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>