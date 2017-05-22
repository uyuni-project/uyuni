<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl"%>

<html>
<head>
    <script type="text/javascript" src="/rhn/dwr/interface/ProductsRenderer.js"></script>
    <script type="text/javascript" src="/rhn/dwr/interface/MgrSyncAJAX.js"></script>
    <script type="text/javascript" src="/rhn/dwr/engine.js"></script>
    <script type="text/javascript" src="/javascript/susemanager-scc-refresh-dialog.js"></script>
    <script type="text/javascript" src="/javascript/susemanager-setup-wizard.js"></script>
    <script type="text/javascript" src="/javascript/susemanager-setup-wizard-suse-products.js"></script>
</head>
<body>
    <div class="responsive-wizard">
        <rhn:toolbar base="h1" icon="header-preferences" helpUrl="/rhn/help/reference/en-US/ref.webui.admin.jsp#ref.webui.admin.wizard">
            <bean:message key="suse-products.jsp.header" />
        </rhn:toolbar>
        <rhn:dialogmenu mindepth="0" maxdepth="1" definition="/WEB-INF/nav/setup_wizard.xml"
            renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />

        <div class="panel panel-default" id="products-content" data-refresh-needed="${refreshNeeded}">
            <div class="panel-body">
                <c:choose>
                    <c:when test='${refreshRunning}'>
                        <div class="alert alert-warning" role="alert"><bean:message key="suse-products.jsp.refresh-running"/></div>
                    </c:when>
                    <c:when test='${issMaster}'>
                        <div class="row" id="suse-products">
                            <div class="col-sm-9">
                                <table class="table table-rounded">
                                    <thead>
                                        <tr>
                                            <th><input type="checkbox" class="select-all" autocomplete="off" /></th>
                                            <th><bean:message key="suse-products.jsp.th.available-products"/></th>
                                            <th><bean:message key="suse-products.jsp.th.arch"/></th>
                                            <th><bean:message key="suse-products.jsp.th.channels"/></th>
                                            <th><bean:message key="suse-products.jsp.th.status"/></th>
                                            <th></th>
                                        </tr>
                                    </thead>
                                    <tbody class="table-content">
                                        <tr id="loading-placeholder">
                                            <td colspan="6">
                                                <div class="spinner-container">
                                                    <rhn:icon type="spinner"></rhn:icon>
                                                    <span><bean:message key="suse-products.jsp.loading" /></span>
                                                </div>
                                            </td>
                                        </tr>
                                    </tbody>
                                    <tfoot>
                                        <tr>
                                            <td><input type="checkbox" class="select-all" autocomplete="off" /></td>
                                            <td colspan="6">
                                                <button class="btn btn-success" id="synchronize">
                                                    <i class="fa fa-plus"></i> <bean:message key='suse-products.jsp.add-products' />
                                                </button>
                                                <button class="btn btn-default"
                                                  id="refresh" data-toggle="tooltip" title="<bean:message key='suse-products.jsp.refresh.help' />">
                                                    <i class="fa fa-refresh"></i> <bean:message key='suse-products.jsp.refresh' />
                                                </button>
                                            </td>
                                        </tr>
                                    </tfoot>
                                </table>
                            </div>
                            <div class="col-sm-3 hidden-xs" id="wizard-faq">
                                <h4><bean:message key="suse-products.jsp.info.h1" /></h4>
                                <p><bean:message key="suse-products.jsp.info.p1" /></p>
                                <p><bean:message key="suse-products.jsp.info.p2" /></p>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="alert alert-warning" role="alert"><bean:message key="suse-products.jsp.iss-slave"/></div>
                    </c:otherwise>
                </c:choose>

                <jsp:include page="/WEB-INF/pages/common/fragments/admin/scc-refresh-dialog.jspf"/>
                <div class="hidden" id="iss-master" data-iss-master="${issMaster}"></div>
                <div class="hidden" id="refresh-running" data-refresh-running="${refreshRunning}"></div>
                <div class="hidden" id="sccconfig.jsp.refresh"><bean:message key="sccconfig.jsp.refresh"/></div>
            </div>
            <jsp:include page="/WEB-INF/pages/common/fragments/setup/setup-tab-footer.jspf" />
        </div>
    </div>
</body>
</html>
