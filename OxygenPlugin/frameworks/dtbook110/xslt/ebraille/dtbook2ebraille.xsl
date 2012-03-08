<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">
    <xsl:output method="text" omit-xml-declaration="yes" encoding="iso-8859-1"/>
    <xsl:param name="Stamp"/>
    <xsl:param name="InsertDecNum">true</xsl:param>
    
    <xsl:template name="mainlang">
        <xsl:choose>
            <xsl:when test="/dtbook/@lang"><xsl:value-of select="/dtbook/@lang"/></xsl:when>
            <xsl:when test="/dtbook/@xml:lang"><xsl:value-of select="/dtbook/@xml:lang"/></xsl:when>
            <xsl:when test="/dtbook/book/@lang"><xsl:value-of select="/dtbook/book/@lang"/></xsl:when>
            <xsl:when test="/dtbook/book/@xml:lang"><xsl:value-of select="/dtbook/book/@xml:lang"/></xsl:when>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="dtbook">
        <xsl:apply-templates select="book"/>
    </xsl:template>
    
    <xsl:template match="frontmatter">
        <xsl:apply-templates/>
        <xsl:call-template name="linebreak"/>
        <xsl:text>===</xsl:text>
        <xsl:call-template name="linebreak"/>
        <xsl:call-template name="linebreak"/>
    </xsl:template>
    
    <xsl:template match="rearmatter">
        <xsl:call-template name="linebreak"/>
        <xsl:text>===</xsl:text>
        <xsl:call-template name="linebreak"/>
        <xsl:call-template name="linebreak"/>
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="doctitle|docauthor">
        <xsl:call-template name="extraPrecedingLinebreak"/>
        <xsl:apply-templates/>
        <xsl:call-template name="linebreak"/>
        <xsl:call-template name="extraFollowingLinebreak"/>
    </xsl:template>
    
    <!--Level headings
        ==============-->
    <xsl:template match="levelhd">
        <xsl:call-template name="extraPrecedingLinebreak"/>
        <xsl:if test="$InsertDecNum='true'">
            <xsl:call-template name="decNum"/>
            <xsl:text>: </xsl:text>
        </xsl:if>
        <xsl:apply-templates/>
        <xsl:call-template name="linebreak"/>
        <xsl:call-template name="extraFollowingLinebreak"/>
    </xsl:template>
    
    <xsl:template match="level">
        <xsl:call-template name="extraPrecedingLinebreak"/>
        <xsl:if test="$InsertDecNum='true' and not(levelhd)">
            <xsl:if test="$InsertDecNum='true'">
                <xsl:call-template name="decNum"/>
                <xsl:text>:</xsl:text>
            </xsl:if>
            <xsl:call-template name="linebreak"/>
            <xsl:call-template name="linebreak"/>
        </xsl:if>
        <xsl:apply-templates/>
        <xsl:call-template name="extraFollowingLinebreak"/>
    </xsl:template>
    
    <xsl:template name="decNum">
        <xsl:variable name="depth" select="count(ancestor::level)"/>
        <xsl:call-template name="h1no"/>
        <xsl:if test="$depth>1">
            <xsl:text>.</xsl:text>
            <xsl:call-template name="h2no"/>
        </xsl:if>
        <xsl:if test="$depth>2">
            <xsl:text>.</xsl:text>
            <xsl:call-template name="h3no"/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="h1no">
        <xsl:variable name="level1before" select="count(ancestor-or-self::level[not(parent::level)]/preceding-sibling::level)"/>
        <xsl:variable name="frontlevel1" select="count(//frontmatter/level)"/> 
        <xsl:variable name="bodylevel1" select="count(//bodymatter/level)"/>
        <xsl:choose>
            <xsl:when test="ancestor::frontmatter">
                <xsl:value-of select="$level1before+1"/>
            </xsl:when>
            <xsl:when test="ancestor::bodymatter">
                <xsl:value-of select="$frontlevel1+$level1before+1"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$frontlevel1+$bodylevel1+$level1before+1"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="h2no">
        <xsl:value-of select="count(ancestor-or-self::level[count(ancestor::level)=1]/preceding-sibling::level)+1"/>        
    </xsl:template>
    
    <xsl:template name="h3no">
        <xsl:value-of select="count(ancestor-or-self::level[count(ancestor::level)=2]/preceding-sibling::level)+1"/>        
    </xsl:template>
    
    <!--Lists
        =====-->    
    <xsl:template match="list">
        <xsl:choose>
            <xsl:when test="ancestor::list">
                <xsl:apply-templates/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="extraPrecedingLinebreak"/>
                <xsl:apply-templates/>
                <xsl:call-template name="extraFollowingLinebreak"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="li">
        <xsl:choose>
            <xsl:when test="parent::list[@type='ul' and @bullet='none']"/>
            <xsl:when test="parent::list[@type='ul']">
                <xsl:call-template name="ulBullit"/>
            </xsl:when>
            <xsl:when test="parent::list[@type='ol']">
                <xsl:call-template name="olBullit"/>
            </xsl:when>
        </xsl:choose>
        <xsl:for-each select="node()">
            <xsl:apply-templates select=".">
                <xsl:with-param name="firstlineindent"><xsl:if test="preceding-sibling::*"><xsl:text>  </xsl:text></xsl:if></xsl:with-param>
                <xsl:with-param name="indent"><xsl:text>  </xsl:text></xsl:with-param>
            </xsl:apply-templates>
        </xsl:for-each>
        <xsl:if test="not(descendant::p)">
            <xsl:call-template name="linebreak"/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="olBullit">
        <xsl:for-each select="ancestor::li">
            <xsl:value-of select="concat(count(preceding-sibling::li)+1,' ')"/>
            <xsl:text>.</xsl:text>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="ulBullit">
        <xsl:variable name="liDepth" select="count(ancestor-or-self::li)"/>
        <xsl:choose>
            <xsl:when test="$liDepth=1"><xsl:text>* </xsl:text></xsl:when>
            <xsl:when test="$liDepth=2"><xsl:text>- </xsl:text></xsl:when>
            <xsl:otherwise><xsl:text>+ </xsl:text></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- 
        Tables
        ======-->
    <xsl:template match="table">
        <xsl:call-template name="extraPrecedingLinebreak"/>
        <xsl:apply-templates select="descendant::tr"/>
        <xsl:call-template name="extraFollowingLinebreak"/>
    </xsl:template>
    
    <xsl:template name="rowName">
        <xsl:variable name="lang">
            <xsl:call-template name="mainlang"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$lang='da'">Række</xsl:when>
            <xsl:when test="$lang='en'">Row</xsl:when>
            <xsl:otherwise>Række</xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="tr">
        <xsl:variable name="rowname"><xsl:call-template name="rowName"/></xsl:variable>
        <xsl:value-of select="concat($rowname, ' ', count(preceding-sibling::tr)+1,':')"/>
        <xsl:call-template name="linebreak"/>
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="th|td">
        <xsl:value-of select="concat('  ',count(preceding-sibling::td|preceding-sibling::th)+1,': ')"/>
        <xsl:choose>
            <xsl:when test="@class='copy'">...</xsl:when>
            <xsl:when test="descendant::text()[not(normalize-space(.)='')]">
                <xsl:for-each select="descendant::text()[not(normalize-space(.)='')]">
                    <xsl:value-of select="concat(normalize-space(.),' ')"/>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>--</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="linebreak"></xsl:call-template>
    </xsl:template> 
    
    <!--Page numbers
        ============-->
    <xsl:template match="pagenum">
        <xsl:if test="not(parent::level)">
            <xsl:call-template name="linebreak"/>
        </xsl:if>
        <xsl:value-of select="concat('--',normalize-space(.),'--')"/>
        <xsl:call-template name="linebreak"/>
    </xsl:template>
    
    <!--Paragraphs
        ==========-->
    <xsl:template match="p">
        <xsl:param name="firstlineindent"></xsl:param>
        <xsl:param name="indent"></xsl:param>
        <xsl:value-of select="$indent"/>
        <xsl:apply-templates>
            <xsl:with-param name="firstlineindent"><xsl:value-of select="$firstlineindent"/></xsl:with-param>
            <xsl:with-param name="indent"><xsl:value-of select="$indent"/></xsl:with-param>
        </xsl:apply-templates>
        <xsl:call-template name="linebreak"/>
    </xsl:template>
    
    <!--Other elements and text nodes
        =============================-->
    <xsl:template match="*">
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="text()">
        <xsl:if test="not(normalize-space(.)='')">
            <xsl:value-of select="normalize-space(.)"/>
            <xsl:text> </xsl:text>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="br">
        <xsl:param name="indent"></xsl:param>
        <xsl:value-of select="$indent"/>
        <xsl:call-template name="linebreak"/>
    </xsl:template>
    
    <!--
        Line break
        ==========-->
    <xsl:template name="linebreak">
        <xsl:text>&#13;&#10;</xsl:text>
    </xsl:template>
    
    <xsl:template name="extraFollowingLinebreak">
        <xsl:if test="following-sibling::node()[not(normalize-space(.)='')]">
            <xsl:call-template name="linebreak"></xsl:call-template>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="extraPrecedingLinebreak">
        <xsl:variable name="prevNode" select="(preceding-sibling::node()[not(normalize-space(.)='')])[last()]"/>
        <xsl:if test="$prevNode and not(local-name($prevNode)='list' or local-name($prevNode)='table' or local-name($prevNode)='levelhd' or local-name($prevNode)='level')">
            <xsl:call-template name="linebreak"></xsl:call-template>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>