<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:ncx="http://www.daisy.org/z3986/2005/ncx/"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all"
    version="3.0">
    <xsl:import href="epub-parameters.xsl"/>
    <xsl:import href="epub-functions.xsl"/>
    <xsl:param name="CONTENT_DOCUMENTS" as="document-node(element(html:html))*"
        select="$OPF_DOCUMENT/opf:package/opf:manifest/opf:item[@id =
                $OPF_DOCUMENT/opf:package/opf:spine/opf:itemref/@idref]/
                doc('zip:' || resolve-uri(@href, $OPF_URI_NO_ZIP))"/>
    <!-- Content documents with all ids generated -->
    <xsl:variable name="CONTENT_DOCUMENTS_WITH_IDS" as="element(document)*">
        <xsl:for-each select="$CONTENT_DOCUMENTS">
        	<xsl:variable name="placement" as="xs:string"
        		select="nota:placement-from-type(html:html/html:body/
        				@epub:type)"/>
        	<xsl:variable name="type" as="xs:string"
                select="nota:get-primary-type(html:html/html:body/
                		@epub:type)"/>
       		<xsl:variable name="uri" as="xs:anyURI" select="base-uri()"/>
       		<xsl:variable name="name" as="xs:string"
       			select="tokenize($uri, '/')[last()]"/>
       		<xsl:message expand-text="yes">
       			<nota:out>{
       				'Generating ids for ' || $name
       			}</nota:out>
       			<nota:systemid>{
       				$uri
       			}</nota:systemid>
       		</xsl:message>
            <document xmlns="" name="{$name}" placement="{$placement}"
            	position="{position()}" type="{$type}" uri="{$uri}">
                <xsl:apply-templates mode="GENERATE_IDS"/>
            </document>
        </xsl:for-each> 
    </xsl:variable>
    <xsl:variable name="NAVIGATION" as="element(navigation)">
        <navigation xmlns="">
            <xsl:for-each-group select="$CONTENT_DOCUMENTS_WITH_IDS"
                group-adjacent="@placement">
                <xsl:element name="{current-grouping-key()}">
                    <xsl:variable name="documents" as="element(document)*"
                        select="nota:group-notes(current-group())"/>
                    <xsl:for-each-group select="$documents"
                        group-starting-with="document[@type eq 'part']">
                        <xsl:choose>
                            <xsl:when test="current-group()[1]/@type eq 'part'">
                                <document xmlns="">
                                    <xsl:copy-of
                                        select="current-group()[1]/(@position|
                                                @type|document)|
                                                current-group()[position() gt
                                                1]"/>
                                </document>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:copy-of select="current-group()"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each-group>
                </xsl:element>
            </xsl:for-each-group>
        </navigation>
    </xsl:variable>
    <!-- NCX navigation document: Based on XHTML ditto -->
    <xsl:variable name="NCX_NAVIGATION_DOCUMENT" as="element(ncx:ncx)">
        <xsl:variable name="depth" as="xs:integer"
            select="xs:integer(max($XHTML_NAVIGATION_DOCUMENT/html:body/
                    html:nav[@epub:type eq 'toc']//html:li[not(html:ol)]/
                    count(ancestor::html:ol)))"/>
        <xsl:variable name="pageCount" as="xs:integer"
            select="count($PAGE_NUMBERS)"/>
        <xsl:variable name="lastPage" as="xs:string"
            select="$PAGE_NUMBERS[last()]/@title"/>
        <ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
            <head>
                <meta content="{$PID}" name="dtb:uid"/>
                <meta content="{$depth}" name="dtb:depth"/>
                <xsl:if test="$PAGE_NUMBERS">
                    <meta content="{$pageCount}" name="dtb:totalPageCount"/>
                    <meta content="{$lastPage}" name="dtb:maxPageNumber"/>
                </xsl:if>
            </head>
            <docTitle>
                <text>
                    <xsl:value-of select="$TITLE"/>
                </text>
            </docTitle>
            <navMap>
                <navLabel>
                    <text>Indhold</text>
                </navLabel>
                <xsl:apply-templates mode="GENERATE_NCX_HEADINGS"
                    select="$XHTML_NAVIGATION_DOCUMENT//html:nav[@epub:type eq
                            'toc']/html:ol/html:li"/>
            </navMap>
            <xsl:if test="$PAGE_NUMBERS">
                <pageList>
                    <navLabel>
                        <text>Liste over sider</text>
                    </navLabel>
                    <xsl:apply-templates mode="GENERATE_NCX_PAGES"
                        select="$PAGE_NUMBERS"/>
                </pageList>
            </xsl:if>
        </ncx>
    </xsl:variable>
    <!-- Page numbers -->
    <xsl:variable name="PAGE_NUMBERS" as="element()*"
        select="$CONTENT_DOCUMENTS_WITH_IDS//html:*[@epub:type eq 'pagebreak']"/>
    <!-- Total number of TOC items: Dumb thing needed for NCX -->
    <xsl:variable name="TOC_ENTRY_COUNT" as="xs:integer"
        select="count($XHTML_NAVIGATION_DOCUMENT//html:nav[@epub:type eq
                'toc']//html:li)"/>
    <!-- XHTML navigation document -->
    <xsl:variable name="XHTML_NAVIGATION_DOCUMENT" as="element(html:html)">
        <html xmlns="http://www.w3.org/1999/xhtml"
            xmlns:epub="http://www.idpf.org/2007/ops"
            xmlns:nordic="http://www.mtm.se/epub/"
            epub:prefix="z3998: http://www.daisy.org/z3998/2012/vocab/structure/#"
            lang="da"
            xml:lang="da">
            <head>
                <meta charset="UTF-8"/>
                <title>
                    <xsl:value-of select="$TITLE"/>
                </title>
                <meta name="dc:identifier" content="{$PID}"/>
                <meta name="viewport" content="width=device-width"/>
                <xsl:for-each
                    select="$OPF_DOCUMENT/opf:package/opf:manifest/opf:item
                            [@media-type eq 'text/css']">
                    <link rel="stylesheet" type="text/css" href="{@href}"/>
                </xsl:for-each>
            </head>
            <body>
                <nav epub:type="toc">
                    <h1 lang="da" xml:lang="da">Indhold</h1>
                    <ol class="list-style-type-none">
                        <xsl:apply-templates mode="GENERATE_NAV_HEADINGS"
                            select="$NAVIGATION/*/document"/>
                    </ol>
                </nav>
                <xsl:if test="$PAGE_NUMBERS">
                    <nav epub:type="page-list">
                        <h1 lang="da" xml:lang="da">Liste over sider</h1>
                        <ol class="list-style-type-none">
                            <xsl:apply-templates mode="GENERATE_NAV_PAGES"
                                select="$PAGE_NUMBERS"/>
                        </ol>
                    </nav>
                </xsl:if>
            </body>
        </html>
    </xsl:variable>
    <xsl:template name="OUTPUT" as="element(document)+">
        <xsl:copy-of select="$CONTENT_DOCUMENTS_WITH_IDS"/>
        <document xmlns=""
            uri="{'zip:' || resolve-uri('nav.xhtml', $OPF_URI_NO_ZIP)}">
            <xsl:copy-of select="$XHTML_NAVIGATION_DOCUMENT"/>
        </document>
        <document xmlns=""
            uri="{'zip:' || resolve-uri('nav.ncx', $OPF_URI_NO_ZIP)}">
            <xsl:copy-of select="$NCX_NAVIGATION_DOCUMENT"/>
        </document>
    </xsl:template>
    <!-- XHTML: Generate ids -->
    <xsl:template mode="GENERATE_IDS" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="GENERATE_IDS" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template mode="GENERATE_IDS"
        match="html:body|html:section|html:*[matches(local-name(), 'h\d')]">
        <xsl:copy>
            <xsl:if test="not(@id)">
                <xsl:variable name="id" as="xs:string" select="generate-id()"/>
                <xsl:message expand-text="yes">
                    <nota:out>{
                        'Assigning id "' || $id || '" to element ' ||
                        local-name()
                    }</nota:out>
                </xsl:message>
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:apply-templates mode="GENERATE_IDS" select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    <!-- XHTML navigation -->
    <xsl:template mode="GENERATE_NAV_HEADINGS" match="document">
        <xsl:variable name="position" as="xs:integer"
            select="xs:integer(@position)"/>
        <xsl:variable name="documentNav" as="element()+">
            <xsl:apply-templates mode="GENERATE_NAV_HEADINGS"
                select="$CONTENT_DOCUMENTS_WITH_IDS[$position]/html:html/
                        html:body"/>
        </xsl:variable>
        <li>
            <xsl:copy-of select="$documentNav[self::html:a]"/>
            <xsl:if test="$documentNav[self::html:ol]|document">
                <ol class="list-style-type-none">
                    <xsl:copy-of
                        select="$documentNav[self::html:ol]/html:li"/>
                    <xsl:apply-templates mode="GENERATE_NAV_HEADINGS"
                        select="document"/>
                </ol>
            </xsl:if>
        </li>
    </xsl:template>
    <xsl:template mode="GENERATE_NAV_HEADINGS"
        match="html:body|html:section">
        <xsl:variable name="heading" as="element()?"
            select="html:*[matches(local-name(), 'h\d')]"/>
        <xsl:variable name="headingText" as="xs:string"
            select="if ($heading) then normalize-space(string-join($heading
                    //text()[not(ancestor::html:a)], ''))
                    else nota:heading-from-type-or-class(@epub:type, @class)"/>
        <xsl:variable name="documentName" as="xs:string"
            select="ancestor::document[1]/@name"/>
        <xsl:variable name="id" as="xs:string"
            select="if ($heading) then $heading/@id else @id"/>
        <a href="{$documentName || '#' || $id}">
            <xsl:value-of select="normalize-space($headingText)"/>
        </a>
        <xsl:variable name="subSections" as="node()*"
            select="html:section[not(matches(@epub:type,
                    '^z3998:(poem|verse)$'))]"/>
        <xsl:if test="$subSections">
            <ol class="list-style-type-none">
                <xsl:for-each select="$subSections">
                    <li>
                        <xsl:apply-templates mode="GENERATE_NAV_HEADINGS"
                            select="."/>
                    </li>
                </xsl:for-each>
            </ol>
        </xsl:if>
    </xsl:template>
    <xsl:template mode="GENERATE_NAV_PAGES"
        match="html:*[@epub:type = 'pagebreak']">
        <li>
            <a href="{ancestor::document[1]/@name || '#' || @id}">
                <xsl:value-of select="@title"/>
            </a>
        </li>
    </xsl:template>
    <!-- NCX navigation -->
    <xsl:template mode="GENERATE_NCX_HEADINGS" match="html:li">
        <xsl:variable name="count" as="xs:integer"
            select="count((preceding::html:li|ancestor-or-self::html:li)
                    intersect ancestor::html:nav//html:li)"/>
        <navPoint xmlns="http://www.daisy.org/z3986/2005/ncx/"
            id="{'navPoint-' || $count}" playOrder="{$count}">
            <navLabel>
                <text>
                    <xsl:value-of select="html:a/text()"/>
                </text>
            </navLabel>
            <content src="{html:a/@href}"/>
            <xsl:if test="html:ol">
                <xsl:apply-templates mode="GENERATE_NCX_HEADINGS"
                    select="html:ol/html:li"/>
            </xsl:if>
        </navPoint>
    </xsl:template>
    <xsl:template mode="GENERATE_NCX_PAGES"
        match="html:*[@epub:type eq 'pagebreak']">
        <xsl:variable name="count" as="xs:integer" select="position()"/>
        <xsl:variable name="type" as="xs:string"
            select="if (@class = 'page-front') then 'front'
                    else if (@class = 'page-special') then 'special'
                    else 'normal'"/>
        <xsl:variable name="documentName" as="xs:string"
            select="ancestor::document[1]/@name"/>
        <pageTarget xmlns="http://www.daisy.org/z3986/2005/ncx/"
            id="{'pageTarget-' || $count}"
            playOrder="{$TOC_ENTRY_COUNT + $count}" type="{$type}">
            <navLabel>
                <text>
                    <xsl:value-of select="@title"/>
                </text>
            </navLabel>
            <content src="{$documentName || '#' || @id}"/>
        </pageTarget>
    </xsl:template>
    <xsl:function name="nota:get-note-ids" as="xs:string*">
        <xsl:param name="n" as="element()"/>
        <xsl:sequence
            select="$n//html:*[nota:has-epub-types(., ('footnote',
                    'rearnote'))]/@id"/>
    </xsl:function>
    <xsl:function name="nota:group-notes" as="element()*">
        <xsl:param name="n" as="element()+"/>
        <xsl:variable name="currentDocument" as="element()" select="$n[1]"/>
        <xsl:variable name="notesDocuments" as="element()*">
            <xsl:if test="$n[2][nota:is-notes-document(.)]">
                <xsl:sequence select="$n[2]|$n[3][nota:is-notes-document(.)]"/>
            </xsl:if>
        </xsl:variable>
        <xsl:variable name="referringDocuments" as="xs:string*">
            <xsl:variable name="noteIds" as="xs:string*"
                select="for $i in $notesDocuments return
                        (for $j in nota:get-note-ids($i)
                        return $i/@name || '#' || $j)"/>
            <xsl:sequence
                select="$CONTENT_DOCUMENTS_WITH_IDS/html:html[.//html:a[@href =
                        $noteIds]]/@name"/>
        </xsl:variable>
        <xsl:variable name="notesBelongToCurrentDocument" as="xs:boolean"
            select="count($referringDocuments) eq 1 and
                    $referringDocuments = $currentDocument/@name"/>
        <xsl:variable name="group" as="element()">
            <xsl:choose>
                <xsl:when test="$notesBelongToCurrentDocument">
                    <document xmlns="" position="{$currentDocument/@position}"
                        type="{$currentDocument/@type}">
                        <xsl:for-each select="$notesDocuments">
                            <xsl:message select="@name"/>
                            <document xmlns="" position="{@position}"
                            	type="{@type}"/>
                        </xsl:for-each>
                    </document>
                </xsl:when>
                <xsl:otherwise>
                    <document xmlns="" position="{$currentDocument/@position}"
                        type="{$currentDocument/@type}"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="nextSequence" as="element()*"
            select="$n[position() gt 1 + $group/count(document)]"/>
        <xsl:sequence
            select="if ($nextSequence)
                    then $group|nota:group-notes($nextSequence)
                    else $group"/>
    </xsl:function>
</xsl:stylesheet>