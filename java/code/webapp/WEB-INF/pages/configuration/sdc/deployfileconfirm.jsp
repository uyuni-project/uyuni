<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>


<html>
<head>
    <meta name="name" value="sdc.config.jsp.header" />
</head>
<body>
<%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>

<rhn:toolbar base="h2" icon="header-system" >
  <bean:message key="sdcdeployconfirm.jsp.header"/>
</rhn:toolbar>

  <div class="page-summary">
    <p>
    <bean:message key="sdcdeployconfirm.jsp.summary"
                  arg0="${fn:escapeXml(system.name)}"/>
    </p>
  </div>

<html:form method="post"
                action="/systems/details/configuration/DeployFileConfirmSubmit.do?sid=${system.id}">
    <rhn:csrf />
    <c:set var="button" value="sdcdeployconfirm.jsp.schedule" />

    <c:if test="${not empty requestScope.pageList}">
        <div class="spacewalk-section-toolbar">
            <div class="action-button-wrapper">
                <html:submit styleClass="btn btn-default" property="dispatch">
                    <bean:message key="${button}" />
                </html:submit>
            </div>
        </div>
        <p><bean:message key="sdcconfigconfirm.jsp.widgetsummary" /></p>

        <jsp:include page="/WEB-INF/pages/common/fragments/schedule-options.jspf"/>
    </c:if>

    <rhn:list pageList="${requestScope.pageList}" noDataText="sdcconfigfiles.jsp.noFiles">
        <rhn:listdisplay filterBy="sdcconfigfiles.jsp.filename">
            <%@ include file="/WEB-INF/pages/common/fragments/configuration/sdc/configfile_rows.jspf" %>
        </rhn:listdisplay>
    </rhn:list>
</html:form>

</body>
</html>
