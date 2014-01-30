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
          function addCredentials() {
            // Read values
            var email = $('#new-creds-email').val();
            var user = $('#new-creds-user').val();
            var password = $('#new-creds-password').val();
            // Empty input fields
            $('#new-creds-email').val("");
            $('#new-creds-user').val("");
            $('#new-creds-password').val("");
            SubscriptionsRenderer.addCredentials(email, user, password,
                makeAjaxCallback("listset-container", false));
          }
          function downloadSubscriptions(id) {
            $("#subscriptions-" + id).html("<i class='fa fa-spinner fa-spin'></i>");
            SubscriptionsRenderer.renderSubscriptions(id,
                makeAjaxCallback("subscriptions-" + id, false));
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
            Please configure your mirror credentials below.
        </p>
        <form class="form-inline">
            <div class="form-group">
                <input type="text" class="form-control" id="new-creds-email" placeholder="Email" />
            </div>
            <div class="form-group">
                <input type="text" class="form-control" id="new-creds-user" placeholder="Username" />
            </div>
            <div class="form-group">
                <input type="password" class="form-control" id="new-creds-password" placeholder="Password" />
            </div>
            <button class="btn btn-default" type="button" onClick="addCredentials();">Add</button>
        </form>
        <div id="listset-container">
            <i class='fa fa-spinner fa-spin'></i><span>Loading ...</span>
            <script>
                SubscriptionsRenderer.renderCredentials(makeAjaxCallback("listset-container", false));
            </script>
        </div>
        <div class="pull-right">
            <hr />
            <a class="btn btn-success" href="/rhn/admin/setup/SUSEProducts.do">
                <bean:message key="mirror-credentials.jsp.dispatch" />
            </a>
        </div>
    </body>
</html>
