﻿<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl="http://exslt.org/common" exclude-result-prefixes="exsl"
>
  <xsl:output method ="xml" encoding="utf-8"/>
  <xsl:template name ="AsciiCopyright">
    <level class="DBBCopyright">
      <levelhd class="DBBCopyright">Om denne udgave</levelhd>


      <xsl:choose>
        <xsl:when test ="contains(//meta[@name='dc:identifier']/@content,'DBB')">
          <p>
            <strong>Bognummer: </strong>
            <xsl:value-of select="substring(//meta[@name='dc:identifier']/@content,8)"/>
          </p>
        </xsl:when>

        <xsl:when test ="contains(//meta[@name='dc:identifier']/@content,'NOTA')">
          <p>
            <strong>Bognummer: </strong>
            <xsl:value-of select="substring(//meta[@name='dc:identifier']/@content,9)"/>
          </p>
        </xsl:when>

      </xsl:choose>
        
      <p>
        <xsl:text>Denne udgave er produceret af Nota, Nationalbibliotek for mennesker med læsevanskeligheder, i henhold til ophavsretslovens bestemmelser og må ikke kopieres uden tilladelse. </xsl:text>
      </p >
      <p>Denne elektroniske tekst er produceret i henhold til ophavsretslovens paragraf 17 stk. 1 og må kun udlånes til blinde, svagsynede og andre, der på grund af handicap er ude af stand til at læse trykt tekst.</p>
      <p>Teksten må ikke viderekopieres. Misbrug kan medføre bortfald af låneadgangen til Nota.</p>

      <xsl:choose>
        <xsl:when test="//pagenum">

          <p>(I denne udgave står sortbogens sidetal markeret i parentes ved sidens begyndelse. Eks. (ss 27). Til hovedafsnit: 3 tomme linier)</p>

        </xsl:when>
        <xsl:otherwise>
          <p>(Til hovedafsnit: 3 tomme linier)</p>

        </xsl:otherwise>
      </xsl:choose>

    </level>
  </xsl:template>

  <xsl:template name ="XMLCopyright">
    <level class="DBBCopyright">
      <levelhd class="DBBCopyright">Om denne udgave</levelhd>

      <xsl:choose>
        <xsl:when test ="contains(//meta[@name='dc:identifier']/@content,'DBB')">
          <p>
            <strong>Bognummer: </strong>
            <xsl:value-of select="substring(//meta[@name='dc:identifier']/@content,8)"/>
          </p>
        </xsl:when>

        <xsl:when test ="contains(//meta[@name='dc:identifier']/@content,'NOTA')">
          <p>
            <strong>Bognummer: </strong>
            <xsl:value-of select="substring(//meta[@name='dc:identifier']/@content,9)"/>
          </p>
        </xsl:when>

      </xsl:choose>

      <p>
        <xsl:text>Denne udgave er produceret af Nota, Nationalbibliotek for mennesker med læsevanskeligheder, i henhold til ophavsretslovens bestemmelser og må ikke kopieres uden tilladelse. </xsl:text>
      </p >
      <p>Denne elektroniske tekst er produceret i henhold til ophavsretslovens paragraf 17 stk. 1 og må kun udlånes til blinde, svagsynede og andre, der på grund af handicap er ude af stand til at læse trykt tekst.</p>
      <p>Teksten må ikke viderekopieres. Misbrug kan medføre bortfald af låneadgangen til Nota.</p>
    </level>
  </xsl:template>

  <!--<xsl:template name ="HTMLCopyright">
    <div class="DBBCopyright">
      <h2 class="DBBCopyright">Om denne udgave</h2>
      <p>
        <strong>Bognummer: </strong>
        <xsl:value-of select="substring(//meta[@name='dc:identifier']/@content,8)"/>
      </p>
      <p>
        <xsl:text>Denne udgave er produceret af DBB, Danmarks Blindebibliotek, i henhold til ophavsretslovens bestemmelser og må ikke kopieres uden tilladelse. </xsl:text>
      </p >
      <p>Denne elektroniske tekst er produceret i henhold til ophavsretslovens paragraf 17 stk. 1 og må kun udlånes til blinde, svagsynede og andre, der på grund af handicap er ude af stand til at læse trykt tekst.</p>
      <p>Teksten må ikke viderekopieres. Misbrug kan medføre bortfald af låneadgangen til DBB - Danmarks Blindebibliotek, København.</p>
    </div>
  </xsl:template>-->
</xsl:stylesheet>
