<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" indent="no" omit-xml-declaration="yes" />
<xsl:preserve-space elements="Connector"/>

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="/Server/Service[@name='Catalina']/Connector[@port='8009' and (not(@address) or @address='127.0.0.1')]">
  <xsl:element name="Connector">
    <xsl:copy-of select="@*" />
    <xsl:attribute name="URIEncoding">UTF-8</xsl:attribute>
    <xsl:attribute name="address">127.0.0.1</xsl:attribute>
    <xsl:attribute name="maxThreads">150</xsl:attribute>
    <xsl:attribute name="connectionTimeout">900000</xsl:attribute>
    <xsl:attribute name="keepAliveTimeout">300000</xsl:attribute>
    <xsl:attribute name="secretRequired">false</xsl:attribute>
    <xsl:attribute name="scheme">https</xsl:attribute>
    <xsl:attribute name="secure">true</xsl:attribute>
    <xsl:attribute name="proxyPort">443</xsl:attribute>
  </xsl:element>
  <xsl:if test="not(../Connector[@port='8009' and @address='::1'])">
  <xsl:copy-of select="preceding-sibling::node()[last()][self::text()]" />
  <xsl:element name="Connector">
    <xsl:copy-of select="@*" />
    <xsl:attribute name="URIEncoding">UTF-8</xsl:attribute>
    <xsl:attribute name="address">::1</xsl:attribute>
    <xsl:attribute name="maxThreads">150</xsl:attribute>
    <xsl:attribute name="connectionTimeout">900000</xsl:attribute>
    <xsl:attribute name="keepAliveTimeout">300000</xsl:attribute>
    <xsl:attribute name="secretRequired">false</xsl:attribute>
    <xsl:attribute name="scheme">https</xsl:attribute>
    <xsl:attribute name="secure">true</xsl:attribute>
    <xsl:attribute name="proxyPort">443</xsl:attribute>
  </xsl:element>
  </xsl:if>
</xsl:template>

<xsl:template match="/Server/Service[@name='Catalina']/Connector[@port='8009' and @address='::1']">
  <xsl:element name="Connector">
    <xsl:copy-of select="@*" />
    <xsl:attribute name="URIEncoding">UTF-8</xsl:attribute>
    <xsl:attribute name="address">::1</xsl:attribute>
    <xsl:attribute name="maxThreads">150</xsl:attribute>
    <xsl:attribute name="connectionTimeout">900000</xsl:attribute>
    <xsl:attribute name="keepAliveTimeout">300000</xsl:attribute>
    <xsl:attribute name="secretRequired">false</xsl:attribute>
    <xsl:attribute name="scheme">https</xsl:attribute>
    <xsl:attribute name="secure">true</xsl:attribute>
    <xsl:attribute name="proxyPort">443</xsl:attribute>
  </xsl:element>
</xsl:template>

<xsl:template match="/Server/Service[@name='Catalina'][not(Connector[@port='8009'])]">
  <xsl:copy>
    <xsl:apply-templates select="@*"/>
    <xsl:text>
    </xsl:text>
    <xsl:element name="Connector">
      <xsl:attribute name="port">8009</xsl:attribute>
      <xsl:attribute name="protocol">AJP/1.3</xsl:attribute>
      <xsl:attribute name="redirectPort">8443</xsl:attribute>
      <xsl:attribute name="URIEncoding">UTF-8</xsl:attribute>
      <xsl:attribute name="address">127.0.0.1</xsl:attribute>
      <xsl:attribute name="maxThreads">150</xsl:attribute>
      <xsl:attribute name="connectionTimeout">900000</xsl:attribute>
      <xsl:attribute name="keepAliveTimeout">300000</xsl:attribute>
      <xsl:attribute name="secretRequired">false</xsl:attribute>
      <xsl:attribute name="scheme">https</xsl:attribute>
      <xsl:attribute name="secure">true</xsl:attribute>
      <xsl:attribute name="proxyPort">443</xsl:attribute>
    </xsl:element>
    <xsl:text>
    </xsl:text>
    <xsl:element name="Connector">
      <xsl:attribute name="port">8009</xsl:attribute>
      <xsl:attribute name="protocol">AJP/1.3</xsl:attribute>
      <xsl:attribute name="redirectPort">8443</xsl:attribute>
      <xsl:attribute name="URIEncoding">UTF-8</xsl:attribute>
      <xsl:attribute name="address">::1</xsl:attribute>
      <xsl:attribute name="maxThreads">150</xsl:attribute>
      <xsl:attribute name="connectionTimeout">900000</xsl:attribute>
      <xsl:attribute name="keepAliveTimeout">300000</xsl:attribute>
      <xsl:attribute name="secretRequired">false</xsl:attribute>
      <xsl:attribute name="scheme">https</xsl:attribute>
      <xsl:attribute name="secure">true</xsl:attribute>
      <xsl:attribute name="proxyPort">443</xsl:attribute>
    </xsl:element>
  <xsl:apply-templates select="node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="/Server/Service[@name='Catalina']/Connector[@port='8080']">
  <xsl:element name="Connector">
    <xsl:copy-of select="@*" />
    <xsl:attribute name="URIEncoding">UTF-8</xsl:attribute>
    <xsl:attribute name="address">127.0.0.1</xsl:attribute>
  </xsl:element>
</xsl:template>

<xsl:template match="/Server/Service[@name='Catalina']/Engine/Host">
  <xsl:copy>
    <xsl:apply-templates select="@*"/>
    <xsl:if test="not(Valve[@className='org.apache.catalina.valves.RemoteIpValve'])">
      <xsl:element name="Valve">
        <xsl:attribute name="className">org.apache.catalina.valves.RemoteIpValve</xsl:attribute>
        <xsl:attribute name="internalProxies">127\.0\.0\.1|0:0:0:0:0:0:0:1</xsl:attribute>
        <xsl:attribute name="protocolHeader">X-Forwarded-Proto</xsl:attribute>
        <xsl:attribute name="remoteIpHeader">X-Forwarded-For</xsl:attribute>
      </xsl:element>
    </xsl:if>
    <xsl:apply-templates select="node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
