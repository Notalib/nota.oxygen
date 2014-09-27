<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl="http://exslt.org/common" exclude-result-prefixes="exsl"
>
    <xsl:output method="xml" indent="yes" omit-xml-declaration="no" />

  <xsl:template name ="ConvertXML">
    <xsl:param name ="NewXMLDoc"/>

    <xsl:for-each select ="exsl:node-set($NewXMLDoc)/node()">
      <xsl:if test ="self::dtbook">
        <xsl:apply-templates mode ="xml" select ="self::node()"/>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
<!-- Removed by OHA - another template with the same match and mode
  <xsl:template match ="dtbook" mode ="xml">
    <xsl:apply-templates mode ="xml"/>
  </xsl:template>
-->
  <xsl:template match ="bodymatter" mode ="xml">
    <bodymatter>
      <xsl:for-each select ="./node()">
        <xsl:copy-of select ="self::node()"/>
      </xsl:for-each>

<!--
      <xsl:choose>
        <xsl:when test ="/dtbook/book/rearmatter"/>
        <xsl:otherwise>

          <level class="kolofon" depth="1">
            <levelhd>Her slutter bogens tekst</levelhd>
            <p>Indlæst af:</p>
          </level>

        </xsl:otherwise>
      </xsl:choose>
-->
    </bodymatter>

  </xsl:template>

  <xsl:template match ="frontmatter" mode ="xml">
    <frontmatter>
      <xsl:for-each select ="./node()">
        <xsl:copy-of select ="self::node()"/>
      </xsl:for-each>
    </frontmatter>

  </xsl:template>

  
  <xsl:template match ="rearmatter" mode ="xml">
    <rearmatter>
      <xsl:for-each select ="./node()">
        <xsl:copy-of select ="self::node()"/>
      </xsl:for-each>

<!--
      <level class="kolofon" depth="1">
        <levelhd>Her slutter bogens tekst</levelhd>
        <p>Indlæst af:</p>
      </level>
-->
    </rearmatter>
  </xsl:template>

  <xsl:template match ="dtbook" mode ="xml">
    <dtbook>
      <xsl:attribute name ="version">
        <xsl:text>1.1.0</xsl:text>
      </xsl:attribute>
      <xsl:apply-templates mode ="xml"/>
    </dtbook>
  </xsl:template>

  <xsl:template match ="head" mode ="xml">
    <head>
      <xsl:for-each select ="./node()">
        <xsl:copy-of select ="self::node()"/>
      </xsl:for-each>
    </head>
  </xsl:template>

  <xsl:template match ="book" mode ="xml">
    <book>
      <xsl:if test ="@lang">
        <xsl:attribute name ="lang">
          <xsl:value-of select ="@lang"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates mode ="xml"/>
    </book>
  </xsl:template>
</xsl:stylesheet>
