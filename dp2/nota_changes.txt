********************
*** MAIN PROJECT ***
********************

FILE: SRC/MAIN/RESOURCES/META-INF/CATALOG.XML
<!-- INSERT -->
<uri name="http://www.daisy.org/pipeline/modules/nordic/mtm2015-1.nota.sch" uri="../xml/schema/mtm2015-1.nota.sch"/>
<uri name="http://www.daisy.org/pipeline/modules/nordic/nordic2015-1.nota.sch" uri="../xml/schema/nordic2015-1.nota.sch"/>


FILE: SRC/MAIN/RESOURCES/XML/XPROC/STEP/DTBOOK.VALIDATE.STEP.XPL
<!-- FROM -->
<p:document href="../../schema/mtm2015-1.sch"/>
<!-- TO -->
<p:document href="../../schema/mtm2015-1.nota.sch"/>


FILE: SRC/MAIN/RESOURCES/XML/XPROC/STEP/HTML.VALIDATE.STEP.XPL
<!-- FROM -->
<p:document href="../../schema/nordic2015-1.sch"/>
<!-- TO -->
<p:document href="../../schema/nordic2015-1.nota.sch"/>


FILE: SRC/MAIN/RESOURCES/XML/XSLT/EPUB3-TO-DTBOOK.XSL
<!-- FROM -->
<xsl:variable name="special-classes" select="('part','cover','colophon','nonstandardpagination','jacketcopy','frontcover','rearcover','leftflap','rightflap','precedingemptyline','precedingseparator','indented','asciimath','byline','dateline','address','definition','keyboard','initialism','truncation','cite','bdo','quote','exercisenumber','exercisepart','answer','answer_1','box')"/>
<!-- TO -->
<xsl:variable name="special-classes" select="('part','cover','colophon','nonstandardpagination','jacketcopy','frontcover','rearcover','leftflap','rightflap','precedingemptyline','precedingseparator','indented','asciimath','byline','dateline','address','definition','keyboard','initialism','truncation','cite','bdo','quote','exercisenumber','exercisepart','answer','answer_1','box','note_identifier')"/>

<!-- FROM -->
<xsl:template name="attlist.note">
    <xsl:call-template name="attrsrqd">
        <xsl:with-param name="except-classes" select="'*'" tunnel="yes"/>
    </xsl:call-template>
</xsl:template>
<!-- TO: -->
<xsl:template name="attlist.note">
    <xsl:call-template name="attrsrqd">
        <xsl:with-param name="except-classes" select="'*'" tunnel="yes"/>
    </xsl:call-template>
    <xsl:attribute name="class" select="if (f:types(.)='footnote') then 'footnote required' else if (f:types(.)='rearnote') then 'rearnote required' else ('')"/>
</xsl:template>

	
	
********************
*** TEST PROJECT ***
********************

FILE: SRC/TEST/XSPEC/EPUB3-TO-DTBOOK.XSPEC
AREA: NOTES

<!-- inside -->
<!-- <x:expect label="the resulting element should be a note"> -->
<!-- for all notes add -->
class=""


<!-- inside -->
<!-- <dtbook:level1 id="frontmatter-notes"> -->
<!-- for all notes add -->
class=""

<!-- inside -->
<!-- <dtbook:level1 id="rearnotes" class="rearnotes"> -->
<!-- for all notes add -->
class="rearnote required"

<!-- inside -->
<!-- <dtbook:level1 id="bodymatter-notes" class="chapter"> -->
<!-- for all notes add -->
class=""

<!-- inside -->
<!-- <dtbook:level1 id="chapter-with-rearnotes" class="chapter"> -->
<!-- for all notes add -->
class="rearnote required"

<!-- inside -->
<!-- <dtbook:level1 id="footnotes" class="footnotes"> -->
<!-- for all notes add -->
class="footnote required"

<!-- inside -->
<!-- <dtbook:level1 id="rearmatter-notes"> -->
<!-- for all notes add --> 
class="footnote required"
<!-- and the last note -->
class=""


<!-- inside -->
<!-- <x:expect label="the text node and the span element should be wrapped in a p, while the p should not be wrapped in another p"> -->
<!-- for all notes add --> 
class="rearnote required"
<!-- and the last note -->
class="footnote required"


<!-- inside -->
<!-- <x:expect label="there should be no wrapping &lt;list&gt; element"> -->
<!-- for all notes add --> 
class="footnote required"


<!-- inside -->
<!-- <x:expect label="the nested lists should adjust their depths as the nesting list item becomes a note"> -->
<!-- for all notes add --> 
class="footnote required"
