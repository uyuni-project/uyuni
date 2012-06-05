<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>

<html:html xhtml="true">

<head>
  <link rel="stylesheet" type="text/css" href="/css/sp-migration.css" />
  <script type="text/javascript" src="/javascript/sp-migration.js"></script>
</head>

<body onload="initChannelSelect();">
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
      <html:form method="post" styleId="migrationForm" action="/systems/details/SPMigration.do?sid=${system.id}" onsubmit="prepareSubmitChannels();">
        <table class="details" align="center">
          <tbody>
            <tr>
              <th><bean:message key="spmigration.jsp.setup.installed-products" /></th>
              <td>
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
              </td>
            </tr>
            <tr>
              <th><bean:message key="spmigration.jsp.setup.target-products" /></th>
              <td>
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
              </td>
            </tr>
            <c:if test="${empty targetProducts.missingChannels}">
              <tr>
                <th><bean:message key="spmigration.jsp.setup.base-channel" /></th>
                <td>
                  <select name="baseChannel" id="base-channel-select" onchange="showChannelTree(this.options[selectedIndex].value);">
                    <c:forEach items="${channelMap}" var="alternative">
                      <option value="${alternative.key.id}" title="${alternative.key.name}">
                        <c:out value="${alternative.key.name}" />
                      </option>
                    </c:forEach>
                  </select>
                </td>
              </tr>
            </c:if>
          </tbody>
        </table>
        <c:if test="${empty targetProducts.missingChannels}">
          <%@ include file="/WEB-INF/pages/systems/spmigration/channel-details.jspf" %>
        </c:if>

        <hr />
        <div class="button-submit">
          <html:submit styleId="submitButton" property="dispatch" disabled="${not empty targetProducts.missingChannels}">
            <bean:message key="spmigration.jsp.setup.submit" />
          </html:submit>
        </div>
        <html:hidden property="baseProduct" value="${targetProducts.baseProduct.id}" />
        <c:forEach items="${targetProducts.addonProducts}" var="current">
          <html:hidden property="addonProducts[]" value="${current.id}" />
        </c:forEach>
        <html:hidden property="step" value="setup" />
        <html:hidden property="submitted" value="true" />
        <rhn:csrf />
      </html:form>
    </c:otherwise>
  </c:choose>
</body>
</html:html>
