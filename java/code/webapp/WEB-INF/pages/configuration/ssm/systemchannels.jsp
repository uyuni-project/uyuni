<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>


<html>
<body>
<%@ include file="/WEB-INF/pages/common/fragments/ssm/header.jspf" %>
<h2>
  <i class="fa spacewalk-icon-channels-config" title="<bean:message key="config.common.channelsAlt" />"></i>
  <bean:message key="ssmsystemchannels.jsp.header" />
</h2>

  <div class="page-summary">
    <p>
      <c:set var="beanarg" scope="request">
        <a href="/rhn/systems/details/configuration/Overview.do?sid=${system.id}"
          <i class="fa fa-desktop" title="<bean:message key='system.common.systemAlt' />"></i>
          ${system.name}
        </a>
      </c:set>
      <bean:message key="ssmsystemchannels.jsp.summary"
                    arg0="${beanarg}"
                    arg1="/rhn/systems/details/configuration/ConfigChannelList.do?sid=${system.id}"/>
    </p>
  </div>

<form method="post" name="rhn_list" action="/rhn/systems/ssm/config/SystemChannels.do?sid=${param.sid}">
  <rhn:csrf />
  <rhn:submitted />
  <rhn:list pageList="${requestScope.pageList}"
            noDataText="ssmsystemchannels.jsp.noChannels">
    <rhn:listdisplay filterBy="config.common.configChannel">
      <rhn:column header="config.common.configChannel"
                  url="/rhn/configuration/ChannelOverview.do?ccid=${current.id}">
        <i class="fa spacewalk-icon-software-channels" title="<bean:message key="config.common.globalAlt" />"></i>
        ${current.name}
      </rhn:column>

      <rhn:column header="ssmsystemchannels.jsp.files">
        <c:choose>
          <c:when test="${current.fileCount == 1}">
            <bean:message key="ssmsystemchannels.jsp.onefile" />
          </c:when>
          <c:otherwise>
            <bean:message key="ssmsystemchannels.jsp.numfiles" arg0="${current.fileCount}"/>
          </c:otherwise>
        </c:choose>
      </rhn:column>
    </rhn:listdisplay>
  </rhn:list>
</form>

</body>
</html>
