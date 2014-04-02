<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl"%>

<html>
<head>
    <script type="text/javascript" src="/rhn/dwr/interface/ProductsRenderer.js"></script>
    <script type="text/javascript" src="/rhn/dwr/engine.js"></script>
    <script type="text/javascript" src="/javascript/responsive-tab.js"></script>
    <script type="text/javascript" src="/javascript/susemanager-setup-wizard.js"></script>
    <script type="text/javascript">
        $(function(){
          ProductsRenderer.renderAsync(makeAjaxCallback("table-content", false));
        });
    </script>
</head>
<body>
    <div class="responsive-wizard">
        <rhn:toolbar base="h1" icon="header-preferences">
                Setup Wizard
            </rhn:toolbar>
        <rhn:dialogmenu mindepth="0" maxdepth="1" definition="/WEB-INF/nav/setup_wizard.xml"
            renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />

        <div class="panel panel-default" id="products-content">
            <div class="panel-body">
                <div class="row" id="suse-products">
                    <div class="col-sm-9">
                        <table class="table table-rounded">
                            <thead>
                                <tr>
                                    <th>Available Products below</th>
                                    <th>Architecture</th>
                                </tr>
                            </thead>
                            <tbody id="table-content">
                                <tr>
                                    <td colspan=2>
                                        <rhn:icon type="spinner"></rhn:icon>
                                        <span>Loading...</span>
                                    </td>
                                </tr>
                            </tbody>
                            <tfoot>
                                <tr>
                                    <td colspan="3">
                                        <button class="btn btn-success">
                                            <i class="fa fa-refresh"></i> Synchronize
                                        </button>
                                    </td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                    <div class="col-sm-3 hidden-xs" id="wizard-faq">
                        <h4>Why aren't all SUSE products displayed in the list?</h4>
                        <p>The products displayed on this list are directly linked to
                            trigger your Mirror Credentials as well as your SUSE
                            subscriptions.</p>
                        <p>If you believe there are products missing, make sure you have
                            added the correct Mirror Credential in the synchronization step
                            before</p>
                    </div>
                </div>
            </div>
            <div class="panel-footer">
                <div class="row">
                    <div class="col-sm-3 hidden-xs">1 of 3</div>
                        <div class="col-sm-6 text-center">
                            <a class="btn btn-default" href="/rhn/admin/setup/MirrorCredentials.do"><i class="fa fa-arrow-left"></i><bean:message key="setup-wizard.prev"/></a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
