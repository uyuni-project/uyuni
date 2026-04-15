<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html>
<body>
<%@ include file="/WEB-INF/pages/common/fragments/ssm/header.jspf" %>

<p><bean:message key="reboot.jsp.widgetsummary" /></p>
<rl:listset name="systemListSet">
  <c:set var="notSelectable" value="True"/>
  <c:set var="noCsv" value="1" />
  <c:set var="noAddToSsm" value="1" />

  <rhn:submitted />
  <div class="spacewalk-section-toolbar">
    <div class="action-button-wrapper">
      <html:submit styleClass="btn btn-primary" property="dispatch">
        <bean:message key="ssm.misc.reboot.confirm" />
      </html:submit>
    </div>
  </div>

  <jsp:include page="/WEB-INF/pages/common/fragments/schedule-options.jspf"/>

  <%@ include file="/WEB-INF/pages/common/fragments/systems/system_listdisplay.jspf" %>
</rl:listset>
</body>
</html>
