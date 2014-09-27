<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:exsl="http://exslt.org/common" exclude-result-prefixes="exsl"
>
  <!--<xsl:output method="text"/>-->

  <!--Da languagekoden er blevet ændret skal begge typer kunne behandles-->

  <!-- Indlæs filen med sprogkoder -->
  <xsl:variable name="DBCLangCodes" select="document('DBCLangCodes.xml')"/>

  <xsl:template name ="GetLang">

    <!-- Den tilsendte sprogkode der skal behandles -->
    <xsl:param name ="LangCode"/>

    <xsl:param name ="LongLang">

      <xsl:choose>

        <xsl:when test ="string-length($LangCode)='0'">
          <xsl:value-of select ="'_none'"/>
        </xsl:when>
        
        <xsl:when test ="string-length($LangCode)='2'">
          
          <xsl:choose>
            
            <xsl:when test ="$DBCLangCodes/languagecodes/language[@TwoLetterISOLangCode=$LangCode]">
              <xsl:value-of select ="string($DBCLangCodes/languagecodes/language[@TwoLetterISOLangCode=$LangCode]/@name)"/>
            </xsl:when>

            <xsl:otherwise>
              <xsl:value-of select ="'_none'"/>
            </xsl:otherwise>
            
          </xsl:choose>
          
        </xsl:when>

        <xsl:when test ="string-length($LangCode)='3'">
          <xsl:choose>
            <xsl:when test ="$DBCLangCodes/languagecodes/language[@code=$LangCode]">
              <xsl:value-of select ="string($DBCLangCodes/languagecodes/language[@code=$LangCode]/@name)"/>
            </xsl:when>

            <xsl:otherwise>
              <xsl:value-of select ="'_none'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
      </xsl:choose>

    </xsl:param>
    <xsl:value-of select ="$LongLang"/>
  </xsl:template>



  <xsl:template name ="GetISOLangCode">

    <!-- Den tilsendte sprogkode der skal behandles -->
    <xsl:param name ="LangCode"/>

    <xsl:param name ="ISOLang">

      <!-- Returner den samme værdi-->
      <xsl:if test ="string-length($LangCode)='2'">
        <xsl:value-of select ="$LangCode"/>
      </xsl:if>

      <xsl:if test ="string-length($LangCode)='3'">
        <xsl:choose>
          <xsl:when test ="$DBCLangCodes/languagecodes/language[@code=$LangCode]">
            <xsl:value-of select ="string($DBCLangCodes/languagecodes/language[@code=$LangCode]/@TwoLetterISOLangCode)"/>
          </xsl:when>

          <xsl:otherwise>
            <xsl:value-of select ="'_none'"/>
          </xsl:otherwise>
        </xsl:choose>

      </xsl:if>
    </xsl:param>
    
    <xsl:value-of select ="$ISOLang"/>
    
  </xsl:template>
  
</xsl:stylesheet>
