<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>


<rhn:require acl="user_role(org_admin) or user_role(system_group_admin)">
 <c:set var="msg_key" value="systems.groups.jsp.noGroups" />
 <c:set var="msg_arg0" value="/rhn/systems/details/groups/Add.do?sid=${param.sid}" />
 <c:set var="msg_arg1" value="Join" />

 <c:set var="summary_key" value="systems.groups.jsp.summary" />
 <c:set var="summary_arg0" value="systems.groups.jsp.remove" />
</rhn:require>

<rhn:require acl="not user_role(org_admin);not user_role(system_group_admin)">
<c:set var="msg_key"><bean:message key="systems.groups.jsp.noGroups.nonadmin"/></c:set>

<c:set var="summary_key" value="systems.groups.jsp.summary" />
</rhn:require>


<c:import url="/WEB-INF/pages/common/fragments/systems/groups.jspf">
        <c:param name = "title_key" value="systems.groups.jsp.title"/>
        <c:param name = "summary_key" value="${summary_key}"/>
        <c:param name = "summary_arg0" value="${summary_arg0}"/>
        <c:param name = "action_key" value="systems.groups.jsp.remove"/>
        <c:param name = "empty_message_key" value="${msg_key}"/>
        <c:param name = "empty_message_arg0" value="${msg_arg0}"/>
        <c:param name = "empty_message_arg1" value="${msg_arg1}"/>
</c:import>
