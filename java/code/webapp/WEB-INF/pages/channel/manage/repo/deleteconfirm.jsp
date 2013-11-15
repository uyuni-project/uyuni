<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>


<html>
<body>
<rhn:toolbar base="h1" icon="fa-info-circle" imgAlt="info.alt.img">
	<c:out value="${requestScope.label}"/>
</rhn:toolbar>

<h2><bean:message key="repos.jsp.delete.header2"/></h2>
<p><bean:message key="repos.jsp.delete.summary"/></p>
<p><rhn:warning key= "repos.jsp.delete.warning"/></p>
<div>
	<form method="post" action="/rhn/channels/manage/repos/RepoDelete.do">
    <rhn:csrf />
<h2><bean:message key="repos.jsp.delete.info.header"/></h2>
    <table class="details">
    <tr>
        <th>
         <bean:message key="repos.jsp.create.label"/>
        </th>
        <td>
			<c:out value="${label}"/></pre>
       </td>
    </tr>
    <tr>
      <th>
       <bean:message key="repos.jsp.create.url"/>
      </th>
      <td>
	    <c:out value="${url}"/></pre>
      </td>
    </tr>

    </table>
    <hr />
    <table align="right">
    <tr>
      <td></td>
      <rhn:submitted/>
      <input type="hidden" name="id" value="${requestScope.id}"/>
      <td align="right"><input type=submit name="dispatch"
       value="${rhn:localize('repos.jsp.delete.submit')}"/></td>
    </tr>
        </table>
    </form>
</div>

</body>
</html>
