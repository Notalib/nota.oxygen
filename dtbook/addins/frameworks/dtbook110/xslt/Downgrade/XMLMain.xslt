﻿<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl="http://exslt.org/common" exclude-result-prefixes="exsl"
>
  <xsl:output method="xml" indent="yes" encoding="utf-8"/>

  <xsl:include href ="MetaServices.xslt"/>
  

  <!-- 
    Denne template omdanner head og frontmatter til 
    konverterbar xml
    -->

  <xsl:template name ="xml2xml">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match ="head">
    <head>
      <xsl:apply-templates />
      <xsl:call-template name ="CreateMeta"/>
    </head>
  </xsl:template>

  <xsl:template match ="title">
    <title>
      <xsl:value-of select ="."/>
    </title>
  </xsl:template >

  <xsl:template match ="meta"/>

  <xsl:template match ="doctitle" name ="doctitle">
    <xsl:copy-of select ="./doctitle"/>
  </xsl:template>

  <xsl:template match ="book">
    <book>
      
      <!--Check om der er et dc:language metatag - dette sprog skal påføres book-->
      <xsl:if test ="/dtbook/head/meta[@name='dc:language']">

        <xsl:variable name ="ISOLanguage">
          <xsl:call-template name ="GetISOLang"/>
        </xsl:variable>
        
        <xsl:attribute name ="lang">
         <xsl:value-of select ="$ISOLanguage"/>
        </xsl:attribute>
      </xsl:if>
      
      <xsl:apply-templates/>
      
    </book>
  </xsl:template>

  <xsl:template match ="frontmatter">
    <frontmatter>
      <xsl:call-template name ="doctitle"/>
      <xsl:call-template name ="MetaServices"/>
    </frontmatter>
  </xsl:template>

  <xsl:template match ="bodymatter">
    <bodymatter>
      <xsl:copy-of select ="."/>
    </bodymatter>
  </xsl:template>

  <xsl:template match ="rearmatter">
    <rearmatter>
      <xsl:copy-of select ="."/>
    </rearmatter>
  </xsl:template>

  <xsl:template name ="CreateMeta">

    <meta>
      <xsl:attribute name ="name">
        <xsl:text>dbb:TrackingID</xsl:text>
      </xsl:attribute>

      <xsl:attribute name ="content">
        <xsl:value-of select ="$stampValue"/>
      </xsl:attribute>
    </meta>

    <xsl:if test ="./meta[@name='dc:identifier']">
      <xsl:copy-of select ="./meta[@name='dc:identifier']"/>
    </xsl:if>

    <xsl:if test ="./meta[@name='dc:title']">
      <xsl:copy-of select ="./meta[@name='dc:title']"/>
    </xsl:if>

    <xsl:if test ="./meta[@name='dc:creator']">
      <xsl:copy-of select ="./meta[@name='dc:creator']"/>
    </xsl:if>

    <xsl:if test ="./meta[@name='dc:publisher']">
      <xsl:copy-of select ="./meta[@name='dc:publisher']"/>
    </xsl:if>

    <xsl:if test ="./meta[@name='dc:date']">
      <xsl:copy-of select ="./meta[@name='dc:date']"/>
    </xsl:if>

  </xsl:template>

</xsl:stylesheet>
