<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>

<html:html xhtml="true">

<head>
  <link rel="stylesheet" type="text/css" href="/css/sp-migration.css" />
  <script type="text/javascript" src="/javascript/sp-migration.js"></script>
</head>

<body>
  <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf"%>
  <h2>
    <img src="/img/rhn-icon-channels.gif" alt="channel" />
    <bean:message key="spmigration.jsp.setup.title" />
  </h2>
  <c:choose>
    <c:when test="${not empty migrationScheduled}">
      <div class="page-summary">
        <p><bean:message key="spmigration.jsp.error.scheduled" arg0="${system.id}" arg1="${migrationScheduled.id}" /></p>
      </div>
    </c:when>
    <c:when test="${not empty latestServicePack}">
      <div class="page-summary">
        <p><bean:message key="spmigration.jsp.error.up-to-date" /></p>
      </div>
    </c:when>
    <c:when test="${zyppPluginInstalled and not upgradeSupported}">
      <div class="page-summary">
        <p><bean:message key="spmigration.jsp.error.update-zypp-plugin" /></p>
      </div>
    </c:when>
    <c:when test="${not zyppPluginInstalled or targetProducts == null}">
      <div class="page-summary">
        <p><bean:message key="spmigration.jsp.error.unsupported" /></p>
      </div>
    </c:when>
    <c:otherwise>
      <c:if test="${not empty targetProducts.missingChannels}">
        <div class="page-summary">
        <div class="site-alert">
          <p><bean:message key="spmigration.jsp.error.missing-channels" /></p>
          <ul class="missing-channels-list">
            <c:forEach items="${targetProducts.missingChannels}" var="missingChannelLabel" varStatus="loop">
              <li class="missing-channel"><c:out value="${missingChannelLabel}" /></li>
            </c:forEach>
          </ul>
        </div>
        </div>
      </c:if>
      <html:form method="post" styleId="migrationForm" action="/systems/details/SPMigration.do?sid=${system.id}">
        <div class="form-horizontal">
          <div class="form-group">
            <label class="col-sm-2 control-label">
              <bean:message key="spmigration.jsp.setup.installed-products" />
            </label>
            <div class="col-sm-10">
              <ul class="products-list">
                <li>
                  <strong><c:out value="${system.installedProducts.baseProduct.friendlyName}" /></strong>
                  <ul>
                    <c:forEach items="${system.installedProducts.addonProducts}" var="addonProduct">
                      <li class="addon-product"><c:out value="${addonProduct.friendlyName}" /></li>
                    </c:forEach>
                  </ul>
                </li>
              </ul>
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-2 control-label">
              <bean:message key="spmigration.jsp.setup.target-products" />
            </label>
            <div class="col-sm-10">
              <ul class="products-list">
                <li>
                  <strong><c:out value="${targetProducts.baseProduct.friendlyName}" /></strong>
                  <ul>
                    <c:forEach items="${targetProducts.addonProducts}" var="addonProduct">
                      <li class="addon-product" id="${addonProduct.id}"><c:out value="${addonProduct.friendlyName}" /></li>
                    </c:forEach>
                  </ul>
                </li>
              </ul>
            </div>
          </div>
          <c:if test="${empty targetProducts.missingChannels}">
          <div class="form-group">
            <label class="col-sm-2 control-label">
              <bean:message key="spmigration.jsp.setup.base-channel" />
            </label>
            <div class="col-sm-10 col-lg-4">
              <select class="form-control" name="baseChannel" id="base-channel-select">
                <c:forEach items="${channelMap}" var="alternative">
                  <option value="${alternative.key.id}" title="${alternative.key.name}">
                    <c:out value="${alternative.key.name}" />
                  </option>
                </c:forEach>
              </select>
            </div>
          </div>
          </c:if>
        <div class="form-group">
          <div class="col-sm-offset-2 col-sm-10">
            <c:if test="${empty targetProducts.missingChannels}">
              <%@ include file="/WEB-INF/pages/systems/spmigration/channel-details.jspf" %>
            </c:if>
          </div>
        </div>
        <div class="form-group">
          <div class="col-sm-offset-2 col-sm-10">
          <button type="submit" class="btn btn-primary" id="submitButton"
                  name="dispatch"<c:if test="${not empty targetProducts.missingChannels}"> disabled</c:if>>
            <bean:message key="spmigration.jsp.setup.submit" />
          </button>
          </div>
        </div>
        <html:hidden property="baseProduct" value="${targetProducts.baseProduct.id}" />
        <c:forEach items="${targetProducts.addonProducts}" var="current">
          <html:hidden property="addonProducts[]" value="${current.id}" />
        </c:forEach>
        <html:hidden property="step" value="setup" />
        <html:hidden property="submitted" value="true" />
        <rhn:csrf />
        </div>
      </html:form>
    </c:otherwise>
  </c:choose>
</body>
</html:html>
