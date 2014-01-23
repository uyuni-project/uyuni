<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>
    <head>
    </head>
    <body>
        <rhn:toolbar base="h1" icon="header-preferences">Setup Wizard</rhn:toolbar>
        <rhn:dialogmenu mindepth="0" maxdepth="1" definition="/WEB-INF/nav/setup_wizard.xml"
                        renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />
        <p>
            Configure your mirror credentials below.
        </p>
        <table class="table">
            <tr>
                <th>User</th>
                <th>Password</th>
                <th>Email</th>
            </tr>
        </table>
    </body>
</html>
