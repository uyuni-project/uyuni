<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>


<html>
<head>
</head>
<body>
<script src="/javascript/rank_options.js?cb=${rhn:getConfig('web.buildtimestamp')}" type="text/javascript"> </script>
    <%@ include file="/WEB-INF/pages/common/fragments/ssm/header.jspf" %>

<h2>
  <rhn:icon type="header-configuration" title="config.common.channelsAlt" />
  <bean:message key="ssm.config.rank.jsp.header" />
</h2>
<h3><bean:message key="ssm.config.rank.jsp.step"/></h3>
<div class="page-summary">
  <p>
    <bean:message key="ssm.config.rank.jsp.summary" />
  </p>
</div>
<html:form method="POST"
               action="/systems/ssm/config/Rank.do">
        <rhn:csrf />
        <rhn:submitted />
                <h2><bean:message key="sdc.config.rank.jsp.subscribed_channels"/></h2>
                <table style="width:60%;">
                        <tr>
                        <%@ include file="/WEB-INF/pages/common/fragments/configuration/rankchannels.jspf" %>
                        <td>
                          <table class="schedule-action-interface">
                <tr>
                  <td><html:radio property="priority" value="lowest" /></td>
                  <th><bean:message key="ssm.config.rank.jsp.lowest" /></th>
                </tr>
                <tr>
                  <td><html:radio property="priority" value="highest" /></td>
                  <th><bean:message key="ssm.config.rank.jsp.highest" /></th>
                </tr>
                <tr>
                  <td><html:radio property="priority" value="replace" /></td>
                  <th><bean:message key="ssm.config.rank.jsp.replace" /></th>
                </tr>
              </table>
                        </td>
                        </tr>
                </table>

        <div class="text-right">
      <hr />
      <html:hidden property="dispatch" value="${rhn:localize('ssm.config.rank.jsp.apply')}"/>
      <button type="submit" name="dispatcher" class="btn btn-default"
        onclick="handle_ranking_dispatch('ranksWidget','rankedValues','channelRanksForm');">
        ${rhn:localize('ssm.config.rank.jsp.apply')}
      </button>
    </div>
        </html:form>
</body>
</html>
