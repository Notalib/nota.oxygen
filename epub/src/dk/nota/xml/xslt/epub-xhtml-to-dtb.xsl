<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:nota="http://www.nota.dk/oxygen"
    xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:saxon="http://saxon.sf.net/"
    xmlns:html="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all"
    version="3.0">
    <xsl:import href="epub-parameters.xsl"/>
    <xsl:param name="CONCAT_DOCUMENT" as="element(html:html)?"
    	select="/html:html"/>
    <xsl:param name="NAV_DOCUMENT" as="document-node(element(html:html))?"
        select="doc('zip:' || resolve-uri('nav.xhtml', $OPF_URI_NO_ZIP))"/>
    <xsl:variable name="INLINE_ELEMENT_NAMES" as="xs:string+"
        select="('b', 'big', 'i', 'small', 'tt', 'abbr', 'acronym', 'cite',
                'code', 'dfn', 'em', 'kbd', 'strong', 'samp', 'time', 'var',
                'a', 'bdo', 'br', 'img', 'map', 'object', 'q', 'script', 'span',
                'sub', 'sup', 'button', 'input', 'label', 'select', 'textarea')"/>
    <xsl:template name="OUTPUT" as="element(document)">
        <xsl:variable name="frontmatter" as="element(level)*">
            <xsl:variable name="coverSection" as="element(html:section)?"
                select="$CONCAT_DOCUMENT/html:body/html:section
                        [nota:has-epub-types(., 'cover')]"/>
            <xsl:apply-templates select="$coverSection"/>    
            <xsl:call-template name="LEVELS.GROUP">
                <xsl:with-param name="sections" as="element(html:section)*"
                    select="$CONCAT_DOCUMENT/html:body/html:section
                            [nota:has-epub-types(., 'frontmatter')]
                            except $coverSection"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="bodymatter" as="element(level)*">
            <xsl:call-template name="LEVELS.GROUP">
                <xsl:with-param name="sections" as="element(html:section)*"
                    select="$CONCAT_DOCUMENT/html:body/html:section
                            [nota:has-epub-types(., 'bodymatter')]"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="rearmatter" as="element(level)*">
            <xsl:call-template name="LEVELS.GROUP">
                <xsl:with-param name="sections" as="element(html:section)*"
                    select="$CONCAT_DOCUMENT/html:body/html:section
                            [nota:has-epub-types(., 'backmatter')]"/>
            </xsl:call-template>
        </xsl:variable>
        <document uri="{$OUTPUT_URI}">
        	<dtbook version="1.1.0">
	            <head>
	                <title>
	                    <xsl:value-of select="$TITLE"/>
	                </title>
	                <meta name="prod:AutoBrailleReady" content="no"/>
	                <meta name="dc:identifier" content="{$PID}"/>
	                <meta name="dc:type" content="Format: DTBook"/>
	                <xsl:apply-templates
	                    select="$OPF_DOCUMENT/opf:package/opf:metadata/dc:*"/>
	            </head>
	            <book lang="{$LANGUAGE}">
	                <frontmatter>
	                    <doctitle>
	                        <xsl:value-of select="$TITLE"/>
	                    </doctitle>
	                    <xsl:copy-of select="$frontmatter"/>
	                </frontmatter>
	                <bodymatter>
	                    <xsl:copy-of select="$bodymatter"/>
	                </bodymatter>
	                <xsl:if test="$rearmatter">
	                    <rearmatter>
	                        <xsl:copy-of select="$rearmatter"/>
	                    </rearmatter>
	                </xsl:if>
	            </book>
        	</dtbook>
        </document>
        <xsl:variable name="text" as="xs:string"
            select="string-join($frontmatter//text()|$bodymatter//text()|
                    $rearmatter//text(), '')"/>
        <xsl:for-each select="distinct-values(string-to-codepoints($text))">
            <xsl:if test=". gt 256">
                <xsl:message expand-text="yes">
                    <nota:out>{
                        'WARNING: Character ' || codepoints-to-string(.) ||
                        ' is beyond Latin-1 Supplement'
                    }</nota:out>
                </xsl:message>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="node()">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="text()" priority="1">
        <xsl:copy/>
    </xsl:template>
    <!-- OPF -->
    <!-- Convert metadata elements -->
    <xsl:template match="dc:*">
        <meta name="{name()}" content="{text()}"/>
    </xsl:template>
    <xsl:template match="dc:format"/>
    <xsl:template match="dc:identifier"/>
    <xsl:template match="dc:source">
        <meta name="{name()}" content="{replace(text(), '^urn:isbn:|-', '')}"/>
    </xsl:template>
    <!-- XHTML -->
    <!-- Named templates for attributes -->
    <xsl:template name="ATTRIBUTES.COMBINE" as="attribute()*">
        <xsl:param name="primarySet" as="attribute()*"/>
        <xsl:param name="secondarySet" as="attribute()*"/>
        <xsl:if test="($primarySet|$secondarySet)[name() eq 'class']">
            <xsl:attribute name="class"
                select="distinct-values(($primarySet|$secondarySet)
                        [name() eq 'class']/tokenize(., '\s+'))"/>
        </xsl:if>
        <xsl:copy-of
            select="$primarySet[name() ne 'class']|$secondarySet[not(name() =
                    ('class', $primarySet/name()))]"/>
    </xsl:template>
    <xsl:template name="ATTRIBUTES.GENERIC" as="attribute()*">
        <xsl:param name="context" as="element()" select="."/>
        <xsl:copy-of select="$context/@id"/>
        <xsl:call-template name="ATTRIBUTES.LANGUAGE">
            <xsl:with-param name="context" as="node()" select="$context"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template name="ATTRIBUTES.GENERIC.WITH_CLASS" as="attribute()*">
        <xsl:param name="context" as="element()" select="."/>
        <xsl:param name="classesToAdd" as="xs:string*"/>
        <xsl:param name="classesToDiscard" as="xs:string*"/>
        <xsl:variable name="classes" as="xs:string*"
            select="tokenize($context/@class, '\s+')[not(. = $classesToDiscard)],
                    $classesToAdd"/>
        <xsl:if test="count($classes[normalize-space() ne '']) gt 0">
            <xsl:attribute name="class"
                select="distinct-values($classes[normalize-space() ne ''])"/>
        </xsl:if>
        <xsl:call-template name="ATTRIBUTES.GENERIC">
            <xsl:with-param name="context" as="node()" select="$context"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template name="ATTRIBUTES.IMAGE" as="attribute()*">
        <xsl:param name="context" as="element()" select="."/>
        <xsl:call-template name="ATTRIBUTES.GENERIC">
            <xsl:with-param name="context" as="node()" select="$context"/>
        </xsl:call-template>
        <xsl:copy-of select="$context/(@alt|@height|@width)"/>
        <xsl:attribute name="src"
            select="nota:get-file-name-from-path($context/@src)"/>
    </xsl:template>
    <xsl:template name="ATTRIBUTES.LANGUAGE" as="attribute()*">
        <xsl:param name="context" as="element()" select="."/>
        <xsl:choose>
            <xsl:when test="not($context/@lang)">
                <xsl:if test="$context/@xml:lang">
                    <xsl:attribute name="lang" select="$context/@xml:lang"/>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="$context/@lang"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="ATTRIBUTES.TABLE.CELL" as="attribute()*">
        <xsl:param name="context" as="element()" select="."/>
        <xsl:call-template name="ATTRIBUTES.GENERIC">
            <xsl:with-param name="context" as="node()" select="$context"/>
        </xsl:call-template>
        <xsl:copy-of select="$context/(@colspan|@rowspan)"/>
    </xsl:template>
    <!-- Named template for copied element with generic attributes -->
    <xsl:template name="ELEMENT.COPY.GENERIC" as="element()*">
        <xsl:element name="{local-name()}">
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
    <xsl:template name="ELEMENT.BRIDGEHEAD">
        <xsl:param name="context" as="node()" select="."/>
        <p>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                <xsl:with-param name="classesToAdd" as="xs:string"
                    select="'bridgehead'"/>
                <xsl:with-param name="context" as="node()" select="$context"/>
            </xsl:call-template>
            <xsl:apply-templates select="$context/node()">
                <xsl:with-param name="discardEmStrong" as="xs:boolean"
                    tunnel="yes" select="true()"/>
            </xsl:apply-templates>
        </p>
    </xsl:template>
    <!-- Named template for page numbers -->
    <xsl:template name="ELEMENT.LIST_ITEM.PAGENUM.AFTER" as="element()*">
        <xsl:for-each
            select="descendant::html:*[nota:is-page-break(.)]
                    [nota:ends-list-item(.)] except descendant::html:li/
                    descendant::node()">
            <xsl:call-template name="ELEMENT.PAGENUM"/>
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="ELEMENT.LIST_ITEM.PAGENUM.BEFORE" as="element()*">
        <xsl:for-each
            select="descendant::html:*[nota:is-page-break(.)]
                    [nota:starts-list-item(.)] except descendant::html:li/
                    descendant::node()">
            <xsl:call-template name="ELEMENT.PAGENUM"/>
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="ELEMENT.PAGENUM" as="element()">
        <pagenum>
            <xsl:attribute name="id" select="@id"/>
            <xsl:attribute name="page" select="replace(@class, '^page-', '')"/>
            <xsl:value-of select="@title"/>
        </pagenum>
    </xsl:template>
    <!-- Named template for table captions -->
    <xsl:template name="ELEMENT.TABLE.CAPTION" as="element()*">
        <xsl:if test="html:caption">
            <xsl:variable name="attributes" as="attribute()*">
                <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                    <xsl:with-param name="classesToAdd" as="xs:string"
                        select="'bridgehead'"/>
                    <xsl:with-param name="context" as="element()"
                        select="html:caption"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:for-each select="html:caption/node()">
                <xsl:choose>
                    <xsl:when test="nota:starts-inline(.)">
                        <xsl:apply-templates mode="GROUP_INLINE_CONTENT"
                            select=".">
                            <xsl:with-param name="attributes" as="attribute()*"
                                select="$attributes[name() ne 'id']"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="self::html:* and not(nota:is-inline(.))">
                        <xsl:apply-templates mode="DISSOLVED_PARENT"
                            select=".">
                            <xsl:with-param name="attributes" as="attribute()*"
                                select="$attributes[name() ne 'id']"/>
                        </xsl:apply-templates>
                    </xsl:when>
                </xsl:choose>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>
    <!-- Template for grouping levels according to navigation depth -->
    <xsl:template name="LEVELS.GROUP">
        <xsl:param name="sections" as="element()*"/>
        <xsl:param name="level" as="xs:integer" select="1"/>
        <xsl:for-each-group select="$sections"
            group-starting-with="html:section[nota:get-nav-depth(.) eq $level]">
            <level depth="{$level}">
                <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                    <xsl:with-param name="context" as="element()"
                        select="current-group()[1]"/>
                    <xsl:with-param name="classesToAdd" as="xs:string"
                        select="nota:map-type-to-class(current-group()[1])"/>
                </xsl:call-template>
                <xsl:apply-templates select="current-group()[1]/node()">
                    <xsl:with-param name="depthModifier" as="xs:integer"
                        tunnel="yes" select="$level - 1"/>
                </xsl:apply-templates>
                <xsl:call-template name="LEVELS.GROUP">
                    <xsl:with-param name="sections" as="element()*"
                        select="current-group()[position() gt 1]"/>
                    <xsl:with-param name="level" as="xs:integer"
                        select="$level + 1"/>
                </xsl:call-template>
            </level>
        </xsl:for-each-group>
    </xsl:template>
    <!-- Generic template for XHTML elements -->
    <xsl:template match="html:*">
        <xsl:call-template name="ELEMENT.COPY.GENERIC"/>
    </xsl:template>
    <!-- Special mode for grouping inline content in paragraphs -->
    <xsl:template mode="GROUP_INLINE_CONTENT" priority="1"
        match="node()[nota:starts-inline(.)]">
        <xsl:param name="attributes" as="attribute()*"/>
        <xsl:variable name="group" as="node()*"
            select="self::node()|following-sibling::node() except
                    following-sibling::node()[not(nota:is-inline(.))]
                    [1]/(self::node()|following-sibling::node())"/>
        <xsl:if test="$group[normalize-space() ne '']">
            <p>
                <xsl:copy-of select="$attributes"/>
                <xsl:apply-templates select="$group"/>
            </p>
        </xsl:if>
    </xsl:template>
    <xsl:template mode="GROUP_INLINE_CONTENT"
        match="node()[nota:is-inline(.)]"/>
    <xsl:template mode="GROUP_INLINE_CONTENT" match="node()">
        <xsl:apply-templates select="."/>
    </xsl:template>
    <!-- Special mode for handling children of dissolved elements -->
    <xsl:template match="html:*" mode="DISSOLVED_PARENT">
        <xsl:param name="attributes" as="attribute()*"/>
        <xsl:variable name="element" as="element()">
            <xsl:apply-templates select="."/>
        </xsl:variable>
        <xsl:element name="{$element/local-name()}">
            <xsl:call-template name="ATTRIBUTES.COMBINE">
                <xsl:with-param name="primarySet" as="attribute()*"
                    select="$element/@*"/>
                <xsl:with-param name="secondarySet" as="attribute()*"
                    select="$attributes"/>
            </xsl:call-template>
            <xsl:copy-of select="$element/node()"/>
        </xsl:element>
    </xsl:template>
    <!-- A -->
    <xsl:template  match="html:a">
        <xsl:variable name="isExternal" as="xs:boolean?"
            select="matches(@href, '^[a-z]+:')"/>
        <xsl:choose>
            <xsl:when test="$isExternal">
                <a>
                    <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS"/>
                    <xsl:copy-of select="@href"/>
                    <xsl:apply-templates/>
                </a>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="html:a[nota:has-classes(., 'noteref')]">
        <noteref idref="{replace(@href, '^.*?#', '')}">
        	<xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
        	    <xsl:with-param name="classesToDiscard" as="xs:string"
        	        select="'noteref'"/>
        	</xsl:call-template>
            <xsl:apply-templates/>
        </noteref>
    </xsl:template>
    <!-- ABBR -->
    <xsl:template match="html:abbr">
        <acronym>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS"/>
            <xsl:apply-templates/>
        </acronym>
    </xsl:template>
    <!-- ASIDE -->
    <xsl:template match="html:aside">
        <sidebar>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                <xsl:with-param name="classesToDiscard" as="xs:string"
                    select="'sidebar'"/>
            </xsl:call-template>
            <xsl:apply-templates mode="GROUP_INLINE_CONTENT"/>
        </sidebar>
    </xsl:template>
    <xsl:template match="html:figure/html:aside">
        <prodnote render="required">
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                <xsl:with-param name="classesToAdd" as="xs:string"
                    select="'imgprodnote'"/>
                <xsl:with-param name="classesToDiscard" as="xs:string+"
                    select="('desc', 'prodnote')"/>
            </xsl:call-template>
            <xsl:apply-templates mode="GROUP_INLINE_CONTENT"/>
        </prodnote>
    </xsl:template>
    <!-- BLOCKQUOTE -->
    <xsl:template match="html:blockquote">
        <div>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                <xsl:with-param name="classesToAdd" as="xs:string"
                    select="'blockquote'"/>
            </xsl:call-template>
            <xsl:apply-templates/>
        </div>
    </xsl:template>
    <!-- EM and STRONG -->
    <xsl:template match="html:em|html:strong">
        <xsl:param name="discardEmStrong" as="xs:boolean?" tunnel="yes"/>
        <xsl:choose>
            <xsl:when test="$discardEmStrong">
                <xsl:apply-templates>
                    <xsl:with-param name="discardEmStrong" as="xs:boolean"
                        tunnel="yes" select="$discardEmStrong"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="ELEMENT.COPY.GENERIC"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- FIGCAPTION -->
    <xsl:template match="html:figcaption">
        <prodnote>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                <xsl:with-param name="classesToAdd" as="xs:string"
                    select="'caption'"/>
            </xsl:call-template>
            <xsl:apply-templates mode="GROUP_INLINE_CONTENT"/>
        </prodnote>
    </xsl:template>
    <!-- FIGURE -->
    <xsl:template match="html:figure">
        <imggroup>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                <xsl:with-param name="classesToDiscard" as="xs:string+"
                    select="('image', 'image-series')"/>
            </xsl:call-template>
            <xsl:choose>
                <xsl:when test="html:figure">
                    <xsl:apply-templates select="html:figcaption"/>
                    <xsl:apply-templates select="html:figure/html:*"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="html:*"/>
                </xsl:otherwise>
            </xsl:choose>
        </imggroup>
    </xsl:template>
    <xsl:template match="html:figure[nota:has-classes(., 'sidebar')]">
        <sidebar>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                <xsl:with-param name="classesToDiscard" as="xs:string"
                    select="'sidebar'"/>
            </xsl:call-template>
            <xsl:apply-templates/>
        </sidebar>
    </xsl:template>
    <!-- H1, H2, H3 etc. -->
    <xsl:template match="html:*[matches(local-name(), '^h\d$')]">
        <xsl:param name="depthModifier" as="xs:integer" tunnel="yes"
            select="0"/>
        <xsl:variable name="depth" as="xs:integer"
            select="count(ancestor::html:section) + $depthModifier"/>
        <xsl:variable name="demote" as="xs:boolean"
            select="$depth > 6 or parent::html:aside or parent::html:section
                    [nota:is-poem(.)]"/>
        <xsl:choose>
            <xsl:when test="$demote">
                <p class="bridgehead">
                    <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS"/>
                    <xsl:apply-templates/>
                </p>
            </xsl:when>
            <xsl:otherwise>
                <levelhd>
                    <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS"/>
                    <xsl:attribute name="depth" select="$depth"/>
                    <xsl:apply-templates/>
                </levelhd>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- HR -->
    <xsl:template match="html:hr"/>
    <!-- IMG -->
    <xsl:template match="html:img">
        <img>
            <xsl:call-template name="ATTRIBUTES.IMAGE"/>
        </img>
    </xsl:template>
    <!-- LI -->
    <xsl:template match="html:li">
        <xsl:if test="not(ancestor::html:tr)">
            <xsl:call-template name="ELEMENT.LIST_ITEM.PAGENUM.BEFORE"/>
        </xsl:if>
        <li>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS"/>
            <xsl:apply-templates/>
        </li>
        <xsl:if test="not(ancestor::html:tr)">
            <xsl:call-template name="ELEMENT.LIST_ITEM.PAGENUM.AFTER"/>
        </xsl:if>
    </xsl:template>
    <xsl:template mode="NUMBER_LIST_ITEMS" match="html:li">
        <xsl:param name="type" as="xs:string" select="'1'"/>
        <xsl:param name="reversed" as="xs:boolean" select="false()"/>
        <xsl:param name="start" as="xs:integer" select="1"/>
        <xsl:variable name="value" as="xs:integer"
            select="if (@value) then xs:integer(@value)
                    else if ($reversed)
                    then count(following-sibling::html:li) + $start
                    else count(preceding-sibling::html:li) + $start"/>
        <xsl:variable name="formattedNumber" as="xs:string">
            <xsl:number value="$value" format="{$type}"/>
        </xsl:variable>
        <xsl:variable name="numberString" as="xs:string"
            select="if (descendant::text()[normalize-space() ne ''][1]/
                    matches(., '^\s+')) then concat($formattedNumber, '.')
                    else concat($formattedNumber, '. ')"/>
        <xsl:call-template name="ELEMENT.LIST_ITEM.PAGENUM.BEFORE"/>
        <li>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS"/>
            <xsl:choose>
                <xsl:when
                    test="node()[normalize-space() ne ''][1]/self::html:p">
                    <p>
                        <xsl:copy-of select="html:p[1]/(@id|@lang)"/>
                        <xsl:value-of select="$numberString"/>
                        <xsl:apply-templates select="html:p[1]/node()"/>
                    </p>
                    <xsl:apply-templates mode="GROUP_INLINE_CONTENT"
                        select="html:p[1]/following-sibling::node()"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$numberString"/>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </li>
        <xsl:call-template name="ELEMENT.LIST_ITEM.PAGENUM.AFTER"/>
    </xsl:template>
    <xsl:template match="html:li//html:*[nota:is-page-break(.)]" priority="1">
        <xsl:if test="not(nota:starts-list-item(.) or nota:ends-list-item(.))">
            <xsl:call-template name="ELEMENT.PAGENUM"/>
        </xsl:if>
    </xsl:template>
    <!-- NOTES -->
    <xsl:template match="html:*[nota:is-note(.)]" priority="1">
        <xsl:variable name="class" as="xs:string"
            select="nota:map-type-to-class(.)"/>
        <note>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                <xsl:with-param name="classesToAdd" as="xs:string"
                    select="$class"/>
                <xsl:with-param name="classesToDiscard" as="xs:string"
                    select="'notebody'"/>
            </xsl:call-template>
            <xsl:apply-templates mode="GROUP_INLINE_CONTENT"/>
        </note>
    </xsl:template>
    <!-- OL -->
    <xsl:template match="html:ol">
        <list type="ul" bullet="none">
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                <xsl:with-param name="classesToDiscard" as="xs:string"
                    select="'list-style-type-none'"/>
            </xsl:call-template>
            <xsl:choose>
                <xsl:when test="nota:has-classes(., 'list-style-type-none')">
                    <xsl:apply-templates/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="NUMBER_LIST_ITEMS">
                        <xsl:with-param name="type" as="xs:string"
                            select="if (@type) then @type else '1'"/>
                        <xsl:with-param name="reversed" as="xs:boolean"
                            select="exists(@reversed)"/>
                        <xsl:with-param name="start" as="xs:integer"
                            select="if (@start) then xs:integer(@start)
                                    else 1"/>
                    </xsl:apply-templates>
                </xsl:otherwise>
            </xsl:choose>
        </list>
    </xsl:template>
    <xsl:template match="html:ol[html:li[nota:is-note(.)]]">
        <xsl:variable name="attributes" as="attribute()*">
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                <xsl:with-param name="classesToDiscard" as="xs:string"
                    select="'list-style-type-none'"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:for-each select="html:*">
            <xsl:apply-templates mode="DISSOLVED_PARENT" select=".">
                <xsl:with-param name="attributes" as="attribute()*"
                    select="$attributes[name() ne 'id']"/>
            </xsl:apply-templates>
        </xsl:for-each>
    </xsl:template>
    <!-- P -->
    <xsl:template match="html:p[nota:has-classes(., 'line')]" priority="1">
        <line>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                <xsl:with-param name="classesToDiscard" as="xs:string"
                    select="'line'"/>
            </xsl:call-template>
            <xsl:apply-templates/>
        </line>
    </xsl:template>
    <xsl:template match="html:p[nota:is-bridgehead(.)]" priority="1">
        <xsl:call-template name="ELEMENT.BRIDGEHEAD"/>
    </xsl:template>
    <xsl:template match="html:p[preceding-sibling::*[1]/self::html:hr]">
        <xsl:variable name="class" as="xs:string"
            select="if (nota:has-classes(preceding-sibling::*[1],
                    'emptyline')) then 'precedingemptyline'
                    else if (nota:has-classes(preceding-sibling::*[1],
                    'separator')) then 'precedingseparator'
                    else ''"/>
        <p>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                <xsl:with-param name="classesToAdd" as="xs:string"
                    select="$class"/>
            </xsl:call-template>
            <xsl:apply-templates/>
        </p>
    </xsl:template>
    <xsl:template match="html:p[parent::html:caption]">
        <xsl:call-template name="ELEMENT.BRIDGEHEAD"/>
    </xsl:template>
    <!--  PRE -->
    <xsl:template match="html:pre">
        <p class="preformatted">
        	<xsl:apply-templates/>
        </p>
    </xsl:template>
    <!-- PAGE BREAK -->
    <xsl:template
        match="html:*[nota:is-page-break(.)]">
        <xsl:call-template name="ELEMENT.PAGENUM"/>
    </xsl:template>
    <!-- SECTION -->
    <xsl:template match="html:section">
        <xsl:param name="depthModifier" as="xs:integer" tunnel="yes"
            select="0"/>
        <xsl:variable name="depth" as="xs:integer"
            select="count(ancestor-or-self::html:section) + $depthModifier"/>
        <xsl:choose>
            <xsl:when test="$depth le 6">
                <level depth="{$depth}">
                    <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                        <xsl:with-param name="classesToAdd" as="xs:string"
                            select="nota:map-type-to-class(.)"/>
                    </xsl:call-template>
                    <xsl:apply-templates/>
                </level>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="html:section[nota:has-epub-types(., 'cover')]">
        <xsl:apply-templates select="html:*">
            <xsl:with-param name="depthModifier" as="xs:integer"
                tunnel="yes" select="-1"/>
        </xsl:apply-templates>
    </xsl:template>
    <xsl:template match="html:section[nota:is-poem(.)]">
        <div>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                <xsl:with-param name="classesToAdd" as="xs:string"
                    select="'poem'"/>
            </xsl:call-template>
            <xsl:apply-templates/>
        </div>
    </xsl:template>
    <xsl:template
        match="html:section[nota:is-poem(.)]/html:div[nota:is-stanza(.)]">
        <div class="stanza">
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                <xsl:with-param name="classesToAdd" as="xs:string"
                    select="'stanza'"/>
                <xsl:with-param name="classesToDiscard" as="xs:string"
                    select="'linegroup'"/>
            </xsl:call-template>
            <xsl:apply-templates/>
        </div>
    </xsl:template>
    <!-- SPAN -->
    <xsl:template match="html:span[nota:has-classes(., 'lic')]">
        <lic>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                <xsl:with-param name="classesToAdd" as="xs:string"
                    select="'pageref'"/>
                <xsl:with-param name="classesToDiscard" as="xs:string"
                    select="'lic'"/>
            </xsl:call-template>
            <xsl:apply-templates/>
        </lic>
    </xsl:template>
    <xsl:template match="html:span[nota:has-classes(., 'linenum')]">
        <linenum>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                <xsl:with-param name="classesToDiscard" as="xs:string"
                    select="'linenum'"/>
            </xsl:call-template>
            <xsl:apply-templates/>
        </linenum>
    </xsl:template>
    <xsl:template match="html:span[nota:has-classes(., 'roman')]">
        <span>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS"/>
            <xsl:if test="not(@id)">
                <xsl:attribute name="id" select="generate-id()"/>
            </xsl:if>
            <xsl:apply-templates/>
        </span>
    </xsl:template>
    <!-- TABLE -->
    <xsl:template match="html:table">
        <xsl:call-template name="ELEMENT.TABLE.CAPTION"/>
        <table>
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS"/>
            <xsl:apply-templates/>
        </table>
    </xsl:template>
    <xsl:template match="html:table[nota:get-page-breaks(.)]">
        <xsl:variable name="attributes" as="attribute()*">
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS"/>
        </xsl:variable>
        <xsl:call-template name="ELEMENT.TABLE.CAPTION"/>
        <xsl:variable name="firstPass" as="node()">
            <table>
                <xsl:apply-templates
                    select="nota:get-preceding-rows(descendant::html:tr
                            [nota:get-page-breaks(.)][1])"/>
                <xsl:apply-templates mode="ISOLATE_PAGE_BREAKS"
                    select="descendant::html:tr[nota:get-page-breaks(.)][1]"/>
            </table>
        </xsl:variable>
        <xsl:for-each-group group-starting-with="pagenum"
            select="$firstPass/*">
            <xsl:copy-of select="current-group()[self::pagenum]"/>
            <xsl:if test="current-group()[self::tr]">
                <table>
                    <xsl:copy-of
                        select="if (position() eq 1) then $attributes
                                else $attributes[name() ne 'id']"/>
                    <xsl:copy-of select="current-group()[self::tr]"/>
                </table>
            </xsl:if>
        </xsl:for-each-group>
    </xsl:template>
    <xsl:template match="html:table/html:caption"/>
    <xsl:template match="html:table//html:*[nota:is-page-break(.)]"
        priority="1"/>
    <xsl:template match="html:tbody|html:tfoot|html:thead">
        <xsl:variable name="attributes" as="attribute()*">
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS"/>
        </xsl:variable>
        <xsl:for-each select="html:*">
            <xsl:apply-templates mode="DISSOLVED_PARENT" select=".">
                <xsl:with-param name="attributes" as="attribute()*"
                    select="$attributes[name() ne 'id']"/>
            </xsl:apply-templates>
        </xsl:for-each>
    </xsl:template>
    <!-- TABLE CELLS -->
    <xsl:template match="html:td">
        <td>
            <xsl:call-template name="ATTRIBUTES.TABLE.CELL"/>
            <xsl:apply-templates/>
        </td>
    </xsl:template>
    <xsl:template match="html:th">
        <th>
            <xsl:call-template name="ATTRIBUTES.TABLE.CELL"/>
            <xsl:apply-templates/>
        </th>
    </xsl:template>
    <!-- TABLE ROWS WITH PAGE BREAKS -->
    <xsl:template mode="ISOLATE_PAGE_BREAKS"
        match="html:tr[nota:get-page-breaks(.)]">
        <xsl:for-each
            select="nota:get-page-breaks(.)[nota:starts-row(.)]">
            <xsl:call-template name="ELEMENT.PAGENUM"/>
        </xsl:for-each>
        <xsl:apply-templates select="self::html:tr"/>
        <xsl:for-each
            select="nota:get-page-breaks(.)[nota:ends-row(.)]">
            <xsl:call-template name="ELEMENT.PAGENUM"/>
        </xsl:for-each>
        <xsl:apply-templates select="nota:get-following-rows(.)"/>
        <xsl:apply-templates mode="ISOLATE_PAGE_BREAKS"
            select="nota:get-next-row-with-page-break(.)"/>
    </xsl:template>
    <!-- UL -->
    <xsl:template match="html:ul">
        <list type="ul">
            <xsl:call-template name="ATTRIBUTES.GENERIC.WITH_CLASS">
                <xsl:with-param name="classesToDiscard" as="xs:string"
                    select="'list-style-type-none'"/>
            </xsl:call-template>
            <xsl:attribute name="bullet"
                select="if (nota:has-classes(., 'list-style-type-none'))
                        then 'none' else 'yes'"/>
            <xsl:apply-templates/>
        </list>
    </xsl:template>
    <!-- FUNCTIONS -->
    <xsl:function name="nota:ends-list-item" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="not(exists($n/(following::node() intersect ancestor::html:li
                    [1]/descendant::node())[normalize-space() ne '']))"/>
    </xsl:function>
    <xsl:function name="nota:ends-row" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="not(exists($n/(following::node() intersect ancestor::html:tr
                    [1]/descendant::node())[normalize-space() ne '']))"/>
    </xsl:function>
    <xsl:function name="nota:get-page-breaks" as="element()*">
        <xsl:param name="n" as="node()"/>
        <xsl:sequence select="$n/descendant::html:*[nota:is-page-break(.)]"/>
    </xsl:function>
    <xsl:function name="nota:get-file-name-from-path" as="xs:string">
        <xsl:param name="path" as="xs:string"/>
        <xsl:value-of select="tokenize($path, '/')[position() = last()]"/>
    </xsl:function>
    <xsl:function name="nota:get-preceding-rows" as="element(html:tr)*">
        <xsl:param name="n" as="node()"/>
        <xsl:sequence
            select="$n/((preceding::html:tr intersect ancestor::html:table[1]/
                    descendant::html:tr) except preceding::html:tr
                    [nota:get-page-breaks(.)][1]/(self::html:tr|
                    preceding::html:tr))"/>
    </xsl:function>
    <xsl:function name="nota:get-following-rows" as="element(html:tr)*">
        <xsl:param name="n" as="node()"/>
        <xsl:sequence
            select="$n/((following::html:tr intersect ancestor::html:table[1]/
                    descendant::html:tr) except following::html:tr
                    [nota:get-page-breaks(.)][1]/(self::html:tr|
                    following::html:tr))"/>
    </xsl:function>
    <xsl:function name="nota:get-nav-depth" as="xs:integer">
        <xsl:param name="n" as="element()"/>
        <xsl:variable name="id" as="xs:string*"
            select="$n/(@id|html:*[matches(local-name(), '^h\d$')][1]/@id)"/>
        <xsl:variable name="navItem" as="element()?"
            select="$NAV_DOCUMENT//html:nav[@epub:type eq 'toc']//(html:a
                    [substring-after(@href, '#') = $id])[1]"/>
        <xsl:value-of
            select="if ($navItem) then $navItem/count(ancestor::html:li)
                    else 1"/>
    </xsl:function>
    <xsl:function name="nota:get-next-row-with-page-break" as="element(html:tr)?">
        <xsl:param name="n" as="node()"/>
        <xsl:sequence
            select="$n/(following::html:tr[nota:get-page-breaks(.)] intersect
                    ancestor::html:table[1]//descendant::html:tr)[1]"/>
    </xsl:function>
    <xsl:function name="nota:has-classes" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:param name="classes" as="xs:string+"/>
        <xsl:value-of select="tokenize($n/@class, '\s+') = $classes"/>
    </xsl:function>
    <xsl:function name="nota:has-epub-types" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:param name="types" as="xs:string+"/>
        <xsl:value-of select="tokenize($n/@epub:type, '\s+') = $types"/>
    </xsl:function>
    <xsl:function name="nota:is-bridgehead" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:value-of
            select="$n/(nota:has-classes(., 'bridgehead') or
                    nota:has-epub-types(., 'bridgehead'))"/>
    </xsl:function>
    <xsl:function name="nota:is-inline" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="$n/(self::text() or local-name() = $INLINE_ELEMENT_NAMES)"/>
    </xsl:function>
    <xsl:function name="nota:is-note" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:value-of
            select="nota:has-epub-types($n, ('note', 'footnote', 'rearnote'))"/>
    </xsl:function>
    <xsl:function name="nota:is-page-break" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:value-of select="nota:has-epub-types($n, 'pagebreak')"/>
    </xsl:function>
    <xsl:function name="nota:is-poem" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:value-of
            select="nota:has-epub-types($n, ('z3998:poem', 'z3998:verse'))
                    or nota:has-classes($n, 'poem')"/>
    </xsl:function>
    <xsl:function name="nota:is-stanza" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:value-of select="nota:has-classes($n, 'linegroup')"/>
    </xsl:function>
    <xsl:function name="nota:is-subordinate-division" as="xs:boolean">
        <xsl:param name="n" as="element()"/>
        <xsl:value-of select="nota:get-nav-depth($n) gt 1"/>
    </xsl:function>
    <xsl:function name="nota:map-type-to-class" as="xs:string">
        <xsl:param name="n" as="element()"/>
        <xsl:variable name="types" as="xs:string*"
            select="$n/tokenize(@epub:type, '\s+')"/>
        <xsl:value-of
            select="if ($types = 'part') then 'part'
                    else if ($types = ('footnotes', 'rearnotes')) then 'notes'
                    else if ($types = 'footnote') then 'footnote required'
                    else if ($types = 'rearnote') then 'endnote required'
                    else if ($types = 'titlepage') then 'title'
                    else if ($types = 'colophon') then 'colophon'
                    else if ($types = 'toc') then 'toc'
                    else if ($types = 'index') then 'index'
                    else ''"/>
    </xsl:function>
    <xsl:function name="nota:starts-sequence" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="not($n/preceding-sibling::node()[normalize-space() ne ''])"/>
    </xsl:function>
    <xsl:function name="nota:starts-inline" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:variable name="previous" as="node()?"
            select="$n/preceding-sibling::node()[1]"/>
        <xsl:value-of
            select="$n/(nota:is-inline(.) and (not(exists($previous)) or
                    not($previous/nota:is-inline(.))))"/>
    </xsl:function>
    <xsl:function name="nota:starts-list-item" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="not(exists($n/(preceding::node() intersect ancestor::html:li
                    [1]/descendant::node())[normalize-space() ne '']))"/>
    </xsl:function>
    <xsl:function name="nota:starts-row" as="xs:boolean">
        <xsl:param name="n" as="node()"/>
        <xsl:value-of
            select="not(exists($n/(preceding::node() intersect ancestor::html:tr
                    [1]/descendant::node())[normalize-space() ne '']))"/>
    </xsl:function>
    
</xsl:stylesheet>