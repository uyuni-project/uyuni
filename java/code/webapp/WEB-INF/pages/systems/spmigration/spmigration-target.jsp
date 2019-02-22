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
        <bean:message key="spmigration.jsp.target.title" />
    </rhn:toolbar>

    <c:choose>
        <c:when test="${not empty migrationScheduled}">
            <div class="alert alert-info">
                <bean:message key="spmigration.jsp.error.scheduled" arg0="${system.id}"
                    arg1="${migrationScheduled.id}" />
            </div>
        </c:when>
        <c:when test="${not empty latestServicePack}">
            <div class="alert alert-warning">
                <bean:message key="spmigration.jsp.error.up-to-date" />
            </div>
        </c:when>
        <c:when test="${not isMinion and zyppPluginInstalled and not upgradeSupported}">
            <div class="alert alert-warning">
                <bean:message key="spmigration.jsp.error.update-zypp-plugin" />
            </div>
        </c:when>
        <c:when test="${(isMinion and not isSUSEMinion)
                or (not isMinion and not zyppPluginInstalled)
                or targetProducts == null}">
            <div class="alert alert-warning">
                <bean:message key="spmigration.jsp.error.unsupported" />
            </div>
        </c:when>
        <c:when test="${isMinion and not isSaltUpToDate}">
            <div class="alert alert-warning">
                <bean:message key="spmigration.jsp.error.update-salt-package-needed" />
            </div>
        </c:when>
        <c:otherwise>

            <c:if test="${targetProductSelectedEmpty}">
                <div class="alert alert-warning">
                    <bean:message key="spmigration.jsp.error.targetProductSelectedEmpty" />
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
                                <c:set var="itemCounter" scope="page" value="0" />
                                <c:forEach items="${targetProducts}" var="target">
                                    <li <c:if test="${!target.isEveryChannelSynced}">
                                                title="<bean:message key="spmigration.jsp.target.notSyncedChannels" />
                                                    <c:out value="${target.missingChannelsMessage}" />"</c:if>>
                                        <input type="radio" name="targetProductSelected"
                                            id="target${target.serializedProductIDs}"
                                            value="${target.serializedProductIDs}"
                                                ${itemCounter == 0 && target.isEveryChannelSynced ? "checked" : "" }
                                                ${!target.isEveryChannelSynced ? "disabled" : ""} />
                                        <label for="target${target.serializedProductIDs}">
                                            <strong><c:out value="${target.baseProduct.friendlyName}" /></strong>
                                            <ul>
                                                <c:forEach items="${target.addonProducts}"
                                                    var="addonProduct">
                                                    <li class="addon-product" id="${addonProduct.id}">
                                                        <c:out value="${addonProduct.friendlyName}" />
                                                    </li>
                                                </c:forEach>
                                            </ul>
                                        </label>
                                    </li>
                                    <c:set var="itemCounter" scope="page" value="${targetEnabled ? itemCounter+1 : itemCounter}"/>
                                </c:forEach>
                            </ul>
                        </div>
                    </div>
                </div>

                <div class="form-horizontal">
                    <div class="form-group">
                        <div class="col-sm-offset-2 col-sm-10">
                            <button type="submit" name="dispatch"
                                class="btn btn-success" id="submitButton">
                                <bean:message key="spmigration.jsp.target.submit" />
                            </button>
                        </div>
                    </div>
                    <html:hidden property="step" value="target" />
                    <html:hidden property="submitted" value="true" />
                    <rhn:csrf />
                </div>
            </html:form>
        </c:otherwise>
    </c:choose>
</body>
</html:html>
