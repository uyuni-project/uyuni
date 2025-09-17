<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<html>
    <body>
        <%@ include file="/WEB-INF/pages/common/fragments/user/user-header.jspf" %>
        <h2><bean:message key="address.jsp.addresses"/></h2>
        <p><bean:message key="address.jsp.associated" /></p>
        <rhn:address type="M" action="user" user="${requestScope.targetuser}" address="${requestScope.addressMarketing}"/>
    </body>
</html>
