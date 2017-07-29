<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>


<html>
<head>
</head>
<body>
	<rhn:toolbar base="h1" icon="header-system" imgAlt="system.common.systemAlt"
 helpUrl="/rhn/help/reference/en-US/ref.webui.systems.systems.jsp#ref.webui.systems.systems.virtual">
	  <bean:message key="virtuallist.jsp.toolbar"/>
	</rhn:toolbar>
	
	<rl:listset name="systemListSet" legend="system">
	    <c:set var="noAddToSsm" value="1" />
	    <%@ include file="/WEB-INF/pages/common/fragments/systems/system_listdisplay.jspf" %>
            <c:choose>
              <c:when test="${current.accessible}">
              </c:when>
              <c:otherwise>
            <c:out value="${current.serverName}" escapeXml="true"/>
              </c:otherwise>
            </c:choose>
	</rl:listset>
</body>
</html>
