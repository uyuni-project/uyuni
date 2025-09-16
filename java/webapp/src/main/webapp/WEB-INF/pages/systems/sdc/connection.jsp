<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html:html>
  <body>
    <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>

    <rhn:toolbar base="h2" icon="header-proxy"
           aclMixins="com.redhat.rhn.common.security.acl.SystemAclHandler"
           miscUrl="/rhn/manager/systems/details/proxy?sid=${system.id}"
           miscText="sdc.details.connection.change"
           miscIcon="header-proxy"
           miscAcl="user_role(org_admin); not system_is_proxy(); system_has_salt_entitlement()">
      <bean:message key="sdc.details.connection.header"/>
    </rhn:toolbar>

    <p><bean:message key="sdc.details.connection.summary1"/></p>
    <p><bean:message key="sdc.details.connection.summary2"/></p>

    <rhn:list pageList="${requestScope.pageList}"
            noDataText="sdc.details.connection.empty">

      <rhn:listdisplay>
        <rhn:column header="sdc.details.connection.proxyorder">
          <c:out value="${current.position}" escapeXml="true" />
        </rhn:column>

        <rhn:column header="row.hostname">
          <a href="/rhn/systems/details/Overview.do?sid=${current.id}"> ${fn:escapeXml(current.hostname)} </a>
        </rhn:column>

        <rhn:column header="systemlist.jsp.entitlement">
          <c:out value="${current.entitlementLevel}" escapeXml="false" />
        </rhn:column>
      </rhn:listdisplay>

    </rhn:list>

  </body>
</html:html>
