<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
    xmlns:nota="http://www.nota.dk/dtbook2docx"
    xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"
    xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
    exclude-result-prefixes="nota xs"
    version="2.0">
    <xsl:import href="dtbook2docx-functions.xsl"/>
    <!-- DEBUGGING: Uncomment <xsl:key> and <xsl:template> below to run just
    this stylesheet -->
    <!--<xsl:key name="notes" match="note" use="@id"/>
    <xsl:template match="/">
        <xsl:apply-templates mode="DOCUMENT" select="dtbook/book"/>
    </xsl:template>-->
    <!-- NAMED TEMPLATES -->
    <xsl:template name="BLOCKS_OR_PARAGRAPH">
        <xsl:choose>
            <xsl:when test="nota:has-block-elements(.)">
                <xsl:apply-templates mode="DOCUMENT"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="PARAGRAPH">
                    <xsl:with-param name="content" select="node()"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="HEADING">
        <xsl:param name="content" as="node()*"/>
        <xsl:param name="depth" as="xs:integer"/>
        <xsl:param name="count" as="xs:integer"
            select="count(ancestor-or-self::level|preceding::level)"/>
        <xsl:variable name="bookmarkName" as="xs:string"
            select="concat('level_', $count)"/>
        <xsl:call-template name="PARAGRAPH">
            <xsl:with-param name="content" select="$content"/>
            <xsl:with-param name="paragraphStyle"
                select="concat('Heading', $depth)"/>
            <xsl:with-param name="wordContentBefore">
                <w:bookmarkStart w:id="{$count}" w:name="{$bookmarkName}"/>
            </xsl:with-param>
            <xsl:with-param name="wordContentAfter">
                <w:bookmarkEnd w:id="{$count}" w:name="{$bookmarkName}"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template name="PARAGRAPH">
        <xsl:param name="content" as="node()*"/>
        <xsl:param name="wordContentBefore" as="node()*"/>
        <xsl:param name="wordContentAfter" as="node()*"/>
        <xsl:param name="paragraphStyle" as="xs:string" select="'Normal'"/>
        <xsl:param name="properties" as="node()*"/>
        <xsl:param name="maintainContext" as="xs:boolean" select="false()"/>
        <xsl:param name="indentLeft" as="xs:integer"
            select="if ($maintainContext) then nota:determine-indent-left(.)
                    else nota:determine-indent-left($content)"/>
        <xsl:param name="indentRight" as="xs:integer"
            select="if ($maintainContext) then nota:determine-indent-right(.)
                    else nota:determine-indent-right($content)"/>
        <w:p>
            <w:pPr>
                <w:pStyle w:val="{$paragraphStyle}"/>
                <xsl:copy-of select="$properties"/>
                <xsl:if test="($indentLeft, $indentRight) &gt; 0">
                    <w:ind w:left="{$indentLeft}" w:right="{$indentRight}"/>
                </xsl:if>
            </w:pPr>
            <xsl:copy-of select="$wordContentBefore"/>
            <xsl:apply-templates mode="INLINE" select="$content"/>
            <xsl:copy-of select="$wordContentAfter"/>
        </w:p>
    </xsl:template>
    <xsl:template name="RUN">
        <xsl:param name="text" as="xs:string"/>
        <xsl:param name="wordContentBefore" as="node()*"/>
        <xsl:param name="wordContentAfter" as="node()*"/>
        <xsl:param name="properties" as="node()*"/>
        <xsl:param name="language" as="xs:string*"/>
        <!--<xsl:param name="language" as="xs:string"
            select="if (nota:get-node-lang(.) eq '') then $LANGUAGE
                    else nota:get-node-lang(.)"/>-->
        <xsl:param name="addBreaks" as="xs:boolean" select="false()"/>
        <w:r>
            <w:rPr>
                <xsl:if test="$language">
                    <w:lang w:val="{$language}"/>
                </xsl:if>
                <xsl:copy-of select="$properties"/>
            </w:rPr>
            <xsl:copy-of select="$wordContentBefore"/>
            <xsl:choose>
                <xsl:when test="$addBreaks">
                    <!-- Word ignores line breaks even with @xml:space set to
                    "preserve": they need to be added as <w:br> elements -->
                    <xsl:analyze-string select="$text" regex="(.*)\n">
                        <xsl:matching-substring>
                            <xsl:if test="string-length(regex-group(1)) gt 0">
                                <w:t xml:space="preserve"><xsl:value-of
                                    select="regex-group(1)"/></w:t>
                            </xsl:if>
                            <w:br w:type="line"/>
                        </xsl:matching-substring>
                        <xsl:non-matching-substring>
                            <w:t xml:space="preserve"><xsl:value-of
                                select="."/></w:t>
                        </xsl:non-matching-substring>
                    </xsl:analyze-string>
                </xsl:when>
                <xsl:otherwise>
                    <w:t xml:space="preserve"><xsl:value-of
                        select="$text"/></w:t>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:copy-of select="$wordContentAfter"/>
        </w:r>
    </xsl:template>
    <xsl:template name="TABLE_CELL_MERGED">
        <w:tc>
            <w:tcPr>
                <w:vMerge w:val="continue"/>
                <xsl:if test="@colspan &gt; 1">
                    <w:gridSpan w:val="{@colspan}"/>
                </xsl:if>
            </w:tcPr>
            <w:p/>
        </w:tc>
    </xsl:template>
    <xsl:template name="TITLE_PAGE">
        <xsl:param name="title" as="xs:string*"/>
        <xsl:param name="authors" as="xs:string*"/>
        <xsl:call-template name="PARAGRAPH">
            <xsl:with-param name="paragraphStyle" select="'Title'"/>
            <xsl:with-param name="content">
                <xsl:value-of select="$title"/>
            </xsl:with-param>
        </xsl:call-template>
        <xsl:for-each select="$authors">
            <xsl:call-template name="PARAGRAPH">
                <xsl:with-param name="content">
                    <xsl:value-of select="."/>
                </xsl:with-param>
                <xsl:with-param name="wordContentAfter">
                    <xsl:if test="position() = last()">
                        <w:r>
                            <w:br w:type="page"/>
                        </w:r>
                    </xsl:if>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="TOC">
        <!-- Gather all levels except those which are marked up as containing
        notes only -->
        <xsl:for-each
            select=".//level[not(nota:has-class(., ('notes', 'footnotes')))]">
            <xsl:variable name="depth" as="xs:integer"
                select="count(ancestor-or-self::level)"/>
            <xsl:variable name="count" as="xs:integer"
                select="count(ancestor-or-self::level|preceding::level)"/>
            <xsl:variable name="bookmarkName" as="xs:string"
                select="concat('level_', $count)"/>
            <xsl:call-template name="PARAGRAPH">
                <xsl:with-param name="paragraphStyle"
                    select="concat('TOCEntry', if ($depth gt 6) then 6 else
                    		$depth)"/>
                <xsl:with-param name="wordContentBefore">
                    <w:fldSimple
                        w:instr="REF {$bookmarkName} \h">
                        <xsl:call-template name="RUN">
                            <xsl:with-param name="text"
                                select="if (levelhd) then normalize-space(
                                		string-join(levelhd/text(), ' '))
                                        else '[...]'"/>
                        </xsl:call-template>
                    </w:fldSimple>
                    <w:r>
                        <w:t xml:space="preserve"> </w:t>
                    </w:r>
                    <w:fldSimple
                        w:instr="PAGEREF {$bookmarkName} \h"/>
                    <xsl:if test="position() = last()">
                        <w:r>
                            <w:br w:type="page"/>
                        </w:r>
                    </xsl:if>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="TOC_HEADING">
        <xsl:call-template name="PARAGRAPH">
            <xsl:with-param name="paragraphStyle" select="'Heading1'"/>
            <xsl:with-param name="content">
                Indholdsfortegnelse [Word]
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- GENERIC TEMPLATE: Applies to all nodes in both modes: if no specific
    template matches a particular node, we skip that node and continue with
    any child nodes or attributes -->
    <xsl:template mode="DOCUMENT INLINE" match="@*|node()">
        <xsl:apply-templates mode="#current" select="@*|node()"/>
    </xsl:template>
    <!-- DOCUMENT MODE: Block and section elements -->
    <!-- BLOCKQUOTE and DIV.blockquote -->
    <xsl:template mode="DOCUMENT"
        match="blockquote|div[nota:has-class(., 'blockquote')]">
        <xsl:param name="properties" as="node()*"/>
        <xsl:param name="indentLeft" as="xs:integer*"/>
        <xsl:param name="indentRight" as="xs:integer*"/>
        <xsl:param name="indentModifier" as="xs:integer*"/>
        <xsl:choose>
            <xsl:when test="nota:has-block-elements(.)">
                <xsl:apply-templates mode="DOCUMENT" select="*[1]">
                    <xsl:with-param name="properties" select="$properties"/>
                    <xsl:with-param name="indentLeft"
                        select="$indentLeft + $indentModifier"/>
                    <xsl:with-param name="indentRight"
                        select="$indentRight + $indentModifier"/>
                </xsl:apply-templates>
                <xsl:apply-templates mode="DOCUMENT"
                    select="*[position() gt 1]">
                    <xsl:with-param name="properties"
                        select="$properties[not(self::w:numPr)]"/>
                    <xsl:with-param name="indentLeft"
                        select="$indentLeft + $indentModifier"/>
                    <xsl:with-param name="indentRight"
                        select="$indentRight + $indentModifier"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="PARAGRAPH">
                    <xsl:with-param name="content" select="node()"/>
                    <xsl:with-param name="indentLeft"
                        select="$indentLeft + $indentModifier"/>
                    <xsl:with-param name="indentRight"
                        select="$indentRight + $indentModifier"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- BOOK: Establish document and document body -->
    <xsl:template mode="DOCUMENT" match="book">
        <w:document>
            <w:body>
                <xsl:call-template name="TITLE_PAGE">
                    <xsl:with-param name="title"
                        select="../head/meta[@name = ('dc:title',
                                'dc:Title')]/string(@content)"/>
                    <xsl:with-param name="authors"
                        select="../head/meta[@name = ('dc:creator',
                                'dc:Creator', 'prod:semi-creator')]/
                                string(@content)"/>
                </xsl:call-template>
                <xsl:call-template name="TOC_HEADING"/>
                <xsl:call-template name="TOC"/>
                <xsl:apply-templates mode="DOCUMENT"/>
            </w:body>
        </w:document>
    </xsl:template>
    <!-- DD -->
    <xsl:template mode="DOCUMENT" match="dd">
        <xsl:call-template name="BLOCKS_OR_PARAGRAPH"/>
    </xsl:template>
    <!-- DIV.stanza: A stanza is converted to a paragraph; the <line>
    elements are converted to runs followed by line breaks -->
    <xsl:template mode="DOCUMENT" match="div[nota:has-class(., 'stanza')]">
        <xsl:param name="properties" as="node()*" tunnel="yes"/>
        <xsl:call-template name="PARAGRAPH">
            <xsl:with-param name="content" select="line|p|pagenum"/>
            <xsl:with-param name="properties" select="$properties"/>
        </xsl:call-template>
    </xsl:template>
    <!-- DT -->
    <xsl:template mode="DOCUMENT" match="dt">
        <xsl:param name="properties" as="node()*" tunnel="yes"/>
        <xsl:call-template name="PARAGRAPH">
            <xsl:with-param name="content" select="node()"/>
            <xsl:with-param name="properties" select="$properties"/>
        </xsl:call-template>
    </xsl:template>
    <!-- IMG -->
    <xsl:template mode="DOCUMENT" match="img">
        <xsl:param name="properties" as="node()*" tunnel="yes"/>
        <xsl:call-template name="PARAGRAPH">
            <xsl:with-param name="content" select="."/>
            <xsl:with-param name="properties" select="$properties"/>
        </xsl:call-template>
    </xsl:template>
    <!-- LEVEL: Only relevant when no heading is present -->
    <xsl:template mode="DOCUMENT" match="level[not(levelhd)]">
        <xsl:variable name="depth" as="xs:integer"
            select="count(ancestor-or-self::level)"/>
        <xsl:variable name="initialPagenum" as="node()*"
            select="*[1]/self::pagenum"/>
        <xsl:if test="$initialPagenum">
            <xsl:apply-templates mode="DOCUMENT" select="$initialPagenum"/>
        </xsl:if>
        <xsl:call-template name="HEADING">
            <xsl:with-param name="content">
                [***]
            </xsl:with-param>
            <xsl:with-param name="depth"
                select="if ($depth gt 6) then 6 else $depth"/>
        </xsl:call-template>
        <xsl:choose>
            <xsl:when test="$initialPagenum">
                <xsl:apply-templates mode="DOCUMENT"
                    select="*[position() gt 1]"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates mode="DOCUMENT"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- LEVEL.(notes|footnotes): Discard in main document -->
    <xsl:template mode="DOCUMENT" priority="1"
        match="level[nota:has-class(., ('notes', 'footnotes'))]"/>
    <!-- LEVELHD -->
    <xsl:template mode="DOCUMENT" match="levelhd">
        <xsl:variable name="depth" as="xs:integer"
            select="count(ancestor::level)"/>
        <xsl:call-template name="HEADING">
            <xsl:with-param name="content" select="node()"/>
            <xsl:with-param name="depth"
                select="if ($depth gt 6) then 6 else $depth"/>
        </xsl:call-template>
    </xsl:template>
    <!-- LI -->
    <xsl:template mode="DOCUMENT" match="li">
        <xsl:variable name="numId" as="xs:integer"
            select="1 + count(parent::list/(ancestor::list|preceding::list))"/>
        <xsl:variable name="numberingProperties" as="node()">
            <w:numPr>
                <w:ilvl w:val="0"/>
                <w:numId w:val="{$numId}"/>
            </w:numPr>
        </xsl:variable>
        <xsl:call-template name="PARAGRAPH">
            <xsl:with-param name="content" select="node()"/>
            <xsl:with-param name="properties"
                select="$numberingProperties"/>
        </xsl:call-template>
    </xsl:template>
    <!-- LI with block elements -->
    <xsl:template mode="DOCUMENT" match="li[nota:has-block-elements(.)]">
        <xsl:variable name="numId" as="xs:integer"
            select="1 + count(parent::list/(ancestor::list|preceding::list))"/>
        <xsl:variable name="numberingProperties" as="node()">
            <w:numPr>
                <w:ilvl w:val="0"/>
                <w:numId w:val="{$numId}"/>
            </w:numPr>
        </xsl:variable>
        <!-- Gather inline nodes (text and formatting) which may precede or
        follow the nested blocks -->
        <xsl:variable name="initialInlineNodes" as="node()*"
            select="*[nota:is-block-element(.)][1]/
                    preceding-sibling::node()"/>
        <xsl:variable name="finalInlineNodes" as="node()*"
            select="*[nota:is-block-element(.)][position() = last()]/
                    following-sibling::node()"/>
        <xsl:choose>
            <!-- If initial inline nodes have actual content, convert them
            to a paragraph... -->
            <xsl:when test="$initialInlineNodes[normalize-space(.)]">
                <xsl:call-template name="PARAGRAPH">
                    <xsl:with-param name="content"
                        select="$initialInlineNodes"/>
                    <xsl:with-param name="properties"
                        select="$numberingProperties"/>
                </xsl:call-template>
                <!-- ... then continue with the following block content -->
                <xsl:apply-templates mode="DOCUMENT"
                    select="$initialInlineNodes/following-sibling::*"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- If no inline nodes, pass the numbering properties to the
                first child element, then continue -->
                <!-- TODO: Handle <div> and other containers without immediate
                content at this position -->
                <xsl:apply-templates mode="DOCUMENT" select="*[1]">
                    <xsl:with-param name="properties" tunnel="yes"
                        select="$numberingProperties"/>
                </xsl:apply-templates>
                <xsl:apply-templates mode="DOCUMENT"
                    select="*[position() gt 1]"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="$finalInlineNodes[normalize-space(.)]">
            <xsl:call-template name="PARAGRAPH">
                <xsl:with-param name="content"
                    select="$finalInlineNodes"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    <!-- LINE -->
    <xsl:template mode="DOCUMENT" match="line">
        <xsl:param name="paragraphStyle" as="xs:string*" tunnel="yes"/>
        <xsl:param name="properties" as="node()*" tunnel="yes"/>
        <xsl:call-template name="PARAGRAPH">
            <xsl:with-param name="content" select="node()"/>
            <xsl:with-param name="paragraphStyle" select="$paragraphStyle"/>
            <xsl:with-param name="properties" select="$properties"/>
        </xsl:call-template>
    </xsl:template>
    <!-- NOTE: Only table notes are preserved in main document -->
    <!-- TODO: Linking -->
    <xsl:template mode="DOCUMENT" match="note[nota:is-table-note(.)]">
        <xsl:apply-templates mode="DOCUMENT">
            <xsl:with-param name="paragraphStyle" tunnel="yes"
                select="'NoteText'"/>
        </xsl:apply-templates>
    </xsl:template>
    <xsl:template mode="DOCUMENT" match="note"/>
    <!-- P -->
    <xsl:template mode="DOCUMENT" match="p">
        <xsl:param name="properties" as="node()*" tunnel="yes"/>
        <xsl:param name="paragraphStyle" as="xs:string*" tunnel="yes"/>
        <xsl:if test="nota:has-class(., 'precedingseparator')">
            <xsl:call-template name="PARAGRAPH">
                <xsl:with-param name="content">§§§</xsl:with-param>
                <xsl:with-param name="properties">
                    <w:jc w:val="center"/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
        <xsl:call-template name="PARAGRAPH">
            <xsl:with-param name="content" select="node()"/>
            <xsl:with-param name="properties" select="$properties"/>
            <xsl:with-param name="paragraphStyle">
                <xsl:choose>
                    <xsl:when test="$paragraphStyle">
                        <xsl:value-of select="$paragraphStyle"/>
                    </xsl:when>
                    <xsl:when test="nota:has-class(., 'bridgehead')">
                        <xsl:value-of select="'Bridgehead'"/>
                    </xsl:when>
                    <xsl:when test="nota:has-class(., 'precedingemptyline')">
                        <xsl:value-of select="'NormalPlusSpaceBefore'"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- PAGENUM (block) -->
    <xsl:template mode="DOCUMENT" match="pagenum">
        <!-- Somewhat hackish: Create intermediary paragraph to ensure that the
        inline <pagenum> template functions properly; otherwise an extra line
        will be inserted below -->
        <xsl:variable name="intermediaryParagraph" as="node()">
            <p><xsl:copy-of select="."/></p>
        </xsl:variable>
        <xsl:apply-templates mode="DOCUMENT" select="$intermediaryParagraph"/>
    </xsl:template>
    <!-- Discard page numbers in non-table notes -->
    <xsl:template mode="DOCUMENT INLINE"
        match="pagenum[ancestor::note[not(nota:is-table-note(.))]]"/>
    <!-- PRODNOTE.(caption|imgprodnote): Image caption/description -->
    <xsl:template mode="DOCUMENT"
        match="prodnote[nota:has-class(., ('caption', 'imgprodnote'))]">
        <xsl:variable name="text" as="xs:string"
            select="if (nota:has-class(., 'caption')) then 'Billedtekst'
                    else 'Billedbeskrivelse'"/>
        <xsl:call-template name="PARAGRAPH">
            <xsl:with-param name="maintainContext" select="true()"/>
            <xsl:with-param name="content">
                <xsl:value-of select="concat('[', $text, ' start]')"/>
            </xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="BLOCKS_OR_PARAGRAPH"/>
        <xsl:call-template name="PARAGRAPH">
            <xsl:with-param name="maintainContext" select="true()"/>
            <xsl:with-param name="content">
                <xsl:value-of select="concat('[', $text, ' slut]')"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- SIDEBAR -->
    <xsl:template mode="DOCUMENT" match="sidebar">
        <xsl:call-template name="PARAGRAPH">
            <xsl:with-param name="content">
                [Rammetekst start]
            </xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="BLOCKS_OR_PARAGRAPH"/>
        <xsl:call-template name="PARAGRAPH">
            <xsl:with-param name="content">
                [Rammetekst slut]
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- TABLE -->
    <xsl:template mode="DOCUMENT" match="table">
        <xsl:if test="caption">
            <xsl:call-template name="PARAGRAPH">
                <xsl:with-param name="content" select="caption/node()"/>
                <xsl:with-param name="properties">
                    <w:keepNext/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
        <w:tbl>
            <w:tblPr>
                <w:tblStyle w:val="TableGrid"/>
                <w:tblInd w:w="{144 + nota:determine-indent-left(.)}"/>
                <xsl:if test="ancestor::note">
                    <w:tblCellMar>
                        <w:top w:w="96" w:type="dxa"/>
                        <w:end w:w="96" w:type="dxa"/>
                        <w:bottom w:w="96" w:type="dxa"/>
                        <w:start w:w="96" w:type="dxa"/>
                    </w:tblCellMar>
                </xsl:if>
            </w:tblPr>
            <w:tblGrid/>
            <!-- Apply templates to the first row: the row template will take
            care of subsequent rows -->
            <xsl:apply-templates mode="DOCUMENT" select="(tr|*/tr)[1]"/>
        </w:tbl>
    </xsl:template>
    <!-- TD and TH elements -->
    <xsl:template mode="DOCUMENT" match="td|th">
        <w:tc>
            <xsl:if test="@colspan or @rowspan">
                <w:tcPr>
                    <xsl:if test="@colspan">
                        <w:gridSpan w:val="{@colspan}"/>
                    </xsl:if>
                    <xsl:if test="@rowspan">
                        <w:vMerge w:val="restart"/>
                    </xsl:if>
                </w:tcPr>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="nota:has-block-elements(.)">
                    <xsl:variable name="initialInlineNodes" as="node()*"
                        select="*[nota:is-block-element(.)][1]/
                                preceding-sibling::node()"/>
                    <xsl:variable name="finalInlineNodes" as="node()*"
                        select="*[nota:is-block-element(.)][position() =
                                last()]/following-sibling::node()"/>
                    <xsl:if
                        test="$initialInlineNodes[normalize-space(.)]">
                        <xsl:call-template name="PARAGRAPH">
                            <xsl:with-param name="content"
                                select="$initialInlineNodes"/>
                        </xsl:call-template> 
                    </xsl:if>
                    <xsl:apply-templates mode="DOCUMENT"/>
                    <xsl:if
                        test="$finalInlineNodes[normalize-space(.)]">
                        <xsl:call-template name="PARAGRAPH">
                            <xsl:with-param name="content"
                                select="$finalInlineNodes"/>
                        </xsl:call-template> 
                    </xsl:if>
                    <xsl:variable name="finalParagraph" as="xs:boolean"
                        select="$initialInlineNodes[normalize-space(.)] or
                                $finalInlineNodes[normalize-space(.)] or
                                *[nota:is-block-element(.)][not(self::table)]"/>
                    <xsl:if test="not($finalParagraph)">
                        <w:p/>
                    </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="PARAGRAPH">
                        <xsl:with-param name="content" select="node()"/>
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
        </w:tc>
    </xsl:template>
    <!-- TR: DTBook-style merged cells (with @colspan and @rowspan) are
    converted at this stage -->
    <xsl:template mode="DOCUMENT" match="tr">
        <xsl:param name="previousRow" as="node()*"/>
        <xsl:variable name="currentRow" as="node()">
            <!-- An intermediate variable is needed to store generated cell
            references, as we need to count them later -->
            <xsl:variable name="cells" as="node()+">
                <!-- Examine the current row -->
                <xsl:for-each select="td|th">
                    <xsl:variable name="position" as="xs:integer"
                        select="position()"/>
                    <!-- Get the corresponding cell in the previous row -->
                    <xsl:variable name="cellAbove" as="node()*"
                        select="$previousRow/*[$position]"/>
                    <!-- Check if cellAbove is the first in a "sequence" of
                    rowspan cells. Empty must should be inserted all at once if
                    the table layout is to be preserved. Example:
                    +++++++++++++++++++++++++
                    | 1.1 | 1.2 | 1.3 | 1.4 |
                    |     |     +++++++++++++
                    |     |     | 2.1 | 2.2 |
                    +++++++++++++++++++++++++
                    In this table two empty cells, "representing" 1.1 and 1.2,
                    should be inserted before cell 2.1. We do this by inserting
                    the cell immediately above (1.1) along with all adjacent
                    cells with @rowspan > 1, in this case 1.2. In order to
                    ensure that 2.2 does not also inherit an empty cell from
                    1.2, we do not insert empty cells if the cell above is
                    preceded by cells with @rowspan > 1; these cells should
                    already be in place at previous positions -->
                    <xsl:variable name="firstRowspanInSequence" as="xs:boolean"
                        select="$cellAbove/@rowspan &gt; 1 and
                                not($cellAbove/preceding-sibling::*[1]/
                                    @rowspan &gt; 1)"/>
                    <xsl:if test="$firstRowspanInSequence">
                        <!-- Insert <emptyCell> elements which will be
                        converted to vertically merged cells once templates are
                        applied -->
                        <xsl:for-each
                            select="$cellAbove|
                                    $cellAbove/following-sibling::*
                                        [@rowspan &gt; 1]
                                    intersect $cellAbove/following-sibling::*
                                        [not(@rowspan &gt; 1)][1]/
                                            preceding-sibling::*">
                            <emptyCell rowspan="{@rowspan - 1}">
                                <xsl:copy-of select="@colspan"/>
                            </emptyCell>
                        </xsl:for-each>
                    </xsl:if>
                    <!-- Reference the current cell; inserting a reference
                    rather than a copy makes it possible to apply templates to
                    the cell in context -->
                    <cell cellref="{$position}">
                        <xsl:copy-of select="@colspan|@rowspan"/>
                    </cell>
                </xsl:for-each>
            </xsl:variable>
            <!-- Get the number of cells, including newly generated empty
            cells, in the current row -->
            <xsl:variable name="currentRowLength" as="xs:integer"
                select="count($cells)"/>
            <row>
                <xsl:copy-of select="$cells"/>
                <!-- There may be spanning cells in previous rows which descend
                beyond the end of this row: insert placeholders for these at
                this point -->
                <xsl:for-each
                    select="$previousRow/*[position() gt $currentRowLength]
                            [@rowspan &gt; 1]">
                    <emptyCell rowspan="{@rowspan - 1}">
                        <xsl:copy-of select="@colspan"/>
                    </emptyCell>
                </xsl:for-each>
            </row>
        </xsl:variable>
        <!-- Save a reference to the current row context -->
        <xsl:variable name="context" as="node()" select="."/>
        <w:tr>
            <xsl:if test="parent::thead">
                <w:trPr>
                    <w:tblHeader/>
                </w:trPr>
            </xsl:if>
            <!-- Apply templates to existing cells and create merged cells
            where needed -->
            <xsl:for-each select="$currentRow/*">
                <xsl:choose>
                    <xsl:when test="self::emptyCell">
                        <xsl:call-template name="TABLE_CELL_MERGED"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates mode="DOCUMENT"
                            select="$context/(td|th)
                                    [position() = current()/@cellref]"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </w:tr>
        <!-- Apply templates to the following row with the current row as
        parameter. This is somewhat messy: selecting only following siblings is
        insufficient due to <thead>, <tbody> and similar ancestors -->
        <xsl:apply-templates mode="DOCUMENT"
            select="(following::tr intersect ancestor::table[1]//tr)[1]">
            <xsl:with-param name="previousRow" select="$currentRow"/>
        </xsl:apply-templates>
    </xsl:template>
    <!-- INLINE MODE: Inline elements -->
    <!-- GENERIC TEXT TEMPLATE: Handles non-whitespace text nodes -->
    <xsl:template mode="INLINE" match="text()[normalize-space(.)]">
        <xsl:param name="runProperties" tunnel="yes" as="node()*"/>
        <!-- A parameter may be set to avoid whitespace normalisation (for use
        with <code> markup) -->
        <xsl:param name="preserveSpace" as="xs:boolean" tunnel="yes"
            select="false()"/>
        <!-- Normalisation procedure: first, collapse internal whitespace and
        discard leading/trailing spaces using normalize-space(), then look at
        text nodes before and after to decide whether to add (or rather
        preserve) leading and/or trailing spaces. Note that leading
        whitespace after <pagenum> elements is never preserved -->
        <xsl:variable name="normalisedText" as="xs:string"
            select="normalize-space(.)"/>
        <xsl:variable name="previousTextNode" as="text()*"
            select="preceding::text()[normalize-space(.)][1] intersect
                    ancestor::*[nota:is-block-element(.)][1]//text()"/>
        <xsl:variable name="nextTextNode" as="text()*"
            select="following::text()[normalize-space(.)][1] intersect
                    ancestor::*[nota:is-block-element(.)][1]//text()"/>
        <xsl:variable name="precededByPagenum" as="xs:boolean"
            select="exists(preceding::*[1]/self::pagenum)"/>
        <xsl:variable name="leadingSpace" as="xs:string"
            select="if ($precededByPagenum) then ''
                    else if ($previousTextNode and matches(., '^\s')) then (
                        if (matches($previousTextNode, '\s$')) then '' else ' '
                    )
                    else ''"/>
        <xsl:variable name="trailingSpace" as="xs:string"
            select="if ($nextTextNode and matches(., '\s$')) then ' ' else ''"/>
        <xsl:variable name="text" as="xs:string"
            select="concat($leadingSpace, $normalisedText, $trailingSpace)"/>
        <xsl:call-template name="RUN">
            <xsl:with-param name="text"
                select="if ($preserveSpace) then . else $text"/>
            <xsl:with-param name="properties"
                select="$runProperties"/>
            <xsl:with-param name="addBreaks"
                select="if ($preserveSpace) then true() else false()"/>
        </xsl:call-template>
    </xsl:template>
    <!-- WHITESPACE HANDLING: If space is between elements, preserve it -->
    <xsl:template mode="INLINE" match="text()[not(normalize-space(.))]">
        <!-- If preserveSpace is set, whitespace is not collapsed to a single
        space -->
        <xsl:param name="preserveSpace" as="xs:boolean" tunnel="yes"
            select="false()"/>
        <xsl:variable name="interstitialSpace" as="xs:boolean"
            select="preceding-sibling::node()[1]/self::* and
                    following-sibling::node()[1]/self::*"/>
        <xsl:if test="$interstitialSpace">
            <xsl:call-template name="RUN">
                <xsl:with-param name="text"
                    select="if ($preserveSpace) then . else ' '"/>
                <!--<xsl:with-param name="properties"
                    select="nota:get-range-properties(.)"/>-->
                <xsl:with-param name="addBreaks"
                    select="if ($preserveSpace) then true() else false()"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    <!-- BR -->
    <xsl:template mode="INLINE" match="br">
        <w:r>
            <w:br w:type="line"/> 
        </w:r>
    </xsl:template>
    <!-- BR in <code>: Discard -->
    <xsl:template mode="INLINE" match="br[ancestor::code]"/>
    <!-- CODE: Preserves whitespace -->
    <xsl:template mode="INLINE" match="code">
        <xsl:apply-templates mode="INLINE">
            <xsl:with-param name="preserveSpace" tunnel="yes" select="true()"/>
        </xsl:apply-templates>
    </xsl:template>
    <!-- EM -->
    <xsl:template mode="INLINE" match="em">
        <xsl:param name="runProperties" as="node()*" tunnel="yes"/>
        <xsl:apply-templates mode="INLINE">
            <xsl:with-param name="runProperties" tunnel="yes">
                <xsl:copy-of select="$runProperties"/>
                <w:i/>
            </xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>
    <!-- IMG -->
    <xsl:template mode="INLINE" match="img">
        <xsl:variable name="count" as="xs:integer"
            select="count(preceding::img) + 1"/>
        <xsl:variable name="id" as="xs:string"
            select="concat('image', $count)"/>
        <xsl:variable name="fileName" as="xs:string"
            select="tokenize(@src, '/')[position() = last()]"/>
        <xsl:variable name="height" as="xs:integer"
            select="if (@height) then @height cast as xs:integer else 300"/>
        <xsl:variable name="width" as="xs:integer"
            select="if (@width) then @width cast as xs:integer else 300"/>
        <w:r>
            <w:rPr>
                <w:noProof/>
            </w:rPr>
            <w:drawing>
                <wp:inline>
                    <wp:extent cx="{$width * 9525}" cy="{$height * 9525}"/>
                    <wp:docPr id="{$count}" name="{$fileName}"
                        descr="{@alt}"/>
                    <a:graphic>
                        <a:graphicData
                            uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
                            <pic:pic>
                                <pic:nvPicPr>
                                    <pic:cNvPr id="{$count + 1}"
                                        name="{$fileName}"
                                        descr="{@alt}"/>
                                    <pic:cNvPicPr>
                                        <a:picLocks noChangeAspect="1"/>
                                    </pic:cNvPicPr>
                                    <pic:nvPr/>
                                </pic:nvPicPr>
                                <pic:blipFill>
                                    <a:blip r:embed="{$id}"/>
                                    <a:stretch>
                                        <a:fillRect/>
                                    </a:stretch>
                                </pic:blipFill>
                                <pic:spPr>
                                    <a:xfrm>
                                        <a:ext cx="{$width * 9525}"
                                            cy="{$height * 9525}"/>
                                    </a:xfrm>
                                    <a:prstGeom prst="rect">
                                        <a:avLst/>
                                    </a:prstGeom>
                                    <a:noFill/>
                                    <a:ln>
                                        <a:noFill/>
                                    </a:ln>
                                </pic:spPr>
                            </pic:pic>
                        </a:graphicData>
                    </a:graphic>
                </wp:inline>
            </w:drawing>
        </w:r>
    </xsl:template>
    <!-- LINE|P.line: Not actually inline elements, but treated as such for
    convenience's sake -->
    <xsl:template mode="INLINE" match="line|p[nota:has-class(., 'line')]">
        <xsl:apply-templates mode="INLINE"/>
        <xsl:if test="position() lt last()">
            <w:r>
                <w:br w:type="line"/>
            </w:r>
        </xsl:if>
    </xsl:template>
    <!-- LINENUM -->
    <xsl:template mode="INLINE" match="linenum">
        <xsl:variable name="trailingSpace" as="xs:string"
            select="if (matches(following::text()[1], '^\s')) then ''
                    else ' '"/>
        <xsl:call-template name="RUN">
            <xsl:with-param name="text"
                select="concat(normalize-space(.), $trailingSpace)"/>
        </xsl:call-template>
    </xsl:template>
    <!-- NOTEREF -->
    <xsl:template mode="INLINE" match="noteref">
        <xsl:variable name="reference" as="xs:string"
            select="replace(@idref, '#', '')"/>
        <xsl:variable name="correspondingNote" as="node()*"
            select="key('notes', $reference)"/>
        <xsl:variable name="position" as="xs:integer"
            select="1 + count(preceding::noteref)"/>
        <xsl:call-template name="RUN">
            <xsl:with-param name="text" select="."/>
            <xsl:with-param name="properties">
                <w:rStyle w:val="NoteReference"/>
                <xsl:choose>
                    <xsl:when test="nota:is-endnote($correspondingNote)">
                        <w:endnoteReference w:id="{$position}"
                            w:customMarkFollows="1"/>
                    </xsl:when>
                    <xsl:when test="nota:is-footnote($correspondingNote)">
                        <w:footnoteReference w:id="{$position}"
                            w:customMarkFollows="1"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- PAGENUM (inline) -->
    <xsl:template mode="INLINE" match="pagenum">
        <xsl:call-template name="RUN">
            <xsl:with-param name="text" select="concat('[Side ', ., ']')"/>
            <xsl:with-param name="language" select="'da'"/>
            <xsl:with-param name="wordContentBefore">
                <w:br w:type="page"/>
            </xsl:with-param>
            <xsl:with-param name="wordContentAfter">
                <xsl:if test="not(nota:is-last-node-within-block(.))">
                    <w:br w:type="line"/>
                </xsl:if>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- SPAN.note_identifier -->
    <xsl:template mode="INLINE"
        match="span[nota:has-class(., 'note_identifier')]">
        <xsl:call-template name="RUN">
            <xsl:with-param name="text" select="."/>
            <xsl:with-param name="properties">
                <w:rStyle w:val="NoteReference"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- SPAN.underlined -->
    <xsl:template mode="INLINE" match="span[nota:has-class(., 'underlined')]">
        <xsl:param name="runProperties" as="node()*" tunnel="yes"/>
        <xsl:apply-templates mode="INLINE">
            <xsl:with-param name="runProperties" tunnel="yes">
                <xsl:copy-of select="$runProperties"/>
                <w:u w:val="single"/>
            </xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>
    <!-- STRONG -->
    <xsl:template mode="INLINE" match="strong">
        <xsl:param name="runProperties" as="node()*" tunnel="yes"/>
        <xsl:apply-templates mode="INLINE">
            <xsl:with-param name="runProperties" tunnel="yes">
                <xsl:copy-of select="$runProperties"/>
                <w:b/>
            </xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>
    <!-- SUB -->
    <xsl:template mode="INLINE" match="sub">
        <xsl:param name="runProperties" as="node()*" tunnel="yes"/>
        <xsl:apply-templates mode="INLINE">
            <xsl:with-param name="runProperties" tunnel="yes">
                <xsl:copy-of select="$runProperties"/>
                <w:vertAlign w:val="subscript"/>
            </xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>
    <!-- SUP -->
    <xsl:template mode="INLINE" match="sup">
        <xsl:param name="runProperties" as="node()*" tunnel="yes"/>
        <xsl:apply-templates mode="INLINE">
            <xsl:with-param name="runProperties" tunnel="yes">
                <xsl:copy-of select="$runProperties"/>
                <w:vertAlign w:val="superscript"/>
            </xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>
</xsl:stylesheet>