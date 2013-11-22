<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<html:xhtml />
<html>
<head>
  <meta http-equiv="Pragma" content="no-cache" />
</head>

<body>
  <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>
  <br />

  <h2>
    <img src="/img/icon_kickstart_session-medium.gif"
      alt="<bean:message key='system.common.kickstartAlt' />"
    >
    <bean:message key="kickstart.powermanagement.jsp.heading" />
  </h2>

  <c:if test="${fn:length(types) >= 1}">
    <html:form action="/systems/details/kickstart/PowerManagement.do?sid=${sid}">

      <c:set var="showRequired" value="true" />
      <c:set var="showPowerStatus" value="true" />
      <%@ include file="/WEB-INF/pages/common/fragments/kickstart/powermanagement-options.jspf" %>

      <input type="submit" name="dispatch"
        value="<bean:message key="kickstart.powermanagement.jsp.save" />" />
      <input type="submit" name="dispatch"
        value="<bean:message key="kickstart.powermanagement.jsp.save_power_on" />" />
      <input type="submit" name="dispatch"
        value="<bean:message key="kickstart.powermanagement.jsp.save_power_off" />" />
      <input type="submit" name="dispatch"
        value="<bean:message key="kickstart.powermanagement.jsp.save_reboot" />" />
      <input type="submit" name="dispatch"
        value="<bean:message key="kickstart.powermanagement.jsp.save_get_status" />" />
    </html:form>
  </c:if>
</body>
</html>
