<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<html>
    <body>
        <h1><rhn:icon type="header-info" /><bean:message key="help.jsp.copyright.title"/></h1>
        <p><bean:message key="copyright.jsp.title"/></p>
        <ul>
            <li><bean:message key="copyright.jsp.urlagreement"/></li>
        </ul>
        <bean:message key="copyright.jsp.body"/>
    </body>
</html>
