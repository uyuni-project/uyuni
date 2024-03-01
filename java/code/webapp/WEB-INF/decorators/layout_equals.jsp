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
  </head>
  <c:set var="webTheme" value="${GlobalInstanceHolder.USER_PREFERENCE_UTILS.getCurrentWebTheme(pageContext)}"/>
  <c:set var="isUpdatedPage" value="${GlobalInstanceHolder.VIEW_HELPER.isBootstrapReady(pageContext.request.requestURI)}"/>
  <body class="theme-${webTheme} ${isUpdatedPage ? 'new-theme' : 'old-theme'}" onload="<decorator:getProperty property="body.onload" />">
    <nav class="navbar-pf navbar" role="navigation">
      <jsp:include page="/WEB-INF/includes/header.jsp" />
    </nav>
    <div class="spacewalk-main-column-layout">
      <aside id="spacewalk-aside" class="navbar-collapse collapse">
      </aside>
      <section id="spacewalk-content">
        <decorator:body />
      </section>
    </div>
    <footer>
      <jsp:include page="/WEB-INF/includes/footer.jsp" />
    </footer>
  </body>
</html:html>
