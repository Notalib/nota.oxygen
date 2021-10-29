<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:nota="http://www.nota.dk/oxygen"
    exclude-result-prefixes="#all"
    version="3.0">
    <xsl:param name="OUTPUT_URI" as="xs:anyURI?"/>
   	<xsl:template name="OUTPUT" as="element(document)">
    	<document uri="{$OUTPUT_URI}">
    		<xsl:apply-templates select="."/>
    	</document>
    </xsl:template>
    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="br[nota:preceded-by-dk5(.)]"/>
    <xsl:template match="frontmatter">
        <frontmatter>
            <xsl:copy-of select="doctitle"/>
            <level depth="1" class="title">
                <levelhd depth="1" class="title"><xsl:value-of
                    select="../../head/title"/></levelhd>
                <xsl:if test="level[nota:has-classes(., 'colophon')]">
                    <div class="kolofon">
                        <xsl:copy-of
                            select="level[nota:has-classes(., 'colophon')]/*"/>
                    </div>
                </xsl:if>
            </level>
        </frontmatter>
    </xsl:template>
    <xsl:template match="meta[@name eq 'dc:identifier']/@content">
    	<xsl:attribute name="content"
    		select="replace(., '^(dk-nota-)*INSM', '$1INSE')"/>
    </xsl:template>
    <xsl:template match="span[nota:has-classes(., 'DK5')]"/>
    <xsl:template
        match="span[nota:has-classes(., ('OEE', 'OEP', 'OEL', 'typedescription'))]">
        <strong>
            <xsl:apply-templates/>
        </strong>
    </xsl:template>
    <xsl:function name="nota:has-classes" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:param name="classes" as="xs:string+"/>
        <xsl:value-of select="tokenize($n/@class, '\s+') = $classes"/>
    </xsl:function>
    <xsl:function name="nota:preceded-by-dk5" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="exists($n/preceding-sibling::node()[not(normalize-space()
                    eq '')][1]/self::span[nota:has-classes(., 'DK5')])"/>
    </xsl:function>
</xsl:stylesheet>