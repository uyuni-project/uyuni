<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html:html >
<body>
  <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>
  <div class="alert alert-info">
      <bean:message key="sdc.channels.info.change" />
  </div>

    <div id="subscribe-channels-div"></div>
    <script>
        var csrfToken="<%= com.redhat.rhn.common.security.CSRFTokenValidator.getToken(session) %>";
        var timezone = "<%= com.suse.manager.webui.utils.ViewHelper.getInstance().renderTimezone() %>";
        var localTime = "<%= com.suse.manager.webui.utils.ViewHelper.getInstance().renderLocalTime() %>";
        var actionChains = ${actionChainsJson};

        function getServerId(){
            return ${system.id};
        }
    </script>
    <script src="/javascript/manager/systems/subscribe-channels/subscribe-channels.renderer.bundle.js?cb=${rhn:getConfig('web.version')}" type="text/javascript"></script>

</body>
</html:html>
