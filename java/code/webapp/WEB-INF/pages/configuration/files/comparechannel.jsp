<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>


<html>
<body>

<%@ include file="/WEB-INF/pages/common/fragments/configuration/files/header.jspf" %>
<h2><bean:message key="comparechannel.jsp.header"/></h2>

<div class="page-summary">
  <p>
    <bean:message key="comparechannel.jsp.summary"
                  arg0="/rhn/configuration/file/FileDetails.do?cfid=${file.id}&amp;crid=${revision.id}"
                  arg1="${revision.revision}"/>
  </p>
</div>

<form method="post" name="rhn_list" action="/rhn/configuration/file/CompareChannel.do?cfid=${file.id}&amp;crid=${revision.id}">
<rhn:csrf />
<rhn:submitted />
<rhn:list pageList="${requestScope.pageList}" noDataText="comparechannel.jsp.noChannels">
  <rhn:listdisplay filterBy="comparechannel.jsp.channel">
    <rhn:column header="comparechannel.jsp.channel"
                url="/rhn/configuration/ChannelOverview.do?ccid=${current.id}">
        <c:if test="${current.type == 'normal'}">
    	  <i class="fa spacewalk-icon-software-channels" title="<bean:message key="config.common.globalAlt" />"></i>
          ${current.name}
        </c:if>

        <c:if test="${current.type == 'local_override'}">
          <i class="fa fa-desktop" title="<bean:message key="config.common.localAlt" />"></i>
          ${current.name}
        </c:if>

        <c:if test="${current.type == 'server_import'}">
          <i class="fa spacewalk-icon-sandbox" title="<bean:message key="config.common.sandboxAlt" />"></i>
          ${current.name}
        </c:if>
    </rhn:column>

    <rhn:column header="comparechannel.jsp.type">
        ${current.typeDisplay}
    </rhn:column>

    <rhn:column header="comparechannel.jsp.files"
                url="/rhn/configuration/ChannelFiles.do?ccid=${current.id}">
        <c:if test="${current.fileCount == 1}">
          <bean:message key="comparechannel.jsp.onefile" />
        </c:if>
        <c:if test="${current.fileCount != 1}">
          <bean:message key="comparechannel.jsp.numfiles" arg0="${current.fileCount}" />
        </c:if>
    </rhn:column>

    <rhn:column header="comparechannel.jsp.continue">
        <a class="link-button" href="/rhn/configuration/file/CompareFile.do?cfid=${file.id}&amp;crid=${revision.id}&amp;ccid=${current.id}">
            <bean:message key="comparechannel.jsp.select" />
        </a>
    </rhn:column>
  </rhn:listdisplay>
</rhn:list>
</form>

</body>
</html>

