<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html>
<body>
<rhn:toolbar base="h1" icon="spacewalk-icon-manage-entitlements-subscriptions"
 miscUrl="${url}"
 miscAcl="user_role(org_admin)"
 miscText="${text}"
 miscImg="${img}"
 miscAlt="${text}"
imgAlt="users.jsp.imgAlt">
<bean:message key="sys_entitlements.header"/>
</rhn:toolbar>

<bean:message key="sys_entitlements.description"/>


<c:choose>
	<c:when test = "${orgCount > 1}">
		<c:set var = "countstyle" value= ""/>
		<c:set var = "usagestyle" value = "last-column"/>
	</c:when>
	<c:otherwise>
		<c:set var = "countstyle" value= "last-column"/>
		<c:set var = "usagestyle" value = ""/>
	</c:otherwise>
</c:choose>



<rl:listset name="entitlementSet">
    <rhn:csrf />
    <rhn:submitted />
    <rl:list dataset="pageList"
             width="100%"
             name="pageList"
             styleclass="list"
             emptykey="sys_entitlements.noentitlements">

        <rl:column bound="false"
            sortable="false"
            headerkey="sys_entitlements.ent_name">
            <a href="/rhn/admin/multiorg/EntitlementDetails.do?label=${current.label}">${current.name}</a>
        </rl:column>
        <rl:column bound="false"
            sortable="false"
            headerkey="sys_entitlements.total">
            ${current.total}
        </rl:column>
        <rl:column bound="false"
            sortable="false"
            headerkey="sys_entitlements.available" styleclass="${countstyle}">
            ${current.available}
        </rl:column>
        <c:if test="${orgCount > 1}">
        <rl:column bound="false"
            sortable="false"
            headertext="${rhn:localize('sys_entitlements.usage')} <br/> (${rhn:localize('Used/Allotted')})**"
            styleclass="${usagestyle}"
            >
            <c:choose>
            <c:when test="${empty current.allocated or current.allocated == 0}">
				<bean:message key="None Allocated"/>
            </c:when>
			<c:otherwise>
				<bean:message key="sys_entitlements.usagedata" arg0="${current.used}" arg1="${current.allocated}" arg2="${current.ratio}"/>
			</c:otherwise>
            </c:choose>
        </rl:column>
        </c:if>

    </rl:list>
</rl:listset>

<rhn:tooltip typeKey="Tip">*-<bean:message key = "sys_entitlements.tip"/></rhn:tooltip>
<rhn:tooltip typeKey="Tip">**-<bean:message key = "Used/Allotted.tip"/></rhn:tooltip>
</body>
</html>

