<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>
<head></head>
<body>
    <%@ include file="/WEB-INF/pages/common/fragments/channel/manage/manage_channel_header.jspf" %>

    <h2>
        <rhn:icon type="header-package"/>
        <bean:message key="repos.jsp.channel.repos"/>
    </h2>

    <rl:listset name="packageSet">
        <rhn:csrf/>
        <rhn:submitted/>
        <rhn:hidden name="cid" value="${cid}"/>

        <div class="spacewalk-section-toolbar">
            <div class="action-button-wrapper">
                <div class="btn-group">
                    <button class="btn btn-default" type="submit" name="dispatch"
                        value="<bean:message key='repos.jsp.update.channel'/>">
                        <bean:message key='repos.jsp.update.channel'/>
                    </button>
                    <a href="/rhn/channels/manage/repos/RepoCreate.do?cid=${cid}" class="btn btn-primary">
                        <rhn:icon type="item-add"/>
                        <bean:message key="repos.jsp.createRepo"/>
                    </a>
                </div>
            </div>
        </div>

        <rl:list
                decorator="SelectableDecorator"
                emptykey="repos.jsp.norepos"
                alphabarcolumn="label">

            <rl:decorator name="PageSizeDecorator"/>
            <rl:selectablecolumn value="${current.id}"/>

            <rl:column sortable="true"
                       bound="false"
                       headerkey="repos.jsp.channel.header"
                       sortattr="label"
                       defaultsort="asc">
                <a href="/rhn/channels/manage/repos/RepoEdit.do?id=${current.id}">
                    ${current.label}
                </a>
            </rl:column>
        </rl:list>

    </rl:listset>
</body>
</html>
