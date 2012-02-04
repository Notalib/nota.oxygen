<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl="http://exslt.org/common" exclude-result-prefixes="exsl"
>
    <xsl:output method="xml" encoding="utf-8"/>
  
  <xsl:variable name="upperCase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZÆØÅ'"/>
  <xsl:variable name="lowerCase" select="'abcdefghijklmnopqrstuvwxyzæøå'"/>

  <xsl:template name ="RemoveColon">
    <xsl:param name ="StringToClean"/>

    <xsl:param name ="CleanedString">

      <!-- det er allerede testet om strengen indeholder et kolon 
      -men ikke om det indeholder mellemrum + kolon + mellemrum,
      kolon + mellemrum, mellemrum + kolon, eller kolon-->

      <xsl:choose>
        <xsl:when test ="contains($StringToClean,' : ')">
          <xsl:value-of select ="substring-after($StringToClean,' : ')"/>
        </xsl:when>

        <xsl:when test ="contains($StringToClean,': ')">
          <xsl:value-of select ="substring-after($StringToClean,': ')"/>
        </xsl:when>

        <xsl:when test ="contains($StringToClean,' :')">
          <xsl:value-of select ="substring-after($StringToClean,':')"/>
        </xsl:when>

        <xsl:when test ="contains($StringToClean,':')">
          <xsl:value-of select ="substring-after($StringToClean,':')"/>
        </xsl:when>

        <xsl:otherwise>
          <xsl:value-of select ="$StringToClean"/>
        </xsl:otherwise>
        
      </xsl:choose>
      
    </xsl:param>

    <xsl:value-of select ="$CleanedString"/>

  </xsl:template>

  <xsl:template name ="ToUpper">
    <xsl:param name ="StringToConvert"/>

    <xsl:param name="UpperString">
      <xsl:value-of select ="translate($StringToConvert,$lowerCase,$upperCase)"/>
    </xsl:param>

    <xsl:value-of select ="$UpperString"/>
  </xsl:template>

  <xsl:template name ="FirstCharToUpper">
   
    <xsl:param name ="OrgString"/>
 
      <xsl:param name ="NewString">
      <xsl:call-template name ="ToUpper">
        <xsl:with-param name ="StringToConvert" select ="substring($OrgString,1,1)"/>
       
      </xsl:call-template>

       
    </xsl:param>

    <xsl:value-of select ="concat(string($NewString),substring($OrgString,2))"/>

   

  </xsl:template>
  
</xsl:stylesheet>
