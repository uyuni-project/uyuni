<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">


<html:html lang="true">
  <head>
    <jsp:include page="layout_head.jsp" />
    <decorator:head />
  </head>
  <body onload="<decorator:getProperty property="body.onload" />">
    <nav class="navbar-pf navbar navbar-dark bg-dark" role="navigation">
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
