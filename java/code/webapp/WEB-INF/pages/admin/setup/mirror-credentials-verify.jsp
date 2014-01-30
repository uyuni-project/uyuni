<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>

<c:if test="${success}">
    <rhn:icon type="action-ok" title="mirror-credentials.jsp.success" />
</c:if>
<c:if test="${! success}">
    <rhn:icon type="action-failed" title="mirror-credentials.jsp.failed" />
</c:if>
