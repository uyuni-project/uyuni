<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html:html>
    <body>
        <rhn:toolbar base="h1" icon="header-info" imgAlt="info.alt.img">
            <bean:message key="general.jsp.toolbar"/>
        </rhn:toolbar>
        <p><bean:message key="SUSE Customer Center"/></p>
        <rhn:dialogmenu mindepth="0" maxdepth="1" definition="/WEB-INF/nav/sat_config.xml" renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />
        <div class="panel panel-default">
            <div class="panel-heading">
                <h4>SUSE Customer Center</h4>
            </div>
            <div class="panel-body">

            </div>
        </div>
    </body>
</html:html>

