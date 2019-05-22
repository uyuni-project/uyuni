<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html:html xhtml="true">
  <body>
    <rhn:toolbar base="h1" icon="system-bare-metal"
                 helpUrl="/docs/reference/admin/bare-metal-systems.html">
      <bean:message key="bootstrapsystems.jsp.toolbar"/>
    </rhn:toolbar>

    <div class="page-summary">
      <p>
        <bean:message key="bootstrapsystems.jsp.summary"/>
      </p>
    </div>
    <rhn:dialogmenu mindepth="0" maxdepth="1" definition="/WEB-INF/nav/sat_config.xml" renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />

    <div class="panel panel-default">
        <div class="panel-heading">
            <h4><bean:message key="bootstrapsystems.jsp.header"/></h4>
        </div>
        <div class="panel-body">
    <form styleClass="form-horizontal" action="/rhn/admin/config/BootstrapSystems.do" method="POST">
        <rhn:csrf />
        <rhn:submitted/>
        <c:if test="${enabledForOtherOrg}">
            <p>
                <bean:message key="bootstrapsystems.jsp.bootstrap_enabled_for_other_org" arg0="${enabledOrg}" arg1="${currentOrg}"/>
            </p>
        </c:if>
        <div class="form-group">
                    <label class="col-sm-3 control-label">
                      <bean:message key="bootstrapsystems.jsp.enable_bootstrap_discovery"/>
                </label>
                    <div class="col-sm-9">
                    <c:choose>
                        <c:when test="${disabled}">
                                <input type="submit" name="enable" value="${rhn:localize('enable')}" class="btn btn-success" />
                        </c:when>
                        <c:when test="${enabledForCurrentOrg}">
                                <input type="submit" name="disable" value="${rhn:localize('disable')}" class="btn btn-danger" />
                        </c:when>
                        <c:when test="${enabledForOtherOrg}">
                                <input type="submit" disabled value="${rhn:localize('disable')}" class="btn btn-default" />
                        </c:when>
                    </c:choose>
                </div>
            </div>
    </form>
        </div>
    </div>
  </body>
</html:html>
