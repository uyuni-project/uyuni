<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
    <body>
        <c:if test="${not isUyuni}">
            <h1><rhn:icon type="header-info" /><bean:message key="help.jsp.eula.title"/></h1>
            ${requestScope.EulaText}
        </c:if>
    </body>
</html>
