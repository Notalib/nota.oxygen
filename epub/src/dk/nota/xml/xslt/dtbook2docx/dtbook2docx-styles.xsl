<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:nota="http://www.nota.dk/dtbook2docx"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    exclude-result-prefixes="nota xs"
    version="2.0">
    <xsl:variable name="language" as="xs:string"
        select="if (../book/@lang) then ../book/@lang
                else 'da'"/>
    <xsl:template mode="STYLES" match="book">
        <w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
            <w:docDefaults>
                <w:rPrDefault>
                    <w:rPr>
                        <w:rFonts w:asciiTheme="minorHAnsi" w:eastAsiaTheme="minorHAnsi"
                            w:hAnsiTheme="minorHAnsi" w:cstheme="minorBidi"/>
                        <w:sz w:val="22"/>
                        <w:szCs w:val="22"/>
                        <w:lang w:val="{$language}"/>
                    </w:rPr>
                </w:rPrDefault>
                <w:pPrDefault>
                    <w:pPr>
                        <w:spacing w:after="144" w:lineRule="auto"/>
                    </w:pPr>
                </w:pPrDefault>
            </w:docDefaults>
            <w:style w:type="paragraph" w:styleId="Normal">
                
            </w:style>
            <w:style w:type="paragraph" w:styleId="NormalPlusSpaceBefore">
                <w:basedOn w:val="Normal"/>
                <w:pPr>
                    <w:spacing w:before="288"/>
                </w:pPr>
            </w:style>
            <w:style w:type="paragraph" w:styleId="Bridgehead">
                <w:basedOn w:val="Normal"/>
                <w:rPr>
                    <w:b/>
                </w:rPr>
            </w:style>
            <w:style w:type="paragraph" w:styleId="TOCEntry1">
                <w:name w:val="Indholdsfortegnelse 1"/>
                <w:basedOn w:val="Normal"/>
                <w:next w:val="TOCEntry1"/>
                <w:pPr>
                    <w:tabs>
                        <w:tab w:val="end" w:leader="dot" w:pos="10080"/>
                    </w:tabs>
                    <w:ind w:left="360" w:hanging="360"/>
                </w:pPr>
            </w:style>
            <w:style w:type="paragraph" w:styleId="TOCEntry2">
                <w:name w:val="Indholdsfortegnelse 2"/>
                <w:basedOn w:val="TOCEntry1"/>
                <w:next w:val="TOCEntry2"/>
                <w:pPr>
                    <w:ind w:left="720" w:hanging="360"/>
                </w:pPr>
            </w:style>
            <w:style w:type="paragraph" w:styleId="TOCEntry3">
                <w:name w:val="Indholdsfortegnelse 3"/>
                <w:basedOn w:val="TOCEntry1"/>
                <w:next w:val="TOCEntry3"/>
                <w:pPr>
                    <w:ind w:left="1080" w:hanging="360"/>
                </w:pPr>
            </w:style>
            <w:style w:type="paragraph" w:styleId="TOCEntry4">
                <w:name w:val="Indholdsfortegnelse 4"/>
                <w:basedOn w:val="TOCEntry1"/>
                <w:next w:val="TOCEntry4"/>
                <w:pPr>
                    <w:ind w:left="1440" w:hanging="360"/>
                </w:pPr>
            </w:style>
            <w:style w:type="paragraph" w:styleId="TOCEntry5">
                <w:name w:val="Indholdsfortegnelse 5"/>
                <w:basedOn w:val="TOCEntry1"/>
                <w:next w:val="TOCEntry5"/>
                <w:pPr>
                    <w:ind w:left="1800" w:hanging="360"/>
                </w:pPr>
            </w:style>
            <w:style w:type="paragraph" w:styleId="TOCEntry6">
                <w:name w:val="Indholdsfortegnelse 6"/>
                <w:basedOn w:val="TOCEntry1"/>
                <w:next w:val="TOCEntry6"/>
                <w:pPr>
                    <w:ind w:left="2160" w:hanging="360"/>
                </w:pPr>
            </w:style>
            <w:style w:type="paragraph" w:styleId="NoteText">
                <w:name w:val="Notetekst"/>
                <w:basedOn w:val="Normal"/>
                <w:next w:val="NoteText"/>
                <w:pPr>
                    <w:spacing w:after="96" w:lineRule="auto"/>
                </w:pPr>
                <w:rPr>
                    <w:sz w:val="18"/>
                </w:rPr>
            </w:style>
            <w:style w:type="character" w:styleId="NoteReference">
                <w:name w:val="Notehenvisning"/>
                <w:next w:val="NoteReference"/>
                <w:rPr>
                    <w:vertAlign w:val="superscript"/>
                </w:rPr>
            </w:style>
            <w:style w:type="paragraph" w:styleId="Blockquote">
                <w:name w:val="Blokcitat"/>
                <w:basedOn w:val="Normal"/>
                <w:next w:val="Blockquote"/>
                <w:pPr>
                    <w:ind w:left="720" w:right="720"/>
                </w:pPr>
            </w:style>
            <w:style w:type="paragraph" w:styleId="Title">
                <w:name w:val="Titel"/>
                <w:basedOn w:val="Normal"/>
                <w:next w:val="Normal"/>
                <w:qFormat/>
                <w:pPr>
                    <w:keepNext/>
                    <w:keepLines/>
                </w:pPr>
                <w:rPr>
                    <w:sz w:val="52"/>
                </w:rPr>
            </w:style>
            <w:style w:type="paragraph" w:styleId="Heading1">
                <w:name w:val="Overskrift 1"/>
                <w:basedOn w:val="Normal"/>
                <w:next w:val="Normal"/>
                <w:qFormat/>
                <w:pPr>
                    <w:keepNext/>
                    <w:keepLines/>
                    <w:outlineLvl w:val="0"/>
                </w:pPr>
                <w:rPr>
                    <w:sz w:val="48"/>
                </w:rPr>
            </w:style>
            <w:style w:type="paragraph" w:styleId="Heading2">
                <w:name w:val="Overskrift 2"/>
                <w:basedOn w:val="Normal"/>
                <w:next w:val="Normal"/>
                <w:qFormat/>
                <w:pPr>
                    <w:keepNext/>
                    <w:keepLines/>
                    <w:outlineLvl w:val="1"/>
                </w:pPr>
                <w:rPr>
                    <w:sz w:val="44"/>
                </w:rPr>
            </w:style>
            <w:style w:type="paragraph" w:styleId="Heading3">
                <w:name w:val="Overskrift 3"/>
                <w:basedOn w:val="Normal"/>
                <w:next w:val="Normal"/>
                <w:qFormat/>
                <w:pPr>
                    <w:keepNext/>
                    <w:keepLines/>
                    <w:outlineLvl w:val="2"/>
                </w:pPr>
                <w:rPr>
                    <w:sz w:val="40"/>
                </w:rPr>
            </w:style>
            <w:style w:type="paragraph" w:styleId="Heading4">
                <w:name w:val="Overskrift 4"/>
                <w:basedOn w:val="Normal"/>
                <w:next w:val="Normal"/>
                <w:qFormat/>
                <w:pPr>
                    <w:keepNext/>
                    <w:keepLines/>
                    <w:outlineLvl w:val="3"/>
                </w:pPr>
                <w:rPr>
                    <w:sz w:val="36"/>
                </w:rPr>
            </w:style>
            <w:style w:type="paragraph" w:styleId="Heading5">
                <w:name w:val="Overskrift 5"/>
                <w:basedOn w:val="Normal"/>
                <w:next w:val="Normal"/>
                <w:qFormat/>
                <w:pPr>
                    <w:keepNext/>
                    <w:keepLines/>
                    <w:outlineLvl w:val="4"/>
                </w:pPr>
                <w:rPr>
                    <w:sz w:val="32"/>
                </w:rPr>
            </w:style>
            <w:style w:type="paragraph" w:styleId="Heading6">
                <w:name w:val="Overskrift 6"/>
                <w:basedOn w:val="Normal"/>
                <w:next w:val="Normal"/>
                <w:qFormat/>
                <w:pPr>
                    <w:keepNext/>
                    <w:keepLines/>
                    <w:outlineLvl w:val="5"/>
                </w:pPr>
                <w:rPr>
                    <w:sz w:val="28"/>
                </w:rPr>
            </w:style>
            <w:style w:type="paragraph" w:styleId="Heading7">
                <w:name w:val="Overskrift 7"/>
                <w:basedOn w:val="Normal"/>
                <w:next w:val="Normal"/>
                <w:qFormat/>
                <w:pPr>
                    <w:keepNext/>
                    <w:keepLines/>
                    <w:outlineLvl w:val="6"/>
                </w:pPr>
                <w:rPr>
                    <w:sz w:val="24"/>
                </w:rPr>
            </w:style>
            <w:style w:type="table" w:styleId="TableGrid">
                <w:name w:val="Tabel"/>
                <w:qFormat/>
                <w:tblPr>
                    <w:tblBorders>
                        <w:top w:val="single" w:sz="1" w:space="0"/>
                        <w:end w:val="single" w:sz="1" w:space="0"/>
                        <w:bottom w:val="single" w:sz="1" w:space="0"/>
                        <w:start w:val="single" w:sz="1" w:space="0"/>
                        <w:insideH w:val="single" w:sz="1" w:space="0"/>
                        <w:insideV w:val="single" w:sz="1" w:space="0"/>
                    </w:tblBorders>
                    <w:tblCellMar>
                        <w:top w:w="144" w:type="dxa"/>
                        <w:end w:w="144" w:type="dxa"/>
                        <w:bottom w:w="144" w:type="dxa"/>
                        <w:start w:w="144" w:type="dxa"/>
                    </w:tblCellMar>
                </w:tblPr>
            </w:style>
        </w:styles>
    </xsl:template>
</xsl:stylesheet>