<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean"
	prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html"
	prefix="html"%>


<html>
<body>
<rhn:toolbar base="h1" icon="header-system"
 helpUrl="/rhn/help/reference/en-US/s1-sm-configuration.jsp#config-systems">
  <bean:message key="targetsystems.jsp.toolbar" />
</rhn:toolbar>

  <p>
  <bean:message key="targetsystems.jsp.summary"
    arg0="${channel.displayName}"
	arg1="/rhn/configuration/ChannelOverview.do?ccid=${ccid}"
	arg2="/img/folder-config-sm.png" />
  </p>

<html:form method="POST" action="/configuration/system/TargetSystemsSubmit">
  <rhn:csrf />
  <rhn:list pageList="${requestScope.pageList}"
            noDataText="targetsystems.jsp.noSystems">

    <rhn:listdisplay filterBy="system.common.systemName"
                       set="${requestScope.set}">
      <rhn:set value="${current.id}" disabled="${!current.selectable}"/>
      <%@ include file="/WEB-INF/pages/common/fragments/configuration/enablelist.jspf" %>
    </rhn:listdisplay>
  </rhn:list>
  <%@ include file="/WEB-INF/pages/common/fragments/configuration/enablewidgets.jspf" %>
  <html:hidden property="submitted" value="true"/>
</html:form>

</body>
</html>
