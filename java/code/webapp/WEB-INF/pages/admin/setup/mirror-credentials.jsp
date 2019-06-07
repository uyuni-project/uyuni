<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>
    <body>
        <script type="text/javascript" src="/rhn/dwr/interface/MirrorCredentialsRenderer.js?cb=${rhn:getConfig('web.version')}"></script>
        <script type="text/javascript" src="/javascript/susemanager-setup-wizard.js?cb=${rhn:getConfig('web.version')}"></script>
        <script type="text/javascript" src="/javascript/susemanager-setup-wizard-mirror-credentials.js?cb=${rhn:getConfig('web.version')}"></script>
        <!-- MODAL: Edit credentials -->
        <div class="modal fade" id="modal-edit-credentials">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                        <h4 class="modal-title"><bean:message key="mirror-credentials.jsp.modal-edit.title" /></h4>
                    </div>
                    <div class="modal-body">
                        <div class="alert alert-warning" role="alert" id="mirror-credentials-error-container" style="display:none">
                            <ul>
                                <li class="mirror-credentials-error" id="mirror-credentials-error-duplicate">
                                    <bean:message key="mirror-credentials.jsp.error.duplicate" />
                                </li>
                            </ul>
                        </div>
                        <form class="form-horizontal" role="form" id="add-credentials-form">
                            <div class="form-group">
                                <label for="modal-user" class="col-sm-2 control-label"><bean:message key="mirror-credentials.jsp.username" />:</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" autocomplete="off" id="edit-user" placeholder="Username" required>
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="modal-password" class="col-sm-2 control-label"><bean:message key="mirror-credentials.jsp.password" />:</label>
                                <div class="col-sm-10">
                                    <input type="password" class="form-control" autocomplete="off" id="edit-password" required>
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <span id="edit-credentials-spinner"></span>
                        <button type="button" class="btn btn-default" data-dismiss="modal">
                            <bean:message key="mirror-credentials.jsp.bt.cancel" />
                        </button>
                        <button type="button" class="btn btn-success" onClick="saveCredentials();">
                            <bean:message key="mirror-credentials.jsp.bt.save" />
                        </button>
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
                                <label class="col-sm-2 control-label"><bean:message key="mirror-credentials.jsp.username" />:</label>
                                <div class="col-sm-10">
                                    <p class="form-control-static" id="delete-user"></p>
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <span id="delete-credentials-spinner"></span>
                        <button type="button" class="btn btn-default" data-dismiss="modal">
                            <bean:message key="mirror-credentials.jsp.bt.cancel" />
                        </button>
                        <button type="button" class="btn btn-success" onClick="deleteCredentials();">
                            <bean:message key="mirror-credentials.jsp.bt.delete" />
                        </button>
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
                        <button type="button" class="btn btn-default" data-dismiss="modal">
                            <bean:message key="mirror-credentials.jsp.bt.close" />
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div class="responsive-wizard">
            <rhn:toolbar base="h1" icon="header-preferences" helpUrl="/docs/reference/admin/setup-wizard.html">
                <bean:message key="mirror-credentials.jsp.header" />
            </rhn:toolbar>
            <rhn:dialogmenu mindepth="0" maxdepth="1"
                        definition="/WEB-INF/nav/setup_wizard.xml"
                        renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />
            <div class="panel panel-default">
                <div class="panel-body">
                    <c:choose>
                        <c:when test='${issMaster}'>
                            <div class="row" id="mirror-credentials">
                                <div class="col-sm-9" id="listset-container">
                                    <rhn:icon type="spinner"></rhn:icon>
                                    <script>MirrorCredentialsRenderer.renderCredentials(makeRendererHandler("listset-container", false));</script>
                                </div>
                                <div class="col-sm-3 hidden-xs" id="wizard-faq">
                                    <h4><bean:message key="mirror-credentials.jsp.info.h1" /></h4>
                                    <p><bean:message key="mirror-credentials.jsp.info.p1" /></p>
                                    <img src="../../../img/setup-wizard/credentials-help.png" />
                                    <h4><bean:message key="mirror-credentials.jsp.info.h2" /></h4>
                                    <p><bean:message key="mirror-credentials.jsp.info.p2" /></p>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-warning" role="alert"><bean:message key="mirror-credentials.jsp.iss-slave"/></div>
                        </c:otherwise>
                    </c:choose>
                </div>
                <jsp:include page="/WEB-INF/pages/common/fragments/setup/setup-tab-footer.jspf" />
            </div>
        </div>
    </body>
</html>
