<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html:xhtml/>
<html>
  <body>
    <%@ include file="/WEB-INF/pages/common/fragments/ssm/header.jspf" %>
    <h2><bean:message key="ssm.provisioning.powermanagement.operations.header"/></h2>
    <div class="page-summary">
      <p><bean:message key="ssm.provisioning.powermanagement.operations.summary"/></p>
    </div>

    <%@ include file="/WEB-INF/pages/common/fragments/ssm/system_list.jspf" %>

    <html:form action="/systems/ssm/provisioning/PowerManagementOperations.do">
      <rhn:csrf />
      <rhn:submitted />

      <input type="submit" name="dispatch"
        value="<bean:message key="ssm.provisioning.powermanagement.operations.poweron" />"
        class="btn btn-default" />
      <input type="submit" name="dispatch"
        value="<bean:message key="ssm.provisioning.powermanagement.operations.poweroff" />"
        class="btn btn-default" />
      <input type="submit" name="dispatch"
        value="<bean:message key="ssm.provisioning.powermanagement.operations.reboot" />"
        class="btn btn-default" />
    </html:form>
  </body>
</html>
