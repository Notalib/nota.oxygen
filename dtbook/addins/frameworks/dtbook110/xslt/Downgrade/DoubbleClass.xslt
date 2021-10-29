﻿<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl="http://exslt.org/common" exclude-result-prefixes="exsl"
>
    <xsl:output method="xml" indent="yes" encoding="utf-8"/>


  <xsl:template name ="ProcessDoubbleClass">
    <xsl:param name ="PDC_FrontNameNode"/>
    <xsl:param name ="PDC_FMatter"/>
    <xsl:param name ="PDC_FrontName"/>
    <xsl:param name ="PDC_FNames"/>

    
    <!-- Er der et hit på physical name ?-->
    <xsl:variable name ="IsPhysicalMatch">
      <xsl:call-template name ="CheckFrontmatterForPhysicalMatch">
        <xsl:with-param name ="CFPM_FMatter" select ="$PDC_FMatter"/>
        <xsl:with-param name ="CFPM_PhysName" select ="$PDC_FrontName"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:if test ="contains($IsPhysicalMatch,'1')">
      <!-- Der var et match, dan et level-->


      <level>

        <xsl:attribute name ="class">
          <xsl:value-of select ="string($PDC_FrontNameNode/@name)"/>
        </xsl:attribute>
        <!-- 
     Fordi det drejer sig om en placeringsnode
     dannes heading udfra defhead
     -->
        <levelhd>

          <xsl:attribute name ="class">
            <xsl:value-of select ="string($PDC_FrontNameNode/@name)"/>
          </xsl:attribute>
          
          <xsl:value-of select ="string($PDC_FrontNameNode/@defhead)"/>
        </levelhd>
        
      <!-- 
    Nu skal det checkes om der er et hit på det 
    dobbelte klassenavn.
    Find først PDC_FrontNameNode's child (physical kan ikke optræde alene)
    -->
      <xsl:variable name ="RefType" select ="string($PDC_FrontNameNode/frontname/@reftype)"/>

      <xsl:for-each select ="$PDC_FNames/frontmatter/frontname[@type=$RefType]">
        <xsl:variable name ="FName" select ="./@name"/>
        <xsl:variable name ="Class1" select ="concat($FName,' ',$PDC_FrontName)" />
        <xsl:variable name ="Class2" select ="concat($PDC_FrontName,' ',$FName)" />

        <!-- Test for match -->
        <xsl:variable name ="IsMatch">
          <xsl:call-template name ="CheckFrontmatterForDoubbleMatch">
            <xsl:with-param name ="CFDM_FMatter" select ="$PDC_FMatter"/>
            <xsl:with-param name ="CFDM_FName1" select ="$Class1"/>
            <xsl:with-param name ="CFDM_FName2" select ="$Class2"/>
          </xsl:call-template>
        </xsl:variable>

        <xsl:if test ="$IsMatch='1'">
          <!-- Der er et match på dobbelt klassenavn -->

          <xsl:call-template name ="CreateDoubbleLevel">
            <xsl:with-param name ="CDL_FrontName1" select ="$Class1"/>
            <xsl:with-param name ="CDL_FrontName2" select ="$Class2"/>
            <xsl:with-param name ="CDL_FMatter" select ="$PDC_FMatter"/>
            <xsl:with-param name ="CDL_FrontNode" select ="$PDC_FrontNameNode"/>
            <xsl:with-param name ="CDL_SecondaryFrontNode" select ="self::node()"/>
          </xsl:call-template>
        </xsl:if>

      </xsl:for-each>

      </level>
      
    </xsl:if>
    
 
  </xsl:template>
  
  <!--*****************************************************************-->

  <xsl:template name ="CheckFrontmatterForPhysicalMatch">
    <xsl:param name ="CFPM_FMatter"/>
    <xsl:param name ="CFPM_PhysName"/>
   

    <xsl:for-each select ="$CFPM_FMatter/level">
      <xsl:if test ="contains(./@class,$CFPM_PhysName)">
        <xsl:value-of select ="'1'"/>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!--*****************************************************************-->

  <xsl:template name ="CheckFrontmatterForDoubbleMatch">
    <xsl:param name ="CFDM_FMatter"/>
    <xsl:param name ="CFDM_FName1"/>
    <xsl:param name ="CFDM_FName2"/>

    <xsl:choose>

     
      <xsl:when test ="$CFDM_FMatter/level[@class=$CFDM_FName1]">
        <xsl:value-of select ="'1'"/>
      </xsl:when>

      <xsl:when test ="$CFDM_FMatter/level[@class=$CFDM_FName2]">
        <xsl:value-of select ="'1'"/>
      </xsl:when>

      <xsl:otherwise>
        <xsl:value-of select ="'0'"/>
      </xsl:otherwise> 
    </xsl:choose>
    
  </xsl:template>
  
  <!--*****************************************************************-->
  
  <xsl:template name ="CreateDoubbleLevel">
    <xsl:param name ="CDL_FMatter"/>
    <xsl:param name ="CDL_FrontName1"/>
    <xsl:param name ="CDL_FrontName2"/>
    <xsl:param name ="CDL_FrontNode"/>
    <xsl:param name ="CDL_SecondaryFrontNode"/>

    <!-- Hent level -->
    <xsl:variable name ="DoubleLevel">
      <xsl:call-template name ="GetDoubleLevel">
        <xsl:with-param name ="FDL_FMatter" select ="$CDL_FMatter"/>
        <xsl:with-param name ="Class1" select ="$CDL_FrontName1"/>
        <xsl:with-param name ="Class2" select ="$CDL_FrontName2"/>
      </xsl:call-template>
    </xsl:variable>

    <!-- Det er allerede konstateret at der er et match, så derfor:-->
    <!-- 
    Indsæt et for each loop for hvert level i "Level". Ofte 
    er der kun et level i "Level", men der kan være flere
    -->

    <xsl:for-each select ="exsl:node-set($DoubleLevel)/level">

      <level>

        <!--sæt class på-->
        <xsl:attribute name ="class">
          <xsl:value-of select ="string($CDL_SecondaryFrontNode/@name)"/>
        </xsl:attribute>


        <!-- 
      Er der en overskrifft i level ?
      Hvis nej bruges defhead
      -->

        <xsl:if test ="not(./levelhd)">
          <levelhd>

            <xsl:attribute name ="class">
              <xsl:value-of select ="string($CDL_SecondaryFrontNode/@name)"/>
            </xsl:attribute>

            <xsl:value-of select ="string($CDL_SecondaryFrontNode/@defhead)"/>
          </levelhd>

        </xsl:if>

        <!-- Sæt resten af noderne på -->
        <xsl:for-each select ="./node()">
          <xsl:copy-of select ="."/>
        </xsl:for-each>

      </level>

    </xsl:for-each>
      
      <!-- Nyt level til indhold--><!--

      <level>

        <xsl:attribute name ="class">
          <xsl:value-of select ="string($CDL_SecondaryFrontNode/@name)"/>
        </xsl:attribute>
      
      <xsl:if test ="not(exsl:node-set($DoubleLevel)/level/levelhd)">
        <levelhd>
          <xsl:value-of select ="string($CDL_SecondaryFrontNode/@defhead)"/>
        </levelhd>

      </xsl:if>

      --><!-- Sæt resten af noderne på --><!--
        
        --><!-- Her er en fejl:
        
        Hvis der er flere levels med samme klassenavne bliver de 
        alle kørt igennem - derved kommer der til at mangle overskrifter
        på de enkelte levels
        --><!--
      <xsl:for-each select ="exsl:node-set($DoubleLevel)/level/node()">
        <xsl:copy-of select ="."/>
      </xsl:for-each>
        
      </level>-->
  
  </xsl:template>

  <!--*****************************************************************-->
  <xsl:template name ="GetDoubleLevel">
    <xsl:param name ="FDL_FMatter"/>
    <xsl:param name ="Class1"/>
    <xsl:param name ="Class2"/>


    <xsl:choose>
      <xsl:when test ="$FDL_FMatter/level[@class=$Class1]">
        <xsl:copy-of select ="$FDL_FMatter/level[@class=$Class1]"/>
      </xsl:when>

      <xsl:when test ="$FDL_FMatter/level[@class=$Class2]">
        <xsl:copy-of select ="$FDL_FMatter/level[@class=$Class2]"/>
      </xsl:when>

     
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>
