<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<!-- header.jsp -->

<div class="top-content"> <!--Added for the purpose of SuSE Manager. Header wrap [et]-->
  <div id="utility">
<c:set var="custom_header" scope="page" value="${rhn:getConfig('java.custom_header')}" />
<c:if test="${! empty custom_header}">
    <center><p><c:out value="${custom_header}" escapeXml="false"/></p></center>
</c:if>

<rhn:require acl="user_authenticated()">
	<!--div id="utilityLinks">

<p id="geo"><c:out value="${rhnActiveLang} "/> (<a href="/rhn/account/LocalePreferences.do"><bean:message key="header.jsp.change"/></a>)</p>
		<p id="linx"><span class="hide"><strong><bean:message key="header.jsp.shortcuts"/></strong> </span><a href="http://kbase.redhat.com/"><bean:message key="header.jsp.knowledgebase"/></a> <span class="navPipe">|</span> <a href="/help"><bean:message key="header.jsp.documentation"/></a></p>

	</div-->
	<div id="utilityAccount">
        <p>
         <span id="acc-logged-user">
         <span class="label"><bean:message key="header.jsp.loggedin"/></span> <a href="/rhn/account/UserDetails.do"><c:out escapeXml="true" value="${requestScope.session.user.login}" /></a>
         </span>
         <span class="navPipe" id="acc-logged-user-pipe">|</span>

         <span id="acc-logged-user-org">
         <span class="label"><bean:message key="header.jsp.org"/></span> <a><c:out escapeXml="true" value="${requestScope.session.user.org.name}" /></a>
         </span>
         <span class="navPipe" id="acc-logged-user-org-pipe">|</span>

         <span id="acc-prefs">
         <a href="/rhn/account/UserPreferences.do"><bean:message key="header.jsp.preferences"/></a>
         </span>
         <span class="navPipe" id="acc-prefs-pipe">|</span>

         <span id="acc-logout">
         <html:link forward="logout"><span><bean:message key="header.jsp.signout"/></span></html:link>
         </span>
        </p>
	</div>
</rhn:require>
  </div><!-- id="utility" -->

  <div id="header">
      <a href="<bean:message key="layout.jsp.vendor.website"/>" title="<bean:message key="layout.jsp.vendor.title"/>"><img src="/img/logo_vendor.png" alt="<bean:message key="layout.jsp.vendor.name"/>" id="rhLogo" /></a>
        <a href="/" title="<bean:message key="layout.jsp.productname"/> homepage">
          <img src="/img/logo_product.png" alt="<bean:message key="layout.jsp.productname"/>" id="rhnLogo" accesskey="2"/>
        </a>
      <rhn:require acl="user_authenticated()">
  <div id="searchbar">
    <div id="searchbarinner">
      <form name="form1" action="/rhn/Search.do" method="get">
      <select name="search_type">
      <rhn:require acl="org_entitlement(sw_mgr_enterprise)">
            <option value="systems"><bean:message key="header.jsp.systems"/></option>
          </rhn:require>
      <option value="packages"><bean:message key="header.jsp.packages"/></option>
      <option value="errata"><bean:message key="header.jsp.errata"/></option>
      <option value="docs"><bean:message key="header.jsp.documentation"/></option>
      </select><input type="text" name="search_string" maxlength="40" size="20" accesskey="4" autofocus="autofocus"/>
      <input type="hidden" name="submitted" value="true"/>
      <input type="submit" class="button" name="image-1" value="<bean:message key="button.search"/>" align="top" /></form>
    </div><!-- id="searchbarinner" -->
  </div><!-- id="searchbar" -->
      </rhn:require>
  </div><!-- id="header" -->

  <div id="navWrap">
  <div id="mainNavWrap">
    <rhn:require acl="user_authenticated()">
      <rhn:menu mindepth="0" maxdepth="0"
                definition="/WEB-INF/nav/sitenav-authenticated.xml"
                renderer="com.redhat.rhn.frontend.nav.TopnavRenderer" />
    </rhn:require>
    <rhn:require acl="not user_authenticated()">
      <rhn:menu mindepth="0" maxdepth="0"
               definition="/WEB-INF/nav/sitenav.xml"
               renderer="com.redhat.rhn.frontend.nav.TopnavRenderer" />
    </rhn:require>
  </div> <!-- close div mainNavWrap -->
  </div> <!-- close div navWrap -->
</div> <!--.top-content-->

<div id="bar">
  <div id="systembar">
    <div id="systembarinner">
      <div>
      <rhn:require acl="user_authenticated()">
        <span id="header_selcount">
          <rhn:setdisplay user="${requestScope.session.user}"/>
        </span>
        <a class="button" href="/rhn/ssm/index.do">
        <bean:message key="manage"/>
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
        <a class="button" href="/rhn/systems/Overview.do?empty_set=true&amp;return_url=${rhn:urlEncode(rurl)}">
        <bean:message key="clear"/>
        </a>
      </rhn:require>
      </div>
    </div>
  </div>
</div> <!-- end div bar -->
<!-- end header.jsp -->

