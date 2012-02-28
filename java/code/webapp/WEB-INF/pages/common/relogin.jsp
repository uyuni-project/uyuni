<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<html:xhtml/>
<html>
<head>
  <script src="/javascript/focus.js" type="text/javascript"></script>
  <script src="/javascript/legalnote.js" type="text/javascript"></script>
</head>
<body onLoad="formFocus('loginForm', 'username'); putLegalNote('${rhn:localize('footer.jsp.legalNote')}');" id="relogin_page">

<rhn:require acl="not user_authenticated()">
<c:if test="${requestScope.hasExpired != 'true'}">
    <div id="relogin_page">
     <h1><bean:message key="relogin.jsp.pleasesignin"/></h1>

  <div class="clearBox">
  <div class="clearBoxInner">
  <div class="clearBoxBody">
    <html:form action="/ReLoginSubmit">
        <rhn:csrf />
        <%@ include file="/WEB-INF/pages/common/fragments/login_form.jspf" %>
         <html:hidden property="url_bounce" />
    </html:form>
  </div><!-- end clearBoxBody -->
  </div><!-- end clearBoxInner -->
  </div><!-- end clearBox -->
    </div> <!-- Login Page -->
</c:if>
</rhn:require>

</body>
</html>
