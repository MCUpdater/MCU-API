<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:x="http://www.mcupdater.com"
                exclude-result-prefixes="x" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://www.mcupdater.com http://files.mcupdater.com/ServerPackv2.xsd">

    <xsl:template match="/">
        <html>
            <body>
                <xsl:for-each select="x:ServerPack/x:Server">
                    <h2>
						<img src="{@iconUrl}" height="32" width="32"/>&#160;  
                        <xsl:value-of select="@name"/> &lt;<xsl:value-of select="@id"/>&gt;- Minecraft
                        <xsl:value-of select="@version"/> (revision <xsl:value-of select="@revision"/>)
                    </h2>
                    <xsl:if test="count(x:Import)>0">
                        <h3>Imports</h3>
                        <table border="1">
                            <tr bgcolor="#cccccc">
                                <th>Id</th>
                                <th>URL</th>
                            </tr>
                            <xsl:for-each select="x:Import">
                                <tr>
                                    <td>
                                        <xsl:value-of select="."/>
                                    </td>
                                    <td>
                                        <a href="{@url}">
                                            <xsl:value-of select="@url"/>
                                        </a>
                                    </td>
                                </tr>
                            </xsl:for-each>
                        </table>
                    </xsl:if>
                    <h3>Mods</h3>
                    <table border="1">
                        <tr bgcolor="#cccccc">
                            <th>Name</th>
							<th>Author(s)</th>
                            <th>URL</th>
                            <th>Version</th>
                        </tr>
                        <xsl:for-each select="x:Module">
                            <tr>
                                <td>
                                    <xsl:value-of select="@name"/>
                                </td>
								<td>
									<xsl:value-of select="x:Meta/x:authors"/>
								</td>
                                <td>
                                    <a href="{x:Meta/x:url}">
                                        <xsl:value-of select="x:Meta/x:url"/>
                                    </a>
                                </td>
                                <td>
                                    <xsl:value-of select="x:Meta/x:version"/>
                                </td>
                            </tr>
                        </xsl:for-each>
                    </table>
                    <hr/>
                </xsl:for-each>
                <h5>MCUpdater ServerPack version
                    <xsl:value-of select="x:ServerPack/@version"/>
                </h5>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
