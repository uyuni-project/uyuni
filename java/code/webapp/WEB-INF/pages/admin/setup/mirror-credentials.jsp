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
        <script type="text/javascript" src="/javascript/responsive-tab.js"></script>
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
                            <div class="form-group">
                                <label for="modal-email" class="col-sm-2 control-label"><bean:message key="mirror-credentials.jsp.email" />:</label>
                                <div class="col-sm-10">
                                    <input type="email" class="form-control" id="edit-email" placeholder="Email">
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="modal-user" class="col-sm-2 control-label"><bean:message key="mirror-credentials.jsp.username" />:</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="edit-user" placeholder="Username">
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="modal-password" class="col-sm-2 control-label"><bean:message key="mirror-credentials.jsp.password" />:</label>
                                <div class="col-sm-10">
                                    <input type="password" class="form-control" id="edit-password" placeholder="&bull;&bull;&bull;&bull;&bull;&bull;">
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <span id="edit-credentials-spinner"></span>
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
                        <span id="delete-credentials-spinner"></span>
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

        <div class="responsive-wizard">
            <rhn:toolbar base="h1" icon="header-preferences">
                <bean:message key="Setup Wizard" />
            </rhn:toolbar>
            <rhn:dialogmenu mindepth="0" maxdepth="1"
                        definition="/WEB-INF/nav/setup_wizard.xml"
                        renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />
            <div class="panel panel-default">
                <div class="panel-body">
                    <div class="row" id="mirror-credentials">
                        <div class="col-sm-9" id="listset-container">
                            <rhn:icon type="spinner"></rhn:icon>
                            <script>MirrorCredentialsRenderer.renderCredentials(makeAjaxCallback("listset-container", false));</script>
                        </div>
                        <div class="col-sm-3 hidden-xs" id="wizard-faq">
                            <h4>What are Mirror Credentials</h4>
                            <p>Mirror credentials are your access to SUSE product downloads.</p>
                            <img src="../../../img/setup-wizard/credentials-help.png" />
                            <h4>Where do I find my Mirror Credentials?</h4>
                            <p>You can find and create them in your NCC admin panel. <br/> For more information <a href="https://www.suse.com/documentation/sles10/sle10_sp2_smt_guide/data/smt_mirroring_getcredentials.html" target="_blank">click here</a>.</p>
                        </div>
                    </div>
                </div>
                <div class="panel-footer">
                    <div class="row">
                        <div class="col-sm-3 hidden-xs">
                            2 of 2
                        </div>
                        <div class="col-sm-6 text-center">
                            <a class="btn btn-default" href="/rhn/admin/setup/HttpProxy.do"><i class="fa fa-arrow-left"></i><bean:message key="setup-wizard.prev" /></a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
