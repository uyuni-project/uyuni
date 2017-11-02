<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html:html >
<body>
<%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>
    <h2><bean:message key="delete_confirm.jsp.header"/></h2>
    <bean:message key="delete_confirm.jsp.summary" arg0="${sid}" />
    <c:if test="${issaltsshpush}">
        <bean:message key="delete_confirm.jsp.summary.sshpush"/>
    </c:if>
    <hr/>

    <div id="delete_system_button"></div>
    <script>
        var csrfToken="<%= com.redhat.rhn.common.security.CSRFTokenValidator.getToken(session) %>";

        function getServerIdToDelete(){
            return ${sid};
        }
    </script>
    <script src="/javascript/manager/delete-system-confirm.bundle.js" type="text/javascript"></script>
</body>
</html:html>
