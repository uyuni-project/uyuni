<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ page import="com.redhat.rhn.GlobalInstanceHolder" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">


<html:html lang="true">
  <head>
    <jsp:include page="layout_head.jsp" />
    <decorator:head />
    <rhn:require acl="is(development_environment)">
      <link rel="stylesheet/less" type="text/css" href="/css/susemanager-fullscreen.less" />
      <script>less = { env: 'development' };</script>
      <script src="/javascript/less.js"></script>
    </rhn:require>
    <rhn:require acl="not is(development_environment)">
      <link rel="stylesheet" href="/css/susemanager-fullscreen.css" />
    </rhn:require>
  </head>
  <c:set var="webTheme" value="${GlobalInstanceHolder.USER_PREFERENCE_UTILS.getCurrentWebTheme(pageContext)}"/>
  <c:set var="isUpdatedPage" value="${GlobalInstanceHolder.VIEW_HELPER.isBootstrapReady(pageContext.request.requestURI)}"/>
  <body class="theme-${webTheme} ${isUpdatedPage ? 'updated-theme' : 'old-theme'}" onload="<decorator:getProperty property="body.onload" />">
    <div id="menu-portal-target"></div>
    <div class="spacewalk-main-column-layout">
      <section id="spacewalk-content">
        <decorator:body />
      </section>
    </div>
  </body>
</html:html>
