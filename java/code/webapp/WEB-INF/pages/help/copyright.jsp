<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
    <body>
        <h1><rhn:icon type="header-info" /><bean:message key="help.jsp.copyright.title"/></h1>
        <p><bean:message key="copyright.jsp.title"/></p>
        <c:set var="product_name" scope="page" value="${rhn:getConfig('product_name')}" />
        <c:choose>
            <c:when test="${product_name == 'Uyuni'}">
                <bean:message key="copyright.jsp.body.uyuni"/>
            </c:when>
            <c:otherwise>
                <ul>
                    <li><bean:message key="copyright.jsp.urlagreement"/></li>
                </ul>
                <bean:message key="copyright.jsp.body"/>
            </c:otherwise>
        </c:choose>
    </body>
</html>
