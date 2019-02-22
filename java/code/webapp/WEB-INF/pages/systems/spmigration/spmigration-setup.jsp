<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>

<html:html>

<head>
<link rel="stylesheet" type="text/css" href="/css/susemanager-sp-migration.css?cb=${rhn:getConfig('web.version')}" />
<script src="/javascript/susemanager-sp-migration.js?cb=${rhn:getConfig('web.version')}"></script>
</head>

<body>
    <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf"%>

    <rhn:toolbar base="h2" icon="header-channel">
        <bean:message key="spmigration.jsp.setup.title" />
    </rhn:toolbar>

    <c:if test="${not empty targetProducts.missingChannels}">
        <div class="alert alert-warning">
            <bean:message key="spmigration.jsp.error.missing-channels" />
        </div>
    </c:if>
    <c:if test="${updateStackUpdateNeeded}">
        <div class="alert alert-warning">
            <bean:message key="spmigration.jsp.error.updatestack-update-needed" />
        </div>
    </c:if>
    <html:form method="post" styleId="migrationForm"
        action="/systems/details/SPMigration.do?sid=${system.id}">
        <div class="form-horizontal">
            <div class="form-group">
                <label class="col-sm-2 control-label">
                    <bean:message key="spmigration.jsp.setup.installed-products" />
                </label>
                <div class="col-sm-10">
                    <ul class="form-control-static products-list">
                        <li>
                            <strong><c:out value="${system.installedProductSet.baseProduct.friendlyName}" /></strong>
                            <ul>
                                <c:forEach
                                    items="${system.installedProductSet.addonProducts}"
                                    var="addonProduct">
                                    <li class="addon-product">
                                        <c:out value="${addonProduct.friendlyName}" />
                                    </li>
                                </c:forEach>
                            </ul>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-2 control-label"> <bean:message
                        key="spmigration.jsp.setup.target-products" />
                </label>
                <div class="col-sm-10">
                    <ul class="form-control-static products-list">
                        <li>
                            <strong><c:out value="${targetProducts.baseProduct.friendlyName}" /></strong>
                            <ul>
                                <c:forEach items="${targetProducts.addonProducts}"
                                    var="addonProduct">
                                    <li class="addon-product" id="${addonProduct.id}">
                                        <c:out value="${addonProduct.friendlyName}" />
                                    </li>
                                </c:forEach>
                            </ul>
                        </li>
                    </ul>
                </div>
            </div>
            <c:if test="${empty targetProducts.missingChannels}">
                <div class="form-group">
                    <label class="col-sm-2 control-label">
                        <bean:message key="spmigration.jsp.setup.base-channel" />
                    </label>
                    <div class="col-sm-10 col-lg-4">
                        <select class="form-control" name="baseChannel"
                            id="base-channel-select">
                            <c:forEach items="${channelMap}" var="alternative">
                                <option value="${alternative.key.id}"
                                    title="${alternative.key.name}">
                                    <c:out value="${alternative.key.name}" />
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </c:if>
            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-10">
                    <c:if test="${empty targetProducts.missingChannels}">
                        <%@ include file="/WEB-INF/pages/systems/spmigration/channel-details.jspf"%>
                    </c:if>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-10">
                    <button type="submit" name="dispatch"
                        class="btn btn-success" id="submitButton"
                        <c:if test="${not empty targetProducts.missingChannels}"> disabled</c:if>
                    >
                        <bean:message key="spmigration.jsp.setup.submit" />
                    </button>
                </div>
            </div>
            <html:hidden property="baseProduct"
                value="${targetProducts.baseProduct.id}" />
            <c:forEach items="${targetProducts.addonProducts}" var="current">
                <html:hidden property="addonProducts" value="${current.id}" />
            </c:forEach>
            <html:hidden property="step" value="setup" />
            <html:hidden property="submitted" value="true" />
            <rhn:csrf />
        </div>
    </html:form>
</body>
</html:html>
