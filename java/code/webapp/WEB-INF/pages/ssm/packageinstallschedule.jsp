<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>


<html>
<body>
<%@ include file="/WEB-INF/pages/common/fragments/ssm/header.jspf" %>
<h2>
    <bean:message key="installconfirm.jsp.header"/>
</h2>

<div class="page-summary">
    <c:if test="${requestScope.numSystems != '1'}">
        <p><bean:message key="ssm.package.install.schedule.summary.plural" arg0="${requestScope.numSystems}"/></p>
    </c:if>
    <c:if test="${requestScope.numSystems == '1'}">
        <p><bean:message key="ssm.package.install.schedule.summary.single" arg0="${requestScope.numSystems}"/></p>
    </c:if>
</div>

<rl:listset name="groupSet">
    <rhn:csrf />
    <rhn:submitted />

    <div class="spacewalk-section-toolbar">
        <div class="action-button-wrapper">
            <input type="submit"
                   class="btn btn-primary" name="dispatch"
                   value='<bean:message key="installconfirm.jsp.confirm"/>'/>
        </div>
    </div>
    <div class="form-horizontal">
        <jsp:include page="/WEB-INF/pages/common/fragments/schedule-options.jspf"/>
    </div>

    <rl:list dataset="pageList"
             width="100%"
             name="groupList"
             styleclass="list "
             emptykey="systemlist.jsp.nosystems">

        <rl:column headerkey="actions.jsp.system" bound="false"
                   sortattr="name" sortable="true">
            <c:out value="${current.name}" escapeXml="true" />
        </rl:column>

    </rl:list>

    <rhn:hidden name="packagesDecl" value="${requestScope.packagesDecl}" />
    <rhn:hidden name="cid" value="${param.cid}" />
    <rhn:hidden name="mode" value="${param.mode}" />

</rl:listset>

</body>
</html>
