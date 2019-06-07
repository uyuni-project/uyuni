<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<html>
<head>
</head>
<body>
    <script src="/javascript/susemanager-login.js?cb=${rhn:getConfig('web.version')}"></script>

    <c:if test="${schemaUpgradeRequired == 'true'}">
        <div class="alert alert-danger">
            <bean:message key="login.jsp.schemaupgraderequired" />
        </div>
    </c:if>
    <rhn:require acl="not user_authenticated()">
        <section class="wrap">
            <div class="row">
                <c:choose>
                    <c:when test="${isUyuni}">
                        <div class="col-sm-6">
                            <h1 class="Raleway-font">Uyuni</h1>
                            <p class="gray-text margins-updown">Discover a new way of managing your servers, packages, patches and more via one interface.</p>
                            <p class="gray-text">Learn more about Uyuni: <a href="http://www.uyuni-project.org/" class="btn-dark" target="_blank"> View website</a></p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="col-sm-6">
                            <h1 class="Raleway-font">SUSE<br/> Manager</h1>
                            <p class="gray-text margins-updown">Discover a new way of managing your servers, packages, patches and more via one interface.</p>
                            <p class="gray-text">Learn more about SUSE Manager: <a href="http://www.suse.com/products/suse-manager/" class="btn-dark" target="_blank"> View website</a></p>
                      </div>
                    </c:otherwise>
                </c:choose>
                <div class="col-sm-5 col-sm-offset-1">
                    <h2 class="Raleway-font gray-text">
                        <bean:message key="relogin.jsp.pleasesignin" />
                    </h2>
                    <html:form action="/ReLoginSubmit">
                        <rhn:csrf />
                        <%@ include file="/WEB-INF/pages/common/fragments/login_form.jspf"%>
                    </html:form>
                    <hr/>
                    <c:set var="login_banner" scope="page" value="${rhn:getConfig('java.login_banner')}" />
                    <c:if test="${! empty login_banner}">
                        <p class="gray-text small-text">
                            <c:out value="${login_banner}" escapeXml="false" />
                        </p>
                    </c:if>
                    <c:set var="legal_note" scope="page" value="${rhn:getConfig('java.legal_note')}" />
                    <c:if test="${! empty legal_note}">
                        <p class="gray-text small-text">
                            <c:out value="${legal_note}" escapeXml="false" />
                        </p>
                    </c:if>
                </div>
            </div>
        </section>
    </rhn:require>
</body>
</html>
