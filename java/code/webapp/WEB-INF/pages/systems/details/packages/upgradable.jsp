<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>


<html>

<body>
<%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>

  <h2>
    <rhn:icon type="header-package-upgrade" title="errata.common.upgradepackageAlt" />
    <bean:message key="upgradable.jsp.header" />
  </h2>
  <div class="page-summary">
    <p>
      <bean:message key="upgradable.jsp.summary" />
    </p>
  </div>

<c:set var="pageList" value="${requestScope.all}" />

<rl:listset name="packageListSet">
    <rhn:csrf />
    <rhn:submitted />
    <c:if test="${not empty requestScope.all}">
        <div class="spacewalk-section-toolbar">
            <div class="action-button-wrapper">
                <input type="submit" class="btn btn-default" name="dispatch"
                        value='<bean:message key="upgradable.jsp.upgrade"/>'/>
            </div>
        </div>
    </c:if>
        <rl:list dataset="pageList"
         width="100%"
         name="packageList"
         emptykey="packagelist.jsp.nopackages"
         alphabarcolumn="nvrea">
                        <rl:decorator name="PageSizeDecorator"/>
                        <rl:decorator name="ElaborationDecorator"/>
                        <rl:decorator name="SelectableDecorator"/>
                        <rl:selectablecolumn value="${current.selectionKey}"
                                selected="${current.selected}"
                                disabled="${not current.selectable}"/>

                  <rl:column headerkey="upgradable.jsp.latest" bound="false"
                        sortattr="nvrea"
                        sortable="true" filterattr="nvrea">

                      <a href="/rhn/software/packages/Details.do?sid=${param.sid}&amp;id_combo=${current.idCombo}">
                        ${current.nvrea}</a>
                      <c:if test="${current.appstream != null}">
                          <span class="label label-info" title="AppStream module: ${current.appstream}">
                              <c:out value="${current.appstream}"/>
                          </span>
                          &nbsp;
                      </c:if>
                      <c:if test="${current.pkgReboot}">
                          <rhn:icon type="errata-reboot" title="errata-legend.jsp.reboot" />
                      </c:if>
                  </rl:column>

                  <rl:column headerkey="upgradable.jsp.installed" bound="false">
                      ${current.installedPackage}
                  </rl:column>

    <rl:column headerkey="upgradable.jsp.errata">
      <c:forEach items="${current.errata}" var="errata">
        <c:if test="${not empty errata.advisory}">
          <c:if test="${errata.type == 'Security Advisory'}">
            <rhn:icon type="errata-security" title="erratalist.jsp.securityadvisory" />
          </c:if>
          <c:if test="${errata.type == 'Bug Fix Advisory'}">
            <rhn:icon type="errata-bugfix" title="erratalist.jsp.bugadvisory" />
          </c:if>
          <c:if test="${errata.type == 'Product Enhancement Advisory'}">
            <rhn:icon type="errata-enhance" title="erratalist.jsp.productenhancementadvisory" />
          </c:if>
          <c:if test="${current.errataReboot}">
              <rhn:icon type="errata-reboot" title="errata-legend.jsp.reboot" />
          </c:if>
          <c:if test="${current.errataRestart}">
              <rhn:icon type="errata-restart" title="errata.jsp.restart-tooltip" />
          </c:if>
          <a href="/rhn/errata/details/Details.do?eid=${errata.id}">${errata.advisory}</a><br/>
        </c:if>
      </c:forEach>
    </rl:column>
</rl:list>

</rl:listset>
</body>
</html>
