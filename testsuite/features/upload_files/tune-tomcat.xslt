<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- XSL transformation to tune tomcat server configuration -->

  <xsl:output omit-xml-declaration="yes" />

  <!-- increase maximum number of threads -->
  <xsl:template match="Service[@name='Catalina']/Connector/@maxThreads">
    <xsl:attribute name="maxThreads">256</xsl:attribute>
  </xsl:template>

  <!-- just copy the rest -->
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*" />
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
