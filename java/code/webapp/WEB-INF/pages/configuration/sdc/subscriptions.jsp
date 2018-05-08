<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/config-managment" prefix="cfg" %>

<html>
<head>
  <meta name="name" value="sdc.config.subscriptions.jsp.header"/>
</head>
<body>
<%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>

<rhn:toolbar base="h2" icon="header-channel-configuration">
                        <bean:message key="sdc.config.subscriptions.jsp.header"/>
</rhn:toolbar>
<h3><bean:message key="ssm.config.subscribe.jsp.step"/></h3>
<p><bean:message key="sdc.config.subscriptions.jsp.para1" /></p>
<c:choose>
<c:when test="${not empty pageList}">
<html:form  method="POST" action="/systems/details/configuration/SubscriptionsSubmit.do?sid=${param.sid}">
    <rhn:csrf />

    <rhn:list pageList="${requestScope.pageList}"
                  noDataText="sdc.config.subscriptions.jsp.noChannels">

      <rhn:listdisplay  set="${requestScope.set}"
         filterBy = "sdc.config.subscriptions.jsp.channel"
         >
        <rhn:set value="${current.id}"/>
        <rhn:column header="sdc.config.subscriptions.jsp.channel"
                      url="/rhn/configuration/ChannelOverview.do?ccid=${current.id}">
            <rhn:icon type="header-channel" title="config.common.globalAlt" />
            ${current.name}
        </rhn:column>

        <rhn:column header="sdc.config.subscriptions.jsp.label">
            ${current.label}
        </rhn:column>

      <rhn:column header="sdc.config.subscriptions.jsp.files">
            ${current.filesAndDirsDisplayString}
      </rhn:column>


      </rhn:listdisplay>
      <div class="text-right">
          <hr />
          <html:submit styleClass="btn btn-default" property="dispatch">
                  <bean:message key="sdc.config.subscriptions.jsp.continue"/>
          </html:submit>
      </div>
    </rhn:list>
        <rhn:noscript/>

        <rhn:submitted/>
        </html:form>
</c:when>
<c:otherwise>
    <div class="alert alert-warning">
        <bean:message key="sdc.config.subscriptions.jsp.noChannels"
                      arg0="/rhn/systems/details/configuration/ConfigChannelList.do?sid=${param.sid}"/>
    </div>
</c:otherwise>
</c:choose>
</body>
</html>
