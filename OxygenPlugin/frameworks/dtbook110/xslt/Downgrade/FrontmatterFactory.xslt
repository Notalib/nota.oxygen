<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl="http://exslt.org/common" exclude-result-prefixes="exsl"
>
  <xsl:output method ="xml" encoding ="utf-8"/>
  <xsl:include href ="DoubbleClass.xslt"/>
  <xsl:variable name="FrontNames" select="document('frontmatter_classes.xml')"/>

  <xsl:template name ="CreateFrontmatterContent">

    <xsl:param name ="frontmatterNode"/>

    <!-- Hovedloop -->
    
    <!--Gælder kun dobbelte klassenavne-->
    <xsl:for-each select ="$FrontNames/frontmatter/frontname">

       <xsl:call-template name ="ExecuteFrontName">
        <xsl:with-param name ="EFN_FrontNameNode" select ="self::node()"/>
        <xsl:with-param name ="EFN_FMatter" select ="$frontmatterNode"/>
         
      </xsl:call-template>
      
    </xsl:for-each>
    
    <!--check frontmatter for singleClassname-->

    <xsl:for-each select ="$frontmatterNode/level">
      <xsl:call-template name ="ExecuteFrontClass">
        <xsl:with-param name ="EFC_FrontmatterClass" select ="self::node()"/>
      </xsl:call-template>
    </xsl:for-each>
    
  </xsl:template>

 
  <!--*****************************************************************************-->

  <xsl:template name ="ExecuteFrontClass">
    <xsl:param name ="EFC_FrontmatterClass"/>
    <!--Check i FrontNames om dennne levelclass er tilladt-->
    
  </xsl:template>
  
  <!--*********************************************************************************-->
  <xsl:template name ="ExecuteFrontName">
    <xsl:param name ="EFN_FrontNameNode"/>
    <xsl:param name ="EFN_FMatter"/>

 

    <!-- Hiv attributværdier ud (selvom det ikke vides om der bliver brug for dem -->
    <xsl:variable name ="FrontName" select ="string($EFN_FrontNameNode/@name)"/>
    <xsl:variable name ="FrontType" select ="string($EFN_FrontNameNode/@type)"/>

    <!-- 
    Her skal der ske en forgrening
    den ene gren er enkelte klassenavne - den anden er 
    dobbelte klassenavne
    -->

    <xsl:choose>
      <xsl:when test ="$FrontType='physical'">

        <xsl:call-template name ="ProcessDoubbleClass">
          <xsl:with-param name ="PDC_FrontNameNode" select ="$EFN_FrontNameNode"/>
          <xsl:with-param name ="PDC_FMatter" select ="$EFN_FMatter"/>
          <xsl:with-param name ="PDC_FrontName" select ="$FrontName"/>
          <xsl:with-param name ="PDC_FNames" select ="$FrontNames"/>
        </xsl:call-template>
      </xsl:when>

      <!--Nu bliver otherwise udkommenteret - det udelukker effektivt andet end double classes-->
      <!--Hvad sker der her - hvorfor skal det udkommenteres..??
      Udkommenterig fjernet 2010.02.15
      -->
      <xsl:otherwise>
      
         
        <!--Enkelt klassenavn
      Er der et match i frontmatter? læg resultat i variabel-->
      
        <xsl:variable name ="IsMatch">
          <xsl:call-template name ="CheckFrontmatterForMatch">
            <xsl:with-param name ="CFM_FMatter" select ="$EFN_FMatter"/>
            <xsl:with-param name ="CFM_FName" select ="$FrontName"/>
          </xsl:call-template>
        </xsl:variable>

        <xsl:if test ="contains($IsMatch,'1')">
           <!--Der er et match-->

          <xsl:call-template name ="CreateLevel">
            <xsl:with-param name ="CL_FrontNameNode" select ="self::node()"/>
            <xsl:with-param name ="CL_FMatter" select ="$EFN_FMatter"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>

    
  </xsl:template>

  <!--*****************************************************************************-->
  
  <xsl:template name ="CheckFrontmatterForMatch">
    <xsl:param name ="CFM_FMatter"/>
    <xsl:param name ="CFM_FName"/>

    <xsl:if test ="$CFM_FMatter/level[@class=$CFM_FName]">
      <xsl:value-of select ="'1'"/>
    </xsl:if>
    
  </xsl:template>
  
  <!--*****************************************************************************-->
  
  <xsl:template name ="CreateLevel">
    <xsl:param name ="CL_FrontNameNode"/>
    <xsl:param name ="CL_FMatter"/>

    <!-- Hvilket level drejer det sig om? -->
    <xsl:variable name ="Level">
      <xsl:call-template name ="FindLevel">
        <xsl:with-param name ="FL_FMatter" select ="$CL_FMatter"/>
        <xsl:with-param name ="FL_FrontNameNode" select ="$CL_FrontNameNode"/>
      </xsl:call-template>
    </xsl:variable>
    
    <!-- Det er allerede konstateret at der er et match, så derfor:-->
    <!-- 
    Indsæt et for each loop for hvert level i "Level". Ofte 
    er der kun et level i "Level", men der kan være flere
    -->

    <xsl:for-each select ="exsl:node-set($Level)/level">

      <level>

        <!--sæt class på-->
        <xsl:attribute name ="class">
          <xsl:value-of select ="string($CL_FrontNameNode/@name)"/>
        </xsl:attribute>


        <!-- 
      Er der en overskrifft i level ?
      Hvis nej bruges defhead
      -->

        <xsl:if test ="not(./levelhd)">

          <!-- Er der et defhead (Rettet 30.06.2010)-->
          <xsl:if test ="$CL_FrontNameNode/@defhead">

            <levelhd>

              <xsl:attribute name ="class">
                <xsl:value-of select ="string($CL_FrontNameNode/@name)"/>
              </xsl:attribute>

              <xsl:value-of select ="string($CL_FrontNameNode/@defhead)"/>
            </levelhd>
            
          </xsl:if>


        </xsl:if>

        <!-- Sæt resten af noderne på -->
        <xsl:for-each select ="./node()">
          <xsl:copy-of select ="."/>
        </xsl:for-each>

      </level>
      
    </xsl:for-each>
    
 
    <!--<level>
      
      --><!--sæt class på--><!--
      <xsl:attribute name ="class">
        <xsl:value-of select ="string($CL_FrontNameNode/@name)"/>
      </xsl:attribute>
      

      --><!-- 
      Er der en overskrifft i level ?
      Hvis nej bruges defhead
      --><!--

      <xsl:if test ="not(exsl:node-set($Level)/level/levelhd)">
        <levelhd>

          <xsl:attribute name ="class">
            <xsl:value-of select ="string($CL_FrontNameNode/@name)"/>
          </xsl:attribute>
          
          <xsl:value-of select ="string($CL_FrontNameNode/@defhead)"/>
        </levelhd>

      </xsl:if>
           
     --><!-- Sæt resten af noderne på --><!--
      <xsl:for-each select ="exsl:node-set($Level)/level/node()">
        <xsl:copy-of select ="."/>
      </xsl:for-each>
      
    </level>-->
    

  </xsl:template>

  <!--*****************************************************************************-->

  <xsl:template name ="FindLevel">

    <!-- Kun for levels med et enkelt klassenavn -->
    <xsl:param name ="FL_FMatter"/>
    <xsl:param name ="FL_FrontNameNode"/>

    <!-- Et par hjælpevariable -->
    <xsl:variable name ="FName" select ="string($FL_FrontNameNode/@name)"/>

    <!-- Hvis fr er af typen content så check med physicalnoder-->
    <xsl:if test ="$FL_FrontNameNode[@type='content']">

      <xsl:if test ="$FL_FMatter/level[@class=$FName]">
        <xsl:copy-of select ="$FL_FMatter/level[@class=$FName]"/>
      </xsl:if>

    </xsl:if>

  </xsl:template>

</xsl:stylesheet>
