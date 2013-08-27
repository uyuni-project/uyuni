<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<html:xhtml/>
<html>
<link rel="stylesheet" type="text/css" href="/css/pwstrengthmeter.css" />
<%--  disableAutoComplete() huck added to prevent certain browsers from exposing sensitive data --%>
<body onLoad="disableAutoComplete();">
<script language="javascript" src="/javascript/display.js"></script>
<script language="javascript" src="/javascript/pwstrengthmeter.js"></script>

<%@ include file="/WEB-INF/pages/common/fragments/user/user_attribute_sizes.jspf"%>

<!-- Setup the account_type variable, which is used throughout this page -->
<c:if test="${empty param.account_type}">
  <c:set var="account_type" value="create_corporate" scope="page" />
</c:if>
<c:if test="${!empty param.account_type}">
  <c:set var="account_type" value="${param.account_type}" scope="page"/>
</c:if>

<c:if test="${account_type == 'into_org'}" >
  <rhn:toolbar base="h1" img="/img/rhn-icon-users.gif" imgAlt="user.common.userAlt"
       helpUrl="/rhn/help/reference/en-US/s1-sm-users.jsp">
  <bean:message key="usercreate.toolbar" />
  </rhn:toolbar>
</c:if>


  <c:if test="${empty param.action_path}">
    <c:set var="action_path" value="/newlogin/CreateSatelliteSubmit" scope="page" />
  </c:if>
  <c:if test="${!empty param.action_path}">
    <c:set var="action_path" value="${param.action_path}" scope="page"/>
  </c:if>
<html:form action="${action_path}">

  <rhn:csrf />
  <h2><bean:message key="usercreate.login" /></h2>

  <table class="details" align="center">
    <tr>
      <th><label for="login"><rhn:required-field key="desiredlogin"/>:</label></th>
      <td>
        <html:text property="login" size="15" maxlength="${loginLength}" styleId="login" onkeyup="checkPassword();"/>
      </td>
    </tr>
    <tr>
      <th><label for="desiredpass"><bean:message key="desiredpass" /><span name="password-asterisk"
            class="required-form-field">*</span>:</th></label>
      <td>
        <div class="pwstrength-container">
          <html:password property="desiredpassword" size="15" maxlength="${passwordLength}" styleId="desiredpass" onkeyup="checkPassword();"/>
          <span class="pwstrength-result" id="pwstrength-result">Enter password</span><br/>
          <span class="pwstrength-graybar" id="pwstrength-graybar"></span>
          <span class="pwstrength-colorbar" id="pwstrength-colorbar"></span>
          <span class="pwstrength-percent" id="pwstrength-percent">0%</span>
        </div>
      </td>
    </tr>
    <tr>
      <th><label for="confirmpass"><bean:message key="confirmpass" /><span name="password-asterisk"
            class="required-form-field">*</span>:</label></th>
      <td>
        <html:password property="desiredpasswordConfirm" size="15" maxlength="${passwordLength}" styleId="confirmpass"/>
      </td>
    </tr>
    <c:if test="${displaypam == 'true' && account_type != 'create_sat'}">
      <tr>
        <th><label for="pam"><bean:message key="usercreate.jsp.pam"/></label></th>
        <td>
          <c:choose>
            <c:when test="${displaypamcheckbox == 'true'}">
            <html:checkbox property="usepam" onclick="toggleVisibilityByName('password-asterisk')" styleId="pam"/>
            <label for="pam"><bean:message key="usercreate.jsp.pam.instructions"/></label> <br/>
            <strong><span style="font-size: 10px">
                <bean:message key="usercreate.jsp.pam.instructions.note"/>
            </span></strong>
            </c:when>
            <c:otherwise>
              <bean:message key="usercreate.jsp.pam.reference"/>
            </c:otherwise>
          </c:choose>
        </td>
      </tr>
    </c:if>
  </table>

  <h2><bean:message key="usercreate.accountInfo" /></h2>
  <table class="details" align="center">
    <tr>
      <th><label for="firstNames"><rhn:required-field key="usercreate.names"/>:</th></label>
      <td>
        <html:select property="prefix">
          <html:options collection="availablePrefixes"
            property="value"
            labelProperty="label" />
        </html:select>

        <html:text property="firstNames" size="15" maxlength="${firstNameLength}" styleId="firstNames"/>
        <html:text property="lastName" size="15" maxlength="${lastNameLength}"/>
      </td>
    </tr>

    <tr>
       <th><label for="email"><rhn:required-field key="email"/>:</label></th>
       <td>
           <html:text property="email" size="20" maxlength="${emailLength}" styleId="email"/>
       </td>
    </tr>
<html:hidden property="account_type" value="${account_type}" />

    <tr>
      <td style="border: 0; text-align: center" colspan="2"><span class="required-form-field">*</span> - <bean:message key="usercreate.requiredField" /></td>
    </tr>
</table>


<c:if test="${account_type == 'into_org'}">
  <h2><bean:message key="preferences.jsp.tz"/></h2>
  <p><bean:message key="preferences.jsp.datestimes"/></p>
  <div class="preference"><bean:message key="preferences.jsp.displaytimesas"/>
  <select name="timezone">
    <c:forEach var="tz" items="${requestScope.timezones}">
      <c:if test="${tz.value == requestScope.default_tz}">
        <option value="${tz.value}" selected="selected">${tz.display}</option>
      </c:if>
      <c:if test="${tz.value != requestScope.default_tz}">
        <option value="${tz.value}">${tz.display}</option>
      </c:if>
    </c:forEach>
  </select>
  </div>
</c:if>

<div align="right">
<html:submit>
  <bean:message key="usercreate.jsp.createlogin"/>
</html:submit>
</div>


</html:form>

<%-- This makes sure that the asterisks toggle correctly. Before, they could get off
     if the user checked the usepam checkbox, submitted the form, and had errors. Then
     the form would start with the box checked but the asterisks visible.
--%>
<script language="javascript">
  var items = document.getElementsByName('password-asterisk');
  if (document.getElementById('pam').checked == true) {
    for (var i = 0; i < items.length; i++) {
      items[i].style.display = "none";
    }
  }
  else {
    for (var i = 0; i < items.length; i++) {
      items[i].style.display = "";
    }
  }
</script>
</body>
</html>
