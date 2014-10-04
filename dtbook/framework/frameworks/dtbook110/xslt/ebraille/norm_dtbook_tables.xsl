<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">
    <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>
    
    <xsl:template match="table">
        <xsl:variable name="colexpandedTable">
            <xsl:apply-templates select="." mode="colexpand"/>
        </xsl:variable>
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:for-each select="$colexpandedTable/table">
                <xsl:variable name="trs" select="tr"/>
                <xsl:element name="tr">
                    <xsl:apply-templates select="$trs[1]/@*"/>
                    <xsl:for-each select="$trs[1]/*">
                        <xsl:copy>
                            <xsl:apply-templates select="node()|@*[local-name()!='rowspan']"/>
                        </xsl:copy>
                    </xsl:for-each>
                </xsl:element>
                <xsl:for-each select="$trs">
                    <xsl:variable name="pos" select="position()"/>
                    <xsl:if test="$pos>1">
                        <xsl:variable name="pr" select="$trs[$pos - 1]"/>
                        <xsl:apply-templates select="." mode="rowexspand">
                            <xsl:with-param name="prevRow" select="$pr"/>
                        </xsl:apply-templates>
                    </xsl:if>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
    
    <!--
        Expand rows
        ===========-->
    <xsl:template match="tr" mode="rowexspand">
        <xsl:param name="prevRow"/>
        <xsl:variable name="currentRow" select="."/>
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:for-each select="$prevRow/*">
                <xsl:choose>
                    <xsl:when test="@rowspan>1">
                        <xsl:copy>
                            <xsl:if test="@rowspan>2">
                                <xsl:attribute name="class">copy</xsl:attribute>
                                <xsl:attribute name="rowspan">
                                    <xsl:value-of select="@rowspan-1"/>
                                </xsl:attribute>
                            </xsl:if>
                            <xsl:apply-templates select="@*[local-name()!='rowspan' and local-name()!='class']"/>
                            <xsl:apply-templates/>
                        </xsl:copy>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="index" select="count(preceding-sibling::*[not(@rowspan>1)])"/>
                        <xsl:for-each select="$currentRow/*[$index+1]">
                            <xsl:copy>
                                <xsl:apply-templates select="@*[local-name()!='rowspan']"/>
                                <xsl:apply-templates/>                                
                            </xsl:copy>
                        </xsl:for-each>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template name="processCellRowExpand">
        <xsl:param name="prevRow"/>
    </xsl:template>
    <!--
        Expand columns
        ==============-->
    <xsl:template match="table" mode="colexpand">
        <xsl:copy>
            <xsl:apply-templates mode="colexpand"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="tr" mode="colexpand">
        <xsl:copy>
            <xsl:apply-templates mode="colexpand"/>
        </xsl:copy>      
    </xsl:template>
    
    <xsl:template match="td|th" mode="colexpand">
        <xsl:call-template name="colexpandcell">
            <xsl:with-param name="copies" select="@colspan"/>
        </xsl:call-template>
    </xsl:template> 
    <xsl:template name="colexpandcell">
        <xsl:param name="copies">1</xsl:param>
        <xsl:param name="isCopy">0</xsl:param>
        <xsl:copy>
            <xsl:if test="$isCopy='1'">
                <xsl:attribute name="class">copy</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="node()|@*[local-name()!='colspan' and local-name()!='class']"/>
        </xsl:copy>
        <xsl:if test="$copies>1">
            <xsl:call-template name="colexpandcell">
                <xsl:with-param name="copies" select="$copies - 1"/>
                <xsl:with-param name="isCopy">1</xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="node()">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="@*">
        <xsl:if test="local-name()!='id'">
            <xsl:copy/>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>