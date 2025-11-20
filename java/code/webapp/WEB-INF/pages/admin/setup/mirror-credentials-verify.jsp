<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>


<c:if test="${fromDir}">
    <rhn:icon type="system-warn" title="mirror-credentials.jsp.fromDir" />
</c:if>

<button type="button" class="btn btn-tertiary" onClick="verifyCredentials('${credentialsId}', true);">
    <c:if test="${! fromDir}">
        <c:if test="${success}">
            <rhn:icon type="setup-wizard-creds-verified" title="mirror-credentials.jsp.success" />
        </c:if>
        <c:if test="${! success}">
            <rhn:icon type="setup-wizard-creds-failed" title="mirror-credentials.jsp.failed" />
        </c:if>
    </c:if>
</button>
