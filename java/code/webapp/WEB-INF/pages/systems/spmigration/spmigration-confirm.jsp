<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>

<html:html xhtml="true">

<head>
  <link rel="stylesheet" type="text/css" href="/css/sp-migration.css" />
</head>

<body>
  <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf"%>
  <h2>
    <img src="/img/rhn-icon-channels.gif" alt="channel" />
    <bean:message key="spmigration.jsp.confirm.title" />
  </h2>
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
    <div>
      <table class="schedule-action-interface" align="center">
        <tr>
          <td><input type="radio" id="radio-1" name="use_date" value="false" checked="checked" /></td>
          <th><label for="radio-1"><bean:message key="confirm.jsp.now"/></label></th>
        </tr>
        <tr>
          <td><input type="radio" id="radio-2" name="use_date" value="true" /></td>
          <th><label for="radio-2"><bean:message key="confirm.jsp.than"/></label></th>
        </tr>
        <tr>
          <th><img src="/img/rhn-icon-schedule.gif" alt="<bean:message key="confirm.jsp.selection"/>"
                                                  title="<bean:message key="confirm.jsp.selection"/>"/>
          </th>
          <td>
            <jsp:include page="/WEB-INF/pages/common/fragments/date-picker.jsp">
              <jsp:param name="widget" value="date"/>
            </jsp:include>
          </td>
        </tr>
      </table>
    </div>

    <hr />
    <div class="button-back">
      <html:button property="dispatch" onclick="javascript:history.back();">
        <bean:message key="spmigration.jsp.confirm.back" />
      </html:button>
    </div>
    <div class="button-submit">
      <html:submit property="dispatch">
        <bean:message key="spmigration.jsp.confirm.submit.dry-run" />
      </html:submit>
      <html:submit property="dispatch">
        <bean:message key="spmigration.jsp.confirm.submit" />
      </html:submit>
    </div>

    <html:hidden property="baseProduct" value="${baseProduct.id}" />
    <c:forEach items="${addonProducts}" var="current">
      <html:hidden property="addonProducts[]" value="${current.id}" />
    </c:forEach>
    <html:hidden property="baseChannel" value="${baseChannel.id}" />
    <c:forEach items="${childChannels}" var="current">
      <html:hidden property="childChannels[]" value="${current.id}" />
    </c:forEach>
    <html:hidden property="step" value="confirm" />
    <html:hidden property="submitted" value="true" />
    <rhn:csrf />
  </html:form>
</body>
</html:html>
