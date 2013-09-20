<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<html:xhtml/>
<html>
<head>
    <meta name="page-decorator" content="none" />
</head>
<%-- disableAutoComplete() hack added to prevent certain browsers from exposing sensitive data --%>
<body onLoad="disableAutoComplete();">
<rhn:toolbar base="h1" img="/img/rhn-icon-users.gif" imgAlt="user.common.userAlt">
<bean:message key="Addresses"/>
</rhn:toolbar>
<html:form action="/account/EditAddressSubmit">
<rhn:csrf />
<html:hidden property="type"/>

<div class="page-summary">
<p>
    <bean:message key="your_edit_address.jsp.summary" />
</p>
</div>

<h2>
    ${editAddressForm.map.typedisplay}
    <bean:message key="your_edit_address_record.displayname"/>
</h2>

<%@ include file="/WEB-INF/pages/common/fragments/user/edit_address_form.jspf" %>

<div align="right">
<hr />

    <input type="submit" value="<bean:message key="button.update" />" />
</div>

<html:hidden property="uid" value="${param.uid}"/>

</html:form>
</body>
</html>
