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
  <script src="/javascript/susemanager-cve-audit.js"></script>
</head>

<body onload="formFocus('cveAuditForm','cveIdentifierId');">
  <rhn:toolbar base="h1" icon="header-search"
    imgAlt="audit.jsp.alt"
    helpUrl="/rhn/help/user/en-US/s1-sm-audit.jsp#s2-sm-audit-cve">
    <bean:message key="cveaudit.jsp.overview" />
  </rhn:toolbar>
  <p>
    <bean:message key="cveaudit.jsp.description" />
  </p>
  <div class="panel panel-default">
    <div class="panel-body">
      <html:form action="/audit/CVEAudit.do" styleClass="form-horizontal">
        <rhn:csrf />
        <rhn:submitted />
        <div class="row">
          <label class="col-sm-3 control-label" for="cveIdentifierId">
            <a href="#" role="button" id="cve-popover" class="fa fa-info-circle" data-toggle="popover" data-placement="bottom" data-content="<bean:message key="cveaudit.jsp.popover-content" />" data-trigger="click" data-delay="500"></a>
            <bean:message key="cveaudit.jsp.cvenumber" />
          </label>
          <div class="col-sm-4">
            <div class="input-group">
              <span class="input-group-addon">CVE-</span>
              <html:select property="cveIdentifierYear" styleId="cveIdentifierYear" styleClass="form-control" value="${cveIdentifierYear}">
                <html:options collection="years" property="value" />
              </html:select>
              <span class="input-group-addon">-</span>
              <html:text property="cveIdentifierId" styleId="cveIdentifierId" styleClass="form-control" value="${cveIdentifierId}" title="CVE-ID" />
            </div>
          </div>
        </div>
        <div class="row margin-bottom-sm">
          <div class="col-sm-offset-3 col-sm-4">
            <small>Format: CVE-YYYY-NNNN...N</small>
          </div>
        </div>
        <div class="row margin-bottom-sm">
          <label class="col-sm-3 control-label">
            <bean:message key="cveaudit.jsp.filters" />
          </label>
          <div class="col-sm-6">
            <div class="checkbox">
              <label>
                <html:checkbox property="includeAffectedPatchInapplicable" />
                <img src="/img/patch-status-affected-patch-inapplicable.png"
                     title="<bean:message key="cveaudit.jsp.patchstatus.affectedpatchinapplicable"/>" />
                <bean:message key="cveaudit.jsp.patchstatus.affectedpatchinapplicable" />
              </label>
            </div>
            <div class="checkbox">
              <label>
                <html:checkbox property="includeAffectedPatchApplicable" />
                <img src="/img/patch-status-affected-patch-applicable.png"
                     title="<bean:message key="cveaudit.jsp.patchstatus.affectedpatchapplicable"/>" />
                <bean:message key="cveaudit.jsp.patchstatus.affectedpatchapplicable" />
              </label>
            </div>
            <div class="checkbox">
              <label>
                <html:checkbox property="includeNotAffected" />
                <img src="/img/patch-status-not-affected.png"
                     title="<bean:message key="cveaudit.jsp.patchstatus.notaffected"/>" />
                <bean:message key="cveaudit.jsp.patchstatus.notaffected" />
              </label>
            </div>
            <div class="checkbox">
              <label>
                <html:checkbox property="includePatched" />
                <img src="/img/patch-status-patched.png"
                     title="<bean:message key="cveaudit.jsp.patchstatus.patched"/>" />
                <bean:message key="cveaudit.jsp.patchstatus.patched" />
              </label>
            </div>
          </div>
        </div>
        <hr />
        <div class="row">
          <div class="col-sm-offset-3 col-sm-6">
            <button type="submit" class="btn btn-success"><i class="fa fa-search"></i><bean:message key="cveaudit.jsp.cvenumber.auditsystem" /></button>
          </div>
        </div>
        <input type="hidden" name="submitted" value="true" />
      </html:form>
    </div>
  </div>

  <c:if test="${cveIdentifierId != null && cveIdentifierId != '' && cveIdentifierUnknown == false}">
    <hr />
    <rl:listset name="resultSet">
      <rhn:csrf />

      <%-- Copy parameters over --%>
      <input type="hidden" name="cveIdentifierYear"
        value="${cveIdentifierYear}" />
      <input type="hidden" name="cveIdentifierId"
        value="${cveIdentifierId}" />
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
    <p><small>
      <bean:message key="cveaudit.jsp.updatenotice.pre" />
      <a href="/rhn/admin/BunchDetail.do?label=cve-server-channels-bunch">
        <bean:message key="cveaudit.jsp.updatenotice.link" />
      </a>
      <bean:message key="cveaudit.jsp.updatenotice.post" />
      </small>
    </p>
  </rhn:require>
  <rhn:require acl="not user_role(satellite_admin)">
    <p>
      <bean:message key="cveaudit.jsp.updatenotice.non-admin" />
    </p>
  </rhn:require>
</body>
</html>
