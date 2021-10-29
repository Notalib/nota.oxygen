<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:nota="http://www.nota.dk/dtbook2docx"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    exclude-result-prefixes="nota xs"
    version="2.0">
    <xsl:variable name="BLOCK_ELEMENT_PATTERN" as="xs:string"
        select="'^(caption|dd|div|dl|dt|img|imggroup|levelhd|li|line|list|note|p|prodnote|table|td|th|tr)$'"/>
    <xsl:function name="nota:convert-number-format" as="xs:string">
        <xsl:param name="s" as="xs:string*"/>
        <xsl:value-of
            select="if ($s eq 'a') then 'lowerLetter'
                    else if ($s eq 'U') then 'upperLetter'
                    else if ($s eq 'i') then 'lowerRoman'
                    else if ($s eq 'X') then 'upperRoman'
                    else 'decimal'"/>
    </xsl:function>
    <xsl:function name="nota:determine-indent-left" as="xs:integer">
        <xsl:param name="n" as="node()*"/>
        <xsl:variable name="indentFactor" as="xs:integer"
            select="if ($n/ancestor::note) then 240 else 360"/>
        <xsl:variable name="indentingAncestors" as="node()*"
            select="$n/(ancestor::list|ancestor::blockquote|ancestor::dd|
                        ancestor::div[nota:has-class(., 'blockquote')])"/>
        <xsl:value-of
            select="if ($n/ancestor::table)
                    then $indentFactor * count($indentingAncestors intersect
                        $n/ancestor::table[1]//*)
                    else $indentFactor * count($indentingAncestors)"/>
    </xsl:function>
    <xsl:function name="nota:determine-indent-right" as="xs:integer">
        <xsl:param name="n" as="node()*"/>
        <xsl:variable name="indentFactor" as="xs:integer"
            select="if ($n/ancestor::note) then 240 else 360"/>
        <xsl:variable name="indentingAncestors" as="node()*"
            select="$n/(ancestor::blockquote|
                        ancestor::div[nota:has-class(., 'blockquote')])"/>
        <xsl:value-of
            select="if ($n/ancestor::table)
                    then $indentFactor * count($indentingAncestors intersect
                        $n/ancestor::table[1]//*)
                    else $indentFactor * count($indentingAncestors)"/>
    </xsl:function>
    <xsl:function name="nota:get-ancestors-within-block" as="node()*">
        <xsl:param name="n" as="node()*"/>
        <xsl:param name="ancestorName" as="xs:string"/>
        <xsl:sequence
            select="$n/ancestor::*[local-name() eq $ancestorName]
                    intersect $n/ancestor::*[nota:is-block-element(.)][1]//*"/>
    </xsl:function>
    <xsl:function name="nota:get-file-name-from-path" as="xs:string">
        <xsl:param name="path" as="xs:string"/>
        <xsl:value-of
            select="tokenize($path, '/')[position() = last()]"/>
    </xsl:function>
    <xsl:function name="nota:get-node-lang" as="xs:string">
        <xsl:param name="n" as="node()*"/>
        <xsl:value-of
            select="$n/ancestor-or-self::*[@lang][not(@lang eq 'xx')][1]/@lang"/>
    </xsl:function>
    <xsl:function name="nota:get-paragraph-style" as="xs:string">
        <xsl:param name="n" as="node()*"/>
        <xsl:choose>
            <xsl:when test="$n/ancestor::note">
                <xsl:value-of select="'NoteText'"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="'Normal'"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    <xsl:function name="nota:has-block-elements" as="xs:boolean">
        <xsl:param name="n" as="node()*"/>
        <xsl:value-of
            select="exists($n/*[matches(local-name(),
                        $BLOCK_ELEMENT_PATTERN)])"/>
    </xsl:function>
    <xsl:function name="nota:has-class" as="xs:boolean">
        <xsl:param name="n" as="node()*"/>
        <xsl:param name="classes" as="xs:string+"/>
        <xsl:value-of
            select="tokenize($n/@class, '\s+') = $classes"/>
    </xsl:function>
    <xsl:function name="nota:is-block-element" as="xs:boolean">
        <xsl:param name="n" as="node()*"/>
        <xsl:value-of
            select="$n/matches(local-name(), $BLOCK_ELEMENT_PATTERN)"/>
    </xsl:function>
    <xsl:function name="nota:is-endnote" as="xs:boolean">
        <xsl:param name="n" as="node()*"/>
        <xsl:value-of
            select="nota:has-class($n, ('endnote', 'rearnote'))"/>
    </xsl:function>
    <xsl:function name="nota:is-footnote" as="xs:boolean">
        <xsl:param name="n" as="node()*"/>
        <xsl:value-of
            select="not(nota:is-endnote($n) or nota:is-table-note($n))"/>
    </xsl:function>
    <xsl:function name="nota:is-last-node-within-block" as="xs:boolean">
        <xsl:param name="n" as="node()*"/>
        <xsl:value-of
            select="not(exists($n/following::node() intersect
                        $n/ancestor::*[nota:is-block-element(.)][1]//node()))"/>
    </xsl:function>
    <xsl:function name="nota:is-table-note" as="xs:boolean">
        <xsl:param name="n" as="node()*"/>
        <xsl:value-of
            select="exists($n/preceding-sibling::*[not(self::note)][1]/
                        self::table|$n/ancestor::div[nota:has-class(.,
                        'tablegroup')])"/>
    </xsl:function>
    <xsl:function name="nota:is-whitespace" as="xs:boolean">
        <xsl:param name="n" as="node()*"/>
        <xsl:value-of select="normalize-space($n) eq ''"/>
    </xsl:function>
</xsl:stylesheet>