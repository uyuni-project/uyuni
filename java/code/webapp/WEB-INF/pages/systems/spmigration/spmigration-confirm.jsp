<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>

<html:html>

<head>
</head>

<body>
  <link rel="stylesheet" type="text/css" href="/css/susemanager-sp-migration.css?cb=${rhn:getConfig('web.buildtimestamp')}" />
  <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf"%>

  <rhn:toolbar base="h2" icon="header-channel">
    <bean:message key="spmigration.jsp.confirm.title" />
  </rhn:toolbar>

  <div class="page-summary">
    <p><bean:message key="spmigration.jsp.confirm.description" /></p>
    <ul class="list-channel">
      <li><b><c:out value="${baseProduct.friendlyName}" /></b>
        <ul>
          <c:forEach items="${addonProducts}" var="addon" varStatus="loop">
            <li>${addon.friendlyName}</li>
          </c:forEach>
        </ul>
      </li>
    </ul>

    <p><bean:message key="spmigration.jsp.confirm.channels-after-migration" /></p>
    <div id="channels-tree">
      <ul class="list-channel">
        <li>
          <a href="/rhn/channels/ChannelDetail.do?cid=${baseChannel.id}"><c:out value="${baseChannel.name}" /></a>
          <ul>
            <c:forEach items="${childChannels}" var="current">
              <li>
                <input type="checkbox" disabled="disabled" checked="checked" style="display: none;" />
                <a href="/rhn/channels/ChannelDetail.do?cid=${current.id}"><c:out value="${current.name}" /></a>
              </li>
            </c:forEach>
          </ul>
        </li>
      </ul>
    </div>
  </div>

  <html:form method="post" action="/systems/details/SPMigration.do?sid=${system.id}">
    <hr />
    <rhn:hidden name="schedule_type" value="date" />
    <div class="form-horizontal">
      <div class="form-group">
        <label class="col-sm-3 control-label"><bean:message key="confirm.jsp.than"/></label>
        <div class="col-sm-9 col-lg-4">
          <jsp:include page="/WEB-INF/pages/common/fragments/date-picker.jsp">
            <jsp:param name="widget" value="date"/>
          </jsp:include>
        </div>
      </div>
    </div>
    <hr />
    <c:if test="${!requestScope.completed}">
      <div class="alert alert-danger">
        <rhn:icon type="system-crit" />
        <bean:message key="spmigration.jsp.confirm.note" />
      </div>
    </c:if>
    <div>
      <div class="pull-left">
        <html:submit styleClass="btn btn-default" property="dispatch">
          <bean:message key="spmigration.jsp.confirm.back" />
        </html:submit>
      </div>
      <div class="pull-right">
        <c:if test="${!requestScope.completed}">
          <html:submit styleClass="btn btn-success" property="dispatch">
            <bean:message key="spmigration.jsp.confirm.submit.dry-run" />
          </html:submit>
        </c:if>
        <html:submit styleClass="btn btn-danger" property="dispatch">
          <bean:message key="spmigration.jsp.confirm.submit" />
        </html:submit>
      </div>
    </div>
    <html:hidden property="baseProduct" value="${baseProduct.id}" />
    <c:forEach items="${addonProducts}" var="current">
      <html:hidden property="addonProducts" value="${current.id}" />
    </c:forEach>
    <html:hidden property="baseChannel" value="${baseChannel.id}" />
    <c:forEach items="${childChannels}" var="current">
      <html:hidden property="childChannels" value="${current.id}" />
    </c:forEach>
    <html:hidden property="targetProductSelected"
        value="${targetProducts.serializedProductIDs}" />
    <html:hidden property="step" value="confirm" />
    <html:hidden property="submitted" value="true" />
    <rhn:csrf />
  </html:form>
</body>
</html:html>
