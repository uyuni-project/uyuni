<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<html:xhtml/>
<html>
<head>
  <script src="/javascript/focus.js" type="text/javascript"></script>
  <script src="/javascript/legalnote.js" type="text/javascript"></script>
</head>
<body onLoad="disableAutoComplete();formFocus('loginForm', 'username');putLegalNote('${rhn:localize('footer.jsp.legalNote')}');">
    <div id="relogin_page"> <!-- Trying to make relogin page look more like login -->
    <style> <%-- Rather ugly hack, but at the time being I don't see any other way to do this without breaking the backwards-compatibility --%>
	.sidebar{display:none}
	.page-content{padding-left:0;border-left:none}
    </style> <%-- End of hack --%>

<c:if test="${schemaUpgradeRequired == 'true'}">
    <div class="site-alert">
        <bean:message key="login.jsp.schemaupgraderequired" />
    </div>
</c:if>

<rhn:require acl="not user_authenticated()">
<c:if test="${requestScope.hasExpired != 'true'}">

     <h1 id="rhn_welcome3"><span><bean:message key="relogin.jsp.pleasesignin"/></span></h1>
  <div class="clearBox">
  <div class="clearBoxInner">
  <div class="clearBoxBody">

      <html:form action="/ReLoginSubmit">
          <rhn:csrf />
          <%@ include file="/WEB-INF/pages/common/fragments/login_form.jspf" %>
           <html:hidden property="url_bounce" />
           <html:hidden property="request_method" />
      </html:form>
    </div><!-- end clearBoxBody -->
    </div><!-- end clearBoxInner -->
    </div><!-- end clearBox -->

</c:if>
</rhn:require>

    </div> <!-- Login Page -->
    <div style="clear:both"></div><!-- Clearing div. Let's not have the footer over the context -->
</body>
</html>
