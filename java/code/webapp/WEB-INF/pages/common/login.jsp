<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<html>
<head>
    <meta name="decorator" content="layout_c" />
    <script src="/javascript/susemanager-login.js?cb=${rhn:getConfig('web.version')}"></script>
</head>
<body>
  <c:if test="${schemaUpgradeRequired == 'true'}">
    <div class="alert alert-danger">
      <bean:message key="login.jsp.schemaupgraderequired" />
    </div>
  </c:if>
  <c:if test="${not empty validationErrors}">
    <div class="alert alert-danger">
      <c:forEach items="${validationErrors}" var="err" varStatus="loop">
        ${err}<br/>
      </c:forEach>
    </div>
  </c:if>
  <div class="col-sm-6">
      <c:set var="product_name" scope="page" value="${rhn:getConfig('product_name')}" />
      <c:choose>
          <c:when test="${product_name == 'Uyuni'}">
              <h1 class="Raleway-font">Uyuni</h1>
              <p class="gray-text margins-updown">Discover a new way of managing your servers, packages, patches and more via one interface.</p>
              <p class="gray-text">Learn more about Uyuni: <a href="http://www.uyuni-project.org/" class="btn-dark" target="_blank"> View website</a></p>
          </c:when>
          <c:otherwise>
              <h1 class="Raleway-font">SUSE<br/> Manager</h1>
              <p class="gray-text margins-updown">Discover a new way of managing your servers, packages, patches and more via one interface.</p>
              <p class="gray-text">Learn more about SUSE Manager: <a href="http://www.suse.com/products/suse-manager/" class="btn-dark" target="_blank"> View website</a></p>
          </c:otherwise>
      </c:choose>

      <!-- original text
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
      <c:if test="${empty login_banner}">
          <div class="login-reference-links">
              <p>
                  <small><bean:message key="login.jsp.satbody2" /></small> <small><bean:message key="login.jsp.satbody3" /></small>
              </p>
          </div>
      </c:if>
      -->

  </div>
  <div class="col-sm-5 col-sm-offset-1">
      <h2 class="Raleway-font gray-text">Sign In</h2>
      <html:form action="/LoginSubmit">
          <rhn:csrf />
          <%@ include file="/WEB-INF/pages/common/fragments/login_form.jspf"%>
      </html:form>
      <hr/>
      <c:set var="legal_note" scope="page" value="${rhn:getConfig('java.legal_note')}" />
      <c:if test="${! empty legal_note}">
          <p class="gray-text small-text">
              <c:out value="${legal_note}" escapeXml="false" />
          </p>
      </c:if>
  </div>
</body>
</html>
