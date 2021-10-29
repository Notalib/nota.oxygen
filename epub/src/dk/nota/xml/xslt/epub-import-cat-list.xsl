<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all"
    version="3.0">
    <xsl:param name="DEPTH" as="xs:integer" select="2"/>
    <xsl:template match="/dtbook">
        <xsl:choose>
            <xsl:when
                test="book/frontmatter/doctitle/matches(., '^Top 10')">
                <ol>
                    <xsl:apply-templates mode="CONVERT_TO_LIST"
                        select="book/frontmatter/level/div"/>
                </ol>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each-group group-by="nota:get-classification(.)"
                    select="book/frontmatter/level/div">
                    <nota:hd depth="{$DEPTH}">
                        <xsl:value-of select="current-grouping-key()"/>
                    </nota:hd>
                    <xsl:apply-templates select="current-group()"/>
                </xsl:for-each-group>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="text()|@*">
        <xsl:copy/>
    </xsl:template>
    <xsl:template match="*">
    	<xsl:element name="{local-name()}"
    		namespace="http://www.w3.org/1999/xhtml">
    		<xsl:apply-templates select="node()|@*"/>	
   		</xsl:element>
    </xsl:template>
    <xsl:template match="@id"/>
    <xsl:template match="span[@class eq 'typedescription']">
        <xsl:variable name="id" as="xs:string?"
            select="following-sibling::*[1]/self::span
                    [@class eq 'masternummer']/text()"/>
    	<span class="typedescription">
    		<xsl:choose>
    		    <xsl:when test="$id">
    		        <a href="{'https://nota.dk/bibliotek/bogid/' || $id}"
    		            class="link">
    		            <xsl:value-of select=". || $id"/>
    		        </a>
    		    </xsl:when>
    		    <xsl:otherwise>
    		        <xsl:value-of select="."/>
    		    </xsl:otherwise>
    		</xsl:choose>
    	</span>
    </xsl:template>
    <xsl:template match="span[@class = ('OEE', 'OEL', 'OEP')]">
        <xsl:variable name="id" as="xs:string"
            select="replace(text(), '.+?(\d+)$', '$1')"/>
        <span>
            <xsl:copy-of select="@class"/>
            <a href="{'https://nota.dk/bibliotek/bogid/' || $id}" class="link">
                <xsl:value-of select="."/>
            </a>
        </span>
    </xsl:template>
    <xsl:template match="span[@class eq 'masternummer'][nota:follows-type(.)]"/>
    <xsl:template match="span[@class eq 'playingtime']">
        <xsl:variable name="hoursString" as="xs:string"
            select="replace(text(), '^Spilletid: (\d+).*?$', '$1')"/>
        <xsl:variable name="minutesString" as="xs:string"
            select="replace(text(), '^.*?(\d+) minutter\. $', '$1')"/>
        <xsl:variable name="hours" as="xs:integer"
            select="if (matches($hoursString, '^[0-9]+$'))
                    then xs:integer($hoursString)
                    else -1"/>
        <xsl:variable name="minutes" as="xs:integer"
            select="if (matches($minutesString, '^[0-9]+$'))
                    then xs:integer($minutesString)
                    else -1"/>
        <span class="playingtime">
            <xsl:value-of
                select="if ($hours gt -1 and $minutes gt -1) then
                        'Spilletid: ' || $hours || (if ($hours eq 1) then
                        ' time, ' else ' timer, ') || $minutes || (if ($minutes
                        eq 1)  then ' minut.' else ' minutter.')
                        else text()"/>
        </span>
    </xsl:template>
    <xsl:template mode="CONVERT_TO_LIST" match="div[@class eq 'katalogpost']">
        <li>
            <xsl:apply-templates select="p/node()"/>
        </li>
    </xsl:template>
    <xsl:function name="nota:get-classification" as="xs:string">
        <xsl:param name="n" as="element()"/>
        <xsl:value-of
            select="if ($n/p/span[@class eq 'DK5']/matches(text(), '^99'))
                    then 'Erindringer og biografier'
                    else if ($n/p/span[@class eq 'DK5']/matches(text(),
                    '^8[2-5,7-8]')) then 'Skønlitteratur'
                    else if ($n/p/span[@class eq 'DK5']) then 'Faglitteratur'
                    else 'Skønlitteratur'"/>
    </xsl:function>
    <xsl:function name="nota:follows-type" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:value-of
            select="$n/preceding-sibling::*[1]/self::span/@class eq
                    'typedescription'"/>
    </xsl:function>
</xsl:stylesheet>