<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<html:xhtml/>
<html>

<body>

<%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>

<rhn:require acl="authorized_for(systems.software.packages, R); not authorized_for(systems.software.packages, W); not system_feature(ftr_package_remove)">
        <h2>
                <rhn:icon type="header-package-del" title="errata.common.deletepackageAlt" />
                <bean:message key="packagelist.jsp.installedpackages" />
        </h2>
        <div class="page-summary">
                <p>
                <bean:message key="packagelist.jsp.installedpagesummary" />
                </p>
        </div>
</rhn:require>
<rhn:require acl="authorized_for(systems.software.packages, W); system_feature(ftr_package_remove)">
        <h2>
                <rhn:icon type="header-package-del" title="errata.common.deletepackageAlt" />
                <bean:message key="packagelist.jsp.removablepackages" />
        </h2>
        <div class="page-summary">
                <p>
                <bean:message key="packagelist.jsp.removepagesummary" />
                </p>
        </div>
</rhn:require>

<c:set var="pageList" value="${requestScope.all}" />

<rl:listset name="packageListSet">
    <rhn:csrf />
    <rhn:submitted />

    <c:if test="${not empty requestScope.all}">
        <div class="spacewalk-section-toolbar">
            <div class="action-button-wrapper">
                <rhn:require acl="authorized_for(systems.software.packages, W); system_feature(ftr_package_remove)">
                    <input type="submit" class="btn btn-danger" name ="dispatch" value='<bean:message key="packagelist.jsp.removepackages"/>'/>
                </rhn:require>
            </div>
        </div>
    </c:if>

    <rl:list dataset="pageList" width="100%" name="packageList" emptykey="packagelist.jsp.nopackages" alphabarcolumn="nvre">
        <rl:decorator name="PageSizeDecorator"/>
        <rl:decorator name="SelectableDecorator"/>
        <rl:selectablecolumn value="${current.selectionKey}" selected="${current.selected}" disabled="${not current.selectable}"/>

        <rl:column headerkey="packagelist.jsp.packagename" bound="false" sortattr="nvre" sortable="true" filterattr="nvre" defaultsort="asc">
            <c:if test="${current.retracted}">
                <rhn:icon type="errata-retracted" title="errata.jsp.retracted-package-tooltip" />
            </c:if>
            <c:choose>
                <c:when test="${not empty current.packageId}">
                    <a href="/rhn/software/packages/Details.do?pid=${current.packageId}">${current.nvre}</a>
                </c:when>
                <c:otherwise>
                    <c:out value="${current.nvre}"/>
                </c:otherwise>
            </c:choose>
            <c:if test="${current.appstream != null}">
                <span class="label label-info" title="AppStream module: ${current.appstream}">
                    <c:out value="${current.appstream}"/>
                </span>
                &nbsp;
            </c:if>
        </rl:column>
        <rl:column headerkey="packagelist.jsp.packagesummary" bound="false" sortable="true" sortattr="summary">
            <c:choose>
                <c:when test="${not empty current.summary}">${current.summary}</c:when>
            </c:choose>
        </rl:column>
        <rl:column headerkey="packagelist.jsp.packagearch" bound="false">
            <c:choose>
                <c:when test ="${not empty current.arch}">${current.arch}</c:when>
                <c:otherwise><bean:message key="packagelist.jsp.notspecified"/></c:otherwise>
            </c:choose>
        </rl:column>
        <rl:column headerkey="packagelist.jsp.installtime" bound="false" sortattr="installTimeObj" sortable="true">
            <c:choose>
                <c:when test ="${not empty current.installTime}">
                    <rhn:formatDate humanStyle="calendar" value="${current.installTimeObj}" type="both" dateStyle="short" timeStyle="long"/>
                </c:when>
                <c:otherwise><bean:message key="packagelist.jsp.notspecified"/></c:otherwise>
            </c:choose>
        </rl:column>
    </rl:list>
</rl:listset>
</body>
</html>
