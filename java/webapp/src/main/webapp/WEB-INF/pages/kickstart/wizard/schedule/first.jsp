<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>

<head>

<meta http-equiv="Pragma" content="no-cache" />

</head>

<body>

<script language="javascript">
    //<!--
    function setStep(stepName) {
        var field = document.getElementById("wizard-step");
        field.value = stepName;
    }
    //-->
</script>

<%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>

<h2>
  <rhn:icon type="header-kickstart" title="system.common.kickstartAlt" />
  <c:choose>
    <c:when test="${system.bootstrap}">
      <bean:message key="kickstart.configure.heading1.jsp" />
    </c:when>
    <c:otherwise>
      <bean:message key="kickstart.schedule.heading1.jsp" />
    </c:otherwise>
  </c:choose>
</h2>

<c:if test="${requestScope.isVirtualGuest == 'true' or requestScope.invalidContactMethod}">
    <c:if test="${requestScope.invalidContactMethod}">
        <div class="page-summary">
            <bean:message key="kickstart.schedule.invalid.contact.method"/>
        </div>
    </c:if>
    <c:if test="${requestScope.isVirtualGuest == 'true'}">
        <div class="page-summary">
            <bean:message key="kickstart.schedule.cannot.provision.guest"/>
            <c:if test="${requestScope.virtHostIsRegistered} == 'true'">
                <bean:message key="kickstart.schedule.visit.host.virt.tab" arg0="${requestScope.hostSid}"/>
            </c:if>
        </div>
    </c:if>
</c:if>

<c:if test="${requestScope.isVirtualGuest == 'false' and not requestScope.invalidContactMethod}">

    <div class="page-summary">
    <p>
      <c:choose>
        <c:when test="${system.bootstrap}">
          <bean:message key="kickstart.configure.heading1.text.jsp" />
        </c:when>
        <c:otherwise>
          <bean:message key="kickstart.schedule.heading1.text.jsp" />
        </c:otherwise>
      </c:choose>
    </p>
    <h2><bean:message key="kickstart.schedule.heading2.jsp" /></h2>
    </div>

    <div class="page-summary">
    <p>
        <bean:message key="kickstart.schedule.heading2.text.jsp" />
    </p>

<c:set var="form" value="${kickstartScheduleWizardForm.map}"/>
<c:set var="regularKS" value="true"/>
<rl:listset name="wizard-form">
    <rhn:csrf />
    <rhn:submitted />
                <%@ include file="/WEB-INF/pages/common/fragments/kickstart/schedule/profile-list.jspf" %>
        <%@ include file="/WEB-INF/pages/common/fragments/kickstart/schedule/ks-wizard.jspf" %>
        </rl:listset>
    </div>
</c:if>

</body>
</html>
