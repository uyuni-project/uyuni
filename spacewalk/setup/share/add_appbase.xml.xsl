<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/Server/Service/Engine/Host">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:text>
            </xsl:text>
            <xsl:element name="Host">
                <xsl:copy-of select="@*" />
                <xsl:attribute name="appBase">/usr/share/susemanager/www/tomcat/webapps</xsl:attribute>
            </xsl:element>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
