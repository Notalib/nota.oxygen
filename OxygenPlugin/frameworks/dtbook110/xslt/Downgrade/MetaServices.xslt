<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl="http://exslt.org/common" exclude-result-prefixes="exsl">
  <xsl:output method="xml" encoding ="utf-8"/>


  <xsl:include href ="LangSelector.xslt"/>
  <xsl:include href ="StringFunctions.xslt"/>
  <xsl:include href ="FrontmatterFactory.xslt"/>
  <xsl:include href ="DBB_About.xslt"/>

  <xsl:template name ="MetaServices">
    <!-- sæt titlen i denne globale variable-->
    <xsl:param name ="Title">
      <xsl:call-template name ="SetTitle"/>
    </xsl:param>

    <!-- sæt evt. creator i denne globale variable-->
    <xsl:param name ="Creator">
      <xsl:call-template name ="SetCreator"/>
    </xsl:param>

    <xsl:param name ="SemiCreatorAndContributor">
      <xsl:call-template name ="SetSemiCreatorOrContributor"/>
    </xsl:param>
    
    <!--Nyt metaelement der kun skal fremvises i kolofonen-->

    <xsl:param name ="SecondaryContributor">
      <xsl:call-template name ="SetSecondaryContributor"/>
    </xsl:param>
    
    <xsl:param name ="Translations">
      <xsl:call-template name ="SetTranslationAndlanguage"/>
    </xsl:param>

    <xsl:param name ="SourcePublishingInfo">
      <xsl:call-template name ="SetSourcePublishingInfo"/>
    </xsl:param >

    <xsl:param name ="SourcePublishingFrontpage">
      <xsl:call-template name ="SetSourcePublishingFrontpage"/>
    </xsl:param>

    <xsl:param name ="DBBInfo">
      <xsl:call-template name ="SetDBBInfo"/>
    </xsl:param >

    <xsl:param name ="AdditionalInfo">
      <xsl:call-template name ="SetAdditionalInfo"/>
    </xsl:param >
    <!-- Sæt værdier sammen -->

    <xsl:call-template name ="ArrangeFrontmatter">
      <xsl:with-param name ="xTitle" select ="$Title"/>
      <xsl:with-param name ="xCreator" select ="$Creator"/>
      <xsl:with-param name ="xSemiCreatorAndContributor" select ="$SemiCreatorAndContributor"/>
      <xsl:with-param name ="xTranslations" select ="$Translations"/>
      <xsl:with-param name ="xSourcePublishingInfo" select ="$SourcePublishingInfo"/>
      <xsl:with-param name ="xSourcePublishingFrontpage" select ="$SourcePublishingFrontpage"/>
      <xsl:with-param name ="xDBBInfo" select ="$DBBInfo"/>
      <xsl:with-param name ="xAdditionalInfo" select ="$AdditionalInfo"/>
      <xsl:with-param name ="xSecondaryContributor" select ="$SecondaryContributor"/>
    </xsl:call-template>
  </xsl:template>

  <!--
