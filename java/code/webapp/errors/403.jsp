<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ page contentType="text/html; charset=UTF-8" %>

<page:applyDecorator name="layout_error">
<body>
    <c:choose>
      <c:when test="${rhn:getConfig('java.sso')}">
        <c:set var="logoutUrl" value="/rhn/manager/sso/logout"/>
      </c:when>
      <c:otherwise>
        <c:set var="logoutUrl" value="/rhn/Logout.do"/>
      </c:otherwise>
    </c:choose>
    <c:set value="${requestScope[&quot;jakarta.servlet.error.request_uri&quot;]}" var="errorUrl" />
    <c:set var="escapedUrl" value="${fn:escapeXml(errorUrl)}"/>

    <h1>
      <bean:message key="403.jsp.title"/>
    </h1>
    <c:if test="${not empty escapedUrl}">
      <p><bean:message key="403.jsp.summary" arg0="${escapedUrl}"/></p>
    </c:if>
    <p><bean:message key="403.jsp.instruction"/></p>
    <p><bean:message key="403.jsp.actions" arg0="${logoutUrl}"/></p>
</body>
</page:applyDecorator>
