<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html:xhtml/>
<html>

<body>
  <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>

  <rl:listset name="groupSet">
    <rhn:csrf />
    <html:hidden property="sid" value="${param.sid}" />

    <h2><bean:message key="images.jsp.select" /></h2>
    <rl:list dataset="imagesList" emptykey="images.jsp.noimages">
      <rl:decorator name="PageSizeDecorator" />
      <rl:radiocolumn value="${current.id}" styleclass="first-column" />
      <rl:column headerkey="images.jsp.name">
        <a href="${current.editUrl}"><c:out value="${current.name}" /></a>
      </rl:column>
      <rl:column headerkey="images.jsp.version">
        <c:out value="${current.version}" />
      </rl:column>
      <rl:column headerkey="images.jsp.arch">
        <c:out value="${current.arch}" />
      </rl:column>
      <rl:column headerkey="images.jsp.type">
        <c:out value="${current.imageType}" />
      </rl:column>
    </rl:list>

    <h2><bean:message key="images.jsp.vmsetup" /></h2>
    <table class="details">
      <tr>
        <th><bean:message key="images.jsp.vcpus" /></th>
        <td><html:text property="vcpus" value="1" /></td>
      </tr>
      <tr>
        <th><bean:message key="images.jsp.memory" /></th>
        <td><html:text property="mem_mb" value="512" /></td>
      </tr>
      <tr>
        <th><bean:message key="images.jsp.bridge" /></th>
        <td><html:text property="bridge" value="br0" /></td>
      </tr>
    </table>

    <h2><bean:message key="images.jsp.proxyconfig" /></h2>
    <table class="details">
      <tr>
        <th><bean:message key="images.jsp.proxyserver" /></th>
        <td><html:text property="proxy_server" value="" /></td>
      </tr>
      <tr>
        <th><bean:message key="images.jsp.proxyuser" /></th>
        <td><html:text property="proxy_user" value="" /></td>
      </tr>
      <tr>
        <th><bean:message key="images.jsp.proxypass" /></th>
        <td><html:password property="proxy_pass" value="" /></td>
      </tr>
    </table>

    <div align="right">
      <rhn:submitted />
      <hr />
      <html:submit property="dispatch"
                   disabled="${empty sessionScope.imagesList}">
        <bean:message key="images.jsp.dispatch" />
      </html:submit>
    </div>
  </rl:listset>
</body>
</html>
