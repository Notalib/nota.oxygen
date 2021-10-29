<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:html="http://www.w3.org/1999/xhtml"
    version="3.0">
    <xsl:param name="LANGUAGE" as="xs:string?"
    	select="$OPF_DOCUMENT/opf:package/opf:metadata/dc:language/text()"/>
    <xsl:param name="OPF_DOCUMENT" as="document-node(element(opf:package))?"/>
    <xsl:param name="OPF_URI" as="xs:anyURI?" select="base-uri($OPF_DOCUMENT)"/>
    <xsl:variable name="OPF_URI_NO_ZIP" as="xs:string?"
        select="replace($OPF_URI, '^zip:', '')"/>
    <xsl:param name="OUTPUT_URI" as="xs:anyURI?"/>        
    <xsl:param name="PID" as="xs:string?"
    	select="$OPF_DOCUMENT/opf:package/opf:metadata/dc:identifier/text()"/>
    <xsl:param name="STYLESHEET_REFERENCE" as="xs:string?"
    	select="$OPF_DOCUMENT/opf:package/opf:manifest/opf:item
    			[@media-type eq 'text/css'][1]/@href"/>
    <xsl:param name="TITLE" as="xs:string?"
    	select="$OPF_DOCUMENT/opf:package/opf:metadata/dc:title/text()"/> 
</xsl:stylesheet>