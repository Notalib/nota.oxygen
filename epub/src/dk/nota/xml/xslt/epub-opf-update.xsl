<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns="http://www.idpf.org/2007/opf"
    exclude-result-prefixes="#all"
    version="3.0">
    <xsl:param name="ADD_TO_SPINE" as="xs:boolean" select="false()"/>
    <xsl:param name="ADDITION_IDS" as="xs:string*">
        <xsl:variable name="ids" as="xs:integer*"
            select="$OPF_DOCUMENT/opf:package/opf:manifest/opf:item[matches(@id,
                    '^' || $ID_BASE || '_\d+$')]/xs:integer(tokenize(@id,
                    '_')[2])"/>
        <xsl:variable name="count" as="xs:integer"
            select="if (count($ids) eq 0) then 0 else max($ids)"/>
        <xsl:sequence
            select="for $i in 1 to count($ADDITION_REFS)
            return $ID_BASE || '_' || $count + $i"/>
    </xsl:param>
    <xsl:param name="ADDITION_REFS" as="xs:string*"/>
    <xsl:param name="ADDITION_TYPES" as="xs:string*"/>
    <xsl:param name="ID_BASE" as="xs:string" select="'import'"/>
    <xsl:param name="OPF_DOCUMENT" as="document-node(element(opf:package))"
        select="/"/>
    <xsl:param name="REMOVAL_IDS" as="xs:string*"
        select="$OPF_DOCUMENT/opf:package/opf:manifest/opf:item[@href =
        		$REMOVAL_REFS]/@id"/>
    <xsl:param name="REMOVAL_REFS" as="xs:string*"/>
    <xsl:template name="OPF" as="element(document)">
    	<document xmlns="" uri="{base-uri($OPF_DOCUMENT)}">
    		<xsl:apply-templates mode="OPF" select="$OPF_DOCUMENT"/>
    	</document>
   	</xsl:template>
    <xsl:template match="@*|node()" mode="OPF">
        <xsl:copy>
            <xsl:apply-templates mode="OPF" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="opf:manifest" mode="OPF">
        <xsl:copy>
            <xsl:copy-of select="@*|opf:item[not(@id = $REMOVAL_IDS)]"/>
            <xsl:for-each select="$ADDITION_IDS">
                <xsl:variable name="index" as="xs:integer" select="position()"/>
                <item id="{.}" media-type="{$ADDITION_TYPES[$index]}"
                    href="{$ADDITION_REFS[$index]}"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="opf:spine" mode="OPF">
        <xsl:copy>
            <xsl:copy-of select="@*|opf:itemref[not(@idref = $REMOVAL_IDS)]"/>
            <xsl:if test="$ADD_TO_SPINE">
                <xsl:for-each select="$ADDITION_IDS">
                    <xsl:variable name="index" as="xs:integer"
                        select="position()"/>
                    <xsl:variable name="ref" as="xs:string"
                        select="$ADDITION_REFS[$index]"/>
                    <itemref idref="{.}">
                        <xsl:if
                            test="matches($ref, '(cover|notes)\.xhtml$')">
                            <xsl:attribute name="linear" select="'no'"/>
                        </xsl:if>
                    </itemref>
                </xsl:for-each>
            </xsl:if>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>