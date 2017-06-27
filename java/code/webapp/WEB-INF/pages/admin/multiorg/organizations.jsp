<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html>
<body>
<rhn:toolbar base="h1" icon="header-organisation"
     creationUrl="/rhn/admin/multiorg/OrgCreate.do"
     creationType="org">
  <bean:message key="organizations.jsp.toolbar"/>
</rhn:toolbar>
<c:set var="pageList" value="${requestScope.pageList}" />
<div>
<rl:listset name="orgListSet">
<rhn:csrf />
<rhn:submitted />
<!-- Start of org list -->
<rl:list dataset="pageList"
         width="100%"
         name="orgList"
         styleclass="list"
         filter="com.redhat.rhn.frontend.action.multiorg.OrgListFilter"
         emptykey="orglist.jsp.noOrgs">

        <!-- Organization name column -->
        <rl:column bound="false"
                   sortable="true"
                   headerkey="org.nopunc.displayname"
                   sortattr="name">
                <a href="/rhn/admin/multiorg/OrgDetails.do?oid=${current.id}">
                        <c:out value="${current.name}"/>
                    </a>
                <c:if test="${current.id == 1}">*</c:if>
        </rl:column>
        <rl:column bound="false"
                   sortable="true"
                   headerkey="systems.nopunc.displayname"
                   attr="systems">
                <c:out value="${current.systems}" />
        </rl:column>
        <rl:column bound="false"
                   sortable="true"
                   headerkey="users.nopunc.displayname"
                   attr="users">
                <c:out value="<a href=\"/rhn/admin/multiorg/OrgUsers.do?oid=${current.id}\">${current.users}</a>" escapeXml="false" />
        </rl:column>
   <rl:column bound="false"
              sortable="true"
              headerkey="org.trust.trusts"
              attr="users">
      <a href="/rhn/admin/multiorg/OrgTrusts.do?oid=${current.id}">${current.trusts}</a>
   </rl:column>
</rl:list>

</rl:listset>
<span class="small-text">
    *<bean:message key="organizations.tip"/>
</span>
</div>

</body>
</html>

