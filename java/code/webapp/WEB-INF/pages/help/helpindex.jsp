<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<html>
<head>
<script type="text/javascript" src="/javascript/highlander.js"></script>
</head>
<body>

    <h1><rhn:icon type="header-info" /><bean:message key="help.jsp.helpdesk"/></h1>

    <ul id="help-url-list">

        <li>
            <a style="font-size:12pt" href=/rhn/help/dispatcher/install_guide>
                <bean:message key="help.jsp.install.title"/>
            </a>
            <strong><bean:message key="help.jsp.translation"/></strong>
            <br />
            <bean:message key="help.jsp.install"/>
        </li>

        <li>
            <a style="font-size:12pt" href=/rhn/help/dispatcher/proxy_guide>
                <bean:message key="help.jsp.proxy.title"/>
            </a>
            <br />
            <bean:message key="help.jsp.proxy"/>
        </li>

        <li>
            <a style="font-size:12pt" href=/rhn/help/dispatcher/client_config_guide>
                <bean:message key="help.jsp.clients.title"/>
            </a>
            <strong><bean:message key="help.jsp.translation"/></strong>
            <br />
            <bean:message key="help.jsp.clients"/>
        </li>

        <li>
            <a style="font-size:12pt" href=/rhn/help/dispatcher/user_guide>
                <bean:message key="help.jsp.user.title"/>
            </a>
            <br />
            <bean:message key="help.jsp.user"/>
        </li>

        <li>
            <a style="font-size:12pt" href=/rhn/help/dispatcher/getting_started_guide>
                <bean:message key="help.jsp.start.title"/>
            </a>
            <br />
            <bean:message key="help.jsp.start"/>
        </li>

        <li>
            <a style="font-size:12pt" href=/rhn/help/dispatcher/release_notes>
                <bean:message key="help.jsp.release.title"/>
            </a>
            <strong><bean:message key="help.jsp.translation"/></strong>
            <br />
        </li>

    </ul>

</body>
</html>
