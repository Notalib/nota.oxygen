<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl="http://exslt.org/common" exclude-result-prefixes="exsl"
>

  <xsl:output indent ="yes" method="xml" encoding ="utf-8"/>
  <xsl:template name ="SetDepthInit">

    <xsl:param name ="XDoc"/>
    <xsl:apply-templates mode ="depth" select ="exsl:node-set($XDoc)/dtbook"/>
  </xsl:template>
<!--******************************************************************-->
  <xsl:template match ="frontmatter" mode ="depth">

    <frontmatter>

      <xsl:if test ="child::doctitle">
        <doctitle>
          <xsl:value-of select ="./doctitle"/>
        </doctitle>  
      </xsl:if>
      
    <xsl:for-each select ="./level">
      <level>
        <xsl:if test="@class">
          <xsl:attribute name="class">
            <xsl:value-of select="@class"/>
          </xsl:attribute>
        </xsl:if>

        <xsl:attribute name ="depth">
          <xsl:value-of select ="'1'"/>
        </xsl:attribute>

        <xsl:for-each select ="./node()">
          <xsl:choose>
            <xsl:when test ="self::level">
              <xsl:call-template name ="SetDepth">
                <xsl:with-param name ="IntDepth" select ="'1'"/>
                <xsl:with-param name ="Level" select ="self::node()"/>
              </xsl:call-template>
            </xsl:when>

            <xsl:when test ="self::levelhd">
              <levelhd>

                <xsl:choose>
                  <xsl:when test ="@class">
                    <xsl:attribute name="class">
                      <xsl:value-of select="@class"/>
                    </xsl:attribute>
                  </xsl:when>

                  <xsl:otherwise>
                    <xsl:attribute name="class">
                      <xsl:value-of select="../@class"/>
                    </xsl:attribute>
                  </xsl:otherwise>
                </xsl:choose>
                
                <!--<xsl:if test="@class">
                  <xsl:attribute name="class">
                    <xsl:value-of select="@class"/>
                  </xsl:attribute>
                </xsl:if>-->
                
              <xsl:attribute name ="depth">
                <xsl:value-of select ="'1'"/>
              </xsl:attribute>

                <xsl:for-each select ="./node()">
                  <xsl:copy-of select ="."/>
                </xsl:for-each>
              </levelhd>
              
            </xsl:when>
            
            <xsl:otherwise>
              <xsl:copy-of select ="self::node()"/>
            </xsl:otherwise>
          </xsl:choose>
          
        </xsl:for-each>     
   
      </level>
    </xsl:for-each>

    </frontmatter>
  </xsl:template>
  
  <!--****************************************************************-->
  <xsl:template name ="SetDepth">

    <xsl:param name ="Level"/>
    <xsl:param name ="IntDepth"/>

    <level>
      <xsl:if test="@class">
        <xsl:attribute name="class">
          <xsl:value-of select="@class"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:attribute name ="depth">
        <xsl:value-of select ="$IntDepth+1"/>
      </xsl:attribute>

      <xsl:for-each select ="exsl:node-set($Level)/node()">

        <xsl:choose>
          <xsl:when test ="self::level">
            <xsl:call-template name ="SetDepth">
              <xsl:with-param name ="Level" select ="self::node()"/>
              <xsl:with-param name ="IntDepth" select ="$IntDepth+1"/>
            </xsl:call-template>
          </xsl:when>

          <xsl:when test ="self::levelhd">
            <levelhd>

              <xsl:choose>
                <xsl:when test ="@class">
                  <xsl:attribute name="class">
                    <xsl:value-of select="@class"/>
                  </xsl:attribute>
                </xsl:when>

                <xsl:otherwise>
                  <xsl:attribute name="class">
                    <xsl:value-of select="../@class"/>
                  </xsl:attribute>
                </xsl:otherwise>
              </xsl:choose>

              <!--<xsl:if test="@class">
                <xsl:attribute name="class">
                  <xsl:value-of select="@class"/>
                </xsl:attribute>
              </xsl:if>-->

              <xsl:attribute name ="depth">
                <xsl:value-of select ="$IntDepth+1"/>
              </xsl:attribute>

              <xsl:for-each select ="./node()">
                <xsl:copy-of select ="."/>
              </xsl:for-each>
              
            </levelhd>
          </xsl:when>
          
          <xsl:otherwise>
            <xsl:copy-of select ="self::node()"/>
          </xsl:otherwise>
        </xsl:choose>
        
 
      </xsl:for-each>
      
    </level>
    
  </xsl:template>

  <!-- *************************************************************-->

  <xsl:template match ="bodymatter" mode ="depth">

      <xsl:for-each select ="./node()">
        <xsl:copy-of select ="self::node()"/>
      </xsl:for-each>


  </xsl:template>

  <xsl:template match ="rearmatter" mode ="depth">
   
      <xsl:for-each select ="./node()">
        <xsl:copy-of select ="self::node()"/>
      </xsl:for-each>
 
  </xsl:template>

  <xsl:template match ="dtbook" mode ="depth">
    <dtbook>
      <xsl:apply-templates mode ="depth"/>
    </dtbook>
  </xsl:template>

  <xsl:template match ="head" mode ="depth">
    <head>
      <xsl:for-each select ="./node()">
        <xsl:copy-of select ="self::node()"/>
      </xsl:for-each>
    </head>
  </xsl:template>

  <xsl:template match ="book" mode ="depth">
    <book>
      <xsl:if test ="@lang">
        <xsl:attribute name ="lang">
          <xsl:value-of select ="@lang"/>
        </xsl:attribute>
      </xsl:if>
    <xsl:apply-templates mode ="depth"/>
    </book>
  </xsl:template>
</xsl:stylesheet>
