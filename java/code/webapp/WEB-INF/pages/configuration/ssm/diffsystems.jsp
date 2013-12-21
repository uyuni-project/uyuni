<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<html>
<body>
<%@ include file="/WEB-INF/pages/common/fragments/ssm/header.jspf" %>
<h2>
  <rhn:icon type="header-configuration" title="ssmdiff.jsp.imgAlt" />
  <bean:message key="diffsystems.jsp.header" />
</h2>

  <div class="page-summary">
    <c:set var="beanarg" scope="request">
      <rhn:icon type="header-file" title="config.common.fileAlt" />
      ${fn:escapeXml(requestScope.filepath)}
    </c:set>
    <p>
      <bean:message key="diffsystems.jsp.summary" arg0="${beanarg}"/>
    </p>
  </div>

<form method="post" name="rhn_list" action="/rhn/systems/ssm/config/DiffSystems.do?cfnid=${param.cfnid}">
  <rhn:csrf />
  <rhn:submitted />
  <rhn:list pageList="${requestScope.pageList}"
            noDataText="diffconfirm.jsp.noSystems">
    <rhn:listdisplay filterBy="system.common.systemName">
      <rhn:column header="system.common.systemName"
                  url="/rhn/systems/details/configuration/Overview.do?sid=${current.id}">
        <rhn:icon type="header-system-physical" title="system.common.systemAlt" />
        ${fn:escapeXml(current.name)}
      </rhn:column>

      <rhn:column header="diffsystems.jsp.file">
        <bean:message key="diffsystems.jsp.configfile"
                      arg0="/rhn/configuration/file/FileDetails.do?cfid=${current.configFileId}&crid=${current.configRevisionId}"
                      arg1="${current.configRevision}"
                      arg2="/rhn/configuration/ChannelOverview.do?ccid=${current.configChannelId}"
                      arg3="${current.channelNameDisplay}" />
      </rhn:column>
    </rhn:listdisplay>
  </rhn:list>
</form>

</body>
</html>
