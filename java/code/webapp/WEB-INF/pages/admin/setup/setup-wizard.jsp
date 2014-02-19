<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>
    <head>
        <script type="text/javascript" src="/rhn/dwr/interface/SetupWizardRenderer.js"></script>
        <script type="text/javascript" src="/rhn/dwr/engine.js"></script>
        <script type="text/javascript" src="/javascript/susemanager-setup-wizard.js"></script>
    </head>
    <body>
        <rhn:toolbar base="h1" icon="header-preferences">
            <bean:message key="Setup Wizard" />
        </rhn:toolbar>

        <!-- Custom tabs loading page content via AJAX -->
        <div class="spacewalk-content-nav">
            <ul class="nav nav-tabs">
                <li class="active">
                    <a data-target="#" data-toggle="pill" href="#" onClick="SetupWizardRenderer.renderPage(0, makeAjaxCallback('setup-wizard-content', false));">HTTP Proxy</a>
                </li>
                <li>
                    <a data-target="#" data-toggle="pill" href="#" onClick="SetupWizardRenderer.renderPage(1, makeAjaxCallback('setup-wizard-content', false));">Mirror Credentials</a>
                </li>
                <li>
                    <a data-target="#" data-toggle="pill" href="#" onClick="SetupWizardRenderer.renderPage(2, makeAjaxCallback('setup-wizard-content', false));">SUSE Products</a>
                </li>
                <li>
                    <a data-target="#" data-toggle="pill" href="#" onClick="SetupWizardRenderer.renderPage(3, makeAjaxCallback('setup-wizard-content', false));">Sync Schedule</a>
                </li>
                <li>
                    <a data-target="#" data-toggle="pill" href="#" onClick="SetupWizardRenderer.renderPage(4, makeAjaxCallback('setup-wizard-content', false));">System Groups</a>
                </li>
                <li>
                    <a data-target="#" data-toggle="pill" href="#" onClick="SetupWizardRenderer.renderPage(5, makeAjaxCallback('setup-wizard-content', false));">Activation Keys</a>
                </li>
                <li>
                    <a data-target="#" data-toggle="pill" href="#" onClick="SetupWizardRenderer.renderPage(6, makeAjaxCallback('setup-wizard-content', false));">Bootstrap</a>
                </li>
            </ul>
        </div>

        <!-- Wizard content will be loaded here -->
        <div id="setup-wizard-content"></div>
    </body>
</html>
