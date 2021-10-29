<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"
    xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
    xmlns:rel="http://schemas.openxmlformats.org/package/2006/relationships"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all"
    version="3.0">
    <xsl:import href="epub-parameters.xsl"/>
    <xsl:import href="epub-import-cat-list.xsl"/>
    <xsl:import href="epub-opf-update.xsl"/>
    <xsl:param name="ADD_TO_SPINE" as="xs:boolean" select="true()"/>
    <xsl:param name="ADDITION_REFS" as="xs:string*"
    	select="$SECOND_PASS/@name"/>
    <xsl:param name="ADDITION_TYPES" as="xs:string*"
    	select="for $r in $ADDITION_REFS
    			return 'application/xhtml+xml'"/>
    <xsl:param name="IMPORT_TO_CONCAT" as="xs:boolean"
        select="matches(document-uri(/), 'concat\.xhtml$')"/>
    <xsl:param name="SEPARATE_DOCUMENTS" as="xs:boolean" select="true()"/>
    <xsl:param name="SOURCE_URIS" as="xs:string*"/>
    <xsl:variable name="OPF_DOCUMENT_COUNT" as="xs:integer"
        select="$OPF_DOCUMENT/opf:package/opf:manifest/count(opf:item[@id =
        		//opf:itemref/@idref])"/>
    <xsl:variable name="FIRST_PASS" as="element(html:body)*">
        <xsl:variable name="inputReferences" as="element()*">
            <xsl:for-each select="$SOURCE_URIS">
                <xsl:choose>
                    <xsl:when test="matches(., '\.docx!/word/$')">
                        <nota:docx uri="{.}"/>
                    </xsl:when>
                    <xsl:when test="matches(., '\.kat$')">
                    	<nota:cat uri="{.}"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:for-each>
        </xsl:variable>
        <xsl:for-each-group group-starting-with="nota:docx"
            select="$inputReferences">
            <xsl:call-template name="DOCX.CONVERT">
                <xsl:with-param name="wordFolderUrl" as="xs:string"
                    select="current-group()[1]/@uri"/>
                <xsl:with-param name="catLists" as="document-node()*"
                    select="document(current-group()[position() gt 1]/@uri)"/>
            </xsl:call-template>
        </xsl:for-each-group>
    </xsl:variable>
    <xsl:variable name="FIRST_PASS_GROUPED" as="element(html:section)*">
        <xsl:for-each select="$FIRST_PASS">
            <xsl:call-template name="HEADINGS.GROUP"/>
        </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="SECOND_PASS" as="element(document)*">
        <xsl:for-each
            select="if ($SEPARATE_DOCUMENTS) then $FIRST_PASS_GROUPED
                    else $FIRST_PASS">
            <xsl:call-template name="XHTML.DOCUMENT">
                <xsl:with-param name="content" as="node()*"
                    select="node()"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:variable>
    <xsl:template name="HEADINGS.GROUP" as="element(html:section)*">
        <xsl:param name="sequence" as="node()*" select="node()"/>
        <xsl:variable name="minDepth" as="xs:integer?"
            select="min($sequence[self::nota:hd]/xs:integer(@depth))"/>
        <xsl:for-each-group group-starting-with="nota:hd[@depth = $minDepth]"
            select="$sequence">
            <section>
                <xsl:variable name="nextGroup" as="node()*"
                    select="current-group()[self::nota:hd][2]/((self::nota:hd|
                            following-sibling::node()) intersect
                            current-group())"/>
                <xsl:copy-of select="current-group() except $nextGroup"/>
                <xsl:if test="$nextGroup">
                    <xsl:call-template name="HEADINGS.GROUP">
                        <xsl:with-param name="sequence" as="node()+"
                            select="$nextGroup"/>
                    </xsl:call-template>
                </xsl:if>
            </section>
        </xsl:for-each-group>
    </xsl:template>
    <xsl:template name="DOCX.CONVERT" as="element(html:body)">
        <xsl:param name="wordFolderUrl" as="xs:string" select="."/>
        <xsl:param name="catLists" as="document-node()*"/>
        <xsl:variable name="document" as="document-node()?"
            select="document(concat($wordFolderUrl, 'document.xml'))"/>
        <xsl:variable name="numbering" as="document-node()?"
            select="document(concat($wordFolderUrl, 'numbering.xml'))"/>
        <xsl:variable name="relationships" as="document-node()?"
            select="document(concat($wordFolderUrl,
                    '_rels/document.xml.rels'))"/>
        <xsl:variable name="styles" as="document-node()?"
            select="document(concat($wordFolderUrl, 'styles.xml'))"/>
        <body>
            <xsl:apply-templates select="$document/w:document/w:body/node()">
                <xsl:with-param name="numbering" as="document-node()?"
                    tunnel="yes" select="$numbering"/>
                <xsl:with-param name="relationships" as="document-node()?"
                    tunnel="yes" select="$relationships"/>
                <xsl:with-param name="styles" as="document-node()?"
                    tunnel="yes" select="$styles"/>
            </xsl:apply-templates>
            <xsl:apply-templates select="$catLists"/>
        </body>
    </xsl:template>
    <xsl:template name="OUTPUT" as="element(document)+">
        <xsl:sequence select="$SECOND_PASS"/>
        <xsl:call-template name="OPF"/>
    </xsl:template>
    <xsl:template name="XHTML.DOCUMENT" as="element(document)">
        <xsl:param name="content" as="node()*"/>
        <xsl:param name="position" as="xs:integer" select="position()"/>
        <xsl:variable name="fileName" as="xs:string"
            select="concat($PID, '-', format-number($OPF_DOCUMENT_COUNT +
                    $position, '000'), '-chapter.xhtml')"/>
        <xsl:variable name="outputUri" as="xs:string"
            select="'zip:' || resolve-uri($fileName, $OPF_URI_NO_ZIP)"/>
        <document xmlns="" uri="{$outputUri}" name="{$fileName}">
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
                <body epub:type="chapter bodymatter">
                    <xsl:apply-templates mode="SECOND_PASS" select="$content"/>
                </body>
            </html>
        </document>
    </xsl:template>
    <!--  Special templates for non-namespaced nodes (assumed to be DTBook 
    nodes from included .kat files) -->
    <xsl:template
    	match="node()[namespace-uri() eq '']|@*[namespace-uri() eq '']">
    	<xsl:apply-imports/>
    </xsl:template>
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
    <xsl:template match="w:br">
        <xsl:param name="properties" as="element()*"/>
        <xsl:call-template name="DOCX.FORMATTING.CONVERT">
            <xsl:with-param name="content" as="element(html:br)">
                <br/>
            </xsl:with-param>
            <xsl:with-param name="properties" as="element()*"
                select="$properties"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="w:hyperlink">
        <xsl:param name="relationships" as="document-node()?" tunnel="yes"/>
        <xsl:variable name="relationship" as="element(rel:Relationship)?"
            select="$relationships/rel:Relationships/rel:Relationship[@Id eq
                    current()/@r:id]"/>
		<xsl:choose>
            <xsl:when test="$relationship/@TargetMode eq 'External'">
                <a href="{$relationship/@Target}">
                    <xsl:apply-templates/>
                </a>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="w:p">
        <xsl:param name="numbering" as="document-node()?" tunnel="yes"/>
        <xsl:param name="styles" as="document-node()?" tunnel="yes"/>
        <xsl:variable name="styleName" as="xs:string?"
            select="w:pPr/w:pStyle/@w:val"/>
        <xsl:variable name="styleItem" as="element(w:style)?"
            select="$styles/w:styles/w:style[@w:styleId eq $styleName]"/>
        <xsl:variable name="outlineLevel" as="xs:integer"
            select="if (w:pPr/w:outlineLvl)
                    then xs:integer(w:pPr/w:outlineLvl/@w:val)
                    else if ($styleItem/w:pPr/w:outlineLvl)
                    then xs:integer($styleItem/w:pPr/w:outlineLvl/@w:val)
                    else -1"/>
        <xsl:variable name="numPr" as="element(w:numPr)?"
            select="w:pPr/w:numPr"/>
        <xsl:choose>
            <xsl:when test="$outlineLevel gt -1">
                <nota:hd depth="{$outlineLevel + 1}">
                    <xsl:apply-templates/>
                </nota:hd>
            </xsl:when>
            <xsl:when test="$numPr">
                <xsl:variable name="depth" as="xs:integer?"
                    select="xs:integer($numPr/w:ilvl/@w:val)"/>
                <xsl:variable name="abstractNumberId" as="xs:string?"
                    select="$numbering/w:numbering/w:num[@w:numId = $numPr/
                            w:numId/@w:val]/w:abstractNumId/@w:val"/>
                <xsl:variable name="numberFormat" as="xs:string?"
                    select="$numbering/w:numbering/w:abstractNum
                            [@w:abstractNumId = $abstractNumberId]/w:lvl
                            [@w:ilvl = $depth]/w:numFmt/@w:val"/>
                <li depth="{$depth}" format="{$numberFormat}">
                    <xsl:apply-templates/>
                </li>
            </xsl:when>
            <xsl:otherwise>
                <p>
                    <xsl:apply-templates/>
                </p>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="w:p[not(.//w:r/w:t)]"/>
    <xsl:template match="w:r">
        <xsl:variable name="properties" as="element()*"
            select="w:rPr/(w:b|w:i|w:vertAlign)"/>
        <xsl:apply-templates select="w:t|w:br">
            <xsl:with-param name="properties" as="element()*"
                select="$properties"/>
        </xsl:apply-templates>
    </xsl:template>
    <xsl:template match="w:t">
        <xsl:param name="properties" as="element()*"/>
        <xsl:call-template name="DOCX.FORMATTING.CONVERT">
            <xsl:with-param name="content" as="node()" select="text()"/>
            <xsl:with-param name="properties" as="element()*"
                select="$properties"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="w:tbl">
        <table>
            <tbody>
                <xsl:apply-templates/>
            </tbody>
        </table>
    </xsl:template>
    <xsl:template match="w:tc">
        <xsl:variable name="position" as="xs:integer"
            select="position()"/>
        <xsl:variable name="colSpan" as="xs:integer"
            select="if (w:tcPr/w:gridSpan) then w:tcPr/w:gridSpan/@w:val
                    else 0"/>
        <xsl:variable name="rowSpan" as="xs:integer"
            select="if (w:tcPr/w:vMerge/@w:val eq 'restart')
                    then nota:count-spanned-rows(parent::w:tr, position())
                    else 0"/>
        <td>
            <xsl:if test="$colSpan gt 0">
                <xsl:attribute name="colspan" select="$colSpan"/>
            </xsl:if>
            <xsl:if test="$rowSpan gt 0">
                <xsl:attribute name="rowspan" select="$rowSpan"/>
            </xsl:if>
            <xsl:apply-templates/>
        </td>
    </xsl:template>
    <xsl:template match="w:tc[w:tcPr/w:vMerge[not(@w:val eq 'restart')]]"/>
    <xsl:template match="w:tr">
        <tr>
            <xsl:apply-templates select="w:tc"/>
        </tr>
    </xsl:template>
    <xsl:template mode="SECOND_PASS" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="SECOND_PASS" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template mode="SECOND_PASS" match="html:em">
        <xsl:copy>
            <xsl:for-each select="nota:expand-inline(.)">
                <xsl:apply-templates mode="SECOND_PASS"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
    <xsl:template mode="SECOND_PASS"
        match="html:em[preceding-sibling::node()[1]/self::html:em]"/>
    <xsl:template mode="SECOND_PASS" match="nota:hd/@depth"/>
    <xsl:template mode="SECOND_PASS THIRD_PASS" match="html:li">
        <xsl:variable name="listElementName" as="xs:string"
            select="if (@format eq 'bullet') then 'ul' else 'ol'"/>
        <xsl:variable name="depth" as="xs:integer" select="xs:integer(@depth)"/>
        <xsl:element name="{$listElementName}">
            <xsl:for-each
                select="self::html:li|following-sibling::html:li except
                        following-sibling::*[not(self::html:li)][1]/
                        (self::html:li|following-sibling::html:li)">
                <xsl:copy>
                    <xsl:apply-templates mode="SECOND_PASS" select="@*|node()"/>
                </xsl:copy>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>
    <xsl:template mode="SECOND_PASS" match="nota:hd">
        <xsl:element name="{concat('h', count(ancestor::html:section))}">
            <xsl:apply-templates mode="SECOND_PASS" select="node()"/>
        </xsl:element>
    </xsl:template>
    <xsl:template mode="SECOND_PASS"
        match="html:li[preceding-sibling::node()[1]/self::html:li]"/>
    <xsl:template mode="SECOND_PASS" match="html:li/@depth|html:li/@format"/>
    <xsl:template mode="SECOND_PASS" match="html:strong">
        <xsl:copy>
            <xsl:for-each select="nota:expand-inline(.)">
                <xsl:apply-templates mode="SECOND_PASS"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
    <xsl:template mode="SECOND_PASS"
        match="html:strong[preceding-sibling::node()[1]/self::html:strong]"/>
    <xsl:template mode="SECOND_PASS" match="html:td">
        <xsl:copy>
            <xsl:apply-templates mode="SECOND_PASS" select="@*"/>
            <xsl:choose>
                <xsl:when test="html:p and count(*) eq 1">
                    <xsl:apply-templates mode="SECOND_PASS"
                        select="html:p/node()"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="SECOND_PASS"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>
    <xsl:template name="DOCX.FORMATTING.CONVERT">
        <xsl:param name="content" as="node()" select="."/>
        <xsl:param name="properties" as="element()*"/>
        <xsl:variable name="property" as="element()?" select="$properties[1]"/>
        <xsl:variable name="convertedRun">
            <xsl:choose>
                <xsl:when test="$property/self::w:i">
                    <em><xsl:copy-of select="$content"/></em>
                </xsl:when>
                <xsl:when test="$property/self::w:b">
                    <strong><xsl:copy-of select="$content"/></strong>
                </xsl:when>
                <xsl:when test="$property/@w:val eq 'subscript'">
                    <sub><xsl:copy-of select="$content"/></sub>
                </xsl:when>
                <xsl:when test="$property/@w:val eq 'superscript'">
                    <sup><xsl:copy-of select="$content"/></sup>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="$content"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$properties[2]">
                <xsl:call-template name="DOCX.FORMATTING.CONVERT">
                    <xsl:with-param name="content" as="node()"
                        select="$convertedRun"/>
                    <xsl:with-param name="properties" as="element()*"
                        select="$properties[position() gt 1]"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="$convertedRun"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:function name="nota:count-spanned-rows" as="xs:integer">
        <xsl:param name="row" as="element(w:tr)"/>
        <xsl:param name="position" as="xs:integer"/>
        <xsl:variable name="nextRow" as="element(w:tr)?"
            select="$row/following-sibling::w:tr[1]"/>
        <xsl:variable name="mergedCellBelow" as="element(w:vMerge)?"
            select="$nextRow/w:tc[$position]/w:tcPr/w:vMerge
                    [not(@w:val eq 'restart')]"/>
        <xsl:value-of
            select="if ($mergedCellBelow)
                    then 1 + nota:count-spanned-rows($nextRow, $position)
                    else 1"/>
    </xsl:function>
    <xsl:function name="nota:expand-inline" as="element()*">
        <xsl:param name="element" as="element()"/>
        <xsl:variable name="elementName" as="xs:string"
            select="$element/name()"/>
        <xsl:variable name="firstFollowingSibling" as="node()?"
            select="$element/following-sibling::node()[1]"/>
        <xsl:sequence
            select="if ($firstFollowingSibling/name() eq $elementName)
                    then $element|nota:expand-inline($firstFollowingSibling)
                    else $element"/>
    </xsl:function>
</xsl:stylesheet>