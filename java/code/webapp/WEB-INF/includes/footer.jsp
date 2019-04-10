<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>

<div class="wrapper">
  <div class="footer-copyright">
    <bean:message key="footer.jsp.copyright" />
  </div>
  <div class="footer-release">
    <bean:message key="footer.jsp.release" arg0="/docs/release-notes/release-notes-server.html" arg1="${rhn:getConfig('web.version')}" />
  </div>
  <%@ include file="/WEB-INF/pages/common/fragments/bugzilla.jspf" %>
  <c:set var="custom_footer" scope="page" value="${rhn:getConfig('java.custom_footer')}" />
  <c:if test="${! empty custom_footer}">
    <div><c:out value="${custom_footer}" escapeXml="false"/></div>
  </c:if>
</div>
<div class="bottom-line"></div>
