<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes" encoding="utf-8"/>

  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:if test="self::* and ancestor::*[local-name()='book'] and not(@id)">
        <xsl:attribute name="id">
          <xsl:value-of select="generate-id()"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Check level1 - level 6-->

  <xsl:template match ="pagenum">
    <xsl:choose>
      <xsl:when test ="following-sibling::pagenum"/>
      <xsl:otherwise>
        <xsl:copy-of select ="self::node()"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match ="level1">
    <level>

      <xsl:attribute name ="depth">
        <xsl:text>1</xsl:text>
      </xsl:attribute>

      <xsl:if test ="@class">
        <xsl:attribute name ="class">
          <xsl:value-of select ="@class"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:choose>
        <xsl:when test ="@id">
          <xsl:attribute name ="id">
            <xsl:value-of select ="@id"/>
          </xsl:attribute>
        </xsl:when>

        <xsl:otherwise>
          <xsl:attribute name="id">
            <xsl:value-of select="generate-id()"/>
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
     
      <xsl:apply-templates/>

    </level>
  </xsl:template>

  <xsl:template match ="level2">
    <level>

      <xsl:attribute name ="depth">
        <xsl:text>2</xsl:text>
      </xsl:attribute>

      <xsl:if test ="@class">
        <xsl:attribute name ="class">
          <xsl:value-of select ="@class"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:choose>
        <xsl:when test ="@id">
          <xsl:attribute name ="id">
            <xsl:value-of select ="@id"/>
          </xsl:attribute>
        </xsl:when>

        <xsl:otherwise>
          <xsl:attribute name="id">
            <xsl:value-of select="generate-id()"/>
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates/>

    </level>
  </xsl:template>

  <xsl:template match ="level3">
    <level>

      <xsl:attribute name ="depth">
        <xsl:text>3</xsl:text>
      </xsl:attribute>

      <xsl:if test ="@class">
        <xsl:attribute name ="class">
          <xsl:value-of select ="@class"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:choose>
        <xsl:when test ="@id">
          <xsl:attribute name ="id">
            <xsl:value-of select ="@id"/>
          </xsl:attribute>
        </xsl:when>

        <xsl:otherwise>
          <xsl:attribute name="id">
            <xsl:value-of select="generate-id()"/>
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates/>

    </level>
  </xsl:template>

  <xsl:template match ="level4">
    <level>

      <xsl:attribute name ="depth">
        <xsl:text>4</xsl:text>
      </xsl:attribute>

      <xsl:if test ="@class">
        <xsl:attribute name ="class">
          <xsl:value-of select ="@class"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:choose>
        <xsl:when test ="@id">
          <xsl:attribute name ="id">
            <xsl:value-of select ="@id"/>
          </xsl:attribute>
        </xsl:when>

        <xsl:otherwise>
          <xsl:attribute name="id">
            <xsl:value-of select="generate-id()"/>
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:apply-templates/>

    </level>
  </xsl:template>

  <xsl:template match ="level5">
    <level>

      <xsl:attribute name ="depth">
        <xsl:text>5</xsl:text>
      </xsl:attribute>

      <xsl:if test ="@class">
        <xsl:attribute name ="class">
          <xsl:value-of select ="@class"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:choose>
        <xsl:when test ="@id">
          <xsl:attribute name ="id">
            <xsl:value-of select ="@id"/>
          </xsl:attribute>
        </xsl:when>

        <xsl:otherwise>
          <xsl:attribute name="id">
            <xsl:value-of select="generate-id()"/>
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates/>

    </level>
  </xsl:template>

  <xsl:template match ="level6">
    <level>

      <xsl:attribute name ="depth">
        <xsl:text>6</xsl:text>
      </xsl:attribute>

      <xsl:if test ="@class">
        <xsl:attribute name ="class">
          <xsl:value-of select ="@class"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:choose>
        <xsl:when test ="@id">
          <xsl:attribute name ="id">
            <xsl:value-of select ="@id"/>
          </xsl:attribute>
        </xsl:when>

        <xsl:otherwise>
          <xsl:attribute name="id">
            <xsl:value-of select="generate-id()"/>
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates/>

    </level>
  </xsl:template>

  <!--konvertér h1-h6-->

  <xsl:template match ="h1">
    <levelhd>

      <xsl:attribute name ="depth">
        <xsl:text>1</xsl:text>
      </xsl:attribute>

      <xsl:if test ="@class">
        <xsl:attribute name ="class">
          <xsl:value-of select ="@class"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:choose>
        <xsl:when test ="@id">
          <xsl:attribute name ="id">
            <xsl:value-of select ="@id"/>
          </xsl:attribute>
        </xsl:when>

        <xsl:otherwise>
          <xsl:attribute name="id">
            <xsl:value-of select="generate-id()"/>
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:apply-templates/>

    </levelhd>
  </xsl:template>

  <xsl:template match ="h2">
    <levelhd>

      <xsl:attribute name ="depth">
        <xsl:text>2</xsl:text>
      </xsl:attribute>

      <xsl:if test ="@class">
        <xsl:attribute name ="class">
          <xsl:value-of select ="@class"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:choose>
        <xsl:when test ="@id">
          <xsl:attribute name ="id">
            <xsl:value-of select ="@id"/>
          </xsl:attribute>
        </xsl:when>

        <xsl:otherwise>
          <xsl:attribute name="id">
            <xsl:value-of select="generate-id()"/>
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates/>

    </levelhd>
  </xsl:template>

  <xsl:template match ="h3">
    <levelhd>

      <xsl:attribute name ="depth">
        <xsl:text>3</xsl:text>
      </xsl:attribute>

      <xsl:if test ="@class">
        <xsl:attribute name ="class">
          <xsl:value-of select ="@class"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:choose>
        <xsl:when test ="@id">
          <xsl:attribute name ="id">
            <xsl:value-of select ="@id"/>
          </xsl:attribute>
        </xsl:when>

        <xsl:otherwise>
          <xsl:attribute name="id">
            <xsl:value-of select="generate-id()"/>
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates/>

    </levelhd>
  </xsl:template>

  <xsl:template match ="h4">
    <levelhd>

      <xsl:attribute name ="depth">
        <xsl:text>4</xsl:text>
      </xsl:attribute>

      <xsl:if test ="@class">
        <xsl:attribute name ="class">
          <xsl:value-of select ="@class"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:choose>
        <xsl:when test ="@id">
          <xsl:attribute name ="id">
            <xsl:value-of select ="@id"/>
          </xsl:attribute>
        </xsl:when>

        <xsl:otherwise>
          <xsl:attribute name="id">
            <xsl:value-of select="generate-id()"/>
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:apply-templates/>

    </levelhd>
  </xsl:template>

  <xsl:template match ="h5">
    <levelhd>

      <xsl:attribute name ="depth">
        <xsl:text>5</xsl:text>
      </xsl:attribute>

      <xsl:if test ="@class">
        <xsl:attribute name ="class">
          <xsl:value-of select ="@class"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:choose>
        <xsl:when test ="@id">
          <xsl:attribute name ="id">
            <xsl:value-of select ="@id"/>
          </xsl:attribute>
        </xsl:when>

        <xsl:otherwise>
          <xsl:attribute name="id">
            <xsl:value-of select="generate-id()"/>
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:apply-templates/>

    </levelhd>
  </xsl:template>

  <xsl:template match ="h6">
    <levelhd>

      <xsl:attribute name ="depth">
        <xsl:text>6</xsl:text>
      </xsl:attribute>

      <xsl:if test ="@class">
        <xsl:attribute name ="class">
          <xsl:value-of select ="@class"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:choose>
        <xsl:when test ="@id">
          <xsl:attribute name ="id">
            <xsl:value-of select ="@id"/>
          </xsl:attribute>
        </xsl:when>

        <xsl:otherwise>
          <xsl:attribute name="id">
            <xsl:value-of select="generate-id()"/>
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:apply-templates/>

    </levelhd>
  </xsl:template>

</xsl:stylesheet>
