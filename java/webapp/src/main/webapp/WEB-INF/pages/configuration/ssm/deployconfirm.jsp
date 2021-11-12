<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>


<html>
<body>
<%@ include file="/WEB-INF/pages/common/fragments/ssm/header.jspf" %>
<h2>
  <rhn:icon type="header-configuration" title="ssmdiff.jsp.imgAlt" />
  <bean:message key="deployconfirm.jsp.header" />
</h2>

  <div class="page-summary">
    <p>
    <c:choose>
      <c:when test="${requestScope.filenum == 1}">
        <bean:message key="deployconfirm.jsp.summary.one" />
      </c:when>
      <c:otherwise>
        <bean:message key="deployconfirm.jsp.summary" arg0="${requestScope.filenum}"/>
      </c:otherwise>
    </c:choose>
    </p>
  </div>

<html:form method="post" action="/systems/ssm/config/DeployConfirmSubmit.do">
  <rhn:csrf />

  <c:if test="${not empty requestScope.pageList}">
    <div class="spacewalk-section-toolbar">
        <div class="action-button-wrapper">
            <html:submit styleClass="btn btn-default" property="dispatch">
              <bean:message key="deployconfirm.jsp.confirm" />
            </html:submit>
        </div>
    </div>
    <p><bean:message key="deployconfirm.jsp.widgetsummary" /></p>
    <jsp:include page="/WEB-INF/pages/common/fragments/schedule-options.jspf"/>
  </c:if>

  <%@ include file="/WEB-INF/pages/common/fragments/configuration/ssm/configconfirmlist.jspf"%>

</html:form>

</body>
</html>
