<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<html>
<head>
<script type="text/javascript" src="/javascript/highlander.js?cb=${rhn:getConfig('web.version')}"></script>
</head>
<body>

    <h1><rhn:icon type="header-info" /><bean:message key="help.jsp.helpdesk"/></h1>

    <ul id="help-url-list">

        <li>
            <a style="font-size:12pt" href=/rhn/help/dispatcher/getting_started_guide>
                <bean:message key="help.jsp.gettingstarted.title"/>
            </a>
            <br />
            <bean:message key="help.jsp.gettingstarted"/>
        </li>

        <li>
            <a style="font-size:12pt" href=/rhn/help/dispatcher/reference_guide>
                <bean:message key="help.jsp.refguide.title"/>
            </a>
            <br />
            <bean:message key="help.jsp.detailed"/>
        </li>

        <li>
            <a style="font-size:12pt" href=/rhn/help/dispatcher/best_practices_guide>
                <bean:message key="help.jsp.bestpractices.title"/>
            </a>
            <br />
            <bean:message key="help.jsp.bestpractices"/>
        </li>

        <li>
            <a style="font-size:12pt" href=/rhn/help/dispatcher/advanced_topics_guide>
                <bean:message key="help.jsp.advancedtopics.title"/>
            </a>
            <br />
            <bean:message key="help.jsp.advancedtopics"/>
        </li>

        <li>
            <a style="font-size:12pt" href=/rhn/help/dispatcher/release_notes>
                <bean:message key="help.jsp.release.title"/>
            </a>
            <br />
        </li>

    </ul>

</body>
</html>
