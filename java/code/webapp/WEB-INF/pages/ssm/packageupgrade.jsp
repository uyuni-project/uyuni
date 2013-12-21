<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>


<html>
<body>
<%@ include file="/WEB-INF/pages/common/fragments/ssm/header.jspf" %>
<h2>
    <bean:message key="ssm.package.upgrade.select.header"/>
</h2>

<div class="page-summary">
    <p><bean:message key="ssm.package.upgrade.select.summary"/></p>
</div>

<rl:listset name="groupSet" legend="system-group">
    <rhn:csrf />
    <rl:list dataset="pageList"
             width="100%"
             styleclass="list"
             emptykey="packagelist.jsp.nopackages"
             alphabarcolumn="name">

        <rl:decorator name="PageSizeDecorator"/>
        <rl:decorator name="SelectableDecorator"/>

        <rl:selectablecolumn value="${current.selectionKey}"
                             selected="${current.selected}"
                             disabled="${not current.selectable}"/>

        <rl:column headerkey="packagelist.jsp.packagename" bound="false"
                   sortattr="nvre" sortable="true" filterattr="name">
            <c:out value="${current.name}" escapeXml="false"/>-<c:out value="${current.version}" escapeXml="false"/>-<c:out value="${current.release}" escapeXml="false"/>
        </rl:column>

        <rl:column headerkey="packagelist.jsp.packagearch" bound="false"
                   styleclass="thin-column">
            <c:choose>
                <c:when test="${not empty current.arch}">${current.arch}</c:when>
                <c:otherwise><bean:message
                        key="packagelist.jsp.notspecified"/></c:otherwise>
            </c:choose>
        </rl:column>

        <rl:column headerkey="ssm.package.upgrade.select.numsystems" bound="false"
                   styleclass="thin-column">
            <c:out value="${current.numSystems}"/>
        </rl:column>

        <rl:column headerkey="ssm.package.upgrade.select.advisory" bound="false">
            <c:if test="${not empty current.advisory}">
              <c:if test="${current.advisoryType == 'Security Advisory'}">
                <rhn:icon type="errata-security" title="erratalist.jsp.securityadvisory" />
              </c:if>
              <c:if test="${current.advisoryType == 'Bug Fix Advisory'}">
                <rhn:icon type="errata-bugfix" title="erratalist.jsp.bugadvisory" />
              </c:if>
              <c:if test="${current.advisoryType == 'Product Enhancement Advisory'}">
                <rhn:icon type="errata-enhance" title="erratalist.jsp.productenhancementadvisory" />
              </c:if>
              <a href="/rhn/errata/details/Details.do?eid=${current.advisoryId}">${current.advisory}</a><br/>
            </c:if>
        </rl:column>

    </rl:list>

    <div class="text-right">
        <rhn:submitted/>
        <hr/>
        <input type="submit"
               name="dispatch"
               value='<bean:message key="ssm.package.upgrade.select.confirm"/>'/>
    </div>

</rl:listset>

</body>
</html>
