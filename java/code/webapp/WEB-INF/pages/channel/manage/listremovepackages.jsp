<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>
<head></head>
<body>

    <%@ include file="/WEB-INF/pages/common/fragments/channel/manage/manage_channel_header.jspf" %>
    <h2>
        <rhn:icon type="header-package"/>
        <bean:message key="channel.jsp.package.list"/>
    </h2>

    <rl:listset name="packageSet" legend="system-group">
        <rhn:csrf/>
        <rhn:submitted/>
        <rhn:hidden name="cid" value="${cid}"/>

        <div class="spacewalk-section-toolbar">
            <div class="action-button-wrapper">
                <input type="submit" name="confirm" class="btn btn-danger"
                       value="<bean:message key='channel.jsp.package.removebutton'/>"/>
            </div>
        </div>

        <rl:list dataset="pageList"
                 name="packageList"
                 decorator="SelectableDecorator"
                 emptykey="package.jsp.emptylist"
                 alphabarcolumn="nvrea"
                 filter="com.redhat.rhn.frontend.taglibs.list.filters.PackageFilter">

            <rl:decorator name="PageSizeDecorator"/>

            <rl:selectablecolumn value="${current.id}"
                                 selected="${current.selected}"
                                 disabled="${not current.selectable}"/>

            <rl:column sortable="true"
                       bound="false"
                       headerkey="download.jsp.package"
                       sortattr="nvrea"
                       defaultsort="asc">
                <a href="/rhn/software/packages/Details.do?pid=${current.id}">
                    ${current.nvrea}
                </a>
                <c:if test="${current.retracted}">
                    <rhn:icon type="errata-retracted" title="errata.jsp.retracted-package-tooltip" />
                </c:if>
            </rl:column>

            <rl:column sortable="false"
                       bound="false"
                       headerkey="packagesearch.jsp.summary">
                ${current.summary}
            </rl:column>

            <rl:column sortable="false"
                       bound="false"
                       headerkey="package.jsp.provider">
                ${current.provider}
            </rl:column>
        </rl:list>

        <rl:csv dataset="pageList" name="packageList" exportColumns="id, nvrea, provider"/>

    </rl:listset>

</body>
</html>
