<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:nota="http://www.nota.dk/dtbook2docx"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    exclude-result-prefixes="nota xs"
    version="2.0">
    <xsl:import href="dtbook2docx-functions.xsl"/>
    <xsl:template mode="FOOTNOTES" match="book">
        <w:footnotes
            xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
            xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"
            xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
            xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
            xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing">
            <w:footnote w:type="separator" w:id="-1">
                <w:p>
                    <w:pPr>
                        <w:spacing w:after="0" w:line="240" w:lineRule="auto"/>
                    </w:pPr>
                    <w:r>
                        <w:separator/>
                    </w:r>
                </w:p>
            </w:footnote>
            <w:footnote w:type="continuationSeparator" w:id="0">
                <w:p>
                    <w:pPr>
                        <w:spacing w:after="0" w:line="240" w:lineRule="auto"/>
                    </w:pPr>
                    <w:r>
                        <w:continuationSeparator/>
                    </w:r>
                </w:p>
            </w:footnote>
            <xsl:for-each select="descendant::noteref">
                <xsl:variable name="reference" as="xs:string"
                    select="replace(@idref, '#', '')"/>
                <xsl:variable name="correspondingNote" as="node()*"
                    select="key('notes', $reference)"/>
                <xsl:if test="nota:is-footnote($correspondingNote)">
                    <w:footnote w:id="{position()}">
                        <xsl:apply-templates mode="DOCUMENT"
                            select="$correspondingNote/node()">
                            <xsl:with-param name="paragraphStyle" tunnel="yes"
                                select="'NoteText'"/>
                        </xsl:apply-templates>
                    </w:footnote>
                </xsl:if>
            </xsl:for-each>
            <!--<xsl:for-each select=".//note[nota:is-footnote(.)]">
                <w:footnote w:id="{position()}">
                    <xsl:apply-templates mode="DOCUMENT"/>
                </w:footnote>
            </xsl:for-each>-->
        </w:footnotes>
    </xsl:template>
</xsl:stylesheet>