<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<html>
<body>

<rhn:require acl="user_authenticated()">

<h1>
  <rhn:icon type="system-warn" />
  <bean:message key="missing_capabilities.jsp.header"/>
</h1>
<p><bean:message key="missing_capabilities.jsp.title"/></p>
<p><bean:message key="missing_capabilities.jsp.summary"
                        arg0="/rhn/systems/details/Overview.do?sid=${error.server.id}"
                        arg1="${error.server.name}"
                        arg2="${error.capability}"/></p>

</rhn:require>

</body>
</html>
