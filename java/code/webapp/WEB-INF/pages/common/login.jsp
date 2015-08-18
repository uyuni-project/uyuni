<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<html>
<head>
    <meta name="decorator" content="layout_c" />
    <script src="/javascript/spacewalk-login.js"></script>
</head>
<body>

<c:if test="${schemaUpgradeRequired == 'true'}">
    <div class="alert alert-danger">
        <bean:message key="login.jsp.schemaupgraderequired" />
    </div>
</c:if>

<div id="loginForm-container">
    <c:set var="login_banner" scope="page" value="${rhn:getConfig('java.login_banner')}" />
    <c:choose>
        <c:when test="${! empty login_banner}">
            <p>
                <c:out value="${login_banner}" escapeXml="false" />
            </p>
        </c:when>
        <c:otherwise>
            <h1 id="welcome-title">
                <bean:message key="login.jsp.welcomemessage" />
            </h1>
            <p id="welcome-text">
                <bean:message key="login.jsp.satbody1" />
            </p>
        </c:otherwise>
    </c:choose>

    <html:form styleId="loginForm" action="/LoginSubmit">
        <rhn:csrf />
        <%@ include file="/WEB-INF/pages/common/fragments/login_form.jspf"%>
    </html:form>

    <c:if test="${empty login_banner}">
        <div class="login-reference-links">
            <p>
                <small><bean:message key="login.jsp.satbody2" /></small> <small><bean:message key="login.jsp.satbody3" /></small>
            </p>
        </div>
    </c:if>

    <c:set var="legal_note" scope="page" value="${rhn:getConfig('java.legal_note')}" />
    <c:if test="${! empty legal_note}">
        <p class="legal-note">
            <small><c:out value="${legal_note}" escapeXml="false" /></small>
        </p>
    </c:if>
</div>

</body>
</html>
