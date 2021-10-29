<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:nota="http://www.nota.dk/oxygen"
    version="3.0">
    <xsl:import href="epub-parameters.xsl"/>
    <xsl:function name="nota:create-document-name" as="xs:string"
        expand-text="yes">
        <xsl:param name="section" as="element(html:section)"/>
        <xsl:variable name="position" as="xs:integer"
            select="count($section/preceding-sibling::html:section) + 1"/>
        <xsl:variable name="number" as="xs:string"
            select="format-number($position, '000')"/>
        <xsl:variable name="type" as="xs:string*"
            select="nota:get-primary-type($section/@epub:type)"/>{
        $PID || '-' || $number || '-' || $type || '.xhtml'
    }</xsl:function>
    <xsl:function name="nota:get-primary-type" as="xs:string"
        expand-text="yes">
        <xsl:param name="typeString" as="xs:string"/>
        <xsl:variable name="types"
            select="tokenize(normalize-space($typeString), '\s+')"/>{
        if (count($types) gt 1)
        then $types[not(matches(., '(back|body|front)matter'))][1]
        else $types[1]
    }</xsl:function>
    <xsl:function name="nota:has-epub-types" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:param name="types" as="xs:string+"/>
        <xsl:value-of select="tokenize($n/@epub:type, '\s+') = $types"/>
    </xsl:function>
    <xsl:function name="nota:heading-from-type-or-class" as="xs:string"
        expand-text="yes">
        <xsl:param name="type" as="attribute(epub:type)?"/>
        <xsl:param name="class" as="attribute(class)?"/>
        <xsl:variable name="types" as="xs:string*"
            select="tokenize($type, '\s+')"/>
        <xsl:variable name="classes" as="xs:string*"
            select="tokenize($class, '\s+')"/>{
        if ($types = 'cover') then 'Omslag'
        else if ($types = 'colophon') then 'Kolofon'
        else if ($types = 'footnotes') then 'Fodnoter'
        else if ($types = 'rearnotes') then 'Slutnoter'
        else if ($classes = 'frontcover') then 'Forside'
        else if ($classes = 'rearcover') then 'Bagside'
        else if ($classes = 'leftflap') then 'Venstre flap'
        else if ($classes = 'rightflap') then 'Højre flap'
        else '[***]'
    }</xsl:function>
    <xsl:function name="nota:is-notes-document" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:value-of
            select="nota:has-epub-types($n/html:html/html:body, ('footnotes',
            'rearnotes'))"/>
    </xsl:function>
    <xsl:function name="nota:placement-from-type" as="xs:string"
        expand-text="yes">
        <xsl:param name="typeString" as="xs:string"/>{
        (tokenize($typeString, '\s+')[. = ('cover', 'frontmatter',
        'bodymatter', 'backmatter')][1], 'unknown')[1]
    }</xsl:function>
</xsl:stylesheet>