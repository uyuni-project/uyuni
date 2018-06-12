<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<html>
<body>
  <h1><rhn:icon type="header-info" /><bean:message key="help.jsp.about.title"/></h1>
  <p><bean:message key="help.jsp.about.summary"/></p>
  <c:choose>
    <c:when test="${isUyuni}">
      <p><bean:message key="help.jsp.about.learnmore.uyuni"/></p>
    </c:when>
    <c:otherwise>
      <p><bean:message key="help.jsp.about.learnmore"/></p>
    </c:otherwise>
  </c:choose>
</body>
</html>
