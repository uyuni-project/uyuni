<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<!-- header.jsp -->

<c:set var="custom_header" scope="page" value="${rhn:getConfig('java.custom_header')}" />

<div class="navbar-header">
  <a href="#" class="navbar-toggle">
    <i class="fa fa-bars" aria-hidden="true"></i>
  </a>
  <div id="breadcrumb"></div>
  <c:if test="${! empty custom_header}">
    <div class="custom-text">
      <c:out value="${custom_header}" escapeXml="false"/>
    </div>
  </c:if>
</div>

<rhn:require acl="user_authenticated()">
  <ul class="nav navbar-nav navbar-utility">
    <li>
      <a href="/rhn/account/UserDetails.do"
        title="${requestScope.session.user.login}">
          <rhn:icon type="header-user" /><span>${requestScope.session.user.login}</span>
      </a>
    </li>
    <li>
      <rhn:require acl="user_role(org_admin)">
        <a href="/rhn/multiorg/OrgConfigDetails.do" title="${requestScope.session.user.org.name}">
          <rhn:icon type="header-sitemap" /><span>${requestScope.session.user.org.name}</span>
        </a>
      </rhn:require>
      <rhn:require acl="not user_role(org_admin)">
        <span class="spacewalk-header-non-link" title="${requestScope.session.user.org.name}">
          <rhn:icon type="header-sitemap" /><span>${requestScope.session.user.org.name}</span>
        </span>
      </rhn:require>
    </li>
    <li>
      <a href="/rhn/account/UserPreferences.do" title="<bean:message key="header.jsp.preferences" />"
          alt="<bean:message key="header.jsp.preferences" />">
        <rhn:icon type="header-preferences"/>
      </a>
    </li>
    <li>
    <c:choose>
      <c:when test="${rhn:getConfig('java.sso')}">
        <a href="/rhn/manager/sso/logout" title="<bean:message key="header.jsp.signout" />"
           alt="<bean:message key="header.jsp.signout" />">
      </c:when>
      <c:otherwise>
        <a href="/rhn/Logout.do" title="<bean:message key="header.jsp.signout" />"
          alt="<bean:message key="header.jsp.signout" />">
      </c:otherwise>
    </c:choose>
      <rhn:icon type="header-signout" />
      </a>
    </li>
  </ul>
  <ul class="nav navbar-nav navbar-primary">
    <li id="notifications">
      <script src='/javascript/manager/notifications/notifications.bundle.js?cb=${rhn:getConfig('web.version')}'></script>
    </li>
    <c:if test="${requestScope.legends != null}">
      <li class="legend">
        <a href="#" class="toggle-box" data-toggle="collapse" data-target="#legend-box">
          <i class="fa fa-eye" aria-hidden="true"></i>
        </a>
        <div id="legend-box" class="box-wrapper collapse">
          <jsp:include page="/WEB-INF/includes/legends.jsp" />
        </div>
      </li>
    </c:if>
    <li class="search">
      <a href="#" class="toggle-box" data-toggle="collapse" data-target="form#search-form">
        <i class="fa fa-search" aria-hidden="true"></i>
      </a>
      <form id="search-form" name="form1" class="box-wrapper form-inline collapse" role="form" action="/rhn/Search.do">
        <rhn:csrf />
        <rhn:submitted />
        <div class="form-group">
          <input type="search" class="form-control input-sm" name="search_string" size="20" accesskey="4"
              autofocus="autofocus" placeholder="<bean:message key='button.search'/>" />
          <select name="search_type" class="form-control input-sm">
            <option value="systems"><bean:message key="header.jsp.systems" /></option>
            <option value="packages"><bean:message key="header.jsp.packages" /></option>
            <option value="errata"><bean:message key="header.jsp.errata" /></option>
            <option value="docs"><bean:message key="header.jsp.documentation" /></option>
          </select>
          <button type="submit" class="btn btn-primary input-sm" id="search-btn">
            <rhn:icon type="header-search" /><bean:message key='button.search'/>
          </button>
        </div>
      </form>
    </li>
    <li id="ssm-box" class="ssm-box">
      <a href="/rhn/ssm/index.do" id="manage-ssm" title="<bean:message key="manage"/>">
        <div id="header_selcount"><rhn:setdisplay user="${requestScope.session.user}" /></div>
      </a>
      <%--
        -- Make sure we set the return_url variable correctly here. This will make is to
        -- the user is returned here after clearing the ssm.
        --%>
      <c:choose>
        <c:when test="${not empty pageContext.request.queryString}">
          <c:set var="rurl" value="${pageContext.request.requestURI}?${pageContext.request.queryString}"/>
        </c:when>
        <c:otherwise>
          <c:set var="rurl" value="${pageContext.request.requestURI}" />
        </c:otherwise>
      </c:choose>
      <a id="clear-ssm" href="/rhn/systems/Overview.do?empty_set=true&amp;return_url=${rhn:urlEncode(rurl)}"
          title="<bean:message key="clear"/>">
        <i class="fa fa-eraser"></i>
      </a>
    </li>
  </ul>
</rhn:require>

<rhn:require acl="not user_authenticated()">
  <ul class="nav navbar-nav navbar-utility">
    <li>
      <a class="about-link" href="/rhn/help/about.do"><bean:message key="About Spacewalk"/></a>
    </li>
  </ul>
</rhn:require>
<!-- end header.jsp -->
