<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:nota="http://www.nota.dk/dtbook2docx"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    exclude-result-prefixes="nota xs"
    version="2.0">
    <xsl:import href="dtbook2docx-functions.xsl"/>
    <xsl:template mode="RELATIONSHIPS" match="book">
        <Relationships
            xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="endnotes"
                Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/endnotes"
                Target="endnotes.xml"/>
            <Relationship Id="footnotes"
                Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/footnotes"
                Target="footnotes.xml"/>
            <xsl:for-each select=".//img">
                <xsl:variable name="fileName" as="xs:string"
                    select="nota:get-file-name-from-path(@src)"/>
                <Relationship
                    xmlns="http://schemas.openxmlformats.org/package/2006/relationships"
                    Id="{concat('image', position())}"
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image"
                    Target="{concat('media/', $fileName)}">
                </Relationship>
            </xsl:for-each>
            <Relationship Id="numbering"
                Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering"
                Target="numbering.xml"/>
            <Relationship Id="settings"
                Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings"
                Target="settings.xml"/>
            <Relationship Id="styles"
                Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles"
                Target="styles.xml"/>
        </Relationships>
    </xsl:template>
    
</xsl:stylesheet>