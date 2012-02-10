<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>

<html:xhtml/>
<html>
<body>

<rhn:toolbar base="h1" img="/img/rhn-icon-users.gif" imgAlt="users.jsp.imgAlt"
    helpUrl="/rhn/help/reference/en-US/s1-sm-your-rhn.jsp#s2-sm-your-rhn-account">
  <bean:message key="Credentials"/>
</rhn:toolbar>

<p><bean:message key="yourcredentials.jsp.summary" /></p>

<form method="post" action="/rhn/account/Credentials.do">
  <rhn:csrf />
  <rhn:submitted />

  <h2><bean:message key="yourcredentials.jsp.studio" /></h2>
  <table class="details" align="center">
  <tr>
    <th><label for="studioUser">Username</label></th>
    <td>
      <input type="text" name="studioUser" id="studioUser" value="${creds.username}" />
    </td>
  </tr>
  <tr>
    <th><label for="studioKey">API Key</label></th>
    <td>
      <input type="text" name="studioKey" id="studioKey" value="${creds.password}" />
    </td>
  </tr>
  <tr>
    <th><label for="studioHost">URL</label></th>
    <td>
      <input type="text" name="studioUrl" id="studioUrl" value="${creds.url}" />
    </td>
  </tr>
  </table>

  <div align="right">
    <hr />
    <html:submit>
      <bean:message key="yourcredentials.jsp.submit" />
    </html:submit>
  </div>
</form>

</body>
</html>
