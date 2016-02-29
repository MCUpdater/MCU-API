<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:x="http://www.mcupdater.com"
                exclude-result-prefixes="x" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://www.mcupdater.com http://files.mcupdater.com/ServerPackv2.xsd">

	<xsl:template match="/">
		<html lang="en-US">
			<head>
				<link href="//code.jquery.com/ui/1.11.2/themes/vader/jquery-ui.min.css" rel="stylesheet" type="text/css" />
				<!--[if lt IE 9]>
				<script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
				<![endif]-->
				<style>
					td {font-size:12px; padding: 10px;}
					th {padding: 10px; color: green; text-align:left}
					.modname {font-weight: bold;}
					.mcupdater {text-align: center;}
				</style>
			</head>
			<body class="jquery-ui page singular">
				<div class="container">
					<div class="content-wrapper">
						<div class="ui-accordion ui-widget">
							<xsl:for-each select="x:ServerPack/x:Server">

								<div class="ui-accordion-header ui-state-default ui-accordion-header-active ui-state-active ui-corner-top ui-accordion-icons">
									<h2>
										<img src="{@iconUrl}" height="32" width="32"/>&#160;
										<a href="{@newsUrl}"><xsl:value-of select="@name"/></a> Modpack (v<xsl:value-of select="@revision"/>) for Minecraft <xsl:value-of select="@version"/>
									</h2>
								</div>

								<div class="ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom ui-accordion-content-active">
									<h3>Mods</h3>
									<table class="ui-widget-content">
										<tr>
											<th>Name</th>
											<th>Version</th>
											<th>Description</th>
										</tr>
										<xsl:for-each select="x:Module">
											<tr>
												<td class="modname">
													<xsl:choose>
														<xsl:when test="string-length(x:Meta/x:url) &gt; 0">
															<a href="{x:Meta/x:url}">
																<xsl:value-of select="@name"/>
															</a>
														</xsl:when>
														<xsl:otherwise>
															<xsl:value-of select="@name"/>
														</xsl:otherwise>
													</xsl:choose>
												</td>
												<td>
													<xsl:value-of select="x:Meta/x:version"/>
												</td>
												<td>
													<xsl:value-of select="x:Meta/x:description"/><br/>
													<xsl:if test="string-length(x:Meta/x:authors) &gt; 0">
														Authors: <em><xsl:value-of select="x:Meta/x:authors"/></em>
													</xsl:if>

												</td>
											</tr>
										</xsl:for-each>
									</table>

									<xsl:if test="count(x:Import)&gt;0">
										<h3>Imports</h3>
										<table class="ui-widget-content" style="width:100%">
											<tr>
												<th>Id</th>
											</tr>
											<xsl:for-each select="x:Import">
												<tr>
													<td class="modname">
														<xsl:choose>
															<xsl:when test="string-length(@url) &gt; 0">
																<a href="{@url}">
																	<xsl:value-of select="."/>
																</a>
															</xsl:when>
															<xsl:otherwise>
																<xsl:value-of select="."/>
															</xsl:otherwise>
														</xsl:choose>
													</td>
												</tr>
											</xsl:for-each>
										</table>
									</xsl:if>

								</div>
							</xsl:for-each>

							<div class="mcupdater ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom">
								Created using <a href="http://www.mcupdater.com">MCUpdater</a> ServerPack version <xsl:value-of select="x:ServerPack/@version"/>
							</div>

						</div>
					</div>
				</div>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
