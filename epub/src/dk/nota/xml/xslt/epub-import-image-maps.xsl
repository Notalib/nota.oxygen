<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all"
    version="2.0">
    <xsl:output method="xml" omit-xml-declaration="yes"/>
    <xsl:param name="INSERTION_DEPTH" as="xs:integer" select="2"/>
    <xsl:param name="ORIGINAL_URI" as="xs:anyURI?"/>
    <xsl:template match="/html:html">
    	<xsl:for-each select="html:body">
    		<section>
    			<xsl:apply-templates select="preceding-sibling::html:head[1]"/>
    			<xsl:apply-templates select="."/>
    		</section>
    	</xsl:for-each>
    </xsl:template>
    <xsl:template match="html:area">
        <xsl:variable name="coordinates" as="xs:integer+"
            select="for $i in tokenize(@coords, ',')
                    return $i cast as xs:integer"/>
        <xsl:variable name="height" as="xs:integer"
            select="$coordinates[4] - $coordinates[2]"/>
        <xsl:variable name="width" as="xs:integer"
            select="$coordinates[3] - $coordinates[1]"/>
        <xsl:variable name="style" as="xs:string"
            select="concat('left:', $coordinates[1], 'px;top:', $coordinates[2],
                    'px;width:', $width, 'px;height:', $height, 'px;')"/>
        <div class="area" style="{$style}"/>
    </xsl:template>
    <xsl:template match="html:body">
        <xsl:apply-templates select="html:map"/>
    </xsl:template>
    <xsl:template match="html:head">
        <xsl:variable name="page" as="xs:string?"
            select="replace(html:title/text(), '^image0+', '')"/>
        <xsl:element name="{concat('h', $INSERTION_DEPTH + 1)}">
        	<xsl:value-of
        		select="if ($page eq 'forsiden') then 'Forsiden'
        				else concat('Side ', $page)"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="html:map">
        <xsl:variable name="image" as="element(html:img)"
            select="following-sibling::html:img"/>
        <xsl:variable name="page" as="xs:string?"
            select="if (preceding::html:head[1]/html:title/text() eq 'forside')
            		then 'Forside'
            		else concat('Side ', replace(preceding::html:head[1]/
            			html:title/text(), '^image0+', ''))"/>
        <xsl:variable name="style" as="xs:string"
            select="concat('height:', $image/@height, 'px;width:',
                    $image/@width, 'px;')"/>
        <xsl:variable name="title" as="xs:string"
            select="preceding::html:head[1]/html:title/text()"/>
        <xsl:message>
            <nota:image>
                <xsl:value-of
                	select="resolve-uri($image/@src, $ORIGINAL_URI)"/>
            </nota:image>
       	</xsl:message>
        <div id="{$title}" class="page" style="{$style}">
            <img src="{concat('images/', $image/@src)}" class="page"
            	style="{$style}" alt="{$page}"/>
            <xsl:apply-templates select="html:area[not(@shape eq 'default')]"/>
        </div>            
    </xsl:template>
</xsl:stylesheet>