<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>


<html>

<body>
<%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>

<h2>
    <bean:message key="schedulesync.jsp.profilesync" />
</h2>

<div class="page-summary">
    <bean:message key="schedulesync.jsp.pagesummary"
        arg0="${requestScope.system.name}"
        arg1="${requestScope.profilename}" />
</div>

<rl:listset name="compareListSet">
    <rhn:csrf />
    <rl:list width="100%"
        name="compareList"
        styleclass="list"
        emptykey="schedulesync.jsp.nopackagesselected">

        <rl:column headerkey="schedulesync.jsp.package" bound="false">
            ${current.name}
        </rl:column>

        <rl:column headerkey="packagelist.jsp.packagearch" bound="false">
            ${current.arch}
        </rl:column>

        <rl:column headerkey="schedulesync.jsp.action" bound="false">
            ${current.actionStatus}
        </rl:column>
    </rl:list>

    <p><bean:message key="schedulesync.jsp.disclaimer" /></p>
    <table class="schedule-action-interface" align="center">
        <tr>
            <td><input type="radio" name="use_date" value="false" checked="checked" /></td>
            <th><bean:message key="confirm.jsp.now"/></th>
        </tr>
        <tr>
            <td><input type="radio" name="use_date" value="true"/></td>
            <th><bean:message key="confirm.jsp.than"/></th>
        </tr>
        <tr>
            <th><rhn:icon type="header-schedule" title="confirm.jsp.selection" />
            </th>
            <td>
                <jsp:include page="/WEB-INF/pages/common/fragments/date-picker.jsp">
                <jsp:param name="widget" value="date"/>
                </jsp:include>
            </td>
        </tr>
    </table>

    <html:hidden property="sid" value="${param.sid}" />
    <html:hidden property="prid" value="${param.prid}" />
    <html:hidden property="set_label" value="packages_for_system_sync" />

    <rhn:require acl="system_feature(ftr_delta_action)"
        mixins="com.redhat.rhn.common.security.acl.SystemAclHandler">
        <rhn:submitted/>
        <div class="text-right">
            <hr />
            <html:submit property="dispatch">
                <bean:message key="schedulesync.jsp.schedulesync"/>
            </html:submit>
        </div>
    </rhn:require>
</rl:listset>
</body>
</html>
