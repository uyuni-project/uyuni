<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>
    <head>
        <script type="text/javascript" src="/rhn/dwr/interface/SubscriptionsRenderer.js"></script>
        <script type="text/javascript" src="/rhn/dwr/engine.js"></script>
        <script type="text/javascript">
            function verifyCreds(id) {
                $("#verify-" + id).html("<i class='fa fa-spinner fa-spin'></i>");
                SubscriptionsRenderer.renderAsync(id, makeAjaxCallback("verify-" + id, false));
            }
        </script>
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
                <rl:column headerkey="mirror-credentials.jsp.email"
                           bound="true"
                           attr="email" />
                <rl:column headerkey="mirror-credentials.jsp.actions"
                           bound="false">
                    <span id="verify-${current.id}">
                        <a href="javascript:void(0);" onClick="verifyCreds('${current.id}');">
                            <rhn:icon type="item-cloud-download" title="mirror-credentials.jsp.download" />
                        </a>
                    </span>
                    <a>
                        <rhn:icon type="item-edit" title="mirror-credentials.jsp.edit" />
                    </a>
                    <a>
                        <rhn:icon type="item-del" title="mirror-credentials.jsp.delete" />
                    </a>
                </rl:column>
            </rl:list>

            <div class="pull-right">
                <hr />
                <a class="btn btn-success" href="/rhn/admin/setup/SUSEProducts.do">
                    <bean:message key="mirror-credentials.jsp.dispatch" />
                </a>
            </div>
        </rl:listset>
    </body>
</html>
