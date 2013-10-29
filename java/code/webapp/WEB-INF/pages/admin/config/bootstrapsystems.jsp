<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html:html xhtml="true">
  <body>
    <rhn:toolbar base="h1" img="/img/rhn-icon-info.gif" imgAlt="info.alt.img">
      <bean:message key="bootstrapsystems.jsp.toolbar"/>
    </rhn:toolbar>

    <div class="page-summary">
      <p>
        <bean:message key="bootstrapsystems.jsp.summary"/>
      </p>
    </div>
    <rhn:dialogmenu mindepth="0" maxdepth="1" definition="/WEB-INF/nav/sat_config.xml" renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />

    <div>
      <form action="/rhn/admin/config/BootstrapSystems.do" method="POST">
        <rhn:csrf />

        <h2><bean:message key="bootstrapsystems.jsp.header"/></h2>

        <c:if test="${enabledForOtherOrg}">
          <bean:message key="bootstrapsystems.jsp.bootstrap_enabled_for_other_org" arg0="${enabledOrg}" arg1="${currentOrg}"/>
        </c:if>
        <table class="details">
          <tr>
            <th>
              <bean:message key="bootstrapsystems.jsp.enable_bootstrap_discovery"/>
            </th>
            <td>
              <c:choose>
                <c:when test="${disabled}">
                  <input type="submit" name="enable" value="${rhn:localize('enable')}" />
                </c:when>
                <c:when test="${enabledForCurrentOrg}">
                  <input type="submit" name="disable" value="${rhn:localize('disable')}" />
                </c:when>
                <c:when test="${enabledForOtherOrg}">
                  <input type="submit" disabled="true" value="${rhn:localize('disable')}" />
                </c:when>
              </c:choose>
            </td>
          </tr>
        </table>

        <hr/>

        <rhn:submitted/>
      </form>
    </div>
  </body>
</html:html>
