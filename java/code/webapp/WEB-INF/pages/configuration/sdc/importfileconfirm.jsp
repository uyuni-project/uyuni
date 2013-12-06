<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>


<html>
<head>
    <meta name="name" value="sdc.config.jsp.header" />
</head>
<body>
<%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>

<rhn:toolbar base="h2" icon="fa-desktop" >
  <bean:message key="sdcimportconfirm.jsp.header" />
</rhn:toolbar>

  <div class="page-summary">
    <p>
    <bean:message key="sdcimportconfirm.jsp.summary"
                  arg0="${system.name}"/>
    </p>
  </div>

<html:form method="post"
		action="/systems/details/configuration/addfiles/ImportFileConfirmSubmit.do?sid=${system.id}">
  <rhn:csrf />
  <rhn:list pageList="${requestScope.pageList}"
            noDataText="sdcimportconfirm.jsp.noFiles">

      <rhn:listdisplay>
        <rhn:column header="sdcimportconfirm.jsp.filename">
			${current.path}
      	</rhn:column>

      	<rhn:column header="sdcimportconfirm.jsp.channel"
                  url="/rhn/configuration/ChannelOverview.do?ccid=${current.configChannelId}"
                  renderUrl="${not empty current.configChannelType}">
          <c:choose>
            <c:when test="${empty current.configChannelType}">
              <i><bean:message key="sdcimportconfirm.jsp.new" /></i>
            </c:when>
            <c:when test="${current.configChannelType == 'normal'}">
              <i class="fa spacewalk-icon-software-channels" title="<bean:message key="config.common.globalAlt" />"></i>
    	      ${current.channelNameDisplay}
            </c:when>
            <c:when test="${current.configChannelType == 'local_override'}">
              <i class="fa fa-desktop" title="<bean:message key="config.common.localAlt" />"></i>
              ${current.channelNameDisplay}
            </c:when>
            <c:otherwise>
              <i class="fa spacewalk-icon-sandbox" title="<bean:message key="config.common.sandboxAlt" />"></i>
              ${current.channelNameDisplay}
            </c:otherwise>
          </c:choose>
        </rhn:column>

      </rhn:listdisplay>
    </rhn:list>

    <c:if test="${not empty requestScope.pageList}">
      <p><bean:message key="sdcimportconfirm.jsp.widgetsummary" /></p>
      <table class="schedule-action-interface" align="center">
        <tr>
          <td><html:radio property="use_date" value="false" /></td>
          <th><bean:message key="sdcimportconfirm.jsp.now"/></th>
        </tr>
        <tr>
          <td><html:radio property="use_date" value="true" /></td>
          <th><bean:message key="sdcimportconfirm.jsp.usedate"/></th>
        </tr>
        <tr>
          <th><i class="fa spacewalk-icon-schedule" title="<bean:message key='syncprofile.jsp.selection'/>"></i>
          </th>
          <td>
            <jsp:include page="/WEB-INF/pages/common/fragments/date-picker.jsp">
              <jsp:param name="widget" value="date"/>
            </jsp:include>
          </td>
        </tr>
      </table>

      <div class="text-right">
        <hr />
          <html:submit property="dispatch">
            <bean:message key="sdcimportconfirm.jsp.confirm" />
          </html:submit>
      </div>
    </c:if>

</html:form>

</body>
</html>
