<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>


<html>
    <head>
    </head>
<body>

<script type="text/javascript" language="JavaScript">
<!--
function refreshNotifFields() {
  form = document.forms['probeEditForm'];
  form.notification_interval_min.disabled = !form.notification.checked;
  form.contact_group_id.disabled = !form.notification.checked;
  return true;
}
//-->
</script>

<%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>

<rhn:toolbar base="h2" icon="header-system"
    deletionUrl="/rhn/systems/details/probes/ProbeDelete.do?probe_id=${probe.id}&amp;sid=${system.id}"
    deletionType="probe">
 <bean:message key="probeedit.jsp.editprobe" />
</rhn:toolbar>
<html:form action="/systems/details/probes/ProbeEdit" method="POST">
  <rhn:csrf />
  <table class="details">
    <tr>
      <th><bean:message key="probeedit.jsp.probecommand" /></th>
      <td colspan="3">${probe.command.description}</td>
    </tr>
    <tr>
      <th><bean:message key="probeedit.jsp.satclusterdesc" /></th>
      <td colspan="3">${probe.satCluster.description}</td>
    </tr>
    <c:if test='${not empty probe.command.systemRequirements}'>
    <tr>
      <th><bean:message key="probeedit.jsp.commandrequirements" /></th>
      <td colspan="3"><bean:message key="${probe.command.systemRequirements}"/></td>
    </tr>
    </c:if>
    <c:if test='${not empty probe.command.versionSupport}'>
    <tr>
      <th><bean:message key="probeedit.jsp.versionsupport" /></th>
      <td colspan="3">${probe.command.versionSupport}</td>
    </tr>
    </c:if>
    <tr>
      <th><label for="description"><bean:message key="probeedit.jsp.description" /></label></th>
      <td colspan="3"><html:text property="description" maxlength="100" size="50" styleId="description"/></td>
    </tr>
    <tr>
      <th><label for="notification"><bean:message key="probeedit.jsp.notification" /></label></th>
      <td colspan="3"><html:checkbox onclick="refreshNotifFields()" property="notification" styleId="notification"/></td>
    </tr>
      <tr>
        <th><label for="notifmin"><bean:message key="probeedit.jsp.notifmin" /></label></th>
        <td colspan="3">
            <html:select property="notification_interval_min"
                  disabled="${not probeEditForm.map.notification}" styleId="notifmin">
              <html:options collection="intervals"
                property="value"
                labelProperty="label" />
            </html:select>
        </td>
      </tr>
      <tr>
        <th><label for="notifmethod"><bean:message key="probeedit.jsp.notifmethod" /></label></th>
        <td colspan="3"><html:select property="contact_group_id"
                  disabled="${not probeEditForm.map.notification}" styleId="notifmethod">
              <html:options collection="contactGroups"
                property="value"
                labelProperty="label" />
            </html:select>
        </td>
      </tr>
    <tr>
      <th><label for="checkinterval"><bean:message key="probeedit.jsp.checkinterval" /></label></th>
      <td colspan="3">
        <html:select property="check_interval_min" styleId="checkinterval">
              <html:options collection="intervals"
                property="value"
                labelProperty="label" />
        </html:select>
      </td>
    </tr>

    <%@ include file="/WEB-INF/pages/common/fragments/probes/render-param-value-list.jspf" %>
    <tr>
      <td></td>
      <td colspan="3" align="right"><html:submit><bean:message key="probedit.jsp.updateprobe"/></html:submit></td>
    </tr>
  </table>
  <html:hidden property="sid" value="${param.sid}"/>
  <html:hidden property="probe_id" value="${probe.id}"/>
  <html:hidden property="submitted" value="true"/>
</html:form>

</body>
</html>
