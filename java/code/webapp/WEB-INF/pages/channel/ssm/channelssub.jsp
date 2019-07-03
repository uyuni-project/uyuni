<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>


<html>
<body>
<%@ include file="/WEB-INF/pages/common/fragments/ssm/header.jspf" %>
<div class="alert alert-info">
    <bean:message key="sdc.channels.info.change" />
</div>
  <div id="channels-div"></div>
  <script>
        var csrfToken = "<%= com.redhat.rhn.common.security.CSRFTokenValidator.getToken(session) %>";
        var timezone = "<%= com.suse.manager.webui.utils.ViewHelper.getInstance().renderTimezone() %>";
        var localTime = "<%= com.suse.manager.webui.utils.ViewHelper.getInstance().renderLocalTime() %>";
        var userPrefPageSize = <%= new com.redhat.rhn.frontend.struts.RequestContext(request).getCurrentUser().getPageSize() %>;
        var actionChains = ${actionChainsJson};
  </script>
  <script src="/javascript/manager/systems/ssm/ssm-subscribe-channels.bundle.js?cb=${rhn:getConfig('web.version')}" type="text/javascript"></script>
</body>
</html>
