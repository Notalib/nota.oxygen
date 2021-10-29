<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:nota="http://www.nota.dk/dtbook2docx"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    exclude-result-prefixes="nota xs"
    version="2.0">
    <xsl:import href="dtbook2docx-functions.xsl"/>
    <xsl:template mode="NUMBERING" match="book">
        <w:numbering>
            <xsl:apply-templates mode="NUMBERING" select=".//list"/>
        </w:numbering>
    </xsl:template>
    <xsl:template mode="NUMBERING" match="list">
        <xsl:variable name="leftIndent" as="xs:integer"
            select="nota:determine-indent-left(.) + 
                    (if (ancestor::note) then 240 else 360)"/>
        <w:abstractNum w:abstractNumId="{position()}">
            <w:multiLevelType w:val="singleLevel"/>
            <w:lvl w:ivl="0">
                <w:start w:val="1"/>
                <xsl:choose>
                    <xsl:when test="@type = 'ol'">
                        <w:numFmt
                            w:val="{nota:convert-number-format(@enum)}"/>
                        <w:lvlText w:val="%1"/>
                    </xsl:when>
                    <xsl:when test="@type = 'ul' and @bullet = 'none'">
                        <w:numFmt w:val="none"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <w:numFmt w:val="bullet"/>
                        <w:lvlText w:val=""/>
                        <w:rPr>
                            <w:rFonts w:ascii="Symbol" w:hAnsi="Symbol"
                                w:hint="default"/>
                        </w:rPr>
                    </xsl:otherwise>
                </xsl:choose>
                <w:pPr>
                    <w:ind w:left="{$leftIndent}"
                        w:hanging="{if (ancestor::note) then 240 else 360}">
                    </w:ind>
                </w:pPr>
                <w:lvlJc w:val="left"/>
            </w:lvl>
        </w:abstractNum>
        <w:num w:numId="{position()}">
            <w:abstractNumId w:val="{position()}"/>
        </w:num>
    </xsl:template>
    <!--<xsl:template mode="NUMBERING" match="list">
        <w:num w:numId="{position()}">
            <w:abstractNumId w:val="{position()}"/>
        </w:num>
    </xsl:template>-->
</xsl:stylesheet>