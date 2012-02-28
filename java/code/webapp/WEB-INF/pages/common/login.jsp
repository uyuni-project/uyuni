<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<html:xhtml/>
<html>
<head>
    <script src="/javascript/focus.js" type="text/javascript"></script>
    <script src="/javascript/legalnote.js" type="text/javascript"></script>
    <meta name="decorator" content="layout_equals" />
</head>
<body onLoad="formFocus('loginForm', 'username'); putLegalNote('${rhn:localize('footer.jsp.legalNote')}');">
<c:if test="${requestScope.hasExpired != 'true'}">
<div id="login_page">
  <h1 id="rhn_welcome3"><span><bean:message key="login.jsp.welcomemessage"/></span></h1>
  <div class="clearBox">
  <div class="clearBoxInner">
  <div class="clearBoxBody">

    <html:form action="/LoginSubmit">
        <rhn:csrf />
        <%@ include file="/WEB-INF/pages/common/fragments/login_form.jspf" %>
    </html:form>
  </div>
  </div>
  </div>
</div> <!-- end login_page -->
</c:if>

<div style="clear:both"></div><!-- Clearing div. Let's not have the footer over the context -->
</body>
</html>
