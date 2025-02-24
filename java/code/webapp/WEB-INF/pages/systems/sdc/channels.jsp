<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html:html >
<body>
  <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf" %>

    <script type="text/javascript">
        var csrfToken="<%= com.redhat.rhn.common.security.CSRFTokenValidator.getToken(session) %>";
        var timezone = "<%= com.suse.manager.webui.utils.ViewHelper.getInstance().renderTimezone() %>";
        var localTime = "<%= com.suse.manager.webui.utils.ViewHelper.getInstance().renderLocalTime() %>";
        var actionChains = ${actionChainsJson};

    </script>
    <div id="subscribe-channels-div">
      <script type="text/javascript">
        injectReactPage('systems/subscribe-channels/subscribe-channels', {systemId: '${system.id}'});
      </script>
    </div>
</body>
</html:html>
