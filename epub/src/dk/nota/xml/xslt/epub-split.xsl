<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns:opf="http://www.idpf.org/2007/opf"
    exclude-result-prefixes="#all"
    version="3.0">
    <xsl:import href="epub-parameters.xsl"/>
    <xsl:import href="epub-functions.xsl"/>
    <xsl:import href="epub-opf-update.xsl"/>
    <xsl:param name="ADD_TO_SPINE" as="xs:boolean" select="true()"/>
    <xsl:param name="ADDITION_REFS" as="xs:string*"
        select="/html:html/html:body/html:section/
                nota:create-document-name(.)"/>
    <xsl:param name="ADDITION_TYPES" as="xs:string*"
        select="for $i in $ADDITION_REFS return 'application/xhtml+xml'"/>
    <xsl:param name="REMOVAL_REFS" as="xs:string*" select="'concat.xhtml'"/>
    <xsl:param name="UPDATE_OPF" as="xs:boolean" select="false()"/>
    <xsl:template name="OUTPUT" as="element(document)*">
        <xsl:for-each select="/html:html/html:body/html:section">
            <xsl:variable name="documentName" as="xs:string"
                select="nota:create-document-name(.)"/>
            <xsl:message expand-text="yes">
                <nota:out>{
                    'Splitting ' || $documentName
                }</nota:out>
            </xsl:message>
            <xsl:if test="nota:get-primary-type(@epub:type) eq ''">
                <xsl:message>
                    <nota:out expand-text="yes">{
                        'The division ' || local-name() || '[' || position() ||
                        '] has no epub:type'
                    }</nota:out>
                </xsl:message>
            </xsl:if>
            <document xmlns="" 
                uri="{'zip:' || resolve-uri($documentName, $OPF_URI_NO_ZIP)}">
                <xsl:call-template name="XHTML_DOCUMENT"/>
            </document>
        </xsl:for-each>
        <xsl:if test="$UPDATE_OPF">
            <xsl:call-template name="OPF"/>
        </xsl:if>
    </xsl:template>
    <xsl:template name="ID_ATTRIBUTE">
        <xsl:if test="not(@id)">
           <xsl:attribute name="id" select="generate-id()"/>
        </xsl:if>
    </xsl:template>
    <!-- XHTML content document template -->
    <xsl:template name="XHTML_DOCUMENT">
        <html xmlns="http://www.w3.org/1999/xhtml"
            xmlns:epub="http://www.idpf.org/2007/ops"
            xmlns:nordic="http://www.mtm.se/epub/"
            epub:prefix="z3998: http://www.daisy.org/z3998/2012/vocab/structure/#">
            <xsl:choose>
                <xsl:when test="@lang or @xml:lang">
                    <xsl:attribute name="lang" select="@xml:lang"/>
                    <xsl:attribute name="xml:lang" select="@xml:lang"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="lang" select="$LANGUAGE"/>
                    <xsl:attribute name="xml:lang" select="$LANGUAGE"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:copy-of select="ancestor::html:html/html:head"/>
            <body>
                <xsl:call-template name="ID_ATTRIBUTE"/>
                <xsl:apply-templates mode="SPLIT"
                    select="@*[not(local-name() eq 'lang')]|node()"/>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="@*|node()" mode="SPLIT">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <!-- html: Fix internal references -->
    <xsl:template match="html:a[matches(@href, '^#.+$')]" mode="SPLIT">
        <xsl:variable name="referencedId" as="xs:string"
            select="substring-after(@href, '#')"/>
        <xsl:variable name="referencedElement" as="node()*"
            select="//html:*[@id = $referencedId]"/>
        <xsl:if test="not($referencedElement)">
            <xsl:message>
                <nota:out expand-text="yes">{
                    'Reference to id "' || $referencedId || '" does not resolve'
                }</nota:out>
            </xsl:message>
        </xsl:if>
        <xsl:variable name="referencedSection" as="node()*"
            select="$referencedElement/ancestor-or-self::html:section
                    [position() = last()]"/>
        <xsl:variable name="referencedFile" as="xs:string"
            select="if (ancestor::node() intersect $referencedSection) then ''
                    else nota:create-document-name($referencedSection)"/>
        <xsl:copy>
            <xsl:attribute name="href" select="$referencedFile || @href"/>
            <xsl:apply-templates mode="SPLIT"
            	select="@*[not(name() = 'href')]|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>