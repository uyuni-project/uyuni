<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>

  <div id="footer">
    <bean:message key="footer.jsp.copyright"/>
    <div style="color: black"><bean:message key="footer.jsp.release" arg0="/rhn/help/dispatcher/release_notes" arg1="${rhn:getConfig('web.version')}" /></div>
  </div>

<%--
	Render Javascript here so we can be sure that all of
	the form elements we may need have been rendered.
--%>
<!-- Javascript -->
<script src="/javascript/check_all.js" type="text/javascript"></script>
