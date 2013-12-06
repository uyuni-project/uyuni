<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-html"	prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean"	prefix="bean"%>
<%@ taglib uri="http://rhn.redhat.com/tags/config-managment" prefix="cfg" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>



<html>
<body>
<%@ include
	file="/WEB-INF/pages/common/fragments/configuration/channel/details-header.jspf"%>

<h2><bean:message key="deploysystems.jsp.h2" /></h2>
<p><bean:message key="deploysystems.jsp.note" /></p>
<c:set var="channel_name_display"><cfg:channel id="${ccid}"
									name="${channel.name}"
									type="global"/></c:set>
<p><bean:message key="deploysystems.jsp.warning" arg0="${channel_name_display}"/></p>


<html:form action="/configuration/channel/ChooseSystemsSubmit.do?ccid=${ccid}">
    <rhn:csrf />
	<rhn:submitted/>
	<div>
		<rhn:list pageList="${requestScope.pageList}" noDataText="deploy.jsp.noSystems">
			<rhn:listdisplay filterBy="system.common.systemName" set="${requestScope.set}">
	      		<rhn:set value="${current.id}"/>

			<rhn:column header="system.common.systemName">
              <a href="/rhn/systems/details/configuration/Overview.do?sid=${current.id}">
                <i class="fa fa-desktop" title="<bean:message key='system.common.systemAlt' />"></i>
                <c:out value="${current.name}" />
              </a>
			</rhn:column>
			</rhn:listdisplay>
		</rhn:list>
		<hr />
		<div class="text-right">
			<html:submit property="dispatch"><bean:message key="deploysystems.jsp.deployconfirmbutton" /></html:submit>
		</div>
	</div>
</html:form>
</body>
</html>

