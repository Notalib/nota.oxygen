<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all"
    version="3.0">
    <xsl:import href="epub-parameters.xsl"/>
    <xsl:import href="epub-opf-update.xsl"/>
    <!-- To debug this stylesheet by applying it to an OPF document, uncomment
        the OPF_DOCUMENT variable and the generic root template -->
    <!--<xsl:variable name="OPF_DOCUMENT" as="document-node(element(opf:package))?"
        select="/"/>
    <xsl:template match="/">
        <xsl:call-template name="OPF"/>
    </xsl:template>-->
    <xsl:param name="ADD_TO_SPINE" as="xs:boolean" select="true()"/>
    <xsl:param name="ADDITION_REFS" as="xs:string*" select="'concat.xhtml'"/>
    <xsl:param name="ADDITION_TYPES" as="xs:string*"
        select="'application/xhtml+xml'"/>
    <xsl:param name="CONTENT_DOCUMENTS" as="document-node(element(html:html))*"
        select="for $id in $OPF_DOCUMENT/opf:package/opf:spine/opf:itemref/@idref
                return $OPF_DOCUMENT/opf:package/opf:manifest/opf:item[@id eq $id]/
                doc('zip:' || resolve-uri(@href, $OPF_URI_NO_ZIP))"/>
    <xsl:param name="ID_BASE" as="xs:string" select="'concat'"/>
    <xsl:param name="REMOVAL_REFS" as="xs:string*">
        <xsl:variable name="documentUris" as="xs:string*"
            select="$CONTENT_DOCUMENTS/replace(base-uri(), '^zip:', '')"/>
        <xsl:sequence
            select="$OPF_DOCUMENT/opf:package/opf:manifest/opf:item
            		[resolve-uri(@href, $OPF_URI_NO_ZIP) = $documentUris]/
            		@href"/>
    </xsl:param>
    <xsl:param name="UPDATE_OPF" as="xs:boolean" select="false()"/>
    <!-- Concat document template -->
    <xsl:template name="OUTPUT" as="element(document)+">
    	<document xmlns=""
    	    uri="{'zip:' || resolve-uri('concat.xhtml', $OPF_URI_NO_ZIP)}">
    		<html xmlns="http://www.w3.org/1999/xhtml"
    			xmlns:epub="http://www.idpf.org/2007/ops"
            	xmlns:nordic="http://www.mtm.se/epub/"
            	epub:prefix="z3998: http://www.daisy.org/z3998/2012/vocab/structure/#"
            	lang="{$LANGUAGE}"
            	xml:lang="{$LANGUAGE}">
	            <head>
	                <meta charset="UTF-8"/>
	                <title>
	                    <xsl:value-of select="$TITLE"/>
	                </title>
	                <meta name="dc:identifier" content="{$PID}"/>
	                <meta name="viewport" content="width=device-width"/>
	                <link rel="stylesheet" type="text/css"
	                	href="{$STYLESHEET_REFERENCE}"/>
	            </head>
	            <body>
	            	<xsl:variable name="firstPass"
	            		as="element(documents)">
	            		<documents xmlns="">
		            		<xsl:apply-templates mode="CONCAT_FIRST_PASS"
		            			select="$CONTENT_DOCUMENTS/html:html/
		            			        html:body"/>
	            		</documents>
	            	</xsl:variable>
	            	<xsl:apply-templates mode="CONCAT_SECOND_PASS"
	            		select="$firstPass/html:section"/>
	            </body>
        	</html>
    	</document>
        <xsl:if test="$UPDATE_OPF">
            <xsl:call-template name="OPF"/>
        </xsl:if>
    </xsl:template>
    <xsl:template name="ATTRIBUTE.ID">
        <xsl:if test="not(@id)">
            <xsl:variable name="id" as="xs:string" select="generate-id()"/>
            <xsl:attribute name="id" select="$id"/>
        </xsl:if>
    </xsl:template>
    <xsl:template name="ATTRIBUTE.LANG">
        <xsl:variable name="langAttribute" as="attribute()?"
            select="ancestor-or-self::html:*[@lang][1]/@lang"/>
        <xsl:variable name="xmlLangAttribute" as="attribute()?"
            select="ancestor-or-self::html:*[@xml:lang][1]/@xml:lang"/>
        <xsl:if test="not($langAttribute = $LANGUAGE)">
            <xsl:copy-of select="$langAttribute"/>
        </xsl:if>
        <xsl:if test="not($xmlLangAttribute = $LANGUAGE)">
            <xsl:copy-of select="$xmlLangAttribute"/>
        </xsl:if>
    </xsl:template>
    <xsl:template match="@*|node()" mode="CONCAT_FIRST_PASS CONCAT_SECOND_PASS">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <!-- XHTML first pass: Assign IDs and document names -->
    <xsl:template mode="CONCAT_FIRST_PASS" match="html:body">
    	<xsl:variable name="originalDocumentUri" as="xs:anyURI"
    		select="base-uri(.)"/>
        <xsl:variable name="originalDocumentName" as="xs:string"
            select="tokenize($originalDocumentUri, '/')[last()]"/>
       	<xsl:message expand-text="yes">
            <nota:out>{'Adding ' || $originalDocumentName}</nota:out>
            <nota:document>{$originalDocumentUri}</nota:document>
        </xsl:message>
        <section originalDocumentName="{$originalDocumentName}">
            <xsl:call-template name="ATTRIBUTE.ID"/>
            <xsl:call-template name="ATTRIBUTE.LANG"/>
            <xsl:apply-templates mode="CONCAT_FIRST_PASS"
                select="@*[not(local-name() eq 'lang')]|node()"/>
        </section>
    </xsl:template>
    <xsl:template mode="CONCAT_FIRST_PASS"
        match="html:section|html:h1|html:h2|html:h3|html:h4|html:h5|html:h6">
        <xsl:copy>
            <xsl:call-template name="ATTRIBUTE.ID"/>
            <xsl:apply-templates mode="CONCAT_FIRST_PASS" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template mode="CONCAT_FIRST_PASS" match="html:img[@height|@width]">
    	<xsl:next-match/>
    	<xsl:message expand-text="yes">
    		<nota:image>{
    			'zip:' || resolve-uri(@src, $OPF_URI_NO_ZIP)
    		}</nota:image>
    	</xsl:message>
    </xsl:template>
    <xsl:template mode="CONCAT_FIRST_PASS"
        match="html:img[not(@height|@width)]">
        <xsl:variable name="uri" as="xs:anyURI"
        	select="resolve-uri(@src, $OPF_URI_NO_ZIP)"/>
        <xsl:message expand-text="yes">
    		<nota:image>{'zip:' || $uri}</nota:image>
    	</xsl:message>
    	<xsl:variable name="dimensions" as="xs:integer+"
    		select="nota:get-image-size('zip:' || $uri)"/>
    	<xsl:variable name="height" as="xs:integer" select="$dimensions[2]"/>
        <xsl:variable name="width" as="xs:integer" select="$dimensions[1]"/>
    	<xsl:copy>
    	    <xsl:copy-of select="@* except (@height|@width)"/>
    	    <xsl:if test="$height ne -1 and $width ne -1">
 	        	<xsl:attribute name="height" select="$height"/>
    	    	<xsl:attribute name="width" select="$width"/>
    	    </xsl:if>
    	</xsl:copy>
	</xsl:template>
    <xsl:template mode="CONCAT_FIRST_PASS" match="html:img">
    	<xsl:next-match/>
    	<xsl:message expand-text="yes">
    		<nota:image>{
    			'zip:' || resolve-uri(@src, $OPF_URI_NO_ZIP)
    		}</nota:image>
    	</xsl:message>
    </xsl:template>
    <!-- XHTML second pass: Update references -->
    <xsl:template mode="CONCAT_SECOND_PASS" match="html:a[@href]">
        <xsl:variable name="reference" as="xs:string"
            select="if (not(contains(@href, ':'))) then (
                    if (matches(@href, '#.+$'))
                    then concat('#', substring-after(@href, '#'))
                    else concat('#', ancestor::documents/html:section[matches(
                    @originalDocumentName, current()/@href)]/@id))
                    else @href"/>
        <xsl:copy>
            <xsl:attribute name="href" select="$reference"/>
            <xsl:apply-templates mode="CONCAT_SECOND_PASS"
                select="@*[not(name() = 'href')]|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template mode="CONCAT_SECOND_PASS"
        match="html:section/@originalDocumentName"/>
</xsl:stylesheet>