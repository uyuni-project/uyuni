<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>


<html>
<head>
    <meta name="name" value="Users" />
</head>
<body>
<rhn:toolbar base="h1" icon="header-user"
 helpUrl="/docs/${rhn:getDocsLocale(pageContext)}/reference/users/users-menu.html">
  <bean:message key="userconfirm.jsp.header" />
</rhn:toolbar>

  <div class="page-summary">
    <p>
    <bean:message key="userconfirm.jsp.summary" />
    </p>
  </div>

<c:set var="pageList" value="${requestScope.pageList}" />

<rl:listset name="userConfirmListSet">
    <rhn:csrf />
    <rhn:submitted />
      <div class="spacewalk-section-toolbar">
        <div class="action-button-wrapper">
          <rl:csv dataset="pageList"
            name="userConfirmList"
            exportColumns="userLogin,userLastName,userFirstName,roleNames,lastLoggedIn"/>
            <input class="btn btn-primary" type="submit" name="dispatch" value="<bean:message key='userconfirm.jsp.confirm'/>" />
        </div>
      </div>
        <rl:list dataset="pageList"
         width="100%"
         name="userConfirmList"
         styleclass="list"
                 alphabarcolumn="userLogin">
                <rl:decorator name="PageSizeDecorator"/>
                <%@ include file="/WEB-INF/pages/common/fragments/user/userlist_columns.jspf" %>
        </rl:list>
</rl:listset>
</body>
</html>
