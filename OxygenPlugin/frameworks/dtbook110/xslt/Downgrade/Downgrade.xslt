<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" omit-xml-declaration="no" indent="yes"/>


  <xsl:include href ="XMLMain.xslt"/>
  <xsl:include href ="LevelDepth.xslt"/>
  <xsl:include href ="StandardXMLConverter.xslt"/>

  <xsl:param name="stampValue"></xsl:param>

  <!-- parameter der angiver om der skal dannes xml til en forside -->
  <xsl:param name ="FrontPage" select ="'no'"/>
  <xsl:param name ="OutputMode" select ="'xml'"/>


  <xsl:template match ="dtbook">


    <xsl:choose>

      <xsl:when test ="./head/meta[@name='prod:AutoBrailleReady'][@content='yes']">

        <xsl:variable name ="tmpDoc">
          <dtbook>
            <xsl:call-template name ="xml2xml"/>
          </dtbook>
        </xsl:variable>

        <xsl:call-template name ="StepTwo">
          <xsl:with-param name ="XMLDoc" select ="$tmpDoc"/>
        </xsl:call-template>

      </xsl:when>

      <xsl:otherwise>
        <xsl:if test ="./book/frontmatter/level[@class='title']">
          <!-- 
            brug det gamle stylesheet.
           -->

          <xsl:apply-templates mode ="xml" select ="self::node()"/>

        </xsl:if>

      </xsl:otherwise>

    </xsl:choose>

  </xsl:template>

  <xsl:template name ="StepTwo">

    <xsl:param name ="XMLDoc"/>
    <!-- Orden på depth -->
    <xsl:variable name ="DepthXMLDoc">
      <xsl:call-template name ="SetDepthInit">
        <xsl:with-param name ="XDoc" select ="$XMLDoc"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:call-template name ="ConvertXML">
      <xsl:with-param name ="NewXMLDoc" select ="$DepthXMLDoc"/>
    </xsl:call-template>

    
  </xsl:template>
  <!-- =========================================================================
normalize space
	============================================================================== -->
  <xsl:template match="*/text()[normalize-space()]">
    <xsl:value-of select="normalize-space()"/>
  </xsl:template>
  
  <xsl:template match="*/text()[not(normalize-space())]" />
  
</xsl:stylesheet>
