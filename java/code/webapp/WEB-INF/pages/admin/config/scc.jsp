<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html:html>
    <head>
        <script type="text/javascript" src="/rhn/dwr/interface/SCCConfigAjax.js"></script>
        <script type="text/javascript" src="/rhn/dwr/engine.js"></script>
        <script type="text/javascript" src="/javascript/susemanager-scc-refresh-dialog.js"></script>
        <script type="text/javascript" src="/javascript/susemanager-scc.js"></script>
    </head>
    <body>
        <rhn:toolbar base="h1" icon="header-info" imgAlt="info.alt.img">
            <bean:message key="general.jsp.toolbar"/>
        </rhn:toolbar>
        <p><bean:message key="SUSE Customer Center"/></p>
        <rhn:dialogmenu mindepth="0" maxdepth="1" definition="/WEB-INF/nav/sat_config.xml" renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />
        <div class="panel panel-default">
            <div class="panel-heading">
                <h4><bean:message key="SUSE Customer Center"/></h4>
            </div>
            <div class="panel-body">
                <p id="still-ncc-msg"><i class="fa fa-exclamation-triangle fa-1-5x text-warning"></i><bean:message key="sccconfig.jsp.stillncc"/></p>
                <p><bean:message key="sccconfig.jsp.migrationinfo"/></p>

                <c:if test="${localMirrorUsed}">
                    <div class="alert alert-warning" role="alert"><bean:message key="sccconfig.jsp.migrationinfosmt"/></div>
                </c:if>
                <div class="alert alert-warning" role="alert"><bean:message key="sccconfig.jsp.disablecronjobs"/></div>
                <a id="scc-start-migration-btn" class="btn btn-success"><bean:message key="sccconfig.jsp.migrate"/></a>
            </div>
        </div>

        <jsp:include page="/WEB-INF/pages/common/fragments/admin/scc-refresh-dialog.jspf"/>
        <div class="hidden" id="iss-master" data-iss-master="${issMaster}"></div>
        <div class="hidden" id="sccconfig.jsp.switchingtoscc"><bean:message key="sccconfig.jsp.switchingtoscc"/></div>
    </body>
</html:html>
