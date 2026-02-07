<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>
<head></head>
<body>
    <%@ include file="/WEB-INF/pages/common/fragments/channel/manage/manage_channel_header.jspf" %>

    <rl:listset name="packageSet">
        <rhn:csrf/>
        <rhn:submitted/>
        <rhn:hidden name="cid" value="${cid}"/>
        <rhn:hidden name="other_id" value="${other_id}"/>
        <rhn:hidden name="sync_type" value="${sync_type}"/>
        <p>
            <bean:message key="channel.jsp.package.comparemergemessage1" arg0="<strong>${channel_name}</strong>"/>
        </p>
        <p>
            <bean:message key="channel.jsp.package.comparemergemessage2"/>
        </p>
        <h2>
            <rhn:icon type="header-channel"/>
            <bean:message key="channel.jsp.package.syncchannels"/>
        </h2>

        <div class="spacewalk-section-toolbar">
            <div class="action-button-wrapper">
                <html:submit property="confirm" styleClass="btn btn-primary" disabled="${empty pageList}">
                    <bean:message key="channel.jsp.package.mergeconfirmbutton"/>
                </html:submit>
            </div>
        </div>

        <rl:list dataset="pageList" name="packageList"
                 emptykey="channel.jsp.package.addemptylist" alphabarcolumn="nvrea"
                 filter="com.redhat.rhn.frontend.taglibs.list.filters.PackageFilter">
            <rl:decorator name="ElaborationDecorator"/>
            <rl:decorator name="PageSizeDecorator"/>

            <rl:column sortable="true" bound="false" headerkey="download.jsp.package"
                       sortattr="nvrea" defaultsort="asc">
                <a href="/rhn/software/packages/Details.do?pid=${current.id}">
                    ${current.nvrea}
                </a>
            </rl:column>

            <rl:column sortable="false" bound="false" headerkey="channel.jsp.package.action" sortattr="action">
                <c:choose>
                    <c:when test="${current.action == 1}">
                        <bean:message key="channel.jsp.package.actionadd"/>
                    </c:when>
                    <c:when test="${current.action == -1}">
                        <bean:message key="channel.jsp.package.actionremove"/>
                    </c:when>
                </c:choose>
            </rl:column>
        </rl:list>

    </rl:listset>
</body>
</html>