*********************************************************************
************************** frontmatter ******************************
*********************************************************************
  -->

  <xsl:template name ="ArrangeFrontmatter">

    <!-- Overførte værdier -->
    <xsl:param name ="xTitle"/>
    <xsl:param name ="xCreator"/>
    <xsl:param name ="xSemiCreatorAndContributor"/>
    <xsl:param name ="xTranslations"/>
    <xsl:param name ="xSourcePublishingInfo"/>
    <xsl:param name ="xSourcePublishingFrontpage"/>
    <xsl:param name ="xDBBInfo"/>
    <xsl:param name ="xAdditionalInfo"/>
    <xsl:param name ="xSecondaryContributor"/>

    <!-- Dan de første linier i dokumentet-->

    <xsl:element name ="level">
      <xsl:attribute name ="class">
        <xsl:text>title</xsl:text>
      </xsl:attribute>

      <xsl:attribute name ="depth">
        <xsl:text>1</xsl:text>
      </xsl:attribute>

      <xsl:element name ="levelhd">
        <xsl:attribute name ="depth">
          <xsl:text>1</xsl:text>
        </xsl:attribute>

        <xsl:attribute name ="class">
          <xsl:text>title</xsl:text>
        </xsl:attribute>

        <!--*********************** Titellinie ***********************-->

        <xsl:choose>

          <!--Kun creator-->
          <xsl:when test ="$xCreator !='no_value' and $xSemiCreatorAndContributor='no_value'">
            <!--<span class="creator">-->
            <xsl:value-of select ="$xCreator"/>
            <!--</span>-->
            <xsl:text>: </xsl:text>
            <!--<span class="title">-->
            <xsl:value-of select ="$xTitle"/>
            <!--</span>-->
          </xsl:when>

          <!--Kun semi-creator eller contributor-->
          <xsl:when test ="$xCreator='no_value' and $xSemiCreatorAndContributor !='no_value'">
            <!--<span class="title">-->
            <xsl:value-of select ="$xTitle"/>
            <!--</span>-->

            <br/>


            <xsl:for-each select ="exsl:node-set($xSemiCreatorAndContributor)/node()">
              <xsl:choose>
                <xsl:when test ="self::text()">
                  <!--<span class="semiAndContributor">-->
                  <xsl:value-of select ="self::node()"/>
                  <!--</span>-->
                </xsl:when>

                <xsl:otherwise>
                  <br/>
                </xsl:otherwise>
              </xsl:choose>

            </xsl:for-each>

          </xsl:when>

          <!--begge-->
          <xsl:when test ="$xCreator !='no_value' and $xSemiCreatorAndContributor !='no_value'">
            <!--<span class="creator">-->
            <xsl:value-of select ="$xCreator"/>
            <!--</span>-->
            <xsl:text>: </xsl:text>
            <!--<span class="title">-->
            <xsl:value-of select ="$xTitle"/>
            <!--</span>-->
            <br/>
            <!--<span class="semiCreator">-->
            <!--<xsl:value-of select ="exsl:node-set($xSemiCreatorAndContributor)"/>-->

            <xsl:for-each select ="exsl:node-set($xSemiCreatorAndContributor)/node()">
              <xsl:choose>
                <xsl:when test ="self::text()">
                  <!--<span class="semiAndContributor">-->
                  <xsl:value-of select ="self::node()"/>
                  <!--</span>-->
                </xsl:when>

                <xsl:otherwise>
                  <br/>
                </xsl:otherwise>
              </xsl:choose>

            </xsl:for-each>

            <!--</span>-->

          </xsl:when>

          <!--Ingen-->
          <xsl:otherwise>
            <!--<span class="title">-->
            <xsl:value-of select ="$xTitle"/>
            <!--</span>-->
          </xsl:otherwise>

        </xsl:choose>
      </xsl:element>
      <!-- levelhd -->

      <xsl:choose>
        <xsl:when test ="$OutputMode='ascii'">
          <xsl:call-template name ="AsciiCopyright"/>
        </xsl:when>

        <xsl:otherwise>
          <xsl:call-template name ="XMLCopyright"/>
        </xsl:otherwise>
      </xsl:choose>

      <!-- Hvis det er udgaven til fremvisning af forside...-->
      <xsl:if test ="$FrontPage='yes'">
        <!-- læg udgiverinformation på -->
        <level class="publisher">
          <p class="publisher">
            <xsl:for-each select ="exsl:node-set($xSourcePublishingFrontpage)/node()">
              <xsl:choose>
                <xsl:when test ="self::text()">

                  <xsl:value-of select ="self::node()"/>

                </xsl:when>

                <xsl:otherwise>
                  <br/>
                </xsl:otherwise>
              </xsl:choose>

            </xsl:for-each>
          </p>
        </level>
      </xsl:if>

      <!-- Kolofon -->
      <xsl:element name ="level">
        <xsl:attribute name ="class">
          <xsl:text>kolofon</xsl:text>
        </xsl:attribute>

        <xsl:attribute name ="depth">
          <xsl:text>2</xsl:text>
        </xsl:attribute>

        <xsl:element name ="levelhd">
          <xsl:attribute name ="depth">
            <xsl:text>2</xsl:text>
          </xsl:attribute>

          <xsl:attribute name ="class">
            <xsl:text>kolofon</xsl:text>
          </xsl:attribute>


          <xsl:text>Kolofon og bibliografiske oplysninger</xsl:text>
        </xsl:element>

        <xsl:choose>

          <!-- Kun creator -->
          <xsl:when test ="$xCreator !='no_value' and $xSemiCreatorAndContributor ='no_value'">
            <p>

              <xsl:value-of select ="$xCreator"/>

              <xsl:text>: </xsl:text>

              <xsl:value-of select ="$xTitle"/>

            </p>
          </xsl:when>

          <!-- Kun semi-creator eller contributor -->
          <xsl:when test ="$xCreator ='no_value' and $xSemiCreatorAndContributor !='no_value'">
            <p>

              <xsl:value-of select ="$xTitle"/>

              <xsl:text>: </xsl:text>

              <!--<xsl:value-of select ="$xSemiCreatorAndContributor"/>-->
              <xsl:for-each select ="exsl:node-set($xSemiCreatorAndContributor)/node()">
                <xsl:choose>
                  <xsl:when test ="self::text()">

                    <xsl:value-of select ="self::node()"/>

                  </xsl:when>

                  <xsl:otherwise>
                    <br/>
                  </xsl:otherwise>
                </xsl:choose>

              </xsl:for-each>
            </p>
          </xsl:when>

          <!-- begge -->
          <xsl:when test ="$xCreator !='no_value' and $xSemiCreatorAndContributor !='no_value'">
            <p>
              <xsl:value-of select ="$xCreator"/>
              <xsl:text>: </xsl:text>
              <xsl:value-of select ="$xTitle"/>
              <br/>

              <xsl:for-each select ="exsl:node-set($xSemiCreatorAndContributor)/node()">
                <xsl:choose>
                  <xsl:when test ="self::text()">
                    <!--<span class="semiAndContributor">-->
                    <xsl:value-of select ="self::node()"/>
                    <!--</span>-->
                  </xsl:when>

                  <xsl:otherwise>
                    <br/>
                  </xsl:otherwise>
                </xsl:choose>

              </xsl:for-each>

              <!--<xsl:value-of select ="$xSemiCreatorAndContributor"/>-->
            </p>
          </xsl:when>

          <!-- Ingen -->
          <xsl:otherwise>
            <p>
              <xsl:value-of select ="$xTitle"/>
            </p>
          </xsl:otherwise>

        </xsl:choose>
        
        <!--SecondaryContributor-->
        <xsl:choose>
          <xsl:when test ="$xSecondaryContributor !='no_value'">
            <p>
              <xsl:value-of select ="$xSecondaryContributor"/>
            </p>
          </xsl:when>
        </xsl:choose>

        <!-- Oversætter og sprog -->
        <xsl:choose>
          <xsl:when test ="$xTranslations !='no_value'">
            <p>

              <xsl:variable name ="tmp" select ="exsl:node-set($xTranslations)"/>
              <xsl:for-each select ="$tmp/node()">
                <xsl:choose>
                  <xsl:when test ="self::text()">

                    <xsl:value-of select ="self::node()"/>

                  </xsl:when>

                  <xsl:otherwise>
                    <br/>
                  </xsl:otherwise>
                </xsl:choose>


              </xsl:for-each>

            </p>
          </xsl:when>
        </xsl:choose>
        <!-- Udgiverinformation-->
        <xsl:choose>
          <xsl:when test ="$xSourcePublishingInfo !='no_value'">
            <p>

              <xsl:for-each select ="exsl:node-set($xSourcePublishingInfo)/node()">
                <xsl:choose>
                  <xsl:when test ="self::text()">

                    <xsl:value-of select ="self::node()"/>

                  </xsl:when>

                  <xsl:otherwise>
                    <br/>
                  </xsl:otherwise>
                </xsl:choose>


              </xsl:for-each>

            </p>
          </xsl:when>
        </xsl:choose>

        <!-- DBBinformation-->
        <xsl:choose>
          <xsl:when test ="$xDBBInfo !='no_value'">
            <p>

              <xsl:for-each select ="exsl:node-set($xDBBInfo)/node()">
                <xsl:choose>
                  <xsl:when test ="self::text()">

                    <xsl:value-of select ="self::node()"/>

                  </xsl:when>

                  <xsl:otherwise>
                    <br/>
                  </xsl:otherwise>
                </xsl:choose>


              </xsl:for-each>

            </p>
          </xsl:when>
        </xsl:choose>

        <xsl:if test ="$xAdditionalInfo !='no_value'">
          <p>

            <xsl:for-each select ="exsl:node-set($xAdditionalInfo)/node()">
              <xsl:choose>
                <xsl:when test ="self::text()">

                  <xsl:value-of select ="self::node()"/>

                </xsl:when>

                <xsl:otherwise>
                  <br/>
                </xsl:otherwise>
              </xsl:choose>


            </xsl:for-each>

          </p>
        </xsl:if >

      </xsl:element>
      <!-- level 2 (kolofon)-->
      <xsl:call-template name ="CreateFront"/>

    </xsl:element>
    <!-- Level1-->




  </xsl:template>

  <xsl:template name ="CreateFront">

    <xsl:call-template name ="CreateFrontmatterContent">
      <xsl:with-param name ="frontmatterNode" select ="self::node()"/>
    </xsl:call-template>


  </xsl:template>

  <!-- 
