<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>
    <head>
    </head>
    <body>
        <rhn:toolbar base="h1" icon="header-preferences">
            <bean:message key="Setup Wizard" />
        </rhn:toolbar>
        <rhn:dialogmenu mindepth="0" maxdepth="1"
                        definition="/WEB-INF/nav/setup_wizard.xml"
                        renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />
        <p>
            Configure your mirror credentials below.
        </p>
        <rl:listset name="mirrorCredsListSet">
            <rhn:csrf />
            <rl:list name="mirrorCredsList"
                     dataset="mirrorCredsList"
                     emptykey="mirror-credentials.jsp.empty">
                <rl:column headerkey="mirror-credentials.jsp.user"
                           bound="true"
                           attr="user" />
                <rl:column headerkey="mirror-credentials.jsp.password"
                           bound="true"
                           attr="password" />
                <rl:column headerkey="mirror-credentials.jsp.email"
                           bound="true"
                           attr="email" />
            </rl:list>
        </rl:listset>
    </body>
</html>
