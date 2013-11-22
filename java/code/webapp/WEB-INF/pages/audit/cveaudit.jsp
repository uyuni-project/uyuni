<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
 
<html:xhtml />
<html>
<head>
<meta name="page-decorator" content="none" />
</head>

<body>
  <rhn:toolbar base="h1" img="/img/rhn-icon-search.gif"
    imgAlt="audit.jsp.alt"
    helpUrl="/rhn/help/reference/en-US/s1-sm-audit.jsp#s2-sm-audit-cve">
    <bean:message key="cveaudit.jsp.overview" />
  </rhn:toolbar>
  <p>
    <bean:message key="cveaudit.jsp.description" />
  </p>

  <html:form action="/audit/CVEAudit.do">

    <rhn:csrf />
    <rhn:submitted />

    <div class="search-choices">
      <div class="search-choices-group">
        <table class="details">
          <tr>
            <th>
              <label for="cveIdentifierId"><bean:message key="cveaudit.jsp.cvenumber" /></label>
            </th>
            <td>
              <label for="cveIdentifierId">CVE-</label>
              <html:select property="cveIdentifierYear" styleId="cveIdentifierYear" value="${cveIdentifierYear}">
                <html:options collection="years" property="value" />
              </html:select>
              <label for="cveIdentifierId">-</label>
              <html:text property="cveIdentifierId" styleId="cveIdentifierId" value="${cveIdentifierId}" size="10" title="CVE-ID" />
              <html:submit><bean:message key="cveaudit.jsp.cvenumber.auditsystem" /></html:submit><br />
            </td>
          </tr>

          <tr>
            <th><bean:message key="cveaudit.jsp.filters" /></th>
            <td>
              <label>
                <html:checkbox property="includeAffectedPatchInapplicable" />
                <img
                  src="/img/patch-status-affected-patch-inapplicable.png"
                  title="<bean:message key="cveaudit.jsp.patchstatus.affectedpatchinapplicable"/>" />
                <bean:message
                  key="cveaudit.jsp.patchstatus.affectedpatchinapplicable" />
              </label>
              <br />
              <label>
                <html:checkbox property="includeAffectedPatchApplicable" />
                <img
                  src="/img/patch-status-affected-patch-applicable.png"
                  title="<bean:message key="cveaudit.jsp.patchstatus.affectedpatchapplicable"/>" />
                <bean:message
                  key="cveaudit.jsp.patchstatus.affectedpatchapplicable" />
              </label>
              <br />
              <label>
                <html:checkbox property="includeNotAffected" />
                <img src="/img/patch-status-not-affected.png"
                  title="<bean:message key="cveaudit.jsp.patchstatus.notaffected"/>" />
                <bean:message key="cveaudit.jsp.patchstatus.notaffected" />
              </label>
              <br />
              <label>
                <html:checkbox property="includePatched" />
                <img src="/img/patch-status-patched.png"
                  title="<bean:message key="cveaudit.jsp.patchstatus.patched"/>" />
                <bean:message key="cveaudit.jsp.patchstatus.patched" />
              </label>
              <br />
            </td>
          </tr>
        </table>
      </div>
    </div>
    <input type="hidden" name="submitted" value="true" />
  </html:form>

  <c:if test="${cveIdentifier != null && cveIdentifier != '' && cveIdentifierUnknown == false}">
    <hr />
    <rl:listset name="resultSet">
      <rhn:csrf />

      <%-- Copy parameters over --%>
      <input type="hidden" name="cveIdentifier" value="${cveIdentifier}" />
      <input type="hidden" name="includeAffectedPatchInapplicable"
        value="${includeAffectedPatchInapplicable}" />
      <input type="hidden" name="includeAffectedPatchApplicable"
        value="${includeAffectedPatchApplicable}" />
      <input type="hidden" name="includeNotAffected"
        value="${includeNotAffected}" />
      <input type="hidden" name="includePatched"
        value="${includePatched}" />
      <input type="hidden" name="submitted" value="true" />

      <rl:list name="list" dataset="dataset"
        emptykey="cveaudit.jsp.noresults" width="100%"
        alphabarcolumn="systemName"
      >

        <rl:decorator name="PageSizeDecorator" />
        <rl:decorator name="SelectableDecorator"/>

        <rl:selectablecolumn value="${current.systemID}"
          selected="${current.selected}"
          styleclass="first-column" />

        <rl:column bound="false" headerkey="cveaudit.jsp.patchstatus"
          attr="patchStatus" sortattr="patchStatusRank"
          defaultsort="asc">
          <c:choose>
            <c:when
              test="${current.patchStatus == 'AFFECTED_PATCH_INAPPLICABLE'}">
              <img
                src="/img/patch-status-affected-patch-inapplicable.png"
                title="<bean:message key="cveaudit.jsp.patchstatus.affectedpatchinapplicable"/>" />
              <bean:message
                key="cveaudit.jsp.patchstatus.affectedpatchinapplicable" />
            </c:when>

            <c:when
              test="${current.patchStatus == 'AFFECTED_PATCH_APPLICABLE'}">
              <img src="/img/patch-status-affected-patch-applicable.png"
                title="<bean:message key="cveaudit.jsp.patchstatus.affectedpatchapplicable"/>" />
              <bean:message
                key="cveaudit.jsp.patchstatus.affectedpatchapplicable" />
            </c:when>

            <c:when test="${current.patchStatus == 'NOT_AFFECTED'}">
              <img src="/img/patch-status-not-affected.png"
                title="<bean:message key="cveaudit.jsp.patchstatus.notaffected"/>" />
              <bean:message key="cveaudit.jsp.patchstatus.notaffected" />
            </c:when>

            <c:when test="${current.patchStatus == 'PATCHED'}">
              <img src="/img/patch-status-patched.png"
                title="<bean:message key="cveaudit.jsp.patchstatus.patched"/>" />
              <bean:message key="cveaudit.jsp.patchstatus.patched" />
            </c:when>
          </c:choose>
        </rl:column>

        <rl:column bound="false" headerkey="cveaudit.jsp.system"
          attr="systemName" sortattr="systemName">
          <a
            href="/rhn/systems/details/Overview.do?sid=${current.systemID}">
            ${current.systemName} </a>
        </rl:column>

        <rl:column bound="false" headerkey="cveaudit.jsp.nextaction">
          <c:choose>
            <c:when
              test="${current.patchStatus == 'AFFECTED_PATCH_INAPPLICABLE'}">
              <a
                href="/rhn/systems/details/SystemChannels.do?sid=${current.systemID}">
                <bean:message key="cveaudit.jsp.nextaction.affectedpatchinapplicable" />
              </a>
              <br/>
              <c:choose>
                <c:when test="${fn:length(current.channels) eq 1}">
                  <bean:message key="cveaudit.jsp.candidate" />
                </c:when>
                <c:when test="${fn:length(current.channels) gt 1}">
                  <bean:message key="cveaudit.jsp.candidates" />
                </c:when>
              </c:choose>
              <c:forEach var="channel" items="${current.channels}" varStatus="loopStatus">
                ${channel.name}<c:if test="${not loopStatus.last}">,</c:if>
              </c:forEach>              
            </c:when>

            <c:when
              test="${current.patchStatus == 'AFFECTED_PATCH_APPLICABLE'}">
              <a
                href="/rhn/systems/details/ErrataList.do?sid=${current.systemID}">
                <bean:message key="cveaudit.jsp.nextaction.affectedpatchapplicable" />
              </a>
              <br/>
              <c:choose>
                <c:when test="${fn:length(current.erratas) eq 1}">
                  <bean:message key="cveaudit.jsp.candidate" />
                </c:when>
                <c:when test="${fn:length(current.erratas) gt 1}">
                  <bean:message key="cveaudit.jsp.candidates" />
                </c:when>
              </c:choose>
              <c:forEach var="errata" items="${current.erratas}" varStatus="loopStatus">
                ${errata.advisory}<c:if test="${not loopStatus.last}">,</c:if>
              </c:forEach>
            </c:when>

            <c:when test="${current.patchStatus == 'NOT_AFFECTED'}">
              <bean:message key="cveaudit.jsp.nextaction.notaffected" />
            </c:when>

            <c:when test="${current.patchStatus == 'PATCHED'}">
              <bean:message key="cveaudit.jsp.nextaction.patched" />
            </c:when>
          </c:choose>
        </rl:column>
      </rl:list>

      <rl:csv dataset="dataset" name="list"
        exportColumns="patchStatus,systemName,patchAdvisory,channelName" />
    </rl:listset>
  </c:if>

  <rhn:require acl="user_role(satellite_admin)">
    <p>
      <bean:message key="cveaudit.jsp.updatenotice.pre" />
      <a href="/rhn/admin/BunchDetail.do?label=cve-server-channels-bunch">
        <bean:message key="cveaudit.jsp.updatenotice.link" />
      </a>
      <bean:message key="cveaudit.jsp.updatenotice.post" />
    </p>
  </rhn:require>
  <rhn:require acl="not user_role(satellite_admin)">
    <p>
      <bean:message key="cveaudit.jsp.updatenotice.non-admin" />
    </p>
  </rhn:require>
</body>
</html>
