<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html:html>
    <head>
        <script type="text/javascript" src="/rhn/dwr/interface/SCCConfigAjax.js"></script>
        <script type="text/javascript" src="/rhn/dwr/engine.js"></script>
        <script type="text/javascript" src="/javascript/scc-config.js"></script>
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

                <div class="alert alert-warning" role="alert"><bean:message key="sccconfig.jsp.migrationinfosmt"/></div>

                <a id="scc-start-migration-btn" class="btn btn-success"><bean:message key="sccconfig.jsp.migrate"/></a>
            </div>
        </div>

        <div id="scc-migration-dialog" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="<bean:message key='SUSE Customer Center'/>" aria-hidden="true">
            <div class="modal-dialog modal-sm">
                <div class="modal-content">
                    <div class="modal-header">
                      <h4 class="modal-title"><bean:message key="sccconfig.jsp.switchingtoscc"/></h4>
                    </div>
                    <p>blah blah</p>
                    <ul class="dialog-steps">
                    </ul>
                    <div class="modal-footer">
                      <button id="scc-migrate-dialog-close-btn" type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="hidden" id="sccconfig.jsp.channels"><bean:message key="sccconfig.jsp.channels"/></div>
        <div class="hidden" id="sccconfig.jsp.channelfamilies"></i><bean:message key="sccconfig.jsp.channelfamilies"/></div>
        <div class="hidden" id="sccconfig.jsp.products"><bean:message key="sccconfig.jsp.products"/></div>
        <div class="hidden" id="sccconfig.jsp.productchannels"><bean:message key="sccconfig.jsp.productchannels"/></div>
        <div class="hidden" id="sccconfig.jsp.subscriptions"><bean:message key="sccconfig.jsp.subscriptions"/></div>
        <div class="hidden" id="sccconfig.jsp.upgradepaths"><bean:message key="sccconfig.jsp.upgradepaths"/></div>
    </body>
</html:html>

