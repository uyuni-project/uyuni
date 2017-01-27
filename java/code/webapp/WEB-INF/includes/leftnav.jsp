<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ page import="com.suse.manager.webui.menu.MenuTree" %>

<script type="text/javascript">
  const JSONMenu = <%= MenuTree.getJsonMenu(pageContext) %>
</script>