********************************************************************
********************** Herfra findes variable **********************
********************************************************************
  -->

  <!-- 
  ***************************** Title **************************
  -->
  <xsl:template name ="SetTitle">


    <xsl:choose>

      <!-- der er en undertitel OG alternativ titel-->
      <xsl:when test ="/dtbook/head/meta[@name='prod:subtitle'] and /dtbook/head/meta[@name='prod:alternativeTitle']">
        <xsl:value-of select ="string(/dtbook/head/meta[@name='dc:title']/@content)"/>
        <xsl:text>: </xsl:text>
        <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:subtitle']/@content)"/>
        <xsl:text> - </xsl:text>
        <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:alternativeTitle']/@content)"/>
      </xsl:when>

      <!-- der er en undertitel-->
      <xsl:when test ="/dtbook/head/meta[@name='prod:subtitle']">
        <xsl:value-of select ="string(/dtbook/head/meta[@name='dc:title']/@content)"/>
        <xsl:text>: </xsl:text>
        <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:subtitle']/@content)"/>
      </xsl:when>

      <!-- Der er en alternativ titel-->
      <xsl:when test ="/dtbook/head/meta[@name='prod:alternativeTitle']">
        <xsl:value-of select ="string(/dtbook/head/meta[@name='dc:title']/@content)"/>
        <xsl:text> - </xsl:text>
        <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:alternativeTitle']/@content)"/>
      </xsl:when>

      <!-- Der er kun en titel -->
      <xsl:otherwise>
        <xsl:value-of select ="string(/dtbook/head/meta[@name='dc:title']/@content)"/>
      </xsl:otherwise>

    </xsl:choose>

  </xsl:template>

  <!-- 
  ************************** Creator **************************
  -->

  <!-- Template der finder creator - hvis den findes. Ellers "no_value" -->
  <xsl:template name ="SetCreator">



    <xsl:choose>

      <!-- Der er en creator -->
      <xsl:when test ="/dtbook/head/meta[@name='dc:creator']">
        <xsl:value-of select ="string(/dtbook/head/meta[@name='dc:creator']/@content)"/>
      </xsl:when>

      <xsl:otherwise>
        <xsl:value-of select ="'no_value'"/>
      </xsl:otherwise>
    </xsl:choose>


  </xsl:template>

  <!-- 
  *********************** Semi-creator og eller contributor *******
  -->
  <!-- Template der finder semi-creator og/eller contributor. Ellers "no_value"
  Værdier i denne variabel skal stå EFTER titlen
  -->

  <xsl:template name ="SetSemiCreatorOrContributor">



    <xsl:choose>

      <!-- Både semi-creator og contributor -->
      <xsl:when test ="/dtbook/head/meta[@name='prod:semi-creator'] and /dtbook/head/meta[@name='dc:contributor']">

        <!--konvertér første karakter til uppercase-->
        <xsl:call-template name ="FirstCharToUpper">
          <xsl:with-param name ="OrgString" select ="/dtbook/head/meta[@name='prod:semi-creator']/@content"/>
        </xsl:call-template>

        <br/>

        <!--konvertér første karakter til uppercase-->
        <xsl:call-template name ="FirstCharToUpper">
          <xsl:with-param name ="OrgString" select ="/dtbook/head/meta[@name='dc:contributor']/@content"/>
        </xsl:call-template>

      </xsl:when>

      <!-- Kun semi-creator -->
      <xsl:when test ="/dtbook/head/meta[@name='prod:semi-creator']">
        <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:semi-creator']/@content)"/>
      </xsl:when>

      <!-- Kun contributor -->
      <xsl:when test ="/dtbook/head/meta[@name='dc:contributor']">
        <xsl:value-of select ="string(/dtbook/head/meta[@name='dc:contributor']/@content)"/>
      </xsl:when>

      <xsl:otherwise>
        <xsl:value-of select ="'no_value'"/>
      </xsl:otherwise>

    </xsl:choose>

  </xsl:template>

  <!-- 
  *********************** SecondaryContributor **************************
  -->
  <!--Finder secondary contributor - ellers "no_value". Denne værdi skal kun optræde i kolofonen-->
  <xsl:template name ="SetSecondaryContributor">

    <xsl:choose>

      <xsl:when test ="/dtbook/head/meta[@name='prod:secondary-contributor']">

        <!--konvertér første karakter til uppercase-->
        <xsl:call-template name ="FirstCharToUpper">
          <xsl:with-param name ="OrgString" select ="/dtbook/head/meta[@name='prod:secondary-contributor']/@content"/>
        </xsl:call-template>
        <!--<xsl:value-of select ="string(/dtbook/head/meta[@name='prod:secondary-contributor']/@content)"/>-->
      </xsl:when>

      <xsl:otherwise>
        <xsl:value-of select ="'no_value'"/>
      </xsl:otherwise>
      
    </xsl:choose>
    
  </xsl:template>
  <!-- 
  ******************** Sprog og oversætter ****************
  -->
  <xsl:template name ="SetTranslationAndlanguage">


    <xsl:choose>

      <!-- Er der en translator? -->
      <xsl:when test ="/dtbook/head/meta[@name='prod:translator']">

        <xsl:variable name ="Translator">
          <!-- Rens ovesætternavn -->
          <!-- Er der et kolon?-->
          <xsl:choose>
            <xsl:when test="contains(string(/dtbook/head/meta[@name='prod:translator']/@content),':')">
              <!-- Rens teksten -->
              <xsl:call-template name ="RemoveColon">
                <xsl:with-param name ="StringToClean" select ="string(/dtbook/head/meta[@name='prod:translator']/@content)"/>
              </xsl:call-template>
            </xsl:when>

            <xsl:otherwise>
              <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:translator']/@content)"/>
            </xsl:otherwise>
          </xsl:choose>

        </xsl:variable>

        <xsl:choose>

          <!-- Er der et mellemoriginalsprog?
            Dette er den mindst sandsynlige
            -->
          <xsl:when test ="/dtbook/head/meta[@name='prod:intermediateLanguage']">

            <xsl:variable name ="OrgLanguage">
              <xsl:call-template name ="GetOrglang"/>
            </xsl:variable>

            <xsl:variable name ="InterLanguage">
              <xsl:call-template name ="GetIntermediateLang"/>
            </xsl:variable>

            <xsl:text>Oversat til </xsl:text>
            <xsl:value-of select ="$InterLanguage"/>
            <xsl:text> af </xsl:text>
            <xsl:value-of select ="$Translator"/>
            <xsl:text>. Originalsprog: </xsl:text>
            <xsl:value-of select ="$OrgLanguage"/>

            <xsl:call-template name ="OriginalTitle"/>
          </xsl:when >

          <!-- Der er et originalsprog -->
          <xsl:when test ="/dtbook/head/meta[@name='prod:originalLanguage']">

            <xsl:variable name ="OrgLanguage">
              <xsl:call-template name ="GetOrglang"/>
            </xsl:variable>

            <!--der kan være to eller flere originalsprog!-->
            <xsl:variable name ="SecondOrgLanguage">
              <xsl:call-template name ="GetSecondOrglang"/>
            </xsl:variable>

            <xsl:choose>
              <xsl:when test ="$SecondOrgLanguage='_none'">
                <xsl:text>Oversat fra </xsl:text>
                <xsl:value-of select ="$OrgLanguage"/>
                <xsl:text> af </xsl:text>
                <xsl:value-of select ="$Translator"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>Oversat fra </xsl:text>
                <xsl:value-of select ="$OrgLanguage"/>
                <xsl:text> og </xsl:text>
                <xsl:value-of select ="$SecondOrgLanguage"/>
                <xsl:text> af </xsl:text>
                <xsl:value-of select ="$Translator"/>
              </xsl:otherwise>
            </xsl:choose>



            <xsl:call-template name ="OriginalTitle"/>
          </xsl:when >

          <!-- Der er intet originalsprog (eller mellemorg.sprog) -->
          <xsl:otherwise>
            <xsl:text>Oversat af </xsl:text>
            <xsl:value-of select ="$Translator"/>
            <xsl:call-template name ="OriginalTitle"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>

      <!-- Der er ingen translator -->
      <xsl:otherwise>

        <xsl:choose>
          <xsl:when test ="/dtbook/head/meta[@name='prod:originalLanguage']">
            <xsl:variable name ="OrgLanguage">
              <xsl:call-template name ="GetOrglang"/>
            </xsl:variable>

            <!--der kan være to eller flere originalsprog!-->
            <xsl:variable name ="SecondOrgLanguage">
              <xsl:call-template name ="GetSecondOrglang"/>
            </xsl:variable>

            <xsl:choose>
              <xsl:when test ="$SecondOrgLanguage='_none'">
                <xsl:text>Oversat fra </xsl:text>
                <xsl:value-of select ="$OrgLanguage"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>Oversat fra </xsl:text>
                <xsl:value-of select ="$OrgLanguage"/>
                <xsl:text> og </xsl:text>
                <xsl:value-of select ="$SecondOrgLanguage"/>
              </xsl:otherwise>
            </xsl:choose>

            <xsl:call-template name ="OriginalTitle"/>
          </xsl:when>

          <!-- Der er hverken oversætter eller originalsprog
            Dette er ikke et oversat værk!-->
          <xsl:otherwise>
            <xsl:value-of select ="'no_value'"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>

    </xsl:choose>



  </xsl:template>

  <xsl:template name ="OriginalTitle">
    <xsl:if test ="/dtbook/head/meta[@name='prod:originalLanguageTitle']">
      <br/>
      <xsl:text>Originaltitel: </xsl:text>
      <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:originalLanguageTitle']/@content)"/>
    </xsl:if>
  </xsl:template>

  <!-- 
  ************Oprindeligt forlag, udgave og udgivelsesår *************
  -->

  <xsl:template name ="SetSourcePublishingInfo">

    <xsl:choose>

      <!-- Er der en sourcepublisher og sourceDate? -->
      <xsl:when test ="/dtbook/head/meta[@name='prod:sourcePublisher'] and /dtbook/head/meta[@name='prod:sourceDate']">
        <xsl:text>Udgivet af </xsl:text>
        <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:sourcePublisher']/@content)"/>
        <xsl:text> i </xsl:text>
        <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:sourceDate']/@content)"/>
        <xsl:call-template name ="SetEdition"/>
        <xsl:call-template name ="SetSourceRights"/>
        <xsl:call-template name ="SetPublisherPhysicalAdress"/>
        <xsl:call-template name ="SetPublisherInternet"/>
        <xsl:call-template name ="SetPublisherEMail"/>
      </xsl:when>

      <!-- Er der kun sourcePublisher-->
      <xsl:when test ="/dtbook/head/meta[@name='prod:sourcePublisher']">
        <xsl:text>udgivet af </xsl:text>
        <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:sourcePublisher']/@content)"/>
        <xsl:call-template name ="SetEdition"/>
        <xsl:call-template name ="SetSourceRights"/>
        <xsl:call-template name ="SetPublisherPhysicalAdress"/>
        <xsl:call-template name ="SetPublisherInternet"/>
        <xsl:call-template name ="SetPublisherEMail"/>
      </xsl:when>

      <!-- Er der kun sourceDate-->
      <xsl:when test ="/dtbook/head/meta[@name='prod:sourceDate']">
        <xsl:text>udgivet i </xsl:text>
        <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:sourceDate']/@content)"/>
        <xsl:call-template name ="SetEdition"/>
        <xsl:call-template name ="SetSourceRights"/>
        <xsl:call-template name ="SetPublisherPhysicalAdress"/>
        <xsl:call-template name ="SetPublisherInternet"/>
        <xsl:call-template name ="SetPublisherEMail"/>
      </xsl:when>

      <!-- Ingen af delene -->

      <xsl:otherwise>
        <xsl:value-of select ="'no_value'"/>
      </xsl:otherwise>

    </xsl:choose>

  </xsl:template>

  <xsl:template name ="SetSourcePublishingFrontpage">

    <xsl:choose>

      <!-- Er der en sourcepublisher og sourceDate? -->
      <xsl:when test ="/dtbook/head/meta[@name='prod:sourcePublisher'] and /dtbook/head/meta[@name='prod:sourceDate']">

        <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:sourcePublisher']/@content)"/>
        <br/>
        <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:sourceDate']/@content)"/>

      </xsl:when>

      <!-- Er der kun sourcePublisher-->
      <xsl:when test ="/dtbook/head/meta[@name='prod:sourcePublisher']">

        <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:sourcePublisher']/@content)"/>

      </xsl:when>

      <!-- Ingen af delene -->

      <xsl:otherwise>
        <xsl:value-of select ="'no_value'"/>
      </xsl:otherwise>

    </xsl:choose>

  </xsl:template>

  <xsl:template name ="SetEdition">
    <xsl:if test ="/dtbook/head/meta[@name='prod:sourceEdition']">
      <br/>
      <xsl:text>Udgave: </xsl:text>
      <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:sourceEdition']/@content)"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name ="SetSourceRights">
    <xsl:if test ="/dtbook/head/meta[@name='prod:sourceRights']">
      <br/>
      <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:sourceRights']/@content)"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name ="SetPublisherInternet">

    <!--Her kan være flere linier-->
    <xsl:choose>
      <xsl:when test ="/dtbook/head/meta[@name='prod:sourcePublisherInternetAddress']">
        <xsl:for-each select ="/dtbook/head/meta[@name='prod:sourcePublisherInternetAddress']">
          <br/>
          <xsl:text>Internet: </xsl:text>
          <xsl:value-of select ="string(@content)"/>
          <!--<br/>-->
          <!--<xsl:element name ="br"/>-->
        </xsl:for-each>
      </xsl:when>
      <!--<xsl:otherwise>
        <xsl:value-of select ="'no_value'"/>
      </xsl:otherwise>-->
    </xsl:choose>
    
    <!--<xsl:if test ="/dtbook/head/meta[@name='prod:sourcePublisherInternetAddress']">
      <br/>
      <xsl:text>Internet: </xsl:text>
      <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:sourcePublisherInternetAddress']/@content)"/>
    </xsl:if>-->
  </xsl:template>

  <xsl:template name ="SetPublisherEMail">
    <!--Her kan være flere linier-->
    <xsl:choose>
      <xsl:when test ="/dtbook/head/meta[@name='prod:sourcePublisherMailToAddress']">
        <xsl:for-each select ="/dtbook/head/meta[@name='prod:sourcePublisherMailToAddress']">
          <br/>
          <xsl:text>E-mail: </xsl:text>
          <xsl:value-of select ="string(@content)"/>
          <!--<br/>-->
          <!--<xsl:element name ="br"/>-->
        </xsl:for-each>
      </xsl:when>
      <!--<xsl:otherwise>
        <xsl:value-of select ="'no_value'"/>
      </xsl:otherwise>-->
    </xsl:choose>
  </xsl:template>

  <!--'til address-->
  <xsl:template name ="SetPublisherPhysicalAdress">
    <xsl:if test ="/dtbook/head/meta[@name='prod:sourcePublisherAddress']">
      <br/>
      <xsl:value-of select ="string(/dtbook/head/meta[@name='prod:sourcePublisherAddress']/@content)"/>
    </xsl:if>
  </xsl:template>

  <!-- 
  ***************** DBB copyright og info **********************
  -->

  <xsl:template name ="SetDBBInfo">

    <xsl:choose>
      <!--hvis der både er publisher og date-->
      <xsl:when test ="/dtbook/head/meta[@name='dc:publisher'] and /dtbook/head/meta[@name='dc:date']">
        <xsl:text>Denne udgave: </xsl:text>
        <xsl:value-of select ="string(/dtbook/head/meta[@name='dc:publisher']/@content)"/>
        <xsl:text >, </xsl:text>
        <xsl:value-of select ="string(/dtbook/head/meta[@name='dc:date']/@content)"/>
        <xsl:call-template name ="SetDBBRights"/>
      </xsl:when>

      <!--hvis der kun er publisher-->
      <xsl:when test ="/dtbook/head/meta[@name='dc:publisher']">
        <xsl:text>Denne udgave: </xsl:text>
        <xsl:value-of select ="string(/dtbook/head/meta[@name='dc:publisher']/@content)"/>
        <xsl:call-template name ="SetDBBRights"/>
      </xsl:when>
      
      <xsl:otherwise>
        <xsl:value-of select ="'no_value'"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <xsl:template name ="SetDBBRights">
    <xsl:if test ="/dtbook/head/meta[@name='dc:rights']">
      <br/>
      <xsl:value-of select ="string(/dtbook/head/meta[@name='dc:rights']/@content)"/>
    </xsl:if>
  </xsl:template>

  <!-- 
  ************ Yderligere oplysninger til kolofonen **************
  -->

  <xsl:template name ="SetAdditionalInfo">

    <!--<xsl:variable name ="tst">-->
    <xsl:choose>

      <xsl:when test ="/dtbook/head/meta[@name='prod:colophonAdditionalInfo']">

        <xsl:for-each select ="/dtbook/head/meta[@name='prod:colophonAdditionalInfo']">
          <xsl:value-of select ="string(@content)"/>
          <!--<br/>-->
          <xsl:element name ="br"/>
        </xsl:for-each>

      </xsl:when>

      <xsl:otherwise>
        <xsl:value-of select ="'no_value'"/>
      </xsl:otherwise>
    </xsl:choose>
    <!--</xsl:variable>-->
  </xsl:template>

  <!-- 
  *****************Datakonvertering *********************
  -->
  <!-- Disse to templates kaldes når der er brug for at konvertere sprogkoder-->
  <xsl:template name ="GetOrglang">
    <xsl:variable name ="OrgLang">
      <xsl:call-template name ="GetLang">
        <xsl:with-param name ="LangCode" select ="string(/dtbook/head/meta[@name='prod:originalLanguage']/@content)"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:value-of select ="$OrgLang"/>
  </xsl:template>

  <xsl:template name ="GetSecondOrglang">
    <xsl:variable name ="SecondOrgLang">
      <xsl:call-template name ="GetLang">
        <xsl:with-param name ="LangCode" select ="string(/dtbook/head/meta[@name='prod:originalLanguage'][2]/@content)"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:value-of select ="$SecondOrgLang"/>
  </xsl:template>

  <xsl:template name ="GetIntermediateLang">
    <xsl:variable name ="IntermediateLang">
      <xsl:call-template name ="GetLang">
        <xsl:with-param name ="LangCode" select ="string(/dtbook/head/meta[@name='prod:intermediateLanguage']/@content)"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:value-of select ="$IntermediateLang"/>
  </xsl:template>

  <xsl:template name ="GetISOLang">
    <xsl:variable name ="ISOLangCode">
      <xsl:call-template name ="GetISOLangCode">
        <xsl:with-param name ="LangCode" select ="string(/dtbook/head/meta[@name='dc:language']/@content)"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:value-of select ="$ISOLangCode"/>
  </xsl:template>

</xsl:stylesheet>
