<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/config-managment" prefix="cfg" %>

<html>
<head>
    <meta name="name" value="sdc.config.jsp.header" />
</head>
<body>
<%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>

<rhn:toolbar base="h2"
             icon="header-configuration"
             imgAlt="system.common.systemAlt">
  <bean:message key="sdcimportfile.jsp.header"
                arg0="${fn:escapeXml(system.name)}"/>
</rhn:toolbar>

  <div class="page-summary">
    <p>
    <bean:message key="sdcimportfile.jsp.summary"
                  arg0="${fn:escapeXml(system.name)}"/>
    </p>
  </div>

<html:form method="post"
		action="/systems/details/configuration/addfiles/ImportFileSubmit.do?sid=${system.id}">
  <rhn:csrf />

  <h2><bean:message key="sdcimportfile.jsp.new"/></h2>
  <p><bean:message key="sdcimportfile.jsp.newsummary"/></p>

  <table class="details">
    <tr>
      <th><bean:message key="sdcimportfile.jsp.paths"/></th>
      <td>
        <html:textarea property="contents" rows="10" cols="80" /><br />
        <span class="small-text"><bean:message key="sdcimportfile.jsp.example"/></span>
      </td>
    </tr>
  </table>
<h2><bean:message key="sdcimportfile.jsp.existing"/></h2>
  <p><bean:message key="sdcimportfile.jsp.oldsummary"
                   arg0="${fn:escapeXml(system.name)}"/></p>

  <rhn:list pageList="${requestScope.pageList}"
            noDataText="sdcimportfile.jsp.noFiles">

      <rhn:listdisplay set="${requestScope.set}"
                       filterBy="sdcimportfile.jsp.filename">
        <rhn:set value="${current.id}"/>

        <rhn:column header="sdcimportfile.jsp.filename"
			url="/rhn/configuration/file/FileDetails.do?crid=${current.configRevisionId}&cfid=${current.configFileId}">
			${current.path}
      	</rhn:column>

      	<rhn:column header="sdcimportfile.jsp.channel">
			<cfg:channel id="${current.configChannelId}"   name="${current.channelNameDisplay}"
						 type="${current.configChannelType}"/>
      	</rhn:column>

      </rhn:listdisplay>
  </rhn:list>
  <div class="text-right">
      <hr />
      <html:submit styleClass="btn btn-default" property="dispatch">
          <bean:message key="sdcimportfile.jsp.button"/>
      </html:submit>
  </div>
  <rhn:submitted/>
</html:form>

</body>
</html>
