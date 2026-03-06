<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ page import="com.suse.manager.webui.utils.LoginHelper" %>
<!-- header.jsp -->

<c:set var="custom_header" scope="page" value="${rhn:getConfig('java.custom_header')}" />

<div id="messages-container"></div>

<rhn:require acl="user_authenticated()">
  <c:set var="diskspaceSeverity" value="<%= LoginHelper.validateDiskSpaceAvailability() %>" />
  <c:set var="dbDiskspaceSeverity" value="<%= LoginHelper.validateDBDiskSpaceAvailability() %>" />

  <c:if test="${diskspaceSeverity != 'ok'}">
    <div class="alert ${diskspaceSeverity == 'critical' ? 'alert-danger' : diskspaceSeverity == 'alert' || diskspaceSeverity == 'misconfiguration' ? 'alert-warning' : 'alert-info'}" role="alert">
      <c:choose>
        <c:when test="${diskspaceSeverity == 'critical'}">
          The available disk space for the server is critically low. To prevent an automatic shutdown via health check, please contact your system administrator immediately to allocate additional space.
        </c:when>
        <c:when test="${diskspaceSeverity == 'alert'}">
          The available disk space for the server is low. To prevent an automatic shutdown via health check, please contact your system administrator immediately to allocate additional space.
        </c:when>
        <c:when test="${diskspaceSeverity == 'misconfiguration'}">
          Some important directories are missing. Please contact your system administrator to review the configuration.
        </c:when>
        <c:otherwise>
          Unable to validate the disk space availability on Server. Please contact your system admistrator if this problem persists.
        </c:otherwise>
      </c:choose>
    </div>
  </c:if>

  <c:if test="${dbDiskspaceSeverity != 'ok'}">
    <div class="alert ${dbDiskspaceSeverity == 'critical' ? 'alert-danger' : dbDiskspaceSeverity == 'alert' || dbDiskspaceSeverity == 'misconfiguration' ? 'alert-warning' : 'alert-info'}" role="alert">
      <c:choose>
        <c:when test="${dbDiskspaceSeverity == 'critical'}">
          The available disk space for the database is critically low. To prevent an automatic shutdown via health check, please contact your system administrator immediately to allocate additional space.
        </c:when>
        <c:when test="${dbDiskspaceSeverity == 'alert'}">
          The available disk space for the database is low. To prevent an automatic shutdown via health check, please contact your system administrator immediately to allocate additional space.
        </c:when>
        <c:when test="${dbDiskspaceSeverity == 'misconfiguration'}">
          Some important directories are missing on database. Please contact your system administrator to review the configuration.
        </c:when>
        <c:otherwise>
          Unable to validate the disk space availability on Database. Please contact your system admistrator if this problem persists.
        </c:otherwise>
      </c:choose>
    </div>
  </c:if>
</rhn:require>

<div class="header-content container-fluid">
  <div class="navbar-header d-flex flex-row">
    <a class="navbar-toggle" data-bs-toggle="collapse" href="#spacewalk-aside">
      <i class="fa fa-bars" aria-hidden="true"></i>
    </a>
    <div id="breadcrumb">
      <c:if test="${! empty custom_header}">
        <div class="custom-text">
          <c:out value="${custom_header}" escapeXml="false"/>
        </div>
      </c:if>
    </div>
  </div>

  <rhn:require acl="user_authenticated()">
    <ul class="nav navbar-nav navbar-controls">
      <li id="notifications">
        <script>
          spaImportReactPage('notifications/notifications');
        </script>
      </li>
      <rhn:require acl="authorized_for(systems.search) or authorized_for(software.search) or authorized_for(patches.search)">
        <li class="search" id="header-search">
          <script>
            spaImportReactPage('header/search');
          </script>
        </li>
      </rhn:require>
      <li id="ssm-box" class="ssm-box hide-overflow">
        <div id="ssm-counter"></div>
        <script type="text/javascript">
          window.csrfToken = '<c:out value="${csrf_token}" />';
          spaImportReactPage('systems/ssm/ssm-counter')
            .then(function(module) {
              module.renderer("ssm-counter", {})
            });
        </script>
      </li>
      <li>
        <a href="/rhn/account/UserDetails.do"
          title="${requestScope.session.user.login}">
            <rhn:icon type="header-user" /><span class="menu-link">${requestScope.session.user.login}</span>
        </a>
      </li>
      <li>
        <rhn:require acl="user_role(org_admin)">
          <a href="/rhn/multiorg/OrgConfigDetails.do" title="${requestScope.session.user.org.name}">
            <rhn:icon type="header-sitemap" /><span class="menu-link">${requestScope.session.user.org.name}</span>
          </a>
        </rhn:require>
        <rhn:require acl="not user_role(org_admin)">
          <span class="spacewalk-header-non-link" title="${requestScope.session.user.org.name}">
            <rhn:icon type="header-sitemap" /><span class="menu-link">${requestScope.session.user.org.name}</span>
          </span>
        </rhn:require>
      </li>
      <li>
        <a href="/rhn/account/UserPreferences.do" role="button" data-bs-toggle="tooltip" title="<bean:message key="header.jsp.preferences" />"
            alt="<bean:message key="header.jsp.preferences" />">
          <rhn:icon type="header-preferences"/>
        </a>
      </li>
      <li>
      <c:choose>
        <c:when test="${rhn:getConfig('java.sso')}">
          <a data-senna-off href="/rhn/manager/sso/logout" role="button" data-bs-toggle="tooltip" title="<bean:message key="header.jsp.signout" />"
            alt="<bean:message key="header.jsp.signout" />">
        </c:when>
        <c:otherwise>
          <a data-senna-off href="/rhn/Logout.do" role="button" data-bs-toggle="tooltip" title="<bean:message key="header.jsp.signout" />"
            alt="<bean:message key="header.jsp.signout" />">
        </c:otherwise>
      </c:choose>
        <rhn:icon type="header-signout" />
        </a>
      </li>
    </ul>
  </rhn:require>
</div>
<!-- end header.jsp -->
