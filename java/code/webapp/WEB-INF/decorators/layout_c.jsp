
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"
%><%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"
%><%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"
%><%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"
%><%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page"
%><%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"
%><%@ page contentType="text/html; charset=UTF-8"
%>
<c:if test="${empty param.excludeBody}" >
  <!DOCTYPE HTML>
  <html:html>
    <head>
      <%--&lt;%&ndash;<script src="/javascript/swup.js  "></script>&ndash;%&gt;--%>
      <%--TODO: Pass this to webpack--%>
      <%--TODO: VALIDATE SEENA TRACK for PERFORMANCE + CONFLICTS--%>
      <link rel="stylesheet" href="/javascript/senna.css">
      <%--<!-- Senna -->--%>
      <script src="/javascript/senna-debug.js"></script>
      <jsp:include page="layout_head.jsp" />
      <decorator:head />
    </head>
    <body onload="<decorator:getProperty property="body.onload" />">
    <div class="senna-loading-bar"></div>
    <header class="navbar-pf">
      <jsp:include page="/WEB-INF/includes/header.jsp" />
    </header>
    <div class="spacewalk-main-column-layout">
      <aside id="spacewalk-aside" class="navbar-collapse in">
        <div id="nav"></div>
        <jsp:include page="/WEB-INF/includes/leftnav.jsp" />
        <footer>
          <jsp:include page="/WEB-INF/includes/footer.jsp" />
        </footer>
      </aside>
      <section id="spacewalk-content">
        <noscript>
          <div class="alert alert-danger">
            <bean:message key="common.jsp.noscript"/>
          </div>
        </noscript>
        <!-- Alerts and messages -->
        <logic:messagesPresent>
          <div class="alert alert-warning">
            <ul>
              <html:messages id="message">
                <li><c:out value="${message}"/></li>
              </html:messages>
            </ul>
          </div>
        </logic:messagesPresent>
        <html:messages id="message" message="true">
          <rhn:messages><c:out escapeXml="false" value="${message}" /></rhn:messages>
        </html:messages>
        <c:if test="${ not empty exception }">
          <div class="alert alert-danger">
            <c:out value="${exception}"/>
          </div>
        </c:if>
        <div id="page-body">
          <decorator:body />
        </div>
      </section>
      <script src='/javascript/manager/menu.bundle.js?cb=${rhn:getConfig('web.version')}'></script>
    </div>
    <button id="scroll-top"><i class='fa fa-angle-up'></i></button>
    <script src='/javascript/manager/senna.bundle.js?cb=${cb_version}'></script>
    </body>
  </html:html>
</c:if>

<c:if test="${not empty param.excludeBody}" >
  <decorator:body />
</c:if>


<%--const sync = () => [...document.getElementsByTagName("a")].map(aTag => aTag.addEventListener("click", event => { event.preventDefault(); console.log(event.target.href);     fetch(event.target.href + (event.target.href.includes("?") ? '&excludeBody=true' :  '?excludeBody=true'))--%>
<%--.then(function(response) {--%>
<%--return response.text()--%>
<%--})--%>
<%--.then(text => {jQuery("#page-body").html(text); sync();})--%>
<%--}))--%>


<%--const sync = (tags) => [...tags].map(aTag => aTag.addEventListener("click", event => { event.preventDefault(); console.log(event.target.href);     fetch(event.target.href + (event.target.href.includes("?") ? '&excludeBody=true' :  '?excludeBody=true'))--%>
<%--.then(function(response) {--%>
<%--return response.text()--%>
<%--})--%>
<%--.then(text => {jQuery("#page-body").html(text); sync(document.getElementById("page-body").getElementsByTagName("a"));})--%>
<%--}))--%>
