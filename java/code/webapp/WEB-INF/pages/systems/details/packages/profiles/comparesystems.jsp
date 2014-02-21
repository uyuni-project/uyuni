<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>


<html>

<body>
<%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>

<h2><rhn:icon type="header-package" />
    <bean:message key="compare.jsp.compareto" arg0="${fn:escapeXml(requestScope.systemname)}" />
</h2>

<div class="page-summary">
<bean:message key="systemcompare.jsp.pagesummary" />
</div>

    <rl:listset name="compareListSet">
        <rhn:csrf />

	    <rl:list dataset="pageList"
            width="100%"
            name="compareList"
            emptykey="compare.jsp.nodifferences">

            <rl:decorator name="SelectableDecorator"/>
            <rl:selectablecolumn value="${current.selectionKey}"
	 			selected="${current.selected}"
	 			disabled="${not current.selectable}"/>

            <rl:column headerkey="compare.jsp.package" bound="false" filterattr="name">
		<c:out value="${current.name}" escapeXml="true" />
            </rl:column>

            <rl:column headerkey="packagelist.jsp.packagearch" bound="false">
                ${current.arch}
            </rl:column>

            <rl:column headerkey="compare.jsp.thissystem" bound="false">
                ${current.system.evr}
            </rl:column>

            <rl:column headertext="${fn:escapeXml(requestScope.systemname)}" bound="false">
                ${current.other.evr}
            </rl:column>

            <rl:column headerkey="compare.jsp.difference" bound="false">
		<c:out value="${current.comparison}" escapeXml="true" />
            </rl:column>
        </rl:list>

        <c:if test="${not empty requestScope.pageList}">
            <rhn:require acl="system_feature(ftr_delta_action)"
                mixins="com.redhat.rhn.common.security.acl.SystemAclHandler">
                <div class="text-right">
                    <rhn:submitted/>
                    <hr />
                    <html:submit styleClass="btn btn-default" property="dispatch">
                        <bean:message key="compare.jsp.syncpackageto" arg0="${fn:escapeXml(requestScope.systemname)}"/>
                    </html:submit>
                </div>
            </rhn:require>
            <rhn:require acl="not system_feature(ftr_delta_action)"
                mixins="com.redhat.rhn.common.security.acl.SystemAclHandler">
                <div align="left">
                    <hr />
                    <strong><bean:message key="compare.jsp.noprovisioning"
                        arg0="${fn:escapeXml(system.name)}" arg1="${param.sid}"/></strong>
                </div>
            </rhn:require>
        </c:if>

        <html:hidden property="sid" value="${param.sid}" />
        <html:hidden property="sid_1" value="${param.sid_1}" />

    </rl:listset>

</body>
</html>
