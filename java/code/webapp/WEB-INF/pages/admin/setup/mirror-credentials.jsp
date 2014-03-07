<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>
    <head>
        <script type="text/javascript" src="/rhn/dwr/interface/MirrorCredentialsRenderer.js"></script>
        <script type="text/javascript" src="/rhn/dwr/engine.js"></script>
        <script type="text/javascript" src="/javascript/susemanager-setup-wizard.js"></script>
    </head>
    <body>
        <!-- MODAL: Edit credentials -->
        <div class="modal fade" id="modal-edit-credentials">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                        <h4 class="modal-title"><bean:message key="mirror-credentials.jsp.modal-edit.title" /></h4>
                    </div>
                    <div class="modal-body">
                        <form class="form-horizontal" role="form">
                            <input type="hidden" id="modal-id">
                            <div class="form-group">
                                <label for="modal-email" class="col-sm-2 control-label"><bean:message key="mirror-credentials.jsp.email" />:</label>
                                <div class="col-sm-10">
                                    <input type="email" class="form-control" id="modal-email" placeholder="Email">
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="modal-user" class="col-sm-2 control-label"><bean:message key="mirror-credentials.jsp.username" />:</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="modal-user" placeholder="Username">
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="modal-password" class="col-sm-2 control-label"><bean:message key="mirror-credentials.jsp.password" />:</label>
                                <div class="col-sm-10">
                                    <input type="password" class="form-control" id="modal-password" placeholder="&bull;&bull;&bull;&bull;&bull;&bull;">
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-success" onClick="saveCredentials();">Save</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- MODAL: Delete credentials -->
        <div class="modal fade" id="modal-delete-credentials">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                        <h4 class="modal-title"><bean:message key="mirror-credentials.jsp.modal-delete.title" /></h4>
                    </div>
                    <div class="modal-body">
                        <form class="form-horizontal" role="form">
                            <input type="hidden" id="delete-id">
                            <div class="form-group">
                                <label class="col-sm-2 control-label"><bean:message key="mirror-credentials.jsp.email" />:</label>
                                <div class="col-sm-10">
                                    <p class="form-control-static" id="delete-email"></p>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-2 control-label"><bean:message key="mirror-credentials.jsp.username" />:</label>
                                <div class="col-sm-10">
                                    <p class="form-control-static" id="delete-user"></p>
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-success" onClick="deleteCredentials();">Delete</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- MODAL: List subscriptions -->
        <div class="modal fade" id="modal-list-subscriptions">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                        <h4 class="modal-title"><bean:message key="mirror-credentials.jsp.modal-subscriptions.title" /></h4>
                    </div>
                    <div id="modal-list-subscriptions-body" class="modal-body">
                        <!-- Content will be rendered here -->
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>

        <rhn:toolbar base="h1" icon="header-preferences">
            <bean:message key="Setup Wizard" />
        </rhn:toolbar>
        <rhn:dialogmenu mindepth="0" maxdepth="1"
                        definition="/WEB-INF/nav/setup_wizard.xml"
                        renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />
        <p>
            Please configure and test your mirror credentials below.
            <button type="button" class="btn btn-default pull-right" data-toggle="modal" data-target="#edit-credentials-modal">
                <rhn:icon type="item-add"></rhn:icon>Add
            </button>
        </p>
        <div id="listset-container">
            <rhn:icon type="spinner"></rhn:icon><span>Loading ...</span>
            <script>
                MirrorCredentialsRenderer.renderCredentials(makeAjaxCallback("listset-container", false));
            </script>
        </div>
        <div class="pull-right">
            <hr />
            <a class="btn btn-default" href="/rhn/admin/setup/HttpProxy.do">
                <bean:message key="setup-wizard.prev" />
            </a>
            <a class="btn btn-default" href="/rhn/admin/setup/SUSEProducts.do">
                <bean:message key="setup-wizard.next" />
            </a>
        </div>
    </body>
</html>